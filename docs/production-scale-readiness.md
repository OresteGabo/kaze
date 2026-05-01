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
- Partial: CORS reads `KAZE_CORS_ALLOWED_HOSTS`, and production defaults are tightened.
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
- Partial: Some indexes exist, but every hot query should be reviewed against real query plans.
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
- Partial: Ktor in-process rate limiting does not coordinate across Cloud Run instances.
- Todo: Add Cloud Armor rate limits for public endpoints.
- Todo: Add stricter limits for:
  - `/api/v1/auth/signin`
  - `/api/v1/auth/signup`
  - `/api/v1/auth/refresh`
  - `/api/v1/auth/session/claim`
  - OAuth callback endpoints
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

- Partial: Web auth token persistence is safer now, but public data cache needs more work.
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
