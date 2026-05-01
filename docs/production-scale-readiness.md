# Kaze Production Scale Readiness

This checklist tracks the work needed before Kaze can confidently handle a large traffic spike, such as 10,000 users arriving at the same time.

Status key:

- Done: already implemented in the codebase.
- Partial: started, but needs more production work.
- Todo: not implemented yet.
- Later: intentionally deferred.

## Current Verdict

Kaze is more production-shaped now, but it should not be considered guaranteed for 10,000 simultaneous users until the Todo and Partial items below are handled and verified with load tests.

The current app has important foundations:

- JWT protection defaults for private API routes.
- Production startup guardrails for weak JWT secrets and destructive database modes.
- In-process TTL caching for repeated public reads.
- Public HTTP cache headers for read-heavy endpoints.
- Private/user-specific responses marked `no-store`.
- Android/iOS token storage hardened.
- Cloud Run deploy defaults improved for warm instances and explicit concurrency.
- Stricter Ktor rate limit for auth routes.
- Short client request timeouts across Android, iOS, JS, and Wasm.
- Database-backed active-stay lookup instead of hardcoded stay identity.

The remaining bottlenecks are mostly:

- Real load testing.
- Cloud CDN / Load Balancer / Cloud Armor.
- Shared rate limiting.
- Database pooling and tuning.
- Resource ownership authorization.
- Proper migrations.
- Observability and alerting.

## Security And Auth

- Done: Private API routes default to requiring JWT.
- Done: Production refuses to start with `KAZE_JWT_REQUIRE_FOR_API=false`.
- Done: Production refuses the default development JWT secret.
- Done: Production refuses destructive DB schema modes like `drop` and `create-drop`.
- Done: Android stored values are encrypted with Android Keystore.
- Done: Android backups are disabled/exclude secure prefs.
- Done: iOS secure storage uses Keychain.
- Done: Web auth tokens use `sessionStorage` instead of persistent `localStorage`.
- Done: Generic server errors no longer expose raw exception messages.
- Done: Production hides Swagger UI.
- Done: CORS reads `KAZE_CORS_ALLOWED_HOSTS`, and production refuses local/default CORS hosts.
- Done: Auth and reservation request payloads have max-length and bounds validation.
- Partial: Database-backed active-stay identity exists; empty states and multi-stay selection still need UX work.
- Later: Add resource ownership checks so an authenticated user can only access their own guest/stay/event data.
- Later: Add OAuth redirect allowlist for `appRedirectUri`.
- Todo: Add email verification.
- Todo: Add password reset flow.
- Todo: Add account lockout or progressive throttling for repeated failed login attempts.
- Todo: Add optional MFA or passkeys for staff/admin accounts.
- Todo: Add audit logs for sensitive actions: login, logout, token refresh, RSVP, service request, profile update, staff/admin access.
- Todo: Rotate JWT/OAuth/database secrets through a secret manager instead of local env files.

## Caching And Fast Reads

- Done: Server-side TTL cache for:
  - hotel list
  - hotel detail
  - event days
  - event schedule
  - amenity highlights
  - hotel maps
  - amenity status
- Done: Public read endpoints return cache-friendly `Cache-Control`.
- Done: Private/user-specific routes return `Cache-Control: no-store`.
- Partial: In-process cache helps each Cloud Run instance, but caches are not shared between instances.
- Todo: Add Cloud Load Balancer + Cloud CDN in front of Cloud Run for public GET responses.
- Todo: Add `ETag` or `Last-Modified` support for public read endpoints.
- Todo: Add version fields such as `updatedAt` or `contentVersion` to hotel, map, schedule, and amenity payloads.
- Todo: Add client-side stale-while-revalidate cache for public data.
- Todo: Persist last successful public payloads on mobile so app startup can render immediately offline.
- Todo: Add cache invalidation/admin refresh path for hotel/map/schedule changes.
- Todo: Move larger map/media/static assets to object storage/CDN instead of serving everything through the app server.

### Redis Decision Guide

Redis is not required for Kaze to launch with a small user base. The current in-process `TtlCache` is enough for early-stage traffic if:

- there are few users
- there is only one backend instance, or very few
- cached data is mostly public and changes rarely
- occasional cache misses are acceptable

Redis becomes much more attractive when one or more of these signals appear:

- multiple backend instances are serving traffic and each one rebuilds the same cache separately
- public read endpoints become hot enough that repeated DB reads start to dominate latency or cost
- users expect fast responses for shared data right after deploys or instance cold starts
- cache invalidation needs to be coordinated across instances
- background jobs, workers, or rate limits need shared state
- login/session bootstrap traffic becomes large enough that local-only caches no longer help enough

Reasons to add Redis:

- shared cache across all app instances
- lower database read pressure
- faster warm reads after autoscaling
- better foundation for shared rate limiting, short-lived counters, and background coordination
- much easier than trying to make many app instances keep their own caches consistent

Reasons not to add Redis yet:

- extra cost
- extra operational complexity
- extra moving part to monitor
- little benefit if traffic is still small and one app instance handles most requests

Current recommendation for Kaze:

- Keep the local `TtlCache` now.
- Add Redis later when Kaze has multiple active instances or repeated hot-read pressure.
- Do not introduce Redis just because it sounds more "production"; add it when metrics show repeated duplicate reads, slow cold-start behavior, or cache inconsistency across instances.

### Build Your Own Cache Vs Redis

It is completely reasonable to build your own small cache for an early-stage app. In fact, Kaze already has one in `TtlCache`.

Good use cases for your own simple cache:

- in-memory per-process cache
- short TTL
- public or low-risk data
- no need to share cache state across instances
- no need for persistence after restart

What your own simple cache can do well enough:

- cache hotel list
- cache hotel detail
- cache event days and schedules
- cache amenity status
- cache map metadata or rendered map payloads

What gets hard when building your own bigger caching system:

- sharing cache across many app instances
- keeping cache entries consistent after writes
- avoiding duplicate cache fills under concurrency
- handling eviction policies well under memory pressure
- adding visibility into hit rate, miss rate, and memory use
- using cache for distributed rate limiting, locks, queues, or shared ephemeral state

Practical rule:

- Build your own small in-memory cache: yes
- Build your own Redis replacement: no

If Kaze stays small for a while, improving the current `TtlCache` a little is perfectly sensible. Useful small upgrades would be:

- configurable TTL per cache
- configurable max entries per cache
- request coalescing so many simultaneous misses do not all hit the DB
- hit/miss metrics
- explicit invalidation hooks for admin content changes

### What Should Trigger Redis Later

Use Redis when at least one of these becomes true in production:

- Kaze regularly runs on multiple backend instances
- DB read load is high for shared/public data
- p95 latency spikes happen during cold starts or autoscaling
- you need shared rate limiting across instances
- you need distributed short-lived state such as locks, counters, job coordination, or idempotency keys
- local cache hit rates are low because traffic is spread across many instances

If none of those are true yet, Redis can wait.

## Cloud Run And Traffic Handling

- Done: Deploy script sets `KAZE_ENV=production`.
- Done: Deploy script sets `KAZE_JWT_REQUIRE_FOR_API=true`.
- Done: Deploy script configures `--concurrency`.
- Done: Deploy script defaults `--min-instances=1`.
- Done: Deploy script defaults `--max-instances=10`.
- Partial: Defaults are better, but not proven for 10,000 simultaneous users.
- Todo: Choose target Cloud Run concurrency after load tests, not by guessing.
- Todo: Test multiple CPU/memory profiles, for example:
  - 1 CPU / 1 GiB
  - 2 CPU / 2 GiB
  - 4 CPU / 4 GiB
- Todo: Decide production `max-instances` based on DB connection limits and cost.
- Todo: Set Cloud Run request timeout intentionally.
- Todo: Set Cloud Run CPU allocation policy intentionally.
- Todo: Add a global external HTTPS Load Balancer in front of Cloud Run.
- Todo: Add Cloud Armor rate limits and basic WAF rules.
- Todo: Add separate services if needed:
  - public read API
  - auth/session API
  - private guest/stay API
  - worker/background jobs

## Database And Data Layer

- Done: DB max pool size can be configured with `KAZE_DB_MAXIMUM_POOL_SIZE`.
- Done: Production blocks destructive schema modes.
- Partial: Prepared statements are used widely, which reduces SQL injection risk.
- Partial: Indexes exist for auth/session, invitation/event, active-stay, request, and reservation hot paths, but every hot query should still be reviewed against real query plans.
- Todo: Add PgBouncer or managed connection pooling.
- Todo: Confirm Neon/Postgres max connection limits and tune app pool sizes accordingly.
- Todo: Add Flyway or Liquibase migrations.
- Todo: Run `EXPLAIN ANALYZE` for hot queries.
- Todo: Add or verify indexes for:
  - `hotels.id`
  - `service_places.id`
  - `event_days.hotel_id`
  - `scheduled_experiences.hotel_id, day_id`
  - `amenity_highlights.hotel_id`
  - `amenity_statuses.hotel_id`
  - `maps.hotel_id`
  - `map_floors.map_id`
  - `map_nodes.floor_id`
  - `guests.hotel_id, id`
  - `stays.hotel_id, guest_id, status`
  - `itinerary_items.stay_id`
  - `late_checkout_requests.hotel_id, guest_id, created_at`
  - `service_requests.hotel_id, guest_id, created_at`
  - `auth_refresh_tokens.token_hash`
  - `auth_refresh_tokens.user_id`
  - `auth_refresh_tokens.family_id`
  - `oauth_login_attempts.state_hash`
  - `auth_one_time_login_tokens.token_hash`
  - `event_invitations.invited_user_id`
  - `event_memberships.user_id`
- Todo: Add read models for screens that are expensive to assemble:
  - public hotel profile
  - public event schedule
  - guest dashboard
  - map payload
  - invitation list
- Todo: Consider read replicas for heavy public browsing traffic.
- Todo: Add database backups and restore drills.

## Rate Limiting And Abuse Protection

- Done: Basic Ktor rate limiting exists.
- Done: Auth endpoints have a stricter Ktor rate limit than general API traffic.
- Partial: Ktor in-process rate limiting does not coordinate across Cloud Run instances.
- Todo: Add Cloud Armor rate limits for public endpoints.
- Done: Add stricter in-process limits for:
  - `/api/v1/auth/signin`
  - `/api/v1/auth/signup`
  - `/api/v1/auth/refresh`
  - `/api/v1/auth/session/claim`
- Partial: OAuth callback endpoints share the auth limiter; add provider-specific abuse monitoring before launch.
- Todo: Add per-account and per-IP throttles for auth.
- Todo: Add request body size limits.
- Todo: Add bot protection if public signup becomes abused.
- Todo: Add abuse monitoring dashboards.

## Background Jobs

- Todo: Move slow or side-effect-heavy work out of request handlers.
- Todo: Use Cloud Tasks or Pub/Sub for:
  - emails
  - push notifications
  - analytics ingestion
  - reservation confirmations
  - staff alerts
  - map imports
  - large sync jobs
- Todo: Add retry policies and dead-letter queues.
- Todo: Make write endpoints idempotent with request IDs where user retry is possible.

## Client Performance

- Partial: Web auth token persistence is safer now, client HTTP timeouts are short, but public data cache needs more work.
- Todo: Add cached startup state for:
  - hotel list
  - selected hotel
  - event schedule
  - amenities
  - map metadata
- Todo: Add stale-while-revalidate behavior in app repositories.
- Todo: Add optimistic UI for:
  - RSVP
  - service requests
  - late checkout
  - profile updates
- Todo: Avoid API calls on every search/filter keystroke.
- Todo: Prefetch likely next screens after login and after selecting a hotel.
- Todo: Add graceful offline mode for public browsing.
- Todo: Compress and resize all images; serve WebP/AVIF variants.
- Todo: Lazy-load heavy map data only when needed.

## Offline And Low-Resource Devices

- Done: Startup can continue into guest/offline mode if the backend is slow or unavailable.
- Done: Auth/session refresh failures do not block the whole app from rendering cached/demo/public flows.
- Done: Mobile and web auth clients use short timeouts so low network does not hang screens for too long.
- Partial: Public browsing, maps, schedules, and invitations have enough local/demo data to remain navigable offline, but the last successful live payloads are not persisted yet.
- Todo: Add local persisted caches for public venue catalog, event schedule, map metadata, active pass summary, and last known invitations.
- Todo: Add explicit connectivity state to prevent network calls for screens that can render from cache.
- Todo: Add low-data mode that disables prefetch, avoids large imagery, and prefers cached map metadata.
- Todo: Add low-battery mode that disables expensive animations, map preloads, and background refresh.
- Todo: Add older-device QA targets with memory, startup time, and frame-time budgets.
- Todo: Add compact-screen visual regression tests for 320dp-wide devices and large font/accessibility settings.

## Observability And Operations

- Partial: Request logging exists.
- Todo: Add structured JSON logs.
- Todo: Add trace IDs and return request IDs in response headers.
- Todo: Add metrics dashboards:
  - request count
  - p50/p95/p99 latency
  - error rate
  - Cloud Run instance count
  - CPU and memory
  - DB connection usage
  - DB query latency
  - cache hit/miss rate
  - auth failures
- Todo: Add alerts:
  - 5xx rate spike
  - p95 latency threshold
  - DB connection exhaustion
  - DB CPU/storage threshold
  - Cloud Run instance saturation
  - unusual auth failure rate
- Todo: Add uptime checks for:
  - `/health`
  - public hotel list
  - auth signin/signup health path or synthetic flow
- Todo: Add Sentry, Error Reporting, or equivalent.
- Todo: Add runbooks for common incidents.

## Load Testing Plan

- Todo: Create realistic test scenarios:
  - 10,000 users open app and fetch public hotel/event data.
  - 10,000 users open a map-heavy page.
  - 1,000 users log in over a short window.
  - 1,000 users submit RSVP/service requests over a short window.
  - mixed traffic: 80% public reads, 15% authenticated reads, 5% writes.
- Todo: Use tools such as k6, Locust, or Gatling.
- Todo: Test locally against staging, not production first.
- Todo: Record:
  - max RPS
  - p95/p99 latency
  - error rate
  - DB connections
  - DB CPU
  - Cloud Run instance count
  - cache hit rate
- Todo: Define acceptance targets before launch:
  - p95 public reads under 300 ms from cache/CDN
  - p95 authenticated reads under 700 ms
  - p95 writes under 1.5 s
  - error rate under 0.1% during expected spike
  - no DB connection exhaustion

## Suggested Milestones

### Milestone 1: Safer Small Launch

- Done: JWT required for private routes.
- Done: Production startup safety checks.
- Done: Basic public read caching.
- Done: Safer token storage.
- Todo: Resource ownership checks.
- Todo: OAuth redirect allowlist.
- Todo: Disable public docs in production.
- Todo: Basic dashboards and alerts.

### Milestone 2: Spike-Ready Beta

- Todo: Cloud Load Balancer + Cloud CDN.
- Todo: Cloud Armor rate limits.
- Todo: PgBouncer or managed pooling.
- Todo: Real DB migrations.
- Todo: Read endpoint load tests.
- Todo: Client stale-while-revalidate cache.

### Milestone 3: 10k Simultaneous User Confidence

- Todo: Full mixed-flow load testing.
- Todo: Tuned Cloud Run concurrency, CPU, memory, min/max instances.
- Todo: DB tier validated under peak load.
- Todo: Shared/distributed rate limiting.
- Todo: Read models for hot screens.
- Todo: Production incident runbooks.
- Todo: Backup and restore drills.

## Notes

There is no honest way to guarantee 10,000 simultaneous users from code inspection alone. The guarantee comes from:

1. Designing for cache-first reads.
2. Removing obvious bottlenecks.
3. Running realistic load tests.
4. Measuring the real p95/p99 latency and error rate.
5. Tuning Cloud Run, database, CDN, and rate limits based on those numbers.

Until those tests pass, treat 10,000 simultaneous users as a target, not a guarantee.
