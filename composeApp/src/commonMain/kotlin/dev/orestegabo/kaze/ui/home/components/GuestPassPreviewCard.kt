package dev.orestegabo.kaze.ui.home.components

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun GuestPassPreviewCard(
    onOpenInvitations: () -> Unit,
) {
    val warm = KazeTheme.accents.editorialWarm
    val colors = MaterialTheme.colorScheme

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(1.dp, warm.copy(alpha = 0.24f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.12f),
                            colors.secondary.copy(alpha = 0.08f),
                            colors.surface.copy(alpha = 0f),
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = warm.copy(alpha = 0.11f),
                    radius = size.minDimension * 0.52f,
                    center = Offset(size.width * 0.94f, size.height * 0.06f),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    color = colors.surface.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, warm.copy(alpha = 0.22f)),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = warm.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, warm.copy(alpha = 0.28f)),
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = warm,
                                modifier = Modifier.padding(14.dp).size(34.dp),
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                "Kaze Pass",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                            )
                            Text(
                                "Preview only",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = warm,
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = colors.surfaceVariant.copy(alpha = 0.42f),
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.14f)),
                ) {
                    FlowRow(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MetaPill("Access", leadingIcon = Icons.Default.Lock)
                        MetaPill("Schedule", leadingIcon = Icons.Default.Schedule)
                        MetaPill("Code", leadingIcon = Icons.Default.VpnKey)
                    }
                }

                KazeSecondaryButton(
                    label = "View invites",
                    onClick = onOpenInvitations,
                    leadingIcon = Icons.Default.VpnKey,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
