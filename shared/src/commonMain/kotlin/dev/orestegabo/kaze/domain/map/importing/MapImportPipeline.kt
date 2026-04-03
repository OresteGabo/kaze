package dev.orestegabo.kaze.domain.map.importing

import dev.orestegabo.kaze.domain.map.FloorLevel
import dev.orestegabo.kaze.domain.map.FloorStroke
import dev.orestegabo.kaze.domain.map.HotelMap
import dev.orestegabo.kaze.domain.map.MapArea
import dev.orestegabo.kaze.domain.map.MapEdge
import dev.orestegabo.kaze.domain.map.MapNode
import dev.orestegabo.kaze.domain.map.MapPoint
import dev.orestegabo.kaze.domain.map.MapSize

interface FloorPlanImportStrategy {
    val id: String
    val supportedFormats: Set<MapSourceFormat>

    fun canImport(request: TenantScopedImportRequest): Boolean =
        request.sourceFile.format in supportedFormats

    fun import(request: TenantScopedImportRequest): ImportedFloorGeometry
}

class FloorPlanImportRegistry(
    strategies: List<FloorPlanImportStrategy>,
) {
    private val strategyByFormat: Map<MapSourceFormat, FloorPlanImportStrategy> = strategies
        .flatMap { strategy -> strategy.supportedFormats.map { it to strategy } }
        .toMap()

    fun resolve(format: MapSourceFormat): FloorPlanImportStrategy? = strategyByFormat[format]

    fun require(format: MapSourceFormat): FloorPlanImportStrategy =
        resolve(format) ?: error("No floor-plan importer registered for format '$format'")
}

class HotelMapImportPipeline(
    private val registry: FloorPlanImportRegistry,
    private val normalizer: ImportedGeometryNormalizer = ImportedGeometryNormalizer(),
) {
    fun import(
        manifest: HotelMapSourceManifest,
        requests: List<TenantScopedImportRequest>,
    ): ImportedHotelMapBundle {
        require(requests.isNotEmpty()) { "At least one import request is required." }
        val hotelIds = requests.map { it.hotelId }.distinct()
        require(hotelIds.size == 1 && hotelIds.single() == manifest.hotelId) {
            "Import requests must belong to the manifest hotel '${manifest.hotelId}'."
        }

        val floors = requests
            .sortedBy { it.sourceFile.levelIndex }
            .map { request ->
                val strategy = registry.require(request.sourceFile.format)
                val imported = strategy.import(request)
                normalizer.normalize(imported, request)
            }

        return ImportedHotelMapBundle(
            hotelId = manifest.hotelId,
            mapId = manifest.mapId,
            sourceManifest = manifest,
            floors = floors,
            diagnostics = emptyList(),
        )
    }
}

class ImportedGeometryNormalizer(
    private val fallbackConfig: GeometryNormalizerConfig = GeometryNormalizerConfig(),
) {
    fun normalize(
        floor: ImportedFloorGeometry,
        request: TenantScopedImportRequest,
    ): ImportedFloorGeometry {
        val config = fallbackConfig
        val resolvedSpaces = floor.spaces.map { space ->
            val alias = request.spaceOverrides[space.id] ?: request.spaceOverrides[space.sourceId]
            val accessRule = request.accessOverrides[space.id]
                ?: request.accessOverrides[space.sourceId]
                ?: space.accessRule

            space.copy(
                label = alias?.displayName ?: space.label,
                eventLabel = alias?.eventLabel ?: space.eventLabel,
                kind = alias?.areaKind ?: space.kind,
                accessRule = accessRule,
                polygon = space.polygon.normalizePoints(config),
            )
        }

        val resolvedNodes = floor.nodes.map { node ->
            val alias = request.spaceOverrides[node.id] ?: request.spaceOverrides[node.sourceId]
            node.copy(
                label = alias?.displayName ?: node.label,
                kind = alias?.nodeKind ?: node.kind,
                x = node.x.normalize(config.roundToDecimals),
                y = node.y.normalize(config.roundToDecimals),
            )
        }

        val resolvedEdges = floor.edges.map { edge ->
            edge.copy(weight = edge.weight?.normalize(config.roundToDecimals))
        }

        val resolvedStrokes = floor.strokes.map { stroke ->
            stroke.copy(points = stroke.points.normalizePoints(config))
        }

        val bounds = resolvedSpaces.flatMap { it.polygon } + resolvedNodes.map { ImportedPoint(it.x, it.y) } + resolvedStrokes.flatMap { it.points }
        val width = bounds.maxOfOrNull { it.x }?.normalize(config.roundToDecimals) ?: floor.width
        val height = bounds.maxOfOrNull { it.y }?.normalize(config.roundToDecimals) ?: floor.height

        return floor.copy(
            width = width.coerceAtLeast(floor.width),
            height = height.coerceAtLeast(floor.height),
            spaces = resolvedSpaces,
            nodes = resolvedNodes,
            edges = resolvedEdges,
            strokes = resolvedStrokes,
        )
    }
}

fun ImportedHotelMapBundle.toHotelMap(name: String): HotelMap = HotelMap(
    hotelId = hotelId,
    mapId = mapId,
    name = name,
    sourceManifest = sourceManifest,
    floors = floors.map { floor ->
        FloorLevel(
            id = floor.floorId,
            buildingId = floor.buildingId,
            label = floor.label,
            levelIndex = floor.levelIndex,
            canvasSize = MapSize(width = floor.width, height = floor.height),
            nodes = floor.nodes.map { node ->
                MapNode(
                    id = node.id,
                    label = node.label,
                    kind = node.kind,
                    floorId = floor.floorId,
                    position = MapPoint(x = node.x, y = node.y),
                    accessibilityTags = node.accessibilityTags,
                    isDestination = node.isDestination,
                )
            },
            edges = floor.edges.map { edge ->
                MapEdge(
                    id = edge.id,
                    fromNodeId = edge.fromNodeId,
                    toNodeId = edge.toNodeId,
                    routeType = edge.routeType,
                    bidirectional = edge.bidirectional,
                    weight = edge.weight,
                )
            },
            areas = floor.spaces.map { space ->
                MapArea(
                    id = space.id,
                    label = space.eventLabel ?: space.label,
                    kind = space.kind,
                    points = space.polygon.map { point -> MapPoint(x = point.x, y = point.y) },
                    accessRule = space.accessRule,
                )
            },
            strokes = floor.strokes.map { stroke ->
                FloorStroke(
                    id = stroke.id,
                    kind = stroke.kind,
                    points = stroke.points.map { point -> MapPoint(x = point.x, y = point.y) },
                    closed = stroke.closed,
                )
            },
        )
    },
)

private fun List<ImportedPoint>.normalizePoints(config: GeometryNormalizerConfig): List<ImportedPoint> {
    if (isEmpty()) return this

    val minX = minOf { it.x }
    val minY = minOf { it.y }
    return map { point ->
        ImportedPoint(
            x = (if (config.scaleToPositiveQuadrant) point.x - minX else point.x).normalize(config.roundToDecimals),
            y = (if (config.scaleToPositiveQuadrant) point.y - minY else point.y).normalize(config.roundToDecimals),
        )
    }
}

private fun Float.normalize(decimals: Int): Float {
    val factor = buildString {
        append("1")
        repeat(decimals) { append("0") }
    }.toFloat()
    return kotlin.math.round(this * factor) / factor
}
