# Product Overview

## What Kaze Is

Kaze is an event operating system for conferences, weddings, summits, and premium gatherings.

It is designed for:
- attendees and invited guests
- organizers and hosts
- event-ready venues
- service providers connected to an event
- public event and venue discovery before authentication

Kaze is not trying to be a generic hotel utility app. Its core job is to make event experiences feel organized, premium, easy to enter, and easy to monetize.

## Core User Types

Supported user groups:
- attendee or invited guest
- organizer or host
- venue partner
- service partner
- VIP or restricted-access guest

## Core Experience

Kaze combines:
- invitations and RSVP
- digital event passes
- event schedules and timing
- venue guidance and access-aware maps
- event-linked services
- local payments for packages and add-ons

The ideal journey is:
1. discover an event or receive an invitation
2. confirm attendance
3. receive the right pass
4. arrive and move confidently through the venue
5. use relevant event-linked services before, during, or after the event

## Design Direction

The app should feel:
- premium
- quiet
- fast
- event-first
- architectural

It should avoid looking like:
- a generic Material demo
- a hotel PMS admin screen
- a random marketplace
- a stuffed super-app with unrelated tools

## Current Product Center

The product center should be:
- entry and invitations
- pass and access
- event details and schedule
- venue/place guidance
- event-linked services

Things like generic room-service or hotel-stay workflows should not compete with the main event story.

## Entry And Access Model

Kaze should support both open discovery and restricted access.

### Public discovery

Without signing in, a person should be able to:
- browse events and event-ready venues
- compare venues for conferences and weddings
- see pricing, capacity, and key context
- understand the type of experience Kaze supports

### Private entry

When a person already has a relationship with an event, Kaze should support:
- invitation-based entry
- short code entry
- search by event or venue name

Recommended first-run flow after onboarding:
- `Explore events`
- `I have an invitation`
- `Enter code`

## Invitation And Pass Flow

Invitations should be a first-class product path.

Suggested flow:
1. organizer creates an event
2. organizer invites guests, likely by phone number and optional name
3. invitee receives or finds the invitation
4. Kaze matches the invitation or lets the person confirm it manually
5. after confirmation, Kaze generates the correct pass

This is stronger than forcing every user to search manually, while still keeping short codes and search as fallback entry paths.

## Product Principle

Kaze should model event access, movement, and event-linked value.

That means the system can support:
- event hall access
- restricted zones
- VIP areas
- organizer or staff access
- service entitlements tied to a ticket, invitation, or package

## Commerce Direction

Kaze should support paid event interactions, especially where local organizers and venues need a simpler and cheaper alternative to fragmented manual workflows.

Important commercial directions:
- event package sales
- venue discovery with conversion into booking or invitation flows
- deposits and balance collection
- service packages sold during booking or after invitation acceptance
- organizer-facing tools that justify recurring or per-event fees

Potential add-on categories:
- camera and photography services
- videography and live streaming
- transport
- printing and branding
- decor and styling
- hospitality bundles relevant to the event

## Payments Direction

For Rwanda, payment support should prioritize methods people and businesses already understand and trust.

Priority methods:
- MTN MoMo
- Airtel Money
- BK / RSwitch compatible flows
- other locally relevant mobile or bank-backed payment rails

## Platform Direction

Kaze can still keep a reusable venue-platform layer underneath the product.

That layer may model:
- sites
- buildings
- floors
- rooms and halls
- seats and sections
- gates and entry points
- amenities and points of interest
- restricted areas
- fixed and movable layout objects

That makes Kaze reusable across:
- conference apps
- wedding venue apps
- ticketed event experiences
- future venue categories where access and navigation matter

But this platform layer should serve the event product, not replace it as the main story.

## Competitive Position

Kaze does not need to start with hotel room inventory distribution or generic hotel room-service workflows.

Why:
- those flows make the product broad too quickly
- incumbents already dominate large parts of hotel distribution
- Kaze has a stronger advantage in event coordination, access, and local service linkage

Kaze can create stronger value by:
- reducing event booking friction
- lowering commission pressure on venues and organizers
- adding local payment options
- bundling services around an event
- owning the relationship between guest, organizer, venue, and service provider
- later vertically integrating the strongest service categories into Kaze-owned companies
