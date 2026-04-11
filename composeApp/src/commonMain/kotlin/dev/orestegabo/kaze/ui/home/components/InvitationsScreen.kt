package dev.orestegabo.kaze.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.presentation.demo.InvitationState
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun InvitationsScreen(
    invitations: List<InvitationPreview>,
    onBack: () -> Unit,
    onOpenInvitation: (InvitationPreview) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 20.dp,
) {
    val scrollState = rememberScrollState()
    val activeInvitations = invitations.filter { it.state == InvitationState.ACTIVE }
    val pastInvitations = invitations.filter { it.state != InvitationState.ACTIVE }

    // TODO replace demo invitations with database records and websocket updates when realtime invites are wired.
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KazeGhostButton(
                label = "Back",
                onClick = onBack,
                leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
            )
            MetaPill(
                label = "${invitations.size} invitations",
                leadingIcon = Icons.Default.MarkEmailUnread,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "All invitations",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Open pending invitations, or review older invitations when you need the details again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }

        if (invitations.isEmpty()) {
            EmptyInvitationsCard()
        } else {
            SectionLabel("Active")
            activeInvitations.forEach { invitation ->
                InvitationCard(
                    invitation = invitation,
                    onClick = { onOpenInvitation(invitation) },
                )
            }
            if (activeInvitations.isEmpty()) {
                EmptyInvitationsCard("No active invitations right now.")
            }
            if (pastInvitations.isNotEmpty()) {
                SectionLabel("Past and archived")
                pastInvitations.forEach { invitation ->
                    InvitationCard(
                        invitation = invitation,
                        onClick = { onOpenInvitation(invitation) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyInvitationsCard(
    message: String = "No invitations yet.",
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
            )
        }
    }
}
