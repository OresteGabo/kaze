# User-Linked Data Foundation

Kaze should treat invitations, event memberships, and access passes as data owned by a specific authenticated user, not as shared demo content.

## What Exists Now

- `app_users` stores authenticated users
- `user_auth_providers` stores linked OAuth identities
- `auth_refresh_tokens` stores session refresh tokens

## New Relational Foundation

`server/src/main/resources/db/dev_schema.sql` now includes these user-linked event tables:

- `events`
  - the event itself
  - optional organizer user
  - optional linked place
- `event_memberships`
  - which users belong to which events
  - role and membership status
- `event_invitations`
  - invitation records tied to an event
  - can target a user, email, or phone number
- `access_passes`
  - the issued pass for a given user and event
  - optional source invitation

## Why This Matters

Without these relationships, the app cannot answer basic safe questions correctly:

- which invitations belong to this user?
- which pass belongs to this user?
- which event memberships should be visible after login?
- which user can accept, revoke, or view a specific invitation?

## Current App Limitation

The mobile app still contains demo repositories for stays, events, maps, and invitations. That means:

- authentication is real
- profile editing is real
- most event/pass/invitation content is not yet loaded from the backend

The app has been tightened so authenticated users no longer inherit the shared demo invitation list and demo access contexts as if those records belonged to them.

## Recommended API Order

Implement these authenticated endpoints next:

1. `GET /api/v1/me/invitations`
2. `GET /api/v1/me/access-passes`
3. `GET /api/v1/me/events`
4. `POST /api/v1/events/{eventId}/invitations`
5. `POST /api/v1/invitations/{invitationId}/accept`

## Safety Rules

- never trust a client-supplied `userId` for private data reads
- always resolve the current user from the JWT subject
- always filter invitation, pass, and membership queries by that authenticated user
- keep invite codes unique and hard to guess
- never expose another user's invitations or passes by using shared demo IDs or path parameters alone

## Short-Term Goal

Make the backend the source of truth for:

- a user's invitations
- a user's memberships
- a user's active passes

Only after that should the app reintroduce rich invitation and pass screens for authenticated users.
