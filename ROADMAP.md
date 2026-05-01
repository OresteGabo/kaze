# Kaze Roadmap

## Purpose

This roadmap tracks Kaze's path from prototype to production. It is intentionally product-focused and business-focused, not just engineering-focused.

Current product direction:
- event-first
- conference and wedding centered
- venue-aware, not hotel-bloated
- focused on passes, schedules, access, and event-linked services

Status guide:
- `[x]` Done
- `[~]` In progress / partially done
- `[ ]` Not done yet

## V1 Launch Scope

The goal for v1 is not to finish the whole Kaze platform. The goal is to ship a focused, usable first release for pilots in Rwanda, then expand safely after real usage.

### Must Be Ready Before Launch

- authenticated sign-up, sign-in, refresh, logout, and session restore
- stable first-run flow after onboarding
- public venue/event browsing or a clear invitation/code-first entry path
- invitation acceptance and basic pass/event access flow
- event schedule viewing
- basic stay flow for authenticated guests with active stay lookup
- venue reservation submission that persists on the backend
- production app wiring that uses real repositories for launch-critical flows instead of demo repositories
- basic error messages, empty states, and retry states
- release configuration, backend environment/secrets strategy, and minimum production observability

### Can Wait Until After V1

- Edge AI features
- full event-services marketplace breadth
- rich provider dashboards and operator dashboards
- advanced payment orchestration
- realtime invitation updates
- pathfinding and current-location maps
- white-label platform expansion
- seating planners and reusable venue-map platformization
- advanced analytics, deep CMS tooling, and large partner workflows

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

## Core Event Experience

- [x] onboarding flow
- [ ] first-run entry screen after onboarding
- [ ] public event and venue discovery without sign-in
- [ ] event and venue search by name
- [ ] short code entry flow for conferences, weddings, and private events
- [ ] invitation-first join flow
- [~] invitation acceptance and identity confirmation flow
- [~] post-invitation Kaze Pass generation
- [~] pass-centered home and event entry experience
- [x] event schedule flow
- [x] explore flow
- [x] venue guidance and indoor map screen
- [x] digital access pass UI
- [x] branded theme support
- [x] automatic light/dark theme switching
- [~] guest-facing UX copy cleanup
- [ ] event-first navigation cleanup
- [ ] reduce or remove generic hotel-only flows from primary UX
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
Weddings and conferences often have weak indoor connectivity. Offline answers keep guests oriented, reduce staff interruptions, and make Kaze useful even in low-network environments.

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

## Event Services And Marketplace

- [x] request catalog UI
- [x] service request patterns exist in UI
- [ ] photography service flow
- [ ] videography service flow
- [ ] live-streaming service flow
- [ ] transport service flow
- [ ] printing and branding service flow
- [ ] event decor/styling service flow
- [ ] vendor profile and offer cards
- [ ] request history UI
- [x] request success feedback
- [~] real request persistence from backend
- [ ] request status updates from provider systems
- [ ] cancellation/edit rules from backend
- [ ] partner-side request fulfillment flow
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
- [ ] reusable venue-maps service/database for event venues and partner places
- [ ] generic place model for venues and service locations
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
- [ ] event reminder notifications

## Security, Identity, And Privacy

- [x] secure-store abstraction
- [x] onboarding persistence
- [~] guest and attendee authentication
- [ ] phone-number-based invitation matching
- [ ] role and access control model
- [ ] encrypted sensitive local data
- [x] API auth tokens and refresh flow
- [~] session expiration strategy
- [ ] audit logging
- [ ] privacy-first analytics plan
- [ ] data retention policy implementation
- [ ] DPA / enterprise privacy review

## Backend And Integrations

- [~] backend module exists
- [~] production API endpoints
- [ ] public event and venue catalog API
- [ ] event/venue search API
- [ ] short-code lookup and join API
- [~] invitation API
- [~] invitation-to-pass entitlement flow
- [~] venue reservations API
- [x] authenticated active-stay lookup API
- [ ] event services commerce API
- [ ] payment orchestration layer
- [ ] Rwanda mobile-money integrations
- [ ] card/bank payment integrations for local rails
- [~] reservation-to-event integration
- [ ] reservation-to-pass entitlement integration
- [ ] organizer dashboard integration
- [ ] provider dashboard integration
- [ ] venue operations integration where event-relevant
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
- [~] seed data strategy for pilots

## Venue Reservations And Commerce

- [~] conference-room reservation flow
- [~] wedding venue reservation flow
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
- [~] Kaze Pass-gated event entry after reservation/payment
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
- [ ] authenticated active-stay endpoint tests
- [ ] venue reservation API tests
- [ ] performance tests
- [ ] accessibility tests

## Code TODO Audit

Last checked: 2026-05-01

- [ ] App bootstrap still uses demo dependencies by default: `rememberKazeDependencies()` returns `KazeDependencies.demo()`, so launch-critical app flows are still wired through demo repositories unless explicitly replaced with API-backed dependencies.
- [ ] Authenticated shell still falls back to demo content in several places: guest invitations, event previews, sample hotel branding, and demo feedback UI are still referenced from the main app entry path.
- [ ] Invitation creation/composer still depends on demo contacts and placeholder event options; v1 needs either a trimmed real invite flow or that composer hidden until backend-backed contacts and event linkage exist.
- [~] Reservation draft persistence: backend write path exists and creates a linked draft event; remaining work is catalog-backed packages, availability, pricing, payment methods, and place/service IDs from the public venue API.
- [~] Invitations: authenticated invitations are database-backed and can be accepted/declined; remaining work is guest-mode replacement, websocket/realtime updates, and full invitation-to-pass entitlement completion.
- [ ] Venue detail pages: still need backend photos, availability calendar, verified location, cancellation rules, reviews, and provider contacts.
- [ ] Map placeholders: temporary KotlinConf/SVG map assets and hardcoded coordinates remain until branded hotel/venue plans and a venue-map management pipeline are ready.
- [ ] Payment settings: payment method status is still static and must be tied to user account verification, provider availability, and country/venue rules.
- [~] Active stay identity: production lookup is now database-backed through `/auth/me/active-stay`; remaining work is tests, empty-state UX, and multi-stay selection if a user has overlapping active stays.
- [ ] Test database reliability: server tests still depend on local Postgres credentials; add a deterministic test database/container profile so API tests are not blocked by developer machine auth.

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

1. Replace demo dependencies in the app bootstrap with launch-safe real repositories for auth, events, explore, stay, invitations, and reservations.
2. Build the first-run entry experience after onboarding with `Explore venues`, `Enter code`, and invitation-first access.
3. Build public venue browsing with pricing, capacity, and basic availability signals, or intentionally narrow v1 to invitation/code-first entry and hide unfinished public discovery.
4. Finish short-code and invitation flows that lead into Kaze Pass generation.
5. Tighten the authenticated shell: remove demo fallback content, add real empty states, and keep only features that have real backend support.
6. Finish the first venue reservation flow for conference rooms and wedding spaces with clear operator-facing status handling.
7. Finalize launch operations: release config, secrets/environment strategy, crash reporting, and minimal backend observability.
8. Add Rwanda payment support strategy for MoMo, Airtel Money, BK/RSwitch, and related methods if direct payment is part of v1; otherwise keep reservations in `request/confirm later` mode for launch.

## Suggested V1 Cut

If the goal is to launch sooner, the cleanest v1 is:

- auth
- onboarding
- invitation acceptance
- event schedule
- pass display
- active stay dashboard
- basic venue browsing
- reservation request submission

And explicitly defer:

- full invitation creation workflow
- realtime invitations
- direct payment checkout
- advanced map pipeline
- service marketplace depth
- Edge AI

## Notes

- Kaze is a private commercial product.
- This roadmap is an internal planning document and should be updated as implementation evolves.
