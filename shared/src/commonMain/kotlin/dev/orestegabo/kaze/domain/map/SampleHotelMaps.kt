package dev.orestegabo.kaze.domain.map

val sampleMarriottConventionMap = hotelMap(
    hotelId = "rw-kgl-marriott",
    mapId = "temporary-svg-venue",
    name = "Temporary SVG Venue Map",
) {
    floor(
        id = "l1",
        buildingId = "main-venue",
        label = "Ground floor",
        levelIndex = 0,
        width = 1200f,
        height = 1200f,
    ) {
        // TODO [PENDING]: Replace these temporary venue coordinates and labels with real hotel
        // spaces once branded hotel SVG plans are ready.
        node(id = "auditorium-1", label = "Auditorium, Room 1", kind = MapNodeKind.BALLROOM, x = 252f, y = 924f)
        node(id = "lightning-room", label = "Room 5, Lightning talks", kind = MapNodeKind.ROOM, x = 444f, y = 192f)
        node(id = "room-2", label = "Room 2", kind = MapNodeKind.ROOM, x = 360f, y = 420f)
        node(id = "room-3", label = "Room 3", kind = MapNodeKind.ROOM, x = 420f, y = 420f)
        node(id = "party", label = "Party", kind = MapNodeKind.LANDMARK, x = 816f, y = 342f)
        node(id = "registration", label = "Registration", kind = MapNodeKind.CONCIERGE, x = 552f, y = 792f)
    }

    floor(
        id = "l9",
        buildingId = "main-venue",
        label = "First floor",
        levelIndex = 1,
        width = 1200f,
        height = 1200f,
    ) {
        // TODO [PENDING]: Replace these temporary venue coordinates and labels with real hotel
        // spaces once branded hotel SVG plans are ready.
        node(id = "room-11b", label = "Room 11b", kind = MapNodeKind.ROOM, x = 360f, y = 396f)
        node(id = "room-11a", label = "Room 11a", kind = MapNodeKind.ROOM, x = 360f, y = 444f)
        node(id = "room-12b", label = "Room 12b", kind = MapNodeKind.ROOM, x = 540f, y = 396f)
        node(id = "room-12a", label = "Room 12a", kind = MapNodeKind.ROOM, x = 540f, y = 444f)
        node(id = "room-13a", label = "Room 13a", kind = MapNodeKind.ROOM, x = 396f, y = 252f)
        node(id = "room-13b", label = "Room 13b", kind = MapNodeKind.ROOM, x = 492f, y = 252f)
        node(id = "keynote-room", label = "Keynote, Room 14", kind = MapNodeKind.BALLROOM, x = 864f, y = 324f)
    }
}
