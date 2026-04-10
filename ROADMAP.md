# Kaze Roadmap

## Purpose

This roadmap tracks Kaze's path from prototype to production. It is intentionally product-focused and business-focused, not just engineering-focused.

Status guide:
- `[x]` Done
- `[~]` In progress / partially done
- `[ ]` Not done yet

## Product Foundation

- [x] Kotlin Multiplatform app structure
- [x] Android app target
- [x] iPhone app target
- [x] shared domain module
- [x] initial Ktor backend module
- [x] branded product direction for Kaze
- [x] proprietary/private project documentation baseline
- [~] long-term package structure cleanup
- [~] feature-level MVVM rollout
- [ ] production dependency injection strategy
- [ ] release build flavor strategy

## Core Guest Experience

- [x] onboarding flow
- [x] stay tab with pass, schedule, requests, and suggestions
- [x] event schedule flow
- [x] explore flow
- [x] indoor map screen
- [x] digital access pass UI
- [x] hotel-branded theme support
- [x] automatic light/dark theme switching
- [~] guest-facing UX copy cleanup
- [ ] accessibility review
- [ ] localization strategy
- [ ] tablet layout refinement

## Service Requests

- [x] request catalog UI
- [x] late checkout request flow
- [x] towel request flow
- [x] laundry pickup request flow
- [x] in-room dining request flow
- [x] concierge help request flow
- [x] custom request flow
- [x] request history UI
- [x] request success feedback
- [ ] real request persistence from backend
- [ ] request status updates from hotel systems
- [ ] cancellation/edit rules from backend
- [ ] staff-side request fulfillment flow
- [ ] request SLA / ETA display

## Maps And Wayfinding

- [x] floor switching UI
- [x] zoom and pan interactions
- [x] constrained map movement
- [x] temporary SVG-based venue map architecture
- [x] import pipeline contracts for multiple source formats
- [~] reusable map rendering architecture
- [ ] real hotel floor-plan asset pipeline
- [ ] pathfinding engine
- [ ] current-location support
- [ ] accessible routes
- [ ] map labels from live hotel data
- [ ] restricted-area overlays from policy data
- [ ] offline map caching strategy
- [ ] reusable venue-maps service/database for hotels and non-hotel venues
- [ ] generic venue-space model for stadiums, government buildings, and transport spaces
- [ ] numbering/identification model for seats, zones, rooms, gates, and sections
- [ ] standalone map-management API for reuse across multiple apps
- [ ] fixed-versus-movable object model for venue layouts
- [ ] conference/wedding seating layout planner
- [ ] default seating templates for round tables, rectangles, theater, classroom, and boardroom
- [ ] attendee-count-based layout suggestions
- [ ] layout switching transitions in map/canvas renderer

## Branding And Design System

- [x] Kaze pass styling system
- [x] Kaze and GABO logo asset structure
- [x] custom button styles
- [x] custom bottom navigation style
- [x] reusable accent tokens
- [x] reusable pass tokens
- [x] reusable UI palette tokens
- [~] design-system component reuse
- [ ] dedicated design-system package
- [ ] motion system
- [ ] iconography system
- [ ] typography system beyond scale tokens
- [ ] illustration/art direction system

## Architecture

- [x] app-level MVVM foundation
- [x] StayViewModel
- [x] EventsViewModel
- [x] ExploreViewModel
- [x] MapViewModel
- [x] repository interfaces
- [x] use-case layer started
- [x] platform service abstractions started
- [~] fake repository implementations
- [~] presentation logic extraction from UI
- [ ] real repository implementations
- [ ] remote data source layer
- [ ] local persistence layer
- [ ] sync/conflict strategy
- [ ] error model standardization

## Notifications

- [x] Android notification permission request
- [x] iPhone notification permission request
- [ ] actual local notifications
- [ ] push notification backend integration
- [ ] notification preferences UI
- [ ] notification deep-link handling
- [ ] request status notifications
- [ ] stay reminder notifications

## Security, Identity, And Privacy

- [x] secure-store abstraction
- [x] onboarding persistence
- [ ] guest authentication
- [ ] role and access control model
- [ ] encrypted sensitive local data
- [ ] API auth tokens and refresh flow
- [ ] session expiration strategy
- [ ] audit logging
- [ ] privacy-first analytics plan
- [ ] data retention policy implementation
- [ ] DPA / enterprise privacy review

## Backend And Integrations

- [~] backend module exists
- [ ] production API endpoints
- [ ] venue reservations API
- [ ] venue commerce API for add-on services
- [ ] payment orchestration layer
- [ ] Rwanda mobile-money integrations
- [ ] card/bank payment integrations for local rails
- [ ] reservation-to-pass entitlement integration
- [ ] hotel PMS integration
- [ ] room assignment integration
- [ ] billing / folio integration
- [ ] housekeeping integration
- [ ] concierge / staff dashboard integration
- [ ] event system integration
- [ ] map/venue management admin tools
- [ ] observability for backend services

## Data And Content

- [x] demo content separated from core app entry point
- [~] demo content still used in UI flows
- [ ] replace demo content with real repositories
- [ ] CMS/content strategy for hotel copy
- [ ] feature flags
- [ ] hotel-by-hotel configuration management
- [ ] event overrides for spaces and labels
- [ ] seed data strategy for pilots

## Venue Reservations And Commerce

- [ ] conference-room reservation flow
- [ ] wedding venue reservation flow
- [ ] venue availability calendar and slot logic
- [ ] booking deposit logic
- [ ] quote generation flow
- [ ] negotiation-resistant booking flow with platform-only add-ons
- [ ] layout planning flow for reservable venues
- [ ] event styling/decor service catalog
- [ ] cleaning service catalog
- [ ] insurance add-on flow
- [ ] camera, photography, and video streaming add-ons
- [ ] direct booking checkout with Rwanda payment methods
- [ ] Kaze Pass-gated event entry after reservation/payment
- [ ] venue operator dashboard for reservation approvals
- [ ] cancellation and refund policy engine
- [ ] commission and payout model
- [ ] tax and invoicing model for Rwanda

## Platform Expansion

- [ ] venue-type abstraction beyond hotels
- [ ] stadium support
- [ ] government-building support
- [ ] airport/airplane cabin mapping feasibility review
- [ ] reusable map-and-space API for third-party apps
- [ ] white-label venue app model
- [ ] separate venue-maps service deployment strategy
- [ ] platform pricing for map/data reuse

## Testing And Quality

- [x] architecture tests
- [x] use-case tests
- [x] stay flow unit tests
- [x] presentation/viewmodel tests
- [~] targeted JVM and Android unit verification
- [ ] UI tests
- [ ] end-to-end tests
- [ ] snapshot/design regression tests
- [ ] integration tests for repositories
- [ ] backend contract tests
- [ ] performance tests
- [ ] accessibility tests

## CI/CD And Release Operations

- [x] GitHub Actions CI pipeline
- [x] release artifact workflow baseline
- [ ] protected branch rules
- [ ] signed Android release pipeline
- [ ] TestFlight pipeline
- [ ] Play Store deployment pipeline
- [ ] App Store deployment pipeline
- [ ] semantic versioning/release notes workflow
- [ ] crash reporting
- [ ] analytics dashboards
- [ ] environment/secrets strategy

## Business And Legal

- [x] proprietary license notice
- [x] private contribution policy
- [x] privacy policy draft
- [x] security policy draft
- [x] terms of use draft
- [ ] customer contract templates
- [ ] paid pilot onboarding process
- [ ] SLA / support process
- [ ] pricing and packaging model
- [ ] commercial rollout checklist
- [ ] venue-commerce pricing model
- [ ] operator commission strategy
- [ ] partner onboarding for decorators, cleaners, insurers, and media providers
- [ ] anti-bypass commercial strategy for hotels and venues
- [ ] merchant/payment compliance review for Rwanda

## Highest-Priority Next Steps

1. Replace demo data with real repositories for stay, events, explore, and maps.
2. Build the first real backend-backed service request flow.
3. Design the reusable venue-maps service and generic venue-space model.
4. Add seating/layout planning for conference rooms and wedding venues.
5. Build the first venue reservation flow for conference rooms and wedding spaces.
6. Add Rwanda payment support strategy for MoMo, Airtel Money, BK/RSwitch, and related methods.
7. Add Kaze Pass-gated entry for paid or restricted events.
8. Add platform add-on services that reduce off-platform negotiation pressure.

## Notes

- Kaze is a private commercial product.
- This roadmap is an internal planning document and should be updated as implementation evolves.
