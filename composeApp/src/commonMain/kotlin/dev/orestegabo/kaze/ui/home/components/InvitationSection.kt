package dev.orestegabo.kaze.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationState
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun InvitationSection(
    invitations: List<InvitationPreview>,
    onOpenInvitation: (InvitationPreview) -> Unit,
    onSeeAll: () -> Unit,
) {
    val previewInvitations = invitations.filter { it.state == InvitationState.ACTIVE }.ifEmpty { invitations }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(
                text = "Invitations",
                modifier = Modifier.weight(1f),
            )
            if (invitations.isNotEmpty()) {
                KazeSecondaryButton(
                    label = "See all",
                    onClick = onSeeAll,
                )
            }
        }
        previewInvitations.take(2).forEach { invitation ->
            InvitationCard(
                invitation = invitation,
                onClick = { onOpenInvitation(invitation) },
            )
        }
    }
}

@Composable
internal fun InvitationCard(
    invitation: InvitationPreview,
    onClick: () -> Unit,
) {
    val isActive = invitation.state == InvitationState.ACTIVE
    val statusContainerColor = when (invitation.state) {
        InvitationState.ACTIVE -> MaterialTheme.colorScheme.secondaryContainer
        InvitationState.PAST -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        InvitationState.ARCHIVED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
    }
    val statusTextColor = when (invitation.state) {
        InvitationState.ACTIVE -> MaterialTheme.colorScheme.onSecondaryContainer
        InvitationState.PAST,
        InvitationState.ARCHIVED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusIcon = when (invitation.state) {
        InvitationState.ACTIVE -> Icons.Default.VpnKey
        InvitationState.PAST -> Icons.Default.History
        InvitationState.ARCHIVED -> Icons.Default.Archive
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
            },
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        invitation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        invitation.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                    )
                }
                MetaPill(
                    invitation.statusLabel,
                    leadingIcon = statusIcon,
                    containerColor = statusContainerColor,
                    textColor = statusTextColor,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill(invitation.code, leadingIcon = Icons.Default.Edit)
                MetaPill(
                    if (isActive) "Live event info" else "Saved history",
                    leadingIcon = if (isActive) Icons.Default.Groups else Icons.Default.CheckCircle,
                )
            }
            KazeSecondaryButton(
                label = if (isActive) "Open" else "View details",
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.CalendarMonth,
            )
        }
    }
}
