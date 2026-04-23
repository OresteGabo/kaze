# Kaze Product Focus Audit

## Purpose

This document captures what belongs in Kaze, what should be demoted, and what should likely become a separate future product.

The goal is simple:
- keep Kaze sharp
- reduce feature drift
- avoid turning the app into several unrelated businesses inside one UI

## What Clearly Belongs In Kaze

These areas fit the current event-first vision and should stay central:
- invitations
- RSVP and guest confirmation
- digital passes and access
- event details and schedule
- venue/place guidance
- short codes and entry flows
- event-linked services
- local event payments
- organizer, venue, and guest flows around one event

## What Feels Weak Or Off-Center

These features are not useless, but they weaken focus when they sit at the same level as the event product:
- generic stay dashboards
- hotel room-service flows
- towel requests
- laundry pickup
- late checkout
- in-room dining as a core flow
- broad hotel concierge utilities unrelated to an event

Why they feel off:
- they make Kaze look like a hotel utility app
- they compete with invitations, passes, and event flows
- they expand the product surface without strengthening the event business

## Suggested Product Hierarchy

### Core Kaze

Keep these in main navigation and product storytelling:
- Home centered on active pass, invitation, or upcoming event
- Events
- Invitations
- Explore event-ready venues and public events
- Settings

### Secondary Or Hidden

If needed, keep these only when tied to a specific venue or partner setup:
- venue-specific hospitality actions
- room-related requests during event-linked stays
- add-on hospitality bundles attached to an event package

These should not define the product.

## What To Remove From Primary UX

These are the best candidates to remove from the main app flow or demote heavily:

### 1. Generic stay language

Examples:
- `Stay`
- `My Stay`
- room-first messaging
- hotel-first onboarding copy

Suggested replacement:
- `Pass`
- `Event`
- `Plan`
- `Invitation`

### 2. Hotel-only request catalog

Examples seen in the app structure:
- late checkout
- towels
- laundry
- in-room dining

Suggested replacement:
- event service requests
- photography
- videography
- transport
- decor and styling
- printing and branding
- hospitality bundles tied to an event

### 3. Generic concierge framing

If the concierge does not help the event journey directly, it should not stay central.

Keep only if it helps with:
- venue questions
- access questions
- schedule questions
- event service coordination

## What Can Stay As Infrastructure

These are good underlying capabilities even if the visible product changes:
- venue maps
- access control
- place modeling
- pass entitlements
- layout planning
- partner service catalog logic

These are not the problem. The problem is when hotel-specific use cases dominate the user-facing app.

## What Should Likely Become Separate Products Later

These can become strong businesses, but they should not live inside Kaze as equal product pillars:
- hotel operations app
- room service app
- housekeeping workflow app
- venue staff operations dashboard
- generic hospitality concierge app

## Recommended Next Product Cleanup

### Phase 1

- reduce `stay` language across the app
- stop presenting room-service utilities as core identity
- make pass, invitation, and event the main story

### Phase 2

- convert service surfaces into event-linked services
- make every service clearly belong to an event, venue, or package
- remove dead-end generic request flows

### Phase 3

- split venue or staff operations into separate internal tools if needed
- keep Kaze guest-facing and event-centered

## Final Recommendation

Kaze should become:
- the app that powers event entry, movement, and monetizable event services

Not:
- the app that tries to be hotel guest services, venue navigation, events, and hospitality operations all at once
