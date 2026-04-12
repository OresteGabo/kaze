package dev.orestegabo.kaze.ui.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import dev.orestegabo.kaze.ui.map.model.BuildingData
import dev.orestegabo.kaze.ui.map.model.Feature
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val kazeMapJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

@Composable
internal fun KazeMapView(
    jsonString: String,
    currentFloor: Int,
    selectedRoomId: String?,
    onRoomSelected: (Feature) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp? = 420.dp,
) {
    val colors = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()
    val building = remember(jsonString) {
        runCatching { kazeMapJson.decodeFromString<BuildingData>(jsonString) }.getOrNull()
    }

    if (building == null) {
        Box(
            modifier = modifier
                .then(if (height == null) Modifier.fillMaxSize() else Modifier.height(height))
                .background(colors.surfaceVariant.copy(alpha = 0.32f), RoundedCornerShape(24.dp))
                .padding(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Map data could not be loaded.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.72f),
            )
        }
        return
    }

    val floorFeatures = building.features.filter { feature ->
        feature.geometry.type == "Polygon" && feature.properties.floor == currentFloor && feature.geometry.outerRing.size >= 3
    }
    val scaler = remember(floorFeatures) {
        MapScaler(sourceBounds = floorFeatures.sourceBounds().inflate(3f), contentPadding = 12f)
    }
    var zoom by remember(jsonString, currentFloor) { mutableFloatStateOf(1f) }
    var panOffset by remember(jsonString, currentFloor) { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .then(if (height == null) Modifier.fillMaxSize() else Modifier.height(height))
            .fillMaxWidth()
            .pointerInput(jsonString, currentFloor) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    zoom = (zoom * zoomChange).coerceIn(1f, 4f)
                    panOffset += pan
                }
            }
            .pointerInput(jsonString, currentFloor, selectedRoomId, floorFeatures) {
                detectTapGestures { tapOffset ->
                    val mapOffset = (tapOffset - panOffset) / zoom
                    val selectedFeature = floorFeatures.lastOrNull { feature ->
                        pointInPolygon(
                            point = mapOffset,
                            polygon = scaler.projectPolygon(feature.geometry.outerRing, size.toSize()),
                        )
                    }
                    if (selectedFeature != null) {
                        onRoomSelected(selectedFeature)
                    }
                }
            },
    ) {
        drawMapBackground(colors = colors)

        withTransform({
            translate(panOffset.x, panOffset.y)
            scale(zoom, zoom, pivot = Offset.Zero)
        }) {
            floorFeatures.forEach { feature ->
                val points = scaler.projectPolygon(feature.geometry.outerRing, size)
                val path = points.toPath()
                val isSelected = feature.id == selectedRoomId
                val style = feature.properties.type.toRoomStyle(colors)

                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            style.fillStart.copy(alpha = if (isSelected) style.selectedFillAlpha else style.fillAlpha),
                            style.fillEnd.copy(alpha = if (isSelected) style.selectedFillAlpha * 0.82f else style.fillAlpha * 0.72f),
                        ),
                        start = points.bounds().topLeft,
                        end = points.bounds().bottomRight,
                    ),
                )
                if (isSelected) {
                    drawPath(
                        path = path,
                        color = style.glow.copy(alpha = 0.34f),
                        style = Stroke(width = 12f / zoom),
                    )
                }
                drawPath(
                    path = path,
                    color = if (isSelected) style.selectedStroke else style.stroke,
                    style = Stroke(width = if (isSelected) 4.2f / zoom else style.strokeWidth / zoom),
                )

                if (feature.properties.furnitureSim) {
                    drawFurnitureGrid(
                        bounds = points.bounds(),
                        color = style.detail.copy(alpha = if (isSelected) 0.78f else 0.48f),
                        zoom = zoom,
                    )
                }

                if (feature.properties.type != "exterior") {
                    drawFeatureLabel(
                        feature = feature,
                        points = points,
                        style = style,
                        isSelected = isSelected,
                        zoom = zoom,
                        textMeasurer = textMeasurer,
                    )
                }
            }
        }
    }
}

private data class MapRoomStyle(
    val fillStart: Color,
    val fillEnd: Color,
    val stroke: Color,
    val selectedStroke: Color,
    val detail: Color,
    val glow: Color,
    val label: Color,
    val fillAlpha: Float,
    val selectedFillAlpha: Float,
    val strokeWidth: Float = 2f,
)

private fun String.toRoomStyle(colors: androidx.compose.material3.ColorScheme): MapRoomStyle = when (lowercase()) {
    "meeting" -> blueprintStyle(
        fillStart = Color(0xFF23496A),
        fillEnd = Color(0xFF102B46),
        stroke = Color(0xFF66A9FF),
        detail = Color(0xFF9BC9FF),
    )
    "amenity" -> blueprintStyle(
        fillStart = Color(0xFF2B5A4A),
        fillEnd = Color(0xFF143D34),
        stroke = Color(0xFF62DFA2),
        detail = Color(0xFF9DF3C5),
    )
    "private" -> blueprintStyle(
        fillStart = Color(0xFF3A4055),
        fillEnd = Color(0xFF22283B),
        stroke = Color(0xFFA6B4D8),
        detail = Color(0xFFD6DCF0),
        fillAlpha = 0.32f,
    )
    "public" -> blueprintStyle(
        fillStart = Color(0xFF22485A),
        fillEnd = Color(0xFF0D3447),
        stroke = Color(0xFF8DE8FF),
        detail = Color(0xFFBFF5FF),
    )
    "service" -> blueprintStyle(
        fillStart = Color(0xFF3D4150),
        fillEnd = Color(0xFF222837),
        stroke = Color(0xFFB8C3D0),
        detail = Color(0xFFE0E7EE),
        fillAlpha = 0.22f,
    )
    "work" -> blueprintStyle(
        fillStart = Color(0xFF2A5570),
        fillEnd = Color(0xFF15354D),
        stroke = Color(0xFF7ED3FF),
        detail = Color(0xFFB5EAFF),
    )
    "social" -> blueprintStyle(
        fillStart = Color(0xFF315C49),
        fillEnd = Color(0xFF173B31),
        stroke = Color(0xFF78E3AA),
        detail = Color(0xFFB5F3D1),
    )
    "connect" -> blueprintStyle(
        fillStart = Color(0xFF5A5639),
        fillEnd = Color(0xFF35341F),
        stroke = Color(0xFFE3D171),
        detail = Color(0xFFFFED9A),
        fillAlpha = 0.20f,
    )
    "restricted" -> blueprintStyle(
        fillStart = Color(0xFF5A2D48),
        fillEnd = Color(0xFF3B1E34),
        stroke = Color(0xFFFF73B4),
        detail = Color(0xFFFFBAD7),
        fillAlpha = 0.24f,
    )
    "exterior" -> MapRoomStyle(
        fillStart = Color(0xFF26303A),
        fillEnd = Color(0xFF1B242C),
        stroke = Color(0xFF6F8292),
        selectedStroke = Color(0xFFB9CAD7),
        detail = Color(0xFF97A8B5),
        glow = Color(0xFF8AA8BC),
        label = Color(0xFFD8E7F2),
        fillAlpha = 0.74f,
        selectedFillAlpha = 0.86f,
        strokeWidth = 2.6f,
    )
    else -> blueprintStyle(
        fillStart = Color(0xFF334254),
        fillEnd = Color(0xFF1E2A36),
        stroke = Color(0xFF90A4B7),
        detail = Color(0xFFD4DEE7),
        fillAlpha = 0.20f,
    )
}

private fun blueprintStyle(
    fillStart: Color,
    fillEnd: Color,
    stroke: Color,
    detail: Color,
    fillAlpha: Float = 0.28f,
): MapRoomStyle {
    return MapRoomStyle(
        fillStart = fillStart,
        fillEnd = fillEnd,
        stroke = stroke.copy(alpha = 0.86f),
        selectedStroke = Color.White,
        detail = detail,
        glow = stroke,
        label = Color(0xFFE6F1F8),
        fillAlpha = fillAlpha,
        selectedFillAlpha = (fillAlpha + 0.22f).coerceAtMost(0.62f),
        strokeWidth = 2.1f,
    )
}

private fun List<Offset>.toPath(): Path = Path().apply {
    firstOrNull()?.let { first ->
        moveTo(first.x, first.y)
        drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
}

private fun List<Offset>.bounds(): Rect {
    val minX = minOf { it.x }
    val minY = minOf { it.y }
    val maxX = maxOf { it.x }
    val maxY = maxOf { it.y }
    return Rect(minX, minY, maxX, maxY)
}

private fun List<Offset>.center(): Offset {
    val bounds = bounds()
    return Offset(bounds.left + bounds.width / 2f, bounds.top + bounds.height / 2f)
}

private fun List<Feature>.sourceBounds(): Rect {
    val points = flatMap { it.geometry.outerRing }
    if (points.isEmpty()) return Rect(0f, 0f, 100f, 100f)

    return Rect(
        left = points.minOf { it.x }.toFloat(),
        top = points.minOf { it.y }.toFloat(),
        right = points.maxOf { it.x }.toFloat(),
        bottom = points.maxOf { it.y }.toFloat(),
    )
}

private fun DrawScope.drawFurnitureGrid(
    bounds: Rect,
    color: Color,
    zoom: Float,
) {
    val oneMeterBuffer = 10f
    val chairSize = 8f
    val spacing = chairSize * 2.2f
    val left = bounds.left + oneMeterBuffer
    val top = bounds.top + oneMeterBuffer
    val right = bounds.right - oneMeterBuffer
    val bottom = bounds.bottom - oneMeterBuffer
    var y = top

    while (y + chairSize <= bottom) {
        var x = left
        while (x + chairSize <= right) {
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(chairSize, chairSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f / zoom, 2.5f / zoom),
            )
            x += spacing
        }
        y += spacing
    }
}

private fun DrawScope.drawFeatureLabel(
    feature: Feature,
    points: List<Offset>,
    style: MapRoomStyle,
    isSelected: Boolean,
    zoom: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    val center = points.center()
    val bounds = points.bounds()
    val labelOnRight = center.x < size.width * 0.58f
    val elbowX = if (labelOnRight) bounds.right + 28f / zoom else bounds.left - 28f / zoom
    val labelX = if (labelOnRight) elbowX + 8f / zoom else elbowX - 112f / zoom
    val labelY = (center.y - 8f / zoom).coerceIn(12f / zoom, size.height - 34f / zoom)
    val label = feature.properties.name.ifBlank { feature.id }.uppercase()
    val typeLabel = feature.properties.type.replaceFirstChar { it.uppercase() }
    val labelLayout = textMeasurer.measure(
        text = label,
        style = TextStyle(
            color = style.label.copy(alpha = if (isSelected) 1f else 0.86f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
        ),
    )
    val typeLayout = textMeasurer.measure(
        text = "($typeLabel)",
        style = TextStyle(
            color = style.label.copy(alpha = if (isSelected) 0.78f else 0.60f),
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
        ),
    )

    drawCircle(
        color = style.stroke.copy(alpha = if (isSelected) 1f else 0.84f),
        radius = if (isSelected) 3.8f / zoom else 3f / zoom,
        center = center,
    )
    drawLine(
        color = style.stroke.copy(alpha = if (isSelected) 0.82f else 0.48f),
        start = center,
        end = Offset(elbowX, center.y),
        strokeWidth = if (isSelected) 1.3f / zoom else 0.9f / zoom,
    )
    drawLine(
        color = style.stroke.copy(alpha = if (isSelected) 0.82f else 0.48f),
        start = Offset(elbowX, center.y),
        end = Offset(elbowX, labelY + 8f / zoom),
        strokeWidth = if (isSelected) 1.3f / zoom else 0.9f / zoom,
    )
    drawText(
        textLayoutResult = labelLayout,
        topLeft = Offset(labelX, labelY),
    )
    drawText(
        textLayoutResult = typeLayout,
        topLeft = Offset(labelX, labelY + labelLayout.size.height),
    )
}

private fun DrawScope.drawMapBackground(colors: androidx.compose.material3.ColorScheme) {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF051521),
                Color(0xFF0A1D2D),
                Color(0xFF0D2738),
            ),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        ),
        size = size,
    )
    clipRect {
        val fineGrid = 18f
        val majorGrid = fineGrid * 4f
        var x = 0f
        while (x <= size.width) {
            val isMajor = (x % majorGrid) < 0.1f
            drawLine(
                color = Color(0xFF3A6C87).copy(alpha = if (isMajor) 0.22f else 0.09f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = if (isMajor) 1.2f else 0.7f,
            )
            x += fineGrid
        }
        var y = 0f
        while (y <= size.height) {
            val isMajor = (y % majorGrid) < 0.1f
            drawLine(
                color = Color(0xFF3A6C87).copy(alpha = if (isMajor) 0.22f else 0.09f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = if (isMajor) 1.2f else 0.7f,
            )
            y += fineGrid
        }
    }
}

private fun pointInPolygon(point: Offset, polygon: List<Offset>): Boolean {
    if (polygon.size < 3) return false
    var inside = false
    var previous = polygon.last()

    polygon.forEach { current ->
        val intersects = ((current.y > point.y) != (previous.y > point.y)) &&
            (point.x < (previous.x - current.x) * (point.y - current.y) / (previous.y - current.y) + current.x)
        if (intersects) inside = !inside
        previous = current
    }

    return inside
}
