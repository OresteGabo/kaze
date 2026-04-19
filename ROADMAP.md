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
- [ ] first-run entry screen after onboarding
- [ ] public venue discovery without sign-in
- [ ] venue/event search by name
- [ ] short code entry flow for conferences, weddings, and private events
- [ ] invitation-first join flow
- [ ] invitation acceptance and identity confirmation flow
- [ ] post-invitation Kaze Pass generation
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

## Feature Roadmap: Edge AI

Kaze can use Edge AI to deliver premium hospitality intelligence without creating API costs, requiring constant internet, or sending sensitive guest data to third-party cloud services. These features should run on-device in the KMP app, with the Ktor backend receiving only the final user-approved output when upload or sync is needed.

Technical Implementation:
Use Gemma 4 E2B as the target on-device model family for efficient local inference. The implementation should prefer quantized model builds, small task-specific prompts, streaming where useful, strict output schemas, and fallback deterministic parsers for low-power or budget smartphones. Heavy processing should run only on user action, charging/Wi-Fi-friendly windows, or small batches to protect battery, RAM, and CPU.

Privacy & Cost:
These tasks are designed for zero API cost and offline-first execution. Voice notes, photos, handwritten cards, guest names, and event details should stay on the device during AI processing. Kaze should sync only structured, reviewed, and user-approved results.

### 🎙️ Smart RSVP Voice-to-Data

Status: `[ ]` Planned

Description:
Convert local voice recordings from organizers, hosts, or staff into structured RSVP data. For example, a spoken note like “Claudine and Eric are coming, two seats, vegetarian meal, phone number...” becomes validated JSON for the invitation list.

Why it matters:
Many event organizers collect guest details through voice notes, WhatsApp calls, or informal conversations. Turning speech into clean local data reduces manual typing, prevents mistakes, and works even when the organizer has poor connectivity.

### 🧭 Offline Event Concierge

Status: `[ ]` Planned

Description:
Provide an on-device assistant that answers schedule, venue, access, and FAQ questions from cached event data. Guests can ask where a session is, when dinner starts, or whether a room is included in their pass without needing the internet.

Why it matters:
Hotels, weddings, and conferences often have weak indoor connectivity. Offline answers keep guests oriented, reduce staff interruptions, and make Kaze useful even in low-network environments.

### 🛡️ Local Privacy Shield

Status: `[ ]` Planned

Description:
Detect faces in photos locally and blur guests who have not opted in before any upload, sharing, or gallery sync. This should run as a pre-upload safety step for event photos, organizer media, and venue documentation.

Why it matters:
Private events need trust. Local face detection and blur protects guests before data leaves the phone, reducing privacy risk for weddings, conferences, VIP events, and family celebrations.

### 🖼️ Intelligent Photo Culling

Status: `[ ]` Planned

Description:
Suggest deleting blurry, accidental, or duplicate photos directly on-device before upload. Kaze can group near-duplicates, mark low-quality shots, and let the user approve what to keep.

Why it matters:
Event media can become expensive fast. Local photo culling saves device space, reduces backend storage, lowers upload data usage, and helps users on expensive mobile data avoid unnecessary costs.

### ✍️ Multimodal Thank-You Assistant

Status: `[ ]` Planned

Description:
Use local OCR and multimodal understanding to scan handwritten cards, gift notes, and guest messages into a digital checklist. The app can help organizers track who gave what, who needs a thank-you message, and which notes still need review.

Why it matters:
After weddings, birthdays, and private events, hosts often manage handwritten notes manually. Turning cards into a private checklist saves time while keeping personal messages on-device.

### 🪪 Offline Pass Explainer

Status: `[ ]` Suggested

Description:
Explain what a guest pass allows using locally cached entitlement data. Guests can ask “Can I enter the ballroom?” or “Is breakfast included?” and receive an answer based on their pass, event, venue, and service rules.

Why it matters:
Access rules can be confusing when one person has multiple events, rooms, or service packages. A local pass explainer reduces confusion without exposing private entitlements to a cloud AI service.

### 🌍 Low-Data Translation Helper

Status: `[ ]` Suggested

Description:
Translate key event instructions, venue directions, service labels, and guest messages locally between supported languages. Keep translation short, cached, and focused on practical hospitality phrases.

Why it matters:
Kaze can support international guests and Rwandan multilingual use cases without paying per-translation API fees or requiring a strong connection.

### 🧾 Local Expense And Receipt Summaries

Status: `[ ]` Suggested

Description:
Summarize local receipts, deposits, add-on services, and event costs from photos or manually entered notes into a simple checklist before backend sync.

Why it matters:
Organizers and guests often track payments across mobile money, cash, cards, and bank slips. Local summaries help users stay organized while keeping financial images private unless they choose to upload them.

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
- [ ] phone-number-based invitation matching
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
- [ ] public venue catalog API
- [ ] event/venue search API
- [ ] short-code lookup and join API
- [ ] invitation API
- [ ] invitation-to-pass entitlement flow
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
- [ ] public venue catalog content model
- [ ] venue pricing model for public browsing
- [ ] apartment listing model for future expansion
- [ ] CMS/content strategy for hotel copy
- [ ] feature flags
- [ ] hotel-by-hotel configuration management
- [ ] event overrides for spaces and labels
- [ ] seed data strategy for pilots

## Venue Reservations And Commerce

- [ ] conference-room reservation flow
- [ ] wedding venue reservation flow
- [ ] public pricing and capacity display for reservable venues
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
- [ ] apartment discovery support
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

1. Build the first-run entry experience after onboarding with `Explore venues`, `Enter code`, and invitation-first access.
2. Replace demo data with real repositories for stay, events, explore, maps, and public venue discovery.
3. Build public venue browsing with pricing, capacity, and basic availability signals.
4. Add short-code and invitation flows that lead into Kaze Pass generation.
5. Design the reusable venue-maps service and generic venue-space model.
6. Add seating/layout planning for conference rooms and wedding venues.
7. Build the first venue reservation flow for conference rooms and wedding spaces.
8. Add Rwanda payment support strategy for MoMo, Airtel Money, BK/RSwitch, and related methods.

## Notes

- Kaze is a private commercial product.
- This roadmap is an internal planning document and should be updated as implementation evolves.
