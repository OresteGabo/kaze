# Kaze

Kaze is a Kotlin Multiplatform event operating system for conferences, weddings, summits, and premium gatherings, starting in Rwanda and East Africa with room to expand into West Africa through strong local marketing partners.

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

Kaze should start as a free event and venue marketplace.

At launch:
- attendees and guests should not pay Kaze to use the app
- organizers should be able to coordinate events without a Kaze platform fee
- venue owners and managers should be able to list, show, and rent their places without Kaze commission
- service providers should be able to register and sell event-linked services without Kaze taking an early marketplace cut
- customers should see Kaze as a way to reduce friction, not as another cost layer

The long-term business model should come from demand Kaze helps organize, not from making the first users pay more. Kaze can later earn through:
- Kaze-owned service categories that prove strong demand
- optional managed service packages
- partner service arrangements that do not inflate the customer price
- unavoidable payment-provider charges when a payment rail applies
- local payment support in Rwanda
  - MTN MoMo
  - Airtel Money
  - BK / RSwitch and other compatible payment rails
- future payment rails in other countries
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
- [Free Launch And Future Fees](docs/free-launch-and-future-fees.md)
- [Architecture Diagrams](docs/diagrams/README.md)
- [Access Pass System](docs/access-pass-system.md)
- [Map Access Control](docs/map-access-control.md)
- [UI Architecture](docs/ui-architecture.md)
- [API Contracts](docs/kaze-api-contracts.md)
- [Security Checklist](docs/security-checklist.md)
- [Environments](docs/environments.md)
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

Kaze uses local and deployment-specific server env files:

- [`.env.server.reference`](.env.server.reference): tracked reference for required variables
- `.env.server.local`: real local values and secrets, gitignored
- `.env.server.dev.reference`: tracked reference for development Cloud Run
- `.env.server.staging.reference`: tracked reference for staging Cloud Run
- `.env.server.production.reference`: tracked reference for production Cloud Run

Use `.env.server.local` for:
- `DATABASE_URL`
- `KAZE_JWT_SECRET`
- OAuth client secrets
- any other local or deployment credentials

Apple sign-in server variables also belong in `.env.server.local`:
- `APPLE_SERVICE_ID`
- `APPLE_TEAM_ID`
- `APPLE_KEY_ID`
- `APPLE_PRIVATE_KEY_PEM`
- `APPLE_REDIRECT_URI`
- `KAZE_APPLE_CLIENT_IDS`

Only put these in an Android Studio Run Configuration if you are launching the Ktor server directly from Android Studio. They do not belong in the mobile app environment itself.

Local startup loads `.env.server.local` automatically:

```sh
./run-server.sh
```

Deployments should use an explicit environment file:

```sh
ENV_FILE=.env.server.dev.local ./deploy-cloudrun.sh
ENV_FILE=.env.server.staging.local ./deploy-cloudrun.sh
ENV_FILE=.env.server.production.local ./deploy-cloudrun.sh
```

`./run-server.sh` does not sync Cloud Run by default. It can sync Cloud Run before starting your local backend only when:

- `SYNC_CLOUD_ON_RUN=1`
- `PROJECT_ID` is set
- `KAZE_JWT_SECRET` is set

For normal local-only work:

```sh
./run-server.sh
```

Web:

```sh
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

iOS:

Open [`iosApp`](iosApp) in Xcode.
