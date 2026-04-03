package dev.orestegabo.kaze.domain.map

fun hotelMap(
    hotelId: String,
    mapId: String,
    name: String,
    block: HotelMapBuilder.() -> Unit,
): HotelMap = HotelMapBuilder(hotelId = hotelId, mapId = mapId, name = name)
    .apply(block)
    .build()

class HotelMapBuilder internal constructor(
    private val hotelId: String,
    private val mapId: String,
    private val name: String,
) {
    private val floors = mutableListOf<FloorLevel>()

    fun floor(
        id: String,
        buildingId: String,
        label: String,
        levelIndex: Int,
        width: Float,
        height: Float,
        block: FloorLevelBuilder.() -> Unit,
    ) {
        floors += FloorLevelBuilder(
            id = id,
            buildingId = buildingId,
            label = label,
            levelIndex = levelIndex,
            canvasSize = MapSize(width = width, height = height),
        ).apply(block).build()
    }

    internal fun build(): HotelMap = HotelMap(
        hotelId = hotelId,
        mapId = mapId,
        name = name,
        floors = floors.toList(),
    ).also(::validate)

    private fun validate(map: HotelMap) {
        val knownNodeIds = map.allNodes.keys
        map.floors.flatMap { it.edges }.forEach { edge ->
            require(edge.fromNodeId in knownNodeIds) {
                "Unknown fromNodeId '${edge.fromNodeId}' in edge '${edge.id}'"
            }
            require(edge.toNodeId in knownNodeIds) {
                "Unknown toNodeId '${edge.toNodeId}' in edge '${edge.id}'"
            }
        }
    }
}

class FloorLevelBuilder internal constructor(
    private val id: String,
    private val buildingId: String,
    private val label: String,
    private val levelIndex: Int,
    private val canvasSize: MapSize,
) {
    private val nodes = mutableListOf<MapNode>()
    private val edges = mutableListOf<MapEdge>()
    private val areas = mutableListOf<MapArea>()
    private val strokes = mutableListOf<FloorStroke>()

    fun node(
        id: String,
        label: String,
        kind: MapNodeKind,
        x: Float,
        y: Float,
        accessibilityTags: Set<AccessibilityTag> = emptySet(),
        isDestination: Boolean = true,
    ) {
        nodes += MapNode(
            id = id,
            label = label,
            kind = kind,
            floorId = this.id,
            position = MapPoint(x = x, y = y),
            accessibilityTags = accessibilityTags,
            isDestination = isDestination,
        )
    }

    fun edge(
        fromNodeId: String,
        toNodeId: String,
        routeType: RouteType = RouteType.WALKWAY,
        bidirectional: Boolean = true,
        weight: Float? = null,
        id: String = "${fromNodeId}_to_${toNodeId}",
    ) {
        edges += MapEdge(
            id = id,
            fromNodeId = fromNodeId,
            toNodeId = toNodeId,
            routeType = routeType,
            bidirectional = bidirectional,
            weight = weight,
        )
    }

    fun stroke(
        id: String,
        kind: FloorStrokeKind,
        closed: Boolean = false,
        block: StrokeBuilder.() -> Unit,
    ) {
        val builder = StrokeBuilder()
        builder.block()
        strokes += FloorStroke(
            id = id,
            kind = kind,
            points = builder.build(),
            closed = closed,
        )
    }

    fun area(
        id: String,
        label: String,
        kind: MapAreaKind,
        accessRule: AccessRule = AccessRule(),
        block: StrokeBuilder.() -> Unit,
    ) {
        val builder = StrokeBuilder()
        builder.block()
        areas += MapArea(
            id = id,
            label = label,
            kind = kind,
            points = builder.build(),
            accessRule = accessRule,
        )
    }

    internal fun build(): FloorLevel = FloorLevel(
        id = id,
        buildingId = buildingId,
        label = label,
        levelIndex = levelIndex,
        canvasSize = canvasSize,
        nodes = nodes.toList(),
        edges = edges.toList(),
        areas = areas.toList(),
        strokes = strokes.toList(),
    )
}

class StrokeBuilder {
    private val points = mutableListOf<MapPoint>()

    fun point(x: Float, y: Float) {
        points += MapPoint(x = x, y = y)
    }

    internal fun build(): List<MapPoint> = points.toList()
}
