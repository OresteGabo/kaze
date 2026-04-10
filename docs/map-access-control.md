# Map Access Control

## Why It Matters

Kaze maps are not only spatial. They are permission-aware.

Some areas may be:
- public
- guest-only
- event-only
- VIP-only
- staff-only
- hidden/private
- reservation-only
- pass-required

## Shared Model

The shared map domain supports access rules per map area.

Relevant files:
- [HotelMap.kt](../shared/src/commonMain/kotlin/dev/orestegabo/kaze/domain/map/HotelMap.kt)
- [HotelMapDsl.kt](../shared/src/commonMain/kotlin/dev/orestegabo/kaze/domain/map/HotelMapDsl.kt)
- [SampleHotelMaps.kt](../shared/src/commonMain/kotlin/dev/orestegabo/kaze/domain/map/SampleHotelMaps.kt)

Core concepts:
- `AccessLevel`
- `AccessStatus`
- `AccessRule`

## Rendering Rules

Current renderer behavior:
- accessible areas render normally
- restricted areas can render with translucent red overlays
- hidden areas can be omitted entirely

This allows the same floor plan to look different for different users.

## Example Scenarios

- ballroom reserved for summit delegates
- staff corridor hidden from guests
- VIP lounge hidden from standard visitors
- day-pass visitor sees pool but not room corridors
- wedding reception hall requires a valid Kaze Pass after payment
- conference breakout room becomes accessible only to confirmed attendees
- stadium section access depends on ticketed seat/zone entitlements

## Future Direction

Production access context should come from backend entitlements, not local sample state.

That should include:
- guest type
- event role
- VIP level
- hotel policy
- time window
- reservation state
- payment state
- venue-specific pass entitlements
