package dev.orestegabo.kaze.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.KazePrimaryButton
import dev.orestegabo.kaze.KazeRoundButton
import dev.orestegabo.kaze.KazeSecondaryButton
import dev.orestegabo.kaze.demo.GuestAccessContext
import dev.orestegabo.kaze.demo.sampleGuestAccess
import dev.orestegabo.kaze.domain.map.FloorLevel
import dev.orestegabo.kaze.domain.map.sampleMarriottConventionMap
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.kotlinconf_first_floor_dark_raster
import kaze.composeapp.generated.resources.kotlinconf_first_floor_light_raster
import kaze.composeapp.generated.resources.kotlinconf_ground_floor_dark_raster
import kaze.composeapp.generated.resources.kotlinconf_ground_floor_light_raster
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MapScreen(
    modifier: Modifier = Modifier,
    activeRoute: String,
    activeFloorId: String,
    onStartNavigation: () -> Unit,
    onSwitchFloor: () -> Unit,
) {
    val topChromeHeight = 112.dp
    val bottomChromeHeight = 108.dp
    var selectedFloorId by remember(activeFloorId) { mutableStateOf(activeFloorId) }
    val selectedFloor = remember(selectedFloorId) {
        sampleMarriottConventionMap.floor(selectedFloorId) ?: sampleMarriottConventionMap.floor("l1")!!
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        ZoomableHotelMap(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = topChromeHeight, bottom = bottomChromeHeight),
            floor = selectedFloor,
            topOverlap = topChromeHeight,
        )

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FloorSelectorChip(
                    modifier = Modifier.weight(1f),
                    label = "Ground floor",
                    selected = selectedFloorId == "l1",
                    onClick = { selectedFloorId = "l1" },
                )
                FloorSelectorChip(
                    modifier = Modifier.weight(1f),
                    label = "First floor",
                    selected = selectedFloorId == "l9",
                    onClick = { selectedFloorId = "l9" },
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                activeRoute,
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KazePrimaryButton(
                    label = "Start navigation",
                    onClick = onStartNavigation,
                    modifier = Modifier.weight(1f),
                )
                KazeSecondaryButton(
                    label = "Switch floor",
                    onClick = {
                        onSwitchFloor()
                        selectedFloorId = if (selectedFloorId == "l1") "l9" else "l1"
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FloorSelectorChip(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 9.dp else 7.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                    } else {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    },
                ),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ZoomableHotelMap(
    modifier: Modifier = Modifier,
    floor: FloorLevel,
    topOverlap: Dp = 0.dp,
) {
    var scale by remember(floor.id) { mutableStateOf(1f) }
    var offset by remember(floor.id) { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
    ) {
        val mapWidth = maxWidth * 1.55f
        val mapHeight = maxHeight * 1.25f
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }
        val mapWidthPx = with(density) { mapWidth.toPx() }
        val mapHeightPx = with(density) { mapHeight.toPx() }
        val topOverlapPx = density.run { topOverlap.toPx() }

        fun clampOffset(proposedOffset: Offset, targetScale: Float): Offset {
            if (targetScale <= 1.01f) return Offset.Zero

            val maxTranslationX = ((mapWidthPx * targetScale) - mapWidthPx) / 2f
            val maxTranslationUp = ((mapHeightPx * targetScale) - mapHeightPx) / 2f + topOverlapPx
            val maxTranslationDown = ((mapHeightPx * targetScale) - mapHeightPx) / 2f

            return Offset(
                x = proposedOffset.x.coerceIn(-maxTranslationX.coerceAtLeast(0f), maxTranslationX.coerceAtLeast(0f)),
                y = proposedOffset.y.coerceIn(-maxTranslationUp.coerceAtLeast(0f), maxTranslationDown.coerceAtLeast(0f)),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(floor.id, containerWidthPx, containerHeightPx, mapWidthPx, mapHeightPx) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 4f)
                        scale = newScale
                        offset = clampOffset(offset + pan, newScale)
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .size(mapWidth, mapHeight),
            ) {
                MapPreview(
                    modifier = Modifier.fillMaxSize(),
                    floorId = floor.id,
                    guestAccess = sampleGuestAccess,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    tonalElevation = 4.dp,
                    shadowElevation = 6.dp,
                ) {
                    KazeRoundButton(
                        label = "+",
                        onClick = {
                            val newScale = (scale + 0.25f).coerceAtMost(4f)
                            scale = newScale
                            offset = clampOffset(offset, newScale)
                        },
                    )
                }
                Surface(
                    shape = CircleShape,
                    tonalElevation = 4.dp,
                    shadowElevation = 6.dp,
                ) {
                    KazeRoundButton(
                        label = "-",
                        onClick = {
                            val newScale = (scale - 0.25f).coerceAtLeast(1f)
                            scale = newScale
                            offset = clampOffset(offset, newScale)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MapPreview(
    modifier: Modifier = Modifier,
    floorId: String = "l1",
    guestAccess: GuestAccessContext = sampleGuestAccess,
) {
    val floor = remember(floorId) { sampleMarriottConventionMap.floor(floorId)!! }
    val isDarkMap = MaterialTheme.colorScheme.background.red < 0.5f
    val mapPainter = painterResource(temporaryVenueMapDrawable(floorId = floorId, isDark = isDarkMap))
    val outlineColor = MaterialTheme.colorScheme.outline
    val nodeRingColor = MaterialTheme.colorScheme.primary
    val nodeFillColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .border(1.dp, outlineColor, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
    ) {
        Image(
            painter = mapPainter,
            contentDescription = "Temporary venue floor plan",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / floor.canvasSize.width
            val scaleY = size.height / floor.canvasSize.height

            floor.nodes.forEach { node ->
                val center = Offset(node.position.x * scaleX, node.position.y * scaleY)
                drawCircle(
                    color = nodeRingColor,
                    radius = 10f,
                    center = center,
                    style = Stroke(width = 4f),
                )
                drawCircle(
                    color = nodeFillColor,
                    radius = 5.5f,
                    center = center,
                )
            }
        }
    }
}

private fun temporaryVenueMapDrawable(
    floorId: String,
    isDark: Boolean,
): DrawableResource = when {
    // TODO Replace these temporary rasterized KotlinConf floor plans with hotel-provided
    // SVG-backed venue assets once Kaze receives its own branded architectural exports.
    floorId == "l9" && isDark -> Res.drawable.kotlinconf_first_floor_dark_raster
    floorId == "l9" -> Res.drawable.kotlinconf_first_floor_light_raster
    isDark -> Res.drawable.kotlinconf_ground_floor_dark_raster
    else -> Res.drawable.kotlinconf_ground_floor_light_raster
}
