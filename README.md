# Kaze

Kaze is a Kotlin Multiplatform digital concierge and access platform for luxury hotels, conferences, weddings, and day-visitor experiences in Rwanda and East Africa.

Instead of treating every user as a room guest, Kaze supports multiple visitor profiles:
- in-house hotel guests
- conference attendees
- event-only visitors
- amenity/day-pass visitors

The app combines itinerary management, indoor wayfinding, access-aware venue maps, service requests, and a customizable digital access pass.

## Modules

- [/composeApp](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/composeApp/src): Compose Multiplatform client UI
- [/shared](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/shared/src): shared domain models and map/access logic
- [/server](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/server/src/main/kotlin): Ktor backend
- [/iosApp](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/iosApp/iosApp): iOS entry app

## Current Product Shape

- `My Stay`: confirmed itinerary, access pass, reservations, and linked services
- `Requests`: late checkout, towels, room service, concierge, and other guest requests
- `Suggestions`: personalized concierge-style recommendations
- `Map`: floor-based indoor navigation with restricted and hidden area support

## Documentation

- [Product Overview](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/docs/product-overview.md)
- [Access Pass System](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/docs/access-pass-system.md)
- [Map Access Control](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/docs/map-access-control.md)
- [UI Architecture](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/docs/ui-architecture.md)
- [API Contracts](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/docs/kaze-api-contracts.md)
- [Roadmap](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/ROADMAP.md)

## Legal And Project Policies

- [Privacy Policy](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/PRIVACY_POLICY.md)
- [Terms of Use](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/TERMS_OF_USE.md)
- [Proprietary License Notice](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/LICENSE.md)
- [Contribution Policy](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/CONTRIBUTING.md)
- [Security Policy](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/SECURITY.md)

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

Open [/iosApp](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/iosApp) in Xcode.
