# Kaze

Kaze is a Kotlin Multiplatform event operating system for conferences, weddings, summits, and premium gatherings in Rwanda and East Africa.

Instead of treating every user as a hotel guest, Kaze centers the full event journey for:
- conference attendees
- event guests
- organizers
- partner venues
- service providers linked to an event

The app combines invitations, RSVP, digital passes, schedules, venue guidance, event-linked services, and local payments into one product.

Kaze uses a dual-entry model:
- public event and venue discovery without sign-in
- private event entry through invitations, search, and short codes

Kaze can still power venue mapping and access logic underneath, but the product story is no longer "everything hospitality." The core focus is events first, with only the services that directly increase event value.

## Modules

- [`composeApp/src`](composeApp/src): Compose Multiplatform client UI
- [`shared/src`](shared/src): shared domain models and map/access logic
- [`server/src/main/kotlin`](server/src/main/kotlin): Ktor backend
- [`iosApp/iosApp`](iosApp/iosApp): iOS entry app

## Current Product Shape

- public discovery for events, venues, and event-ready places
- join flow for invitations, short codes, and event search
- digital pass and event access identity
- event schedule and guest-facing timing
- venue guidance and access-aware maps
- event-linked services such as photography, videography, transport, decor, printing, food packages, and other partner offerings

## Revenue Direction

Kaze is not just a guest app. It can grow into an event commerce platform through:
- organizer software fees
- venue partnerships
- service commissions
- featured vendor placement
- event package upsells
- local payment support in Rwanda
  - MTN MoMo
  - Airtel Money
  - BK / RSwitch and other compatible payment rails
- add-on service sales
  - photography and videography
  - live streaming
  - transport
  - decor and styling
  - printing and branding
  - hospitality bundles tied to an event

## Product Principle

Kaze should stay sharp:
- event-first
- guest-friendly
- organizer-useful
- venue-aware
- service-linked

Kaze should avoid becoming a generic hotel utility app with unrelated room-service workflows competing with the main event experience.

## Documentation

- [Product Overview](docs/product-overview.md)
- [Venue Platform Strategy](docs/venue-platform-strategy.md)
- [Event Platform Vision](docs/event-platform-vision.md)
- [Architecture Diagrams](docs/diagrams/README.md)
- [Access Pass System](docs/access-pass-system.md)
- [Map Access Control](docs/map-access-control.md)
- [UI Architecture](docs/ui-architecture.md)
- [API Contracts](docs/kaze-api-contracts.md)
- [Roadmap](ROADMAP.md)

## Legal And Project Policies

- [Privacy Policy](PRIVACY_POLICY.md)
- [Terms of Use](TERMS_OF_USE.md)
- [Proprietary License Notice](LICENSE.md)
- [Contribution Policy](CONTRIBUTING.md)
- [Security Policy](SECURITY.md)

## Build

Android:

```sh
./gradlew :composeApp:assembleDebug
```

Server:

```sh
./gradlew :server:run
```

## Local Server Environment

Kaze uses two server env files:

- [`.env.server.reference`](.env.server.reference): tracked reference for required variables
- `.env.server.local`: real local values and secrets, gitignored

Use `.env.server.local` for:
- `DATABASE_URL`
- `KAZE_JWT_SECRET`
- OAuth client secrets
- any other local or deployment credentials

Both scripts load `.env.server.local` automatically:

```sh
./run-server.sh
./deploy-cloudrun.sh
```

That means you do not need to manually export the same variables every time.

`./run-server.sh` can also sync Cloud Run before starting your local backend when:

- `SYNC_CLOUD_ON_RUN=1`
- `PROJECT_ID` is set
- `KAZE_JWT_SECRET` is set

If you want to skip the deploy step for a local-only run:

```sh
SYNC_CLOUD_ON_RUN=0 ./run-server.sh
```

Web:

```sh
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

iOS:

Open [`iosApp`](iosApp) in Xcode.
