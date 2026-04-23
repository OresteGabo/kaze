# Venue Platform Strategy

## Purpose

This document explains how Kaze can keep a reusable platform layer without losing product focus.

The key decision is:
- Kaze the product should be event-first
- Kaze the platform can remain venue-aware underneath

## Strategic Rule

Kaze should not try to be every kind of hospitality app at once.

The user-facing product should focus on:
- conferences
- weddings
- summits
- premium gatherings
- event-linked services

The reusable platform layer can still support:
- venue maps
- access control
- pass entitlements
- seating and layout planning
- venue and service metadata

## Why This Matters

This separation helps Kaze:
- stay understandable to users
- stay commercially focused
- avoid overloaded navigation and weak feature hierarchy
- keep a reusable technical foundation
- support future B2B products without bloating the main app

## Product Layer Versus Platform Layer

### Product layer

This is what users should feel first:
- invitations
- RSVP
- event pass
- schedule
- venue guidance
- event-linked services
- local payments

### Platform layer

This is what the system can reuse underneath:
- place modeling
- buildings and floors
- halls and rooms
- gates and restricted areas
- seating and layout objects
- access policies
- vendor and service catalog relationships

## Unified Place Model

Kaze should move toward one abstract concept such as `Place` or `VenuePlace`.

That model can represent:
- conference venues
- wedding venues
- hotels used as event venues
- halls inside larger properties
- partner service locations

Specific categories such as hotel, convention center, wedding garden, ballroom, or campus venue can extend that base model instead of each forcing separate product flows.

## Layout Planning Versus Event Styling

Kaze should clearly separate two different ideas.

### Layout planning

This means how a space is organized:
- chairs
- tables
- aisles
- stage position
- seating templates
- capacity rules

This belongs close to maps and venue structure.

### Event styling

This means visual and service decoration:
- flowers
- lighting
- backdrops
- decor packages
- wedding styling

This belongs closer to service commerce than to the core spatial model.

## Access-Gated Events

The platform should let Kaze connect:
- booking state
- payment state
- invitation state
- pass entitlements
- zone access

That makes it possible to support:
- VIP entry
- paid conference sections
- wedding reception access
- lounge and backstage rules

## Service Marketplace Direction

Kaze should attach services directly to event flows instead of offering generic unrelated services.

High-potential categories:
- photography
- videography
- live streaming
- transport
- printing and branding
- decor and styling
- food and hospitality bundles

This gives Kaze a practical path:
1. start with third-party providers
2. take commissions and build demand
3. later own the strongest categories through Kaze-operated companies

## What To Demote

These should not stay at the center of the main app unless they are directly tied to an event:
- generic room service
- late checkout
- broad hotel stay operations
- generic concierge tasks unrelated to an event

Those can remain as:
- venue-specific modules
- partner extensions
- future separate apps or dashboards

## Long-Term Outcome

The strongest structure is:
- one clear Kaze event product
- one reusable venue-and-access platform underneath
- one service-commerce layer attached to events

That gives Kaze focus in the market without throwing away the technical foundation already being built.
