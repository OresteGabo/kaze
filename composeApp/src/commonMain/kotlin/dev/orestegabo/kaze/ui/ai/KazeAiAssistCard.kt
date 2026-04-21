package dev.orestegabo.kaze.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton

@Composable
internal fun KazeAiAssistCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    supportingLabel: String = "Runs locally on this device",
    icon: ImageVector = Icons.Filled.AutoAwesome,
) {
    val colors = MaterialTheme.colorScheme
    val warm = KazeTheme.accents.editorialWarm
    val accent = colors.secondary
    val subtleAccent = colors.primary

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.16f)),
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(
                        colors.secondaryContainer.copy(alpha = 0.24f),
                        colors.surface.copy(alpha = 0f),
                    ),
                ),
            ),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = subtleAccent.copy(alpha = 0.06f),
                    radius = size.minDimension * 0.52f,
                    center = Offset(size.width * 0.96f, size.height * 0.08f),
                )
                drawCircle(
                    color = warm.copy(alpha = 0.07f),
                    radius = size.minDimension * 0.42f,
                    center = Offset(size.width * 0.06f, size.height * 0.94f),
                )
            }
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.secondaryContainer.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, colors.secondary.copy(alpha = 0.18f)),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.onSecondaryContainer,
                            modifier = Modifier.padding(10.dp).size(24.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface,
                        )
                        Text(
                            supportingLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KazeAiChip("On device", colors.secondaryContainer, colors.onSecondaryContainer)
                    KazeAiChip("Offline", colors.surfaceVariant, colors.onSurfaceVariant)
                    KazeAiChip("Private", colors.primaryContainer, colors.onPrimaryContainer)
                }
                KazeSecondaryButton(
                    label = actionLabel,
                    onClick = onAction,
                    leadingIcon = Icons.Filled.CloudOff,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun KazeAiChip(
    label: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor.copy(alpha = 0.70f),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.10f)),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
    }
}
