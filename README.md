# Kaze

Kaze is a Kotlin Multiplatform venue experience, access, and commerce platform for hotels, conferences, weddings, and day-visitor experiences in Rwanda and East Africa.

Instead of treating every user as a room guest, Kaze supports multiple visitor profiles:
- in-house hotel guests
- conference attendees
- event-only visitors
- amenity/day-pass visitors

The app combines itinerary management, indoor wayfinding, access-aware venue maps, service requests, venue reservations, payments, and a customizable digital access pass.

Kaze is also evolving toward a reusable venue-mapping and venue-commerce platform that can support:
- hotels
- conference venues
- wedding venues
- stadiums
- government buildings
- transport spaces such as airports or airplanes

## Modules

- [`composeApp/src`](composeApp/src): Compose Multiplatform client UI
- [`shared/src`](shared/src): shared domain models and map/access logic
- [`server/src/main/kotlin`](server/src/main/kotlin): Ktor backend
- [`iosApp/iosApp`](iosApp/iosApp): iOS entry app

## Current Product Shape

- `My Stay`: confirmed itinerary, access pass, reservations, and linked services
- `Requests`: late checkout, towels, room service, concierge, and other guest requests
- `Suggestions`: personalized concierge-style recommendations
- `Map`: floor-based indoor navigation with restricted and hidden area support

## Revenue Direction

Kaze is not only a guest app. It can also grow into a venue monetization platform through:
- conference-room reservations
- wedding venue reservations
- day-pass and amenity bookings
- local payment support in Rwanda
  - MTN MoMo
  - Airtel Money
  - BK / RSwitch and other compatible payment rails
- add-on service sales
  - decoration
  - cleaning
  - insurance
  - photography and videography
  - live streaming
- lower-commission direct venue bookings compared with some international platforms

## Documentation

- [Product Overview](docs/product-overview.md)
- [Venue Platform Strategy](docs/venue-platform-strategy.md)
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

Web:

```sh
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

iOS:

Open [`iosApp`](iosApp) in Xcode.
