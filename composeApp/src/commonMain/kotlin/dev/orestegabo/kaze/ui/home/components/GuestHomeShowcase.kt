package dev.orestegabo.kaze.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QrCode2
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun GuestHomeShowcase(
    invitations: List<InvitationPreview>,
    code: String,
    onCodeChange: (String) -> Unit,
    onSubmitCode: () -> Unit,
    onOpenInvitations: () -> Unit,
) {
    BoxWithConstraints {
        val isExpanded = maxWidth >= 860.dp

        if (isExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                GuestHomeHeroCard(
                    invitations = invitations,
                    onOpenInvitations = onOpenInvitations,
                    modifier = Modifier.weight(1.18f),
                )
                GuestHomeEntryCard(
                    code = code,
                    onCodeChange = onCodeChange,
                    onSubmitCode = onSubmitCode,
                    modifier = Modifier.weight(0.82f),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GuestHomeHeroCard(
                    invitations = invitations,
                    onOpenInvitations = onOpenInvitations,
                )
                GuestHomeEntryCard(
                    code = code,
                    onCodeChange = onCodeChange,
                    onSubmitCode = onSubmitCode,
                )
            }
        }
    }
}

@Composable
private fun GuestHomeHeroCard(
    invitations: List<InvitationPreview>,
    onOpenInvitations: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val warm = KazeTheme.accents.editorialWarm
    val botanical = KazeTheme.accents.editorialBotanical
    val featuredInvitations = invitations.take(2)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.14f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            warm.copy(alpha = 0.20f),
                            botanical.copy(alpha = 0.12f),
                            colors.primary.copy(alpha = 0.08f),
                            colors.surface.copy(alpha = 0.00f),
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = warm.copy(alpha = 0.12f),
                    radius = size.minDimension * 0.42f,
                    center = Offset(size.width * 0.92f, size.height * 0.06f),
                )
                drawCircle(
                    color = botanical.copy(alpha = 0.10f),
                    radius = size.minDimension * 0.30f,
                    center = Offset(size.width * 0.10f, size.height * 0.84f),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = colors.surface.copy(alpha = 0.72f),
                            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f)),
                        ) {
                            Text(
                                "Event entry made calm",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.primary,
                            )
                        }
                        Text(
                            "From invite to pass in one beautiful flow.",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface,
                        )
                        Text(
                            "Kaze helps guests open invitations, confirm attendance, find the venue, and carry one pass for the event journey.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface.copy(alpha = 0.72f),
                        )
                    }
                    Surface(
                        modifier = Modifier.padding(start = 12.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = colors.surface.copy(alpha = 0.68f),
                        border = BorderStroke(1.dp, warm.copy(alpha = 0.22f)),
                    ) {
                        Box(
                            modifier = Modifier.padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = warm,
                                modifier = Modifier.size(42.dp),
                            )
                        }
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetaPill("Invitations", leadingIcon = Icons.Default.Groups)
                    MetaPill("Pass", leadingIcon = Icons.Default.QrCode2)
                    MetaPill("Schedule", leadingIcon = Icons.Default.CalendarMonth)
                    MetaPill("Venue flow", leadingIcon = Icons.Default.Map)
                }

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = colors.surface.copy(alpha = 0.74f),
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f)),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Featured invitations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            MetaPill(
                                "${invitations.size} ready",
                                leadingIcon = Icons.Default.VpnKey,
                            )
                        }

                        featuredInvitations.forEach { invitation ->
                            GuestInvitationTeaser(invitation = invitation)
                        }

                        KazeSecondaryButton(
                            label = "View invites",
                            onClick = onOpenInvitations,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = Icons.Default.VpnKey,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestInvitationTeaser(
    invitation: InvitationPreview,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            ) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    invitation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    invitation.statusLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                invitation.code,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun GuestHomeEntryCard(
    code: String,
    onCodeChange: (String) -> Unit,
    onSubmitCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }

            Text(
                "Open with a code",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Received a code from SMS, WhatsApp, or an organizer? Enter it here and jump straight into the event.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
            )

            CodeEntryCard(
                code = code,
                onCodeChange = onCodeChange,
                onSubmit = onSubmitCode,
            )
        }
    }
}
