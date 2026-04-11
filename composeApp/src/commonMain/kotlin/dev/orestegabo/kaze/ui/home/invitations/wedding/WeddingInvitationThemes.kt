package dev.orestegabo.kaze.ui.home.invitations.wedding

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.home.invitations.InvitationTheme
import dev.orestegabo.kaze.ui.home.invitations.InvitationThemeCategory
import dev.orestegabo.kaze.ui.home.invitations.WeddingCouplePlaceholder

internal object WeddingRomanceTheme : InvitationTheme {
    override val id = "wedding-romance"
    override val name = "Romance"
    override val category = InvitationThemeCategory.WEDDING
    override val label = "Wedding invite"
    override val detailsTitle = "Wedding invitation"

    override fun supportingText(isActive: Boolean) = if (isActive) {
        "Tap through to confirm access and view the celebration details."
    } else {
        "Saved wedding invitation details."
    }

    @Composable
    override fun PageBackground(modifier: Modifier) {
        WeddingPageBackground(modifier = modifier, botanical = false)
    }

    @Composable
    override fun Cover(invitation: InvitationPreview, isActive: Boolean, modifier: Modifier) {
        WeddingCoverCard(invitation = invitation, isActive = isActive, botanical = false, modifier = modifier)
    }
}

internal object WeddingBotanicalTheme : InvitationTheme {
    override val id = "wedding-botanical"
    override val name = "Botanical"
    override val category = InvitationThemeCategory.WEDDING
    override val label = "Garden invite"
    override val detailsTitle = "Wedding invitation"

    override fun supportingText(isActive: Boolean) = if (isActive) {
        "A softer garden-style invitation for a personal celebration."
    } else {
        "Saved garden invitation details."
    }

    @Composable
    override fun PageBackground(modifier: Modifier) {
        WeddingPageBackground(modifier = modifier, botanical = true)
    }

    @Composable
    override fun Cover(invitation: InvitationPreview, isActive: Boolean, modifier: Modifier) {
        WeddingCoverCard(invitation = invitation, isActive = isActive, botanical = true, modifier = modifier)
    }
}

@Composable
private fun WeddingPageBackground(
    botanical: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(
                    if (botanical) colors.primaryContainer.copy(alpha = 0.62f) else colors.tertiaryContainer.copy(alpha = 0.78f),
                    colors.secondaryContainer.copy(alpha = if (botanical) 0.32f else 0.44f),
                    colors.background,
                ),
            ),
        ),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = (if (botanical) colors.primary else colors.tertiary).copy(alpha = 0.12f),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.86f, size.height * 0.08f),
            )
            drawCircle(
                color = colors.secondary.copy(alpha = 0.09f),
                radius = size.minDimension * 0.32f,
                center = Offset(size.width * 0.08f, size.height * 0.72f),
            )
        }
        repeat(3) { index ->
            Icon(
                imageVector = if (botanical) Icons.Default.LocalFlorist else Icons.Default.Favorite,
                contentDescription = null,
                tint = (if (botanical) colors.primary else colors.tertiary).copy(alpha = 0.10f + index * 0.03f),
                modifier = Modifier
                    .align(listOf(Alignment.TopEnd, Alignment.TopStart, Alignment.CenterEnd)[index])
                    .offset(x = listOf((-32).dp, 38.dp, (-18).dp)[index], y = listOf(28.dp, 86.dp, 24.dp)[index])
                    .size(listOf(46.dp, 28.dp, 34.dp)[index]),
            )
        }
    }
}

@Composable
private fun WeddingCoverCard(
    invitation: InvitationPreview,
    isActive: Boolean,
    botanical: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val primaryInk = if (botanical) colors.onPrimaryContainer else colors.onTertiaryContainer
    val coverBrush = Brush.linearGradient(
        listOf(
            if (botanical) colors.primaryContainer.copy(alpha = 0.94f) else colors.tertiaryContainer.copy(alpha = 0.96f),
            colors.secondaryContainer.copy(alpha = 0.9f),
            colors.surface.copy(alpha = 0.98f),
        ),
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.16f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(coverBrush)
                .padding(22.dp),
        ) {
            WeddingCoverDecoration(botanical = botanical, modifier = Modifier.matchParentSize())
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    MetaPill(
                        label = if (botanical) "Garden invite" else "Wedding invite",
                        leadingIcon = if (botanical) Icons.Default.LocalFlorist else Icons.Default.Groups,
                        containerColor = colors.surface.copy(alpha = 0.56f),
                        textColor = primaryInk,
                    )
                    if (invitation.code.isNotBlank()) {
                        MetaPill(
                            label = invitation.code,
                            leadingIcon = Icons.Default.VpnKey,
                            containerColor = colors.surface.copy(alpha = 0.46f),
                            textColor = primaryInk,
                        )
                    }
                }
                WeddingCouplePlaceholder(invitation = invitation, modifier = Modifier.align(Alignment.CenterHorizontally))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(invitation.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = primaryInk)
                    Text(
                        if (botanical) WeddingBotanicalTheme.supportingText(isActive) else WeddingRomanceTheme.supportingText(isActive),
                        style = MaterialTheme.typography.bodyMedium,
                        color = primaryInk.copy(alpha = 0.72f),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeddingCoverDecoration(
    botanical: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        drawCircle(
            color = (if (botanical) colors.primary else colors.tertiary).copy(alpha = 0.14f),
            radius = size.minDimension * 0.34f,
            center = Offset(size.width * 0.9f, size.height * 0.14f),
        )
        drawCircle(
            color = colors.primary.copy(alpha = 0.10f),
            radius = size.minDimension * 0.26f,
            center = Offset(size.width * 0.08f, size.height * 0.82f),
        )
        drawCircle(color = colors.surface.copy(alpha = 0.34f), radius = size.minDimension * 0.18f, center = Offset(size.width * 0.46f, size.height * 0.46f), style = Stroke(width = 5f))
        drawCircle(color = colors.surface.copy(alpha = 0.26f), radius = size.minDimension * 0.18f, center = Offset(size.width * 0.56f, size.height * 0.46f), style = Stroke(width = 5f))
    }
}
