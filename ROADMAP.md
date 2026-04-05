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

## Highest-Priority Next Steps

1. Replace demo data with real repositories for stay, events, explore, and maps.
2. Build the first real backend-backed service request flow.
3. Finish a real map asset/data pipeline for hotel floor plans.
4. Add local/push notifications for request updates and stay reminders.
5. Add authentication, session handling, and secure guest identity flow.
6. Add crash reporting, analytics, and production observability.
7. Prepare signed Android/TestFlight release pipelines.
8. Complete accessibility, privacy, and release-readiness review.

## Notes

- Kaze is a private commercial product.
- This roadmap is an internal planning document and should be updated as implementation evolves.
