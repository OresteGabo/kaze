# Venue Platform Strategy

## Purpose

This document captures the strategy behind expanding Kaze from a hotel guest app into a broader venue platform with reusable maps, reservations, local payments, and service add-ons.

## Core Idea

Kaze should not be limited to hotel stays.

The same platform ideas can support:
- hotels
- conference venues
- wedding venues
- stadiums
- government buildings
- airports and other transport spaces

The strongest reusable foundation is:
- venue maps
- access control
- space identification
- seating and layout planning
- venue reservations
- local payments
- add-on services
- discovery and join flows for both public and invited users

## Why This Matters

This direction can unlock both product depth and new revenue streams.

It can help Kaze:
- serve more than one venue category
- support both discovery users and invited users
- power multiple branded apps
- reuse the same venue data across different products
- reduce dependency on single-use hardcoded hotel flows
- create B2B platform revenue beyond a single hotel app

## Reusable Venue Maps

Venue maps can become their own reusable platform capability.

That platform should support:
- site
- structure/building
- level/floor
- room/hall
- seat/section
- fixed furniture/fixtures
- movable layout objects
- route/path
- gate/entry
- amenity/POI
- restricted zone

Examples of reuse:
- hotel indoor wayfinding
- conference room navigation
- wedding seating and venue guidance
- stadium seating/ticket validation flows
- public-building navigation

## Layout Planning Versus Event Styling

Kaze should clearly separate two different ideas that might otherwise both be called "decoration".

### 1. Layout Planning

This means how furniture and seating are arranged inside a space.

Examples:
- fixed chairs that cannot be moved
- round tables
- rectangular tables
- banquet layouts
- classroom layouts
- theater layouts
- aisle placement
- suggested seating capacity based on attendee count

This belongs close to the venue-map system because it changes how the space is organized and used.

### 2. Event Styling

This means visual or service decoration added around the layout.

Examples:
- flowers
- lighting
- stage styling
- backdrops
- table styling
- wedding theme styling

This belongs closer to venue-commerce and add-on services than to the base spatial model.

The distinction matters because:
- layout planning affects capacity, movement, and safety
- event styling affects presentation, mood, and service pricing

## Seating And Layout Planning

For conference rooms and wedding venues, Kaze can grow into a space-planning tool as well as a booking platform.

Important capabilities:
- fixed venue objects that cannot move
- configurable open areas that can receive different seating/table layouts
- layout templates based on venue type
- attendee-count suggestions
- space numbering and identification
- movement/aisle clearance rules
- venue operator approval for chosen layouts

Examples of configurable layouts:
- round-table wedding layouts
- rectangular-table dining layouts
- classroom seating
- theater seating
- boardroom setups
- standing event zones

## Access-Gated Events

Some events or venue zones should require a Kaze Pass for entry.

Examples:
- VIP wedding reception area
- paid conference room booking
- ticketed stadium section
- restricted delegate lounge

The map and reservation system should work together so that:
- entry rules are visible in the booking flow
- access can be tied to reservation/payment state
- the visitor's Kaze Pass can unlock the correct zones

## Venue Reservations

Kaze can expand into reservations that are more local and operationally manageable than global hotel room inventory.

High-potential starting categories:
- conference rooms
- wedding venues
- paid event spaces
- amenity/day-pass bookings

These categories fit Kaze because they benefit from:
- space-aware maps
- venue-specific rules
- local payment methods
- add-on service packaging

## Discovery Before Authentication

Kaze should not force every user into a login or private-event path immediately.

A stronger entry model is:
- public venue discovery
- invitation-based entry
- short code entry

This matters because some users are:
- actively looking for a venue
- comparing prices before booking
- exploring conference or wedding options
- not yet linked to a hotel stay or event

That means Kaze can work as:
- a private access product
- a public discovery and conversion product

## First-Run Entry Flow

After onboarding, the most useful first-run experience is likely:
- `Explore venues`
- `I have an invitation`
- `Enter code`

This is better than a purely closed flow because it supports:
- new customers who only want to browse
- invited attendees who need fast access
- people who received a short conference or wedding code

## Short Codes

Conferences, weddings, and private events should support short entry codes.

Good code characteristics:
- short enough to type quickly
- readable
- usable in SMS, posters, invitation cards, and staff instructions

Short codes are useful when:
- the guest was not pre-identified
- an organizer wants simple offline-friendly distribution
- the user arrives with only a printed or verbal reference

## Invitation Strategy

Invitations should become a first-class access path.

Recommended model:
1. organizer creates event
2. organizer adds invitees by phone number and optional name
3. invitee receives invitation
4. Kaze matches the invitation or lets the person confirm it
5. Kaze generates the appropriate pass after confirmation

Phone-number-based invitations are especially relevant in Rwanda because they fit how people already communicate and receive event instructions.

## Public Pricing And Future Apartments

Public venue browsing should include:
- venue name
- space type
- pricing
- capacity
- basic amenities
- location context

This same discovery layer could later support:
- apartments
- local short-stay alternatives
- non-hotel accommodation options

That gives Kaze a broader marketplace surface without abandoning its venue-access strengths.

## Rwanda Payment Strategy

Venue bookings should support payment methods people and businesses in Rwanda already use.

Priority methods:
- MTN MoMo
- Airtel Money
- BK / RSwitch compatible payment flows
- other locally relevant mobile and bank rails as needed

Payment use cases:
- booking deposits
- full reservation payment
- add-on service payment
- event-entry payment tied to Kaze Pass entitlements
- balance settlement
- refunds where policy allows

## Add-On Services

One business risk is that customers may try to negotiate directly with venues after discovering them through Kaze.

Kaze can reduce this risk by making the platform more valuable than the bypass.

Add-on service categories that strengthen platform value:
- event styling and floral decoration
- cleaning
- insurance
- camera and photography
- videography
- live streaming
- event support packages

These services help because they:
- increase booking value
- create more reasons to stay in-platform
- make venue operators and partners more dependent on the workflow
- create additional commission or service revenue

## Room Reservations

Room reservations should not be the first expansion priority.

Why:
- international platforms are already strong there
- hotels often already rely on them
- integrations and inventory control are more complex

Still, room booking should remain a future option.

Possible future value proposition:
- lower commission for direct/local bookings
- tighter integration with access, maps, and on-property services
- bundled venue and room flows for weddings, conferences, and multi-service stays

## Service Boundary Direction

The venue-map capability is a strong candidate for its own dedicated service and database.

That service could later support many apps, including apps that are not Kaze itself.

Possible long-term service boundaries:
- venue maps service
- seating/layout planning service or module
- venue reservations service
- payments/orchestration service
- Kaze guest experience app/backend

This does not require overengineering immediately, but it supports long-term reuse and product expansion.

## Revenue Opportunities

Potential revenue models include:
- SaaS fees for venue operators
- setup/import fees for venue maps
- seating/layout planning fees
- reservation commissions
- add-on service commissions
- white-label app fees
- premium analytics/reporting
- payments-related service fees where appropriate

## Rendering Direction

The map platform should support mostly static spatial drawing with selective transitions when the user changes layout options.

That means the rendering model can stay relatively efficient:
- most venue geometry is static
- layout objects change only when a venue operator or customer selects a different configuration
- animation needs are limited and can stay lightweight at first

Conceptually, this is similar to drawing lines, rectangles, zones, and objects into a scene or canvas, rather than relying on constantly moving elements.

For Kaze's current stack, the equivalent direction is:
- Compose/canvas-based rendering for static geometry
- structured venue objects for seats, tables, aisles, and zones
- lightweight state transitions when switching from one layout option to another

## Recommended Near-Term Focus

1. Define the reusable venue-space data model.
2. Add a layout-planning model for seats, tables, aisles, and fixed versus movable objects.
3. Design the standalone venue-maps API and database strategy.
4. Build a first conference/wedding venue reservation flow.
5. Design Rwanda payment integrations around MoMo, Airtel Money, and BK / RSwitch.
6. Add service categories that make bypassing the platform less attractive.
7. Keep hotel room reservations as a later strategic option, not the first expansion step.
