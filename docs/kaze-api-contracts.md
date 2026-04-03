# Kaze Guest API Contracts

These payloads assume every request is tenant-scoped by `hotelId`, whether the guest enters a property code manually or arrives from a room QR deep link.

## `GET /api/v1/hotels/{hotelId}/guests/{guestId}/stay`

```json
{
  "hotelId": "rw-kgl-marriott",
  "guestId": "guest_48392",
  "stayId": "stay_2026_04_03_001",
  "status": "IN_HOUSE",
  "guest": {
    "firstName": "Aline",
    "lastName": "Mukamana",
    "language": "en",
    "loyaltyTier": "AMBASSADOR"
  },
  "room": {
    "roomNumber": "906",
    "floorId": "l9",
    "mapNodeId": "room-906"
  },
  "windows": {
    "checkInIsoUtc": "2026-04-03T12:00:00Z",
    "checkOutIsoUtc": "2026-04-06T10:00:00Z"
  },
  "tabs": [
    {
      "mode": "MY_STAY",
      "title": "My Stay",
      "sections": [
        {
          "id": "arrival",
          "title": "Arrival",
          "items": [
            {
              "id": "check_in",
              "title": "Suite check-in",
              "category": "CHECK_IN",
              "status": "COMPLETED",
              "startIsoUtc": "2026-04-03T12:00:00Z",
              "endIsoUtc": "2026-04-03T12:20:00Z",
              "venue": {
                "label": "Reception",
                "floorId": "l1",
                "nodeId": "reception"
              }
            }
          ]
        },
        {
          "id": "wellness",
          "title": "Wellness & Dining",
          "items": [
            {
              "id": "spa_booking",
              "title": "Signature massage",
              "category": "SPA",
              "status": "CONFIRMED",
              "startIsoUtc": "2026-04-04T14:00:00Z",
              "endIsoUtc": "2026-04-04T15:00:00Z",
              "venue": {
                "label": "Ubumwe Spa",
                "floorId": "l3",
                "nodeId": "spa"
              },
              "serviceRequestAllowed": true
            }
          ]
        }
      ]
    },
    {
      "mode": "EXPLORE",
      "title": "Explore",
      "sections": [
        {
          "id": "amenities",
          "title": "Amenities",
          "items": [
            {
              "id": "pool_hours",
              "title": "Infinity pool",
              "category": "AMENITY",
              "status": "CONFIRMED",
              "startIsoUtc": "2026-04-03T05:00:00Z",
              "endIsoUtc": "2026-04-03T20:00:00Z",
              "venue": {
                "label": "Pool Deck",
                "floorId": "l4",
                "nodeId": "pool"
              }
            }
          ]
        }
      ]
    }
  ],
  "theme": {
    "primaryHex": "#B8924A",
    "secondaryHex": "#6E5A3A",
    "accentHex": "#D7C09A",
    "logoUrl": "https://cdn.kaze.africa/hotels/rw-kgl-marriott/logo.svg"
  }
}
```

## `GET /api/v1/hotels/{hotelId}/events/schedule`

```json
{
  "hotelId": "rw-kgl-marriott",
  "eventId": "eaf-summit-2026",
  "title": "East Africa Finance Summit 2026",
  "dateRange": {
    "startIsoUtc": "2026-04-04T06:00:00Z",
    "endIsoUtc": "2026-04-06T18:00:00Z"
  },
  "tabs": [
    {
      "mode": "WHATS_ON",
      "title": "What's On",
      "sections": [
        {
          "id": "day_1",
          "title": "Saturday, 4 April",
          "items": [
            {
              "id": "opening_keynote",
              "title": "Opening keynote",
              "category": "EVENT_SESSION",
              "status": "CONFIRMED",
              "startIsoUtc": "2026-04-04T08:00:00Z",
              "endIsoUtc": "2026-04-04T09:15:00Z",
              "venue": {
                "label": "Great Rift Ballroom",
                "floorId": "l1",
                "nodeId": "ballroom"
              },
              "notes": "Doors open 20 minutes early."
            }
          ]
        }
      ]
    }
  ],
  "wayfinding": {
    "defaultStartNodeId": "room-906",
    "featuredDestinationNodeIds": [
      "ballroom",
      "restaurant"
    ]
  }
}
```
