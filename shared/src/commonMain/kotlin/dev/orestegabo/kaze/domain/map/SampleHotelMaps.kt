package dev.orestegabo.kaze.domain.map

val sampleMarriottConventionMap = hotelMap(
    hotelId = "rw-kgl-marriott",
    mapId = "convention-wing",
    name = "Convention Wing",
) {
    floor(
        id = "l1",
        buildingId = "main-tower",
        label = "Lobby Level",
        levelIndex = 1,
        width = 1000f,
        height = 700f,
    ) {
        area(id = "arrival-court", label = "Arrival Court", kind = MapAreaKind.HALLWAY) {
            point(60f, 110f)
            point(950f, 110f)
            point(950f, 180f)
            point(60f, 180f)
        }
        area(id = "grand-lobby", label = "Grand Lobby", kind = MapAreaKind.LOBBY_LOUNGE) {
            point(70f, 190f)
            point(350f, 190f)
            point(350f, 470f)
            point(70f, 470f)
        }
        area(id = "reception-zone", label = "Reception", kind = MapAreaKind.RECEPTION) {
            point(84f, 212f)
            point(194f, 212f)
            point(194f, 302f)
            point(84f, 302f)
        }
        area(id = "central-gallery", label = "Main Gallery", kind = MapAreaKind.HALLWAY) {
            point(350f, 220f)
            point(760f, 220f)
            point(760f, 315f)
            point(350f, 315f)
        }
        area(
            id = "great-rift-ballroom",
            label = "Great Rift Ballroom",
            kind = MapAreaKind.BALLROOM,
            accessRule = AccessRule(
                level = AccessLevel.EVENT_ATTENDEE,
                status = AccessStatus.LIMITED,
                note = "Reserved for summit badge holders until 18:00.",
            ),
        ) {
            point(760f, 170f)
            point(930f, 170f)
            point(930f, 430f)
            point(760f, 430f)
        }
        area(id = "kivu-dining", label = "Kivu Dining", kind = MapAreaKind.DINING) {
            point(430f, 360f)
            point(700f, 360f)
            point(700f, 560f)
            point(430f, 560f)
        }
        area(
            id = "service-back",
            label = "Service Spine",
            kind = MapAreaKind.SERVICE,
            accessRule = AccessRule(
                level = AccessLevel.STAFF,
                status = AccessStatus.RESTRICTED,
                note = "Back-of-house corridor.",
            ),
        ) {
            point(720f, 470f)
            point(930f, 470f)
            point(930f, 575f)
            point(720f, 575f)
        }
        area(
            id = "owners-lounge",
            label = "Private Lounge",
            kind = MapAreaKind.SUPPORT,
            accessRule = AccessRule(
                level = AccessLevel.SECRET,
                status = AccessStatus.HIDDEN,
                note = "Hidden private lounge for management and invited VIPs.",
            ),
        ) {
            point(610f, 115f)
            point(735f, 115f)
            point(735f, 165f)
            point(610f, 165f)
        }

        stroke(id = "l1-outline", kind = FloorStrokeKind.OUTLINE, closed = true) {
            point(40f, 60f)
            point(960f, 60f)
            point(960f, 640f)
            point(40f, 640f)
        }
        stroke(id = "l1-lobby-wall", kind = FloorStrokeKind.ROOM_BOUNDARY, closed = true) {
            point(70f, 190f)
            point(350f, 190f)
            point(350f, 470f)
            point(70f, 470f)
        }
        stroke(id = "l1-corridor-wall", kind = FloorStrokeKind.WALL, closed = true) {
            point(350f, 220f)
            point(760f, 220f)
            point(760f, 315f)
            point(350f, 315f)
        }
        stroke(id = "l1-ballroom-wall", kind = FloorStrokeKind.WALL, closed = true) {
            point(760f, 170f)
            point(930f, 170f)
            point(930f, 430f)
            point(760f, 430f)
        }
        stroke(id = "l1-dining-wall", kind = FloorStrokeKind.ROOM_BOUNDARY, closed = true) {
            point(430f, 360f)
            point(700f, 360f)
            point(700f, 560f)
            point(430f, 560f)
        }

        node(id = "reception", label = "Reception", kind = MapNodeKind.CONCIERGE, x = 138f, y = 256f)
        node(id = "lobby", label = "Grand Lobby", kind = MapNodeKind.LOBBY, x = 240f, y = 280f)
        node(id = "elevator-a", label = "Elevator A", kind = MapNodeKind.ELEVATOR, x = 430f, y = 268f)
        node(id = "ballroom", label = "Great Rift Ballroom", kind = MapNodeKind.BALLROOM, x = 845f, y = 285f)
        node(id = "restaurant", label = "Kivu Dining", kind = MapNodeKind.RESTAURANT, x = 560f, y = 455f)
        node(id = "exit-east", label = "East Exit", kind = MapNodeKind.EXIT, x = 930f, y = 220f, isDestination = false)

        edge("reception", "lobby")
        edge("lobby", "elevator-a")
        edge("elevator-a", "ballroom", routeType = RouteType.ACCESSIBLE)
        edge("lobby", "restaurant")
        edge("ballroom", "exit-east")
    }

    floor(
        id = "l9",
        buildingId = "guest-wing",
        label = "Guest Rooms",
        levelIndex = 9,
        width = 900f,
        height = 500f,
    ) {
        area(id = "guest-corridor", label = "Guest Corridor", kind = MapAreaKind.HALLWAY) {
            point(75f, 180f)
            point(820f, 180f)
            point(820f, 255f)
            point(75f, 255f)
        }
        area(id = "room-906-area", label = "Room 906", kind = MapAreaKind.GUEST_ROOM) {
            point(90f, 90f)
            point(220f, 90f)
            point(220f, 180f)
            point(90f, 180f)
        }
        area(id = "room-908-area", label = "Room 908", kind = MapAreaKind.GUEST_ROOM) {
            point(240f, 90f)
            point(370f, 90f)
            point(370f, 180f)
            point(240f, 180f)
        }
        area(id = "lift-lobby-area", label = "Lift Lobby", kind = MapAreaKind.SUPPORT) {
            point(380f, 120f)
            point(520f, 120f)
            point(520f, 320f)
            point(380f, 320f)
        }
        area(
            id = "service-core-area",
            label = "Service Core",
            kind = MapAreaKind.SERVICE,
            accessRule = AccessRule(
                level = AccessLevel.STAFF,
                status = AccessStatus.RESTRICTED,
                note = "Housekeeping and engineering only.",
            ),
        ) {
            point(610f, 95f)
            point(790f, 95f)
            point(790f, 180f)
            point(610f, 180f)
        }

        stroke(id = "l9-corridor", kind = FloorStrokeKind.WALL, closed = true) {
            point(75f, 180f)
            point(820f, 180f)
            point(820f, 255f)
            point(75f, 255f)
        }
        stroke(id = "l9-room-906", kind = FloorStrokeKind.ROOM_BOUNDARY, closed = true) {
            point(90f, 90f)
            point(220f, 90f)
            point(220f, 180f)
            point(90f, 180f)
        }
        stroke(id = "l9-room-908", kind = FloorStrokeKind.ROOM_BOUNDARY, closed = true) {
            point(240f, 90f)
            point(370f, 90f)
            point(370f, 180f)
            point(240f, 180f)
        }

        node(id = "room-906", label = "Room 906", kind = MapNodeKind.ROOM, x = 155f, y = 138f)
        node(id = "elevator-a-l9", label = "Elevator A", kind = MapNodeKind.ELEVATOR, x = 450f, y = 220f)
        node(id = "stairs-l9", label = "Stairs", kind = MapNodeKind.STAIRS, x = 720f, y = 220f)

        edge("room-906", "elevator-a-l9")
        edge("elevator-a-l9", "stairs-l9", bidirectional = false, routeType = RouteType.SERVICE_ONLY)
    }
}
