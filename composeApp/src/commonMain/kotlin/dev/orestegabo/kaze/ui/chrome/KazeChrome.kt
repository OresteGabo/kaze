package dev.orestegabo.kaze.ui.chrome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.ui.components.KazeGhostButton

@Composable
internal fun KazeAmbientBackground(modifier: Modifier = Modifier) {
    val baseTop = MaterialTheme.colorScheme.background
    val baseBottom = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
    val softLineColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)
    val circlePrimary = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val circleTertiary = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.07f)
    val topPanelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.035f)
    val bottomPanelColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.028f)

    Canvas(modifier = modifier.background(Brush.verticalGradient(listOf(baseTop, baseTop, baseBottom)))) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = topPanelColor,
            topLeft = Offset(w * 0.04f, h * 0.07f),
            size = androidx.compose.ui.geometry.Size(w * 0.92f, h * 0.24f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(42f, 42f),
        )
        drawRoundRect(
            color = bottomPanelColor,
            topLeft = Offset(w * 0.05f, h * 0.66f),
            size = androidx.compose.ui.geometry.Size(w * 0.90f, h * 0.20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(42f, 42f),
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
        drawCircle(color = circleTertiary, radius = w * 0.22f, center = Offset(w * 0.12f, h * 0.82f), style = Stroke(width = 5f))

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
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 14.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 8.dp,
        shadowElevation = 14.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(2.dp).background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                    ),
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KazeDestination.entries.forEach { destination ->
                    val selected = currentDestination == destination
                    val itemShape = RoundedCornerShape(topStart = 22.dp, topEnd = 12.dp, bottomEnd = 22.dp, bottomStart = 12.dp)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(itemShape)
                            .background(
                                if (selected) {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                        ),
                                    )
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f),
                                            Color.Transparent,
                                        ),
                                    )
                                },
                            )
                            .border(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.26f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                itemShape,
                            )
                            .clickable { onDestinationSelected(destination) }
                            .padding(horizontal = 6.dp, vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier.width(28.dp).height(4.dp).clip(RoundedCornerShape(999.dp)).background(
                                if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                            ),
                        )
                        Box(
                            modifier = Modifier.size(38.dp).clip(CircleShape).background(
                                if (selected) {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.78f),
                                        ),
                                    )
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f),
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                        ),
                                    )
                                },
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f),
                            )
                        }
                        Text(
                            destination.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DemoFeedbackBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    if (message.isBlank()) return
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.size(12.dp))
            KazeGhostButton(label = "Dismiss", onClick = onDismiss)
        }
    }
}
