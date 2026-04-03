package dev.orestegabo.kaze.domain.map.importing

import dev.orestegabo.kaze.domain.map.AccessRule
import dev.orestegabo.kaze.domain.map.AccessibilityTag
import dev.orestegabo.kaze.domain.map.FloorStrokeKind
import dev.orestegabo.kaze.domain.map.MapAreaKind
import dev.orestegabo.kaze.domain.map.MapNodeKind
import dev.orestegabo.kaze.domain.map.RouteType

data class MapImportProfile(
    val preferredFormats: List<MapSourceFormat>,
    val fallbackFormats: List<MapSourceFormat> = emptyList(),
    val normalizer: GeometryNormalizerConfig = GeometryNormalizerConfig(),
)

data class HotelMapSourceManifest(
    val hotelId: String,
    val mapId: String,
    val version: String,
    val sourceFiles: List<FloorPlanSourceFile>,
    val sourceSystem: SourceSystemDescriptor? = null,
)

data class SourceSystemDescriptor(
    val name: String,
    val version: String? = null,
    val exporter: String? = null,
)

data class FloorPlanSourceFile(
    val id: String,
    val floorId: String,
    val buildingId: String,
    val label: String,
    val format: MapSourceFormat,
    val path: String,
    val checksum: String? = null,
    val levelIndex: Int,
)

enum class MapSourceFormat {
    SVG,
    DXF,
    DWG,
    IFC,
    IFCXML,
    GBXML,
    JSON,
    PDF,
}

data class TenantScopedImportRequest(
    val hotelId: String,
    val mapId: String,
    val sourceFile: FloorPlanSourceFile,
    val payload: String,
    val buildingAliases: Map<String, String> = emptyMap(),
    val spaceOverrides: Map<String, SpaceAliasOverride> = emptyMap(),
    val accessOverrides: Map<String, AccessRule> = emptyMap(),
)

data class SpaceAliasOverride(
    val displayName: String,
    val eventLabel: String? = null,
    val nodeKind: MapNodeKind? = null,
    val areaKind: MapAreaKind? = null,
)

data class GeometryNormalizerConfig(
    val roundToDecimals: Int = 2,
    val mergeCollinearSegments: Boolean = true,
    val removeTinySegmentsBelow: Float = 1f,
    val scaleToPositiveQuadrant: Boolean = true,
)

data class ImportedHotelMapBundle(
    val hotelId: String,
    val mapId: String,
    val sourceManifest: HotelMapSourceManifest,
    val floors: List<ImportedFloorGeometry>,
    val diagnostics: List<ImportDiagnostic> = emptyList(),
) {
    fun floor(floorId: String): ImportedFloorGeometry? = floors.firstOrNull { it.floorId == floorId }
}

data class ImportedFloorGeometry(
    val floorId: String,
    val buildingId: String,
    val label: String,
    val levelIndex: Int,
    val width: Float,
    val height: Float,
    val spaces: List<ImportedSpaceGeometry>,
    val nodes: List<ImportedNodeGeometry>,
    val edges: List<ImportedEdgeGeometry>,
    val strokes: List<ImportedStrokeGeometry> = emptyList(),
    val sourceFileId: String,
)

data class ImportedSpaceGeometry(
    val id: String,
    val sourceId: String,
    val label: String,
    val eventLabel: String? = null,
    val kind: MapAreaKind,
    val polygon: List<ImportedPoint>,
    val accessRule: AccessRule = AccessRule(),
)

data class ImportedNodeGeometry(
    val id: String,
    val sourceId: String,
    val label: String,
    val kind: MapNodeKind,
    val x: Float,
    val y: Float,
    val accessibilityTags: Set<AccessibilityTag> = emptySet(),
    val isDestination: Boolean = true,
)

data class ImportedEdgeGeometry(
    val id: String,
    val fromNodeId: String,
    val toNodeId: String,
    val routeType: RouteType = RouteType.WALKWAY,
    val bidirectional: Boolean = true,
    val weight: Float? = null,
)

data class ImportedStrokeGeometry(
    val id: String,
    val kind: FloorStrokeKind,
    val points: List<ImportedPoint>,
    val closed: Boolean = false,
)

data class ImportedPoint(
    val x: Float,
    val y: Float,
)

data class ImportDiagnostic(
    val level: ImportDiagnosticLevel,
    val code: String,
    val message: String,
    val sourceId: String? = null,
)

enum class ImportDiagnosticLevel {
    INFO,
    WARNING,
    ERROR,
}
