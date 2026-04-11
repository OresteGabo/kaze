package dev.orestegabo.kaze.ui.chrome

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.theme.KazeTheme
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.k_mark_raster
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun KazeAmbientBackground(modifier: Modifier = Modifier) {
    val uiPalette = KazeTheme.ui
    val baseTop = MaterialTheme.colorScheme.background
    val baseBottom = uiPalette.ambientBottom
    val lineColor = uiPalette.ambientLineStrong
    val softLineColor = uiPalette.ambientLineSoft
    val circlePrimary = uiPalette.ambientCirclePrimary
    val circleSecondary = uiPalette.ambientCircleSecondary
    val topPanelColor = uiPalette.ambientPanelTop
    val bottomPanelColor = uiPalette.ambientPanelBottom

    Canvas(modifier = modifier.background(Brush.verticalGradient(listOf(baseTop, baseTop, baseBottom)))) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = topPanelColor,
            topLeft = Offset(w * 0.04f, h * 0.07f),
            size = Size(w * 0.92f, h * 0.24f),
            cornerRadius = CornerRadius(42f, 42f),
        )
        drawRoundRect(
            color = bottomPanelColor,
            topLeft = Offset(w * 0.05f, h * 0.66f),
            size = Size(w * 0.90f, h * 0.20f),
            cornerRadius = CornerRadius(42f, 42f),
        )

        drawLine(color = lineColor, start = Offset(w * 0.08f, h * 0.10f), end = Offset(w * 0.72f, h * 0.10f), strokeWidth = 5f, cap = StrokeCap.Round)
        drawLine(color = lineColor, start = Offset(w * 0.12f, h * 0.135f), end = Offset(w * 0.88f, h * 0.135f), strokeWidth = 2.5f, cap = StrokeCap.Round)
        drawLine(color = softLineColor, start = Offset(w * 0.18f, h * 0.74f), end = Offset(w * 0.84f, h * 0.74f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color = lineColor, start = Offset(w * 0.14f, h * 0.79f), end = Offset(w * 0.62f, h * 0.79f), strokeWidth = 5f, cap = StrokeCap.Round)

        val topPath = Path().apply {
            moveTo(w * 0.62f, h * 0.06f)
            lineTo(w * 0.76f, h * 0.06f)
            lineTo(w * 0.82f, h * 0.11f)
            lineTo(w * 0.93f, h * 0.11f)
            lineTo(w * 0.93f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.18f)
            lineTo(w * 0.75f, h * 0.24f)
            lineTo(w * 0.58f, h * 0.24f)
        }
        drawPath(path = topPath, color = lineColor, style = Stroke(width = 3f))

        val bottomPath = Path().apply {
            moveTo(w * 0.10f, h * 0.88f)
            lineTo(w * 0.26f, h * 0.88f)
            lineTo(w * 0.33f, h * 0.83f)
            lineTo(w * 0.48f, h * 0.83f)
            lineTo(w * 0.48f, h * 0.90f)
            lineTo(w * 0.34f, h * 0.90f)
            lineTo(w * 0.27f, h * 0.95f)
            lineTo(w * 0.12f, h * 0.95f)
        }
        drawPath(path = bottomPath, color = softLineColor, style = Stroke(width = 3f))

        drawCircle(color = circlePrimary, radius = w * 0.18f, center = Offset(w * 0.88f, h * 0.22f), style = Stroke(width = 5f))
        drawCircle(color = circleSecondary, radius = w * 0.22f, center = Offset(w * 0.12f, h * 0.82f), style = Stroke(width = 5f))

        repeat(7) { index ->
            val y = h * (0.185f + index * 0.018f)
            drawLine(
                color = if (index % 2 == 0) lineColor else softLineColor,
                start = Offset(w * 0.11f, y),
                end = Offset(w * (0.36f + index * 0.06f), y),
                strokeWidth = if (index % 2 == 0) 3f else 2f,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
internal fun KazeBottomBar(
    modifier: Modifier = Modifier,
    currentDestination: KazeDestination,
    onDestinationSelected: (KazeDestination) -> Unit,
    pendingInvitationCount: Int = 0,
) {
    val destinations = listOf(
        KazeDestination.STAY,
        KazeDestination.EVENTS,
        KazeDestination.HOME,
        KazeDestination.INVITATIONS,
        KazeDestination.EXPLORE,
    )
    KazeNavigationContainer(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEach { destination ->
                KazeBottomNavItem(
                    destination = destination,
                    selected = currentDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    badgeCount = if (destination == KazeDestination.INVITATIONS) pendingInvitationCount else 0,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun KazeNavigationContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val uiPalette = KazeTheme.ui
    Surface(
        modifier = modifier,
        color = uiPalette.floatingShell,
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, uiPalette.floatingShellBorder),
    ) {
        content()
    }
}

@Composable
private fun KazeNavigationItemFrame(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Color) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        targetValue = if (selected) colors.secondaryContainer else Color.Transparent,
        label = "kazeNavContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
        label = "kazeNavContent",
    )
    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = if (selected) 2.dp else 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier,
    ) {
        content(contentColor)
    }
}

@Composable
private fun KazeBottomNavItem(
    destination: KazeDestination,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int,
    modifier: Modifier = Modifier,
) {
    KazeNavigationItemFrame(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    ) { contentColor ->
        val isHome = destination == KazeDestination.HOME
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isHome) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(52.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.k_mark_raster),
                        contentDescription = destination.label,
                        modifier = Modifier
                            .requiredSize(60.dp)
                            .offset(y = (-4).dp),
                        colorFilter = ColorFilter.tint(contentColor),
                    )
                }
            } else {
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f) else Color.Transparent,
                            )
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    NavigationBadge(
                        count = badgeCount,
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp),
                    )
                }
            }
            Text(
                text = destination.label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
            )
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(width = 16.dp, height = 3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent),
            )
        }
    }
}

@Composable
internal fun KazeNavigationRail(
    modifier: Modifier = Modifier,
    currentDestination: KazeDestination,
    onDestinationSelected: (KazeDestination) -> Unit,
    pendingInvitationCount: Int = 0,
) {
    KazeNavigationContainer(
        modifier = modifier
            .fillMaxHeight()
            .width(108.dp)
            .padding(start = 12.dp, top = 18.dp, bottom = 18.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            kazePrimaryDestinations.forEach { destination ->
                KazeSideNavItem(
                    destination = destination,
                    selected = currentDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    badgeCount = if (destination == KazeDestination.INVITATIONS) pendingInvitationCount else 0,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private val kazePrimaryDestinations = listOf(
    KazeDestination.STAY,
    KazeDestination.EVENTS,
    KazeDestination.HOME,
    KazeDestination.INVITATIONS,
    KazeDestination.EXPLORE,
)

@Composable
private fun KazeSideNavItem(
    destination: KazeDestination,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int,
) {
    KazeNavigationItemFrame(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) { contentColor ->
        val isHome = destination == KazeDestination.HOME
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f) else Color.Transparent,
                        )
                        .padding(horizontal = if (isHome) 10.dp else 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isHome) {
                        Image(
                            painter = painterResource(Res.drawable.k_mark_raster),
                            contentDescription = destination.label,
                            modifier = Modifier.size(26.dp),
                            colorFilter = ColorFilter.tint(contentColor),
                        )
                    } else {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                NavigationBadge(
                    count = badgeCount,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp),
                )
            }
            Text(
                text = destination.label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 2,
                modifier = Modifier.padding(top = 6.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(width = 18.dp, height = 3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent),
            )
        }
    }
}

@Composable
private fun NavigationBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.36f)),
    ) {
        Text(
            text = count.coerceAtMost(9).toString(),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
internal fun DemoFeedbackBanner(
    message: String,
) {
    if (message.isBlank()) return
    val uiPalette = KazeTheme.ui
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = uiPalette.successContainerSoft,
            ) {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = uiPalette.successContent,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
