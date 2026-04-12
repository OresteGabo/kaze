package dev.orestegabo.kaze.ui.map.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import dev.orestegabo.kaze.ui.map.model.MapCoordinate
import kotlin.math.min

internal class MapScaler(
    private val sourceBounds: Rect = Rect(0f, 0f, 100f, 100f),
    private val contentPadding: Float = 18f,
) {
    fun project(coordinate: MapCoordinate, size: Size): Offset {
        val availableWidth = (size.width - contentPadding * 2f).coerceAtLeast(1f)
        val availableHeight = (size.height - contentPadding * 2f).coerceAtLeast(1f)
        val sourceWidth = sourceBounds.width.coerceAtLeast(1f)
        val sourceHeight = sourceBounds.height.coerceAtLeast(1f)
        val scale = min(availableWidth / sourceWidth, availableHeight / sourceHeight)
        val renderedWidth = sourceWidth * scale
        val renderedHeight = sourceHeight * scale
        val offsetX = (size.width - renderedWidth) / 2f
        val offsetY = (size.height - renderedHeight) / 2f

        return Offset(
            x = offsetX + (coordinate.x.toFloat() - sourceBounds.left) * scale,
            y = offsetY + (coordinate.y.toFloat() - sourceBounds.top) * scale,
        )
    }

    fun projectPolygon(points: List<MapCoordinate>, size: Size): List<Offset> =
        points.map { project(it, size) }
}
