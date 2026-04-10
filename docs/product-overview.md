# Product Overview

## What Kaze Is

Kaze is a digital concierge, venue-navigation, and venue-commerce platform for hospitality and event environments.

It is designed for:
- luxury hotels
- conference and summit venues
- weddings and private events
- day-access experiences like spa, pool, or dining passes
- public venue browsing before authentication
- future apartment discovery for local stay alternatives
- future venue categories such as stadiums, government buildings, and transport spaces

## Core User Types

Kaze should not assume every person has a room.

Supported visitor profiles:
- hotel guest with a room stay
- conference attendee without a room
- event guest with limited venue access
- day visitor with amenity-specific access
- VIP or invited private guest

## Core Experience

Kaze combines five product ideas:
- a personalized itinerary
- a service-request layer
- a concierge-style recommendation layer
- indoor maps with access-aware areas
- a digital access pass

It is also growing toward:
- venue reservation workflows
- local payments
- venue add-on service sales
- reusable map/data infrastructure for many types of spaces
- seating and layout planning for reservable venues
- invitation-based entry and short-code join flows
- public venue search with pricing and discoverability

## Design Direction

The app should feel:
- premium
- quiet
- fast
- architectural
- hospitality-first

It should avoid looking like:
- a generic Material demo
- a conference clone with hotel words pasted on top
- a hotel PMS admin screen

## Current Navigation

- `Entry`: explore venues, accept invitations, or enter a short code
- `Stay`: itinerary and access identity
- `Requests`: things the visitor wants to ask for
- `Suggestions`: relevant recommendations, not yet confirmed
- `Map`: venue and wayfinding experience

## Entry And Access Model

Kaze should support both open discovery and restricted access.

### Public discovery

Without signing in, a person should be able to:
- browse venues
- compare conference rooms
- view wedding venues
- see pricing and key details
- later discover apartments and other local stay options

### Private entry

When a person already has a relationship with a venue or event, Kaze should support:
- invitation-based entry
- short code entry
- search by venue, room, or event name

Recommended first-run flow after onboarding:
- `Explore venues`
- `I have an invitation`
- `Enter code`

## Invitation And Pass Flow

For conferences, weddings, and private events, organizers should be able to invite people directly.

Suggested flow:
1. organizer creates an event
2. organizer invites guests, likely by phone number and optional name
3. invitee receives an invitation
4. Kaze matches the invitation or lets the person confirm it manually
5. after confirmation, Kaze generates the correct pass

This is stronger than requiring every user to search manually, while still keeping short codes and search available as fallback entry paths.

## Product Principle

Kaze should model access, not only accommodation.

That means the system can support:
- room access
- ballroom/event access
- restaurant privileges
- pool/spa/day-pass access
- restricted or hidden hotel zones

## Commerce Direction

Kaze should be able to support paid venue interactions, especially where local venues and organizers need a simpler and cheaper alternative to large international intermediaries.

Important commercial directions:
- conference room bookings
- wedding venue bookings
- paid amenity/day-pass reservations
- public venue discovery with conversion into reservation or invitation flows
- event deposits and balance collection
- venue add-on services sold during booking

Potential add-on categories:
- event styling / decoration
- cleaning
- insurance
- camera and photography services
- videography and live streaming

Important terminology:
- `layout planning` means how chairs, tables, aisles, and seating structures are arranged
- `event styling` means flowers, lighting, decor, and other presentation services

## Payments Direction

For Rwanda, payment support should prioritize methods that users and venue operators already understand and trust.

Priority methods:
- MTN MoMo
- Airtel Money
- BK / RSwitch compatible flows
- other locally relevant mobile or bank-backed payment rails

## Platform Direction

Kaze should not treat maps as a hotel-only feature.

The venue-map capability can become a reusable platform that models:
- sites
- buildings
- floors/levels
- rooms
- halls
- seats
- fixed objects
- movable layout objects
- zones
- gates
- amenities
- restricted areas

That would allow reuse across:
- hotel apps
- conference apps
- wedding venue apps
- stadium/ticketing apps
- government/public-building guidance apps

It can also support:
- seating plans for wedding and conference venues
- event entry controlled by Kaze Pass
- different room-layout presets based on attendee count

## Competitive Position

Kaze does not need to start with hotel room inventory distribution.

Why:
- large international hotel-booking platforms already dominate that space
- many local hotels already depend on those channels
- room distribution is operationally complex

However, Kaze can still create strong value by:
- reducing venue booking friction
- lowering commission pressure on venues
- adding local payment options
- bundling services around reservations
- offering direct relationships for venues that want more control

Room reservation support can still be explored later if it becomes commercially attractive.

Apartments are also a reasonable future category, especially if Kaze becomes a broader local discovery and booking platform rather than only a hotel product.
