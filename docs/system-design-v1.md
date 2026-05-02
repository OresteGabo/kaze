# Kaze System Design (V1)

## Purpose

This document describes the intended system design for Kaze v1: a focused, launchable version of the product for early pilots, not the full long-term platform.

The goal is to make the current codebase easier to reason about and to keep product, app, backend, and data decisions aligned.

## V1 Product Boundary

Kaze v1 should focus on a small set of real workflows:

- sign up / sign in / session restore
- invitation acceptance
- event schedule viewing
- pass-centered guest access
- active stay lookup for signed-in guests
- basic venue browsing
- reservation request submission

The following are intentionally not required for v1 launch:

- full invitation creation workflow
- realtime websocket updates
- direct online payment checkout
- advanced venue-map platformization
- broad marketplace/provider operations
- Edge AI features

## High-Level Architecture

### Client

The Kotlin Multiplatform app should own:

- presentation state
- local secure session storage
- local read caching/offline-first behavior
- navigation and view-model orchestration

The app should not be the source of truth for shared business data.

### Backend

The Ktor backend should remain a single deployable monolith for v1.

It should own:

- authentication and refresh tokens
- invitation access rules
- active stay lookup
- reservation persistence
- event and venue read APIs
- stay-related request validation

This is the right tradeoff for the current stage: lower ops complexity, faster iteration, and simpler debugging.

### Database

Postgres is the source of truth for:

- users
- guests
- stays
- event days and sessions
- invitations
- reservations
- amenity status and venue-map metadata

Business truth belongs here, not in demo content or UI heuristics.

### Caching

For v1:

- keep local in-process TTL caches in the backend for hot read endpoints
- keep client-side cached read models for offline-friendly screens
- do not introduce Redis unless traffic or multi-instance cache invalidation becomes a real problem

## Runtime Boundaries

### Client-to-Server Contract

The app should treat `/api/v1` as the single API root.

That means:

- auth routes live under `/api/v1/auth/...`
- public venue/event routes live under `/api/v1/hotels/...`
- authenticated reservation routes live under `/api/v1/reservations`

The codebase previously mixed "auth base URL" with "API root" in a way that could produce `/api/v1/api/v1/reservations`. That has been corrected so the app now uses one consistent API root.

### Demo Mode vs Production Mode

Demo content is still useful for previews, tests, and isolated UI work.

But the app's default runtime should not boot through demo repositories.

For v1:

- `production()` dependencies should be the default runtime path
- `demo()` dependencies should remain opt-in for previews, tests, or explicit mock mode

This keeps architecture honest: if a feature looks live in the app, it should be backed by the real repository path unless clearly marked otherwise.

## Current Design Decisions That Make Sense

- Ktor monolith instead of early microservices
- Postgres as a single durable data store
- Hikari connection pooling
- JWT + refresh-token auth flow
- server-side caching for hotel/event/map reads
- bootstrap session endpoint for fewer client round trips
- keeping Redis out of the stack for now

## Current Design Risks

### 1. Main app shell still contains demo-era assumptions

Even after switching the default dependency builder to production repositories, some UI paths still reference:

- demo invitation previews
- sample hotel branding/config
- demo contacts and invitation composer options
- temporary map assets

This does not block internal pilots, but it should be narrowed so v1 only exposes flows that have real backend support.

### 2. Single hardcoded launch hotel context

The current production dependency setup uses a launch default:

- hotel: `rw-kgl-marriott`
- map: `map_marriott_main`

This is acceptable for a pilot-oriented v1, but it is not the final architecture. The next step after launch is to derive active hotel/map context from real public discovery, invitation context, or active-stay context rather than static defaults.

### 3. Public discovery is not yet the true source of app context

The app can browse and fetch public data, but the top-level shell is not yet fully driven by:

- selected venue
- invitation-linked venue
- active stay venue

That should be cleaned up after launch if Kaze expands beyond one primary pilot property.

### 4. Offline support is still partial

The current architecture is offline-aware, but not yet fully offline-first.

Needed later:

- local persistence layer for cached event/session/invitation/pass data
- sync strategy for queued actions where safe
- explicit stale-data markers and retry states

## Recommended V1 System Shape

### App

- KMP client
- real API-backed repositories by default
- secure local session storage
- lightweight local caching
- demo mode only for previews/testing

### Backend

- single Ktor service
- authenticated and public APIs under `/api/v1`
- in-process TTL caches for read-heavy endpoints
- Postgres-backed writes and business rules

### Data

- Postgres as source of truth
- seed SQL for dev/test/demo environments only
- schema and seed should stay aligned with the UI/test assumptions

## Post-V1 Evolution Path

Only after real usage validates the v1 should Kaze consider:

- Redis for shared caching
- realtime invitations or notifications infrastructure
- local database sync layer on the client
- payment orchestration expansion
- map-management service extraction
- provider/operator dashboards

## Practical Rules For This Codebase

When adding new features, prefer these rules:

1. If the feature is visible in the production app path, back it with a real repository path or hide it.
2. Keep business rules on the backend.
3. Keep `/api/v1` as the single conceptual API root.
4. Avoid adding new infrastructure unless traffic or operations prove it is needed.
5. Keep v1 narrow and reliable instead of wide and half-real.
