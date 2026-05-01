package dev.orestegabo.kaze.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.graphics.Brush
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
import dev.orestegabo.kaze.domain.map.FloorLevel
import dev.orestegabo.kaze.presentation.map.GuestAccessContext
import dev.orestegabo.kaze.ui.components.InfoToken
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeRoundButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
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
    floors: List<FloorLevel>,
    guestAccess: GuestAccessContext,
    activeRoute: String,
    activeFloorId: String,
    onFloorSelected: (String) -> Unit,
    onStartNavigation: () -> Unit,
    onSwitchFloor: () -> Unit,
    bottomContentPadding: Dp = 20.dp,
) {
    val topChromeHeight = 112.dp
    val bottomChromeHeight = bottomContentPadding + 88.dp
    val selectedFloor = remember(floors, activeFloorId) {
        floors.firstOrNull { it.id == activeFloorId } ?: floors.firstOrNull()
    } ?: return

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        ZoomableHotelMap(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = topChromeHeight, bottom = bottomChromeHeight),
            floor = selectedFloor,
            guestAccess = guestAccess,
            topOverlap = topChromeHeight,
        )

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            FloorSelectorRail(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp),
                floors = floors,
                activeFloorId = activeFloorId,
                onFloorSelected = onFloorSelected,
            )
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                InfoToken(
                    label = activeRoute,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    leadingIcon = Icons.Default.Place,
                )
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KazePrimaryButton(
                    label = "Start navigation",
                    onClick = onStartNavigation,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Map,
                )
                KazeSecondaryButton(
                    label = "Switch floor",
                    onClick = {
                        onSwitchFloor()
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Layers,
                )
            }
            Spacer(Modifier.height(bottomContentPadding))
        }
    }
}

@Composable
private fun FloorSelectorRail(
    modifier: Modifier = Modifier,
    floors: List<FloorLevel>,
    activeFloorId: String,
    onFloorSelected: (String) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            floors.forEach { floor ->
                FloorSelectorChip(
                    label = floor.label,
                    selected = activeFloorId == floor.id,
                    onClick = { onFloorSelected(floor.id) },
                )
            }
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
    val itemShape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .clip(itemShape)
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        ),
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                        ),
                    )
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                },
            )
        }
        Box(
            modifier = Modifier
                .width(if (selected) 28.dp else 18.dp)
                .height(if (selected) 4.dp else 3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.74f)
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                    },
                ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
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
    guestAccess: GuestAccessContext,
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
                    floor = floor,
                    guestAccess = guestAccess,
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
    floor: FloorLevel,
    guestAccess: GuestAccessContext,
) {
    val isDarkMap = MaterialTheme.colorScheme.background.red < 0.5f
    val mapPainter = painterResource(temporaryVenueMapDrawable(floorId = floor.id, isDark = isDarkMap))
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
    // TODO [PENDING] Replace these temporary rasterized KotlinConf floor plans with
    // hotel-provided SVG-backed venue assets once Kaze receives its own branded architectural exports.
    floorId == "l9" && isDark -> Res.drawable.kotlinconf_first_floor_dark_raster
    floorId == "l9" -> Res.drawable.kotlinconf_first_floor_light_raster
    isDark -> Res.drawable.kotlinconf_ground_floor_dark_raster
    else -> Res.drawable.kotlinconf_ground_floor_light_raster
}
