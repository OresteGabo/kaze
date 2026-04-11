package dev.orestegabo.kaze.ui.home.invitations.birthday

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.home.invitations.EventMarkPlaceholder
import dev.orestegabo.kaze.ui.home.invitations.InvitationTheme
import dev.orestegabo.kaze.ui.home.invitations.InvitationThemeCategory

internal object BirthdayJoyTheme : InvitationTheme {
    override val id = "birthday-joy"
    override val name = "Joy"
    override val category = InvitationThemeCategory.BIRTHDAY
    override val label = "Birthday invite"
    override val detailsTitle = "Birthday invitation"

    override fun supportingText(isActive: Boolean) = if (isActive) {
        "A playful invitation for a celebration with friends and family."
    } else {
        "Saved birthday invitation details."
    }

    @Composable
    override fun PageBackground(modifier: Modifier) {
        val colors = MaterialTheme.colorScheme
        Box(
            modifier = modifier.background(
                Brush.verticalGradient(
                    listOf(
                        colors.tertiaryContainer.copy(alpha = 0.72f),
                        colors.primaryContainer.copy(alpha = 0.34f),
                        colors.background,
                    ),
                ),
            ),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(color = colors.tertiary.copy(alpha = 0.14f), radius = size.minDimension * 0.34f, center = Offset(size.width * 0.88f, size.height * 0.10f))
                drawCircle(color = colors.primary.copy(alpha = 0.10f), radius = size.minDimension * 0.28f, center = Offset(size.width * 0.10f, size.height * 0.74f))
                repeat(8) { index ->
                    val x = size.width * (0.12f + index * 0.10f)
                    val y = size.height * (0.18f + (index % 3) * 0.05f)
                    drawCircle(color = colors.secondary.copy(alpha = 0.12f), radius = 8f + index, center = Offset(x, y))
                }
            }
        }
    }

    @Composable
    override fun Cover(invitation: InvitationPreview, isActive: Boolean, modifier: Modifier) {
        val colors = MaterialTheme.colorScheme
        val primaryInk = colors.onTertiaryContainer
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(34.dp),
            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.16f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(238.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                colors.tertiaryContainer.copy(alpha = 0.96f),
                                colors.primaryContainer.copy(alpha = 0.82f),
                                colors.surface.copy(alpha = 0.98f),
                            ),
                        ),
                    )
                    .padding(22.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        MetaPill(label = label, leadingIcon = Icons.Default.Cake, containerColor = colors.surface.copy(alpha = 0.56f), textColor = primaryInk)
                        if (invitation.code.isNotBlank()) {
                            MetaPill(label = invitation.code, leadingIcon = Icons.Default.VpnKey, containerColor = colors.surface.copy(alpha = 0.46f), textColor = primaryInk)
                        }
                    }
                    EventMarkPlaceholder(invitation = invitation, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(invitation.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = primaryInk)
                        Text(supportingText(isActive), style = MaterialTheme.typography.bodyMedium, color = primaryInk.copy(alpha = 0.72f))
                    }
                }
            }
        }
    }
}
