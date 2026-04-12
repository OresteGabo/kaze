package dev.orestegabo.kaze.ui.map.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BuildingData(
    val type: String = "FeatureCollection",
    val name: String? = null,
    val features: List<Feature> = emptyList(),
)

@Serializable
data class Feature(
    val type: String = "Feature",
    val id: String,
    val properties: FeatureProperties = FeatureProperties(),
    val geometry: Geometry,
)

@Serializable
data class FeatureProperties(
    val name: String = "",
    val floor: Int = 1,
    val type: String = "unknown",
    val capacity: Int? = null,
    val status: String? = null,
    val outdoor: Boolean = false,
    @SerialName("furniture_sim")
    val furnitureSim: Boolean = false,
)

@Serializable
data class Geometry(
    val type: String = "Polygon",
    val coordinates: List<List<List<Double>>> = emptyList(),
) {
    val outerRing: List<MapCoordinate>
        get() = coordinates.firstOrNull()
            ?.mapNotNull { coordinate ->
                val x = coordinate.getOrNull(0)
                val y = coordinate.getOrNull(1)
                if (x != null && y != null) MapCoordinate(x, y) else null
            }
            .orEmpty()
}

@Serializable
data class MapCoordinate(
    val x: Double,
    val y: Double,
)
