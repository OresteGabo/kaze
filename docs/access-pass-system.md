# Access Pass System

## Purpose

Kaze uses a single digital access pass instead of assuming a room-key-only model.

The pass can represent:
- a hotel stay
- a conference credential
- a wedding or private-event pass
- a dining or pool day pass
- a mixed-access profile

## Current Domain Model

The shared model lives in:
- [DigitalStayCard.kt](/Users/muhirwagabooreste/AndroidStudioProjects/kaze/shared/src/commonMain/kotlin/dev/orestegabo/kaze/domain/DigitalStayCard.kt)

`DigitalAccessCard` currently carries:
- `id`
- `title`
- `subtitle`
- `contextLabel`
- `primaryAccessRef`
- `linkedAccess`
- `style`

## Styling Strategy

The pass uses a style abstraction so each hotel or event can own its own design language.

Supported style variants:
- `KazeDefault`
- `HotelBranded`
- `EventSignature`

This allows:
- a default Kaze visual
- a hotel-specific branded pass
- a major-event custom pass

## Current UX Pattern

The pass face should stay minimal:
- title
- card number
- visual identity
- primary access reference

The details are revealed after interaction.

## Future Uses

Even without mobile door unlock, the pass can still work as:
- stay identity
- event identity
- staff scan reference
- entitlement container for services
- front-desk verification token

## Future Integration Paths

Possible future additions:
- real QR code generation
- Wallet/pass integration
- vendor lock/mobile-key integration
- staff scanning workflows
- time-bound entitlement rules
