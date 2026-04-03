package dev.orestegabo.kaze.domain.map

import dev.orestegabo.kaze.domain.map.importing.HotelMapSourceManifest
import kotlin.math.hypot

data class HotelMap(
    val hotelId: String,
    val mapId: String,
    val name: String,
    val floors: List<FloorLevel>,
    val sourceManifest: HotelMapSourceManifest? = null,
) {
    val allNodes: Map<String, MapNode> = floors
        .flatMap { floor -> floor.nodes.map { node -> node.id to node } }
        .toMap()

    fun floor(floorId: String): FloorLevel? = floors.firstOrNull { it.id == floorId }

    fun neighbors(nodeId: String): List<MapEdge> = floors.flatMap { floor ->
        floor.edges.filter { edge -> edge.fromNodeId == nodeId || edge.toNodeId == nodeId }
    }
}

data class FloorLevel(
    val id: String,
    val buildingId: String,
    val label: String,
    val levelIndex: Int,
    val canvasSize: MapSize,
    val nodes: List<MapNode>,
    val edges: List<MapEdge>,
    val areas: List<MapArea> = emptyList(),
    val strokes: List<FloorStroke> = emptyList(),
)

data class MapNode(
    val id: String,
    val label: String,
    val kind: MapNodeKind,
    val floorId: String,
    val position: MapPoint,
    val accessibilityTags: Set<AccessibilityTag> = emptySet(),
    val isDestination: Boolean = true,
)

data class MapEdge(
    val id: String,
    val fromNodeId: String,
    val toNodeId: String,
    val routeType: RouteType = RouteType.WALKWAY,
    val bidirectional: Boolean = true,
    val weight: Float? = null,
) {
    fun resolvedWeight(nodes: Map<String, MapNode>): Float {
        val explicitWeight = weight
        if (explicitWeight != null) return explicitWeight

        val from = nodes.getValue(fromNodeId)
        val to = nodes.getValue(toNodeId)
        return hypot(to.position.x - from.position.x, to.position.y - from.position.y)
    }
}

data class FloorStroke(
    val id: String,
    val kind: FloorStrokeKind,
    val points: List<MapPoint>,
    val closed: Boolean = false,
)

data class MapArea(
    val id: String,
    val label: String,
    val kind: MapAreaKind,
    val points: List<MapPoint>,
    val accessRule: AccessRule = AccessRule(),
)

data class MapPoint(
    val x: Float,
    val y: Float,
)

data class MapSize(
    val width: Float,
    val height: Float,
)

enum class MapNodeKind {
    ROOM,
    SUITE,
    ELEVATOR,
    STAIRS,
    LOBBY,
    RESTAURANT,
    BALLROOM,
    SPA,
    GYM,
    POOL,
    BAR,
    CONCIERGE,
    RESTROOM,
    EXIT,
    LANDMARK,
}

enum class RouteType {
    WALKWAY,
    ACCESSIBLE,
    SERVICE_ONLY,
    STAIRS,
    ELEVATOR,
}

enum class FloorStrokeKind {
    OUTLINE,
    WALL,
    ROOM_BOUNDARY,
    DECOR,
}

enum class MapAreaKind {
    HALLWAY,
    LOBBY_LOUNGE,
    BALLROOM,
    DINING,
    SERVICE,
    GUEST_ROOM,
    RECEPTION,
    SUPPORT,
}

data class AccessRule(
    val level: AccessLevel = AccessLevel.PUBLIC,
    val status: AccessStatus = AccessStatus.OPEN,
    val note: String? = null,
)

enum class AccessLevel {
    PUBLIC,
    IN_HOUSE_GUEST,
    EVENT_ATTENDEE,
    VIP,
    STAFF,
    SECRET,
}

enum class AccessStatus {
    OPEN,
    LIMITED,
    RESTRICTED,
    HIDDEN,
}

enum class AccessibilityTag {
    WHEELCHAIR,
    LOW_LIGHT,
    HIGH_TRAFFIC,
    STAFF_ASSIST,
}
