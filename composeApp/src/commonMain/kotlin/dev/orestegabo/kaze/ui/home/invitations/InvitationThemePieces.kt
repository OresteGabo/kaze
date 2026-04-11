package dev.orestegabo.kaze.ui.home.invitations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview

@Composable
internal fun WeddingCouplePlaceholder(
    invitation: InvitationPreview,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val initials = invitation.weddingInitials()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-12).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CouplePortraitCircle(initial = initials.first, color = colors.tertiaryContainer, ink = colors.onTertiaryContainer)
        CouplePortraitCircle(initial = initials.second, color = colors.secondaryContainer, ink = colors.onSecondaryContainer)
    }
}

@Composable
private fun CouplePortraitCircle(
    initial: String,
    color: Color,
    ink: Color,
) {
    Surface(
        modifier = Modifier.size(86.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.94f),
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)),
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                initial,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = ink,
            )
        }
    }
}

@Composable
internal fun EventMarkPlaceholder(
    invitation: InvitationPreview,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(104.dp)
            .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 18.dp, bottomEnd = 34.dp, bottomStart = 18.dp))
            .background(colors.surface.copy(alpha = 0.46f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            invitation.title.initials(3),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSecondaryContainer,
        )
    }
}
