package dev.orestegabo.kaze.ui.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.presentation.demo.InvitationState
import dev.orestegabo.kaze.ui.ai.KazeAiAssistCard
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.home.invitations.InvitationEventType
import dev.orestegabo.kaze.ui.home.invitations.InvitationTheme
import dev.orestegabo.kaze.ui.home.invitations.InvitationThemeCategory
import dev.orestegabo.kaze.ui.home.invitations.resolveInvitationTheme
import dev.orestegabo.kaze.ui.home.invitations.themesForEventType
import dev.orestegabo.kaze.ui.states.KazeEmptyStateScreen
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.empty_invitations_action
import kaze.composeapp.generated.resources.empty_invitations_subtitle
import kaze.composeapp.generated.resources.empty_invitations_title
import kaze.composeapp.generated.resources.wedding_example_hero
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Composable
internal fun InvitationsScreen(
    invitations: List<InvitationPreview>,
    onBack: () -> Unit,
    selectedInvitation: InvitationPreview?,
    onSelectedInvitationChange: (InvitationPreview?) -> Unit,
    onOpenEvent: (InvitationPreview) -> Unit,
    edgeAiEnabled: Boolean,
    onAiAction: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 20.dp,
) {
    val scrollState = rememberScrollState()
    var isCreatingInvitation by remember { mutableStateOf(false) }
    var createdInvitations by remember { mutableStateOf(emptyList<InvitationPreview>()) }
    val visibleInvitations = createdInvitations + invitations
    val activeInvitations = visibleInvitations.filter { it.state == InvitationState.ACTIVE }
    val pastInvitations = visibleInvitations.filter { it.state != InvitationState.ACTIVE }

    if (selectedInvitation != null) {
        InvitationDetailScreen(
            invitation = selectedInvitation,
            onBack = { onSelectedInvitationChange(null) },
            onOpenEvent = { onOpenEvent(selectedInvitation) },
            edgeAiEnabled = edgeAiEnabled,
            onAiAction = onAiAction,
            modifier = modifier,
            bottomContentPadding = bottomContentPadding,
        )
        return
    }

    if (isCreatingInvitation) {
        CreateInvitationScreen(
            onBack = { isCreatingInvitation = false },
            onCreateInvitation = { invitation ->
                createdInvitations = listOf(invitation) + createdInvitations
                isCreatingInvitation = false
                onSelectedInvitationChange(invitation)
            },
            modifier = modifier,
            bottomContentPadding = bottomContentPadding,
        )
        return
    }

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
                label = "${visibleInvitations.size} invitations",
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
            KazePrimaryButton(
                label = "Create invitation",
                onClick = { isCreatingInvitation = true },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Edit,
            )
        }

        if (edgeAiEnabled) {
            KazeAiAssistCard(
                title = "Smart RSVP assistant",
                subtitle = "For organizers, Kaze can turn local voice notes into reviewable guest details before creating invitations.",
                actionLabel = "Prepare RSVP draft",
                onAction = { onAiAction("Smart RSVP Voice-to-Data") },
                icon = Icons.Default.AutoAwesome,
            )
        }

        if (visibleInvitations.isEmpty()) {
            KazeEmptyStateScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                title = stringResource(Res.string.empty_invitations_title),
                subtitle = stringResource(Res.string.empty_invitations_subtitle),
                actionLabel = stringResource(Res.string.empty_invitations_action),
                onAction = { isCreatingInvitation = true },
            )
        } else {
            SectionLabel("Active")
            activeInvitations.forEach { invitation ->
                InvitationCard(
                    invitation = invitation,
                    onClick = { onSelectedInvitationChange(invitation) },
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
                        onClick = { onSelectedInvitationChange(invitation) },
                    )
                }
            }
        }
    }
}

@Composable
private fun InvitationDetailScreen(
    invitation: InvitationPreview,
    onBack: () -> Unit,
    onOpenEvent: () -> Unit,
    edgeAiEnabled: Boolean,
    onAiAction: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 20.dp,
) {
    val scrollState = rememberScrollState()
    val isActive = invitation.state == InvitationState.ACTIVE
    val invitationTheme = invitation.resolveInvitationTheme()
    val isWedding = invitationTheme.category == InvitationThemeCategory.WEDDING

    if (isWedding) {
        WeddingInvitationDetailScreen(
            invitation = invitation,
            isActive = isActive,
            onBack = onBack,
            onOpenEvent = onOpenEvent,
            edgeAiEnabled = edgeAiEnabled,
            onAiAction = onAiAction,
            modifier = modifier,
            bottomContentPadding = bottomContentPadding,
        )
        return
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        invitationTheme.PageBackground(
            modifier = Modifier.matchParentSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            KazeGhostButton(
                label = "Back to invitations",
                onClick = onBack,
                leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                shape = RoundedCornerShape(34.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isWedding) 320.dp else 250.dp)
                        .background(
                            if (isWedding) {
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(
                                        androidx.compose.ui.graphics.Color.Transparent,
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.72f),
                                    ),
                                )
                            } else {
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                    ),
                                )
                            },
                        )
                        .padding(22.dp),
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (isWedding) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.66f),
                            border = BorderStroke(1.dp, if (isWedding) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        ) {
                            Text(
                                invitation.statusLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isWedding) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            invitation.title,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = if (isWedding) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            invitation.subtitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isWedding) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.86f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetaPill(
                                label = if (invitation.code.isNotBlank()) invitation.code else "Pending",
                                leadingIcon = Icons.Default.VpnKey,
                                containerColor = if (isWedding) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.14f) else MaterialTheme.colorScheme.secondaryContainer,
                                textColor = if (isWedding) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            MetaPill(
                                label = if (isActive) "Active" else "Saved",
                                leadingIcon = Icons.Default.CheckCircle,
                                containerColor = if (isWedding) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                                textColor = if (isWedding) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(30.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                "Invitation access",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                if (isWedding) "Wedding invitation" else invitationTheme.detailsTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        if (invitation.code.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    invitation.code,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        InvitationAccessTile(
                            title = "Guest",
                            value = invitation.phoneLabel,
                            modifier = Modifier.weight(1f),
                        )
                        InvitationAccessTile(
                            title = "Status",
                            value = if (isActive) "Ready to open" else "Saved for later",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    InvitationNarrativeCard(isWedding = false)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        KazePrimaryButton(
                            label = if (isActive) "Open event" else "View event",
                            onClick = onOpenEvent,
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Default.CalendarMonth,
                        )
                        if (invitation.code.isNotBlank()) {
                            KazeSecondaryButton(
                                label = "Copy code",
                                onClick = {},
                                modifier = Modifier.weight(1f),
                                leadingIcon = Icons.Default.Edit,
                            )
                        }
                    }
                }
            }

            if (edgeAiEnabled) {
                KazeAiAssistCard(
                    title = "Explain this pass",
                    subtitle = "Kaze can explain access, schedule, and venue rules from this invitation without sending the pass to a server.",
                    actionLabel = "Explain offline",
                    onAction = { onAiAction("Offline Pass Explainer") },
                    icon = Icons.Default.AutoAwesome,
                )
            }
        }
    }
}

@Composable
private fun WeddingInvitationDetailScreen(
    invitation: InvitationPreview,
    isActive: Boolean,
    onBack: () -> Unit,
    onOpenEvent: () -> Unit,
    edgeAiEnabled: Boolean,
    onAiAction: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 20.dp,
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.wedding_example_hero),
            contentDescription = "Wedding invitation hero",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.18f),
                            Color(0xFF522C35).copy(alpha = 0.48f),
                            Color(0xFF2A171B).copy(alpha = 0.92f),
                            Color(0xFFF7ECE5),
                        ),
                    ),
                ),
        )
        WeddingFloatingHearts(
            modifier = Modifier.matchParentSize(),
            heartCount = 18,
            tint = Color(0xFFFFE6EC),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            KazeGhostButton(
                label = "Back to invitations",
                onClick = onBack,
                leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
            )
            Surface(
                shape = RoundedCornerShape(36.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                        ) {
                            Text(
                                invitation.statusLabel,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            "Together with their families",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            invitation.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            "invite you to celebrate their wedding",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFFFE8EE),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MetaPill(
                                label = "28 June 2026",
                                leadingIcon = Icons.Default.CalendarMonth,
                                containerColor = Color.White.copy(alpha = 0.14f),
                                textColor = Color.White,
                            )
                            MetaPill(
                                label = "Kigali",
                                leadingIcon = Icons.Default.CheckCircle,
                                containerColor = Color.White.copy(alpha = 0.14f),
                                textColor = Color.White,
                            )
                        }
                        Text(
                            "Garden ceremony • Sunset reception",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.88f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(34.dp),
                color = Color(0xFFFFFBF7).copy(alpha = 0.94f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.72f)),
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "Your invitation",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFB76B79),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "A beautiful welcome into the wedding day.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF251719),
                        fontWeight = FontWeight.Black,
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = 2,
                    ) {
                        WeddingInvitationMetric(
                            label = "Access",
                            value = invitation.code.ifBlank { "Soon" },
                        )
                        WeddingInvitationMetric(
                            label = "Guest",
                            value = invitation.phoneLabel,
                        )
                    }
                    WeddingInvitationQuoteCard()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        KazePrimaryButton(
                            label = if (isActive) "Open wedding" else "View wedding",
                            onClick = onOpenEvent,
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Default.CalendarMonth,
                        )
                        KazeSecondaryButton(
                            label = "Share look",
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Default.Edit,
                        )
                    }
                }
            }

            if (edgeAiEnabled) {
                KazeAiAssistCard(
                    title = "Explain this pass",
                    subtitle = "Kaze can explain access, schedule, and venue rules from this invitation without sending the pass to a server.",
                    actionLabel = "Explain offline",
                    onAction = { onAiAction("Offline Pass Explainer") },
                    icon = Icons.Default.AutoAwesome,
                )
            }
        }
    }
}

@Composable
private fun WeddingInvitationMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8E6E5),
        border = BorderStroke(1.dp, Color(0xFFEAC8CC)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFB76B79),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF251719),
                fontWeight = FontWeight.Bold,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun WeddingInvitationQuoteCard() {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFFFF5F1),
        border = BorderStroke(1.dp, Color(0xFFF1D8D5)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "With love",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFB76B79),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Save the date, dress with joy, and arrive ready to celebrate.",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF251719),
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Ceremony in the garden. Reception at sunset.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF251719).copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun WeddingFloatingHearts(
    modifier: Modifier = Modifier,
    heartCount: Int,
    tint: Color,
) {
    BoxWithConstraints(modifier = modifier) {
        weddingInviteHeartSpecs.take(heartCount).forEachIndexed { index, spec ->
            val transition = rememberInfiniteTransition(label = "invite-heart-$index")
            val progress = transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = spec.durationMillis,
                        delayMillis = spec.delayMillis,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "invite-heart-progress-$index",
            )
            val drift = spec.drift * progress.value
            val xOffset = (maxWidth * spec.lane) + drift
            val yOffset = maxHeight - ((maxHeight + spec.travelPadding) * progress.value)
            val alpha = when {
                progress.value < spec.fadeIn -> progress.value / spec.fadeIn
                progress.value > spec.fadeOutStart -> (1f - progress.value) / (1f - spec.fadeOutStart)
                else -> spec.maxAlpha
            }.coerceIn(0.12f, spec.maxAlpha)

            Text(
                spec.symbol,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = xOffset, y = yOffset),
                color = tint.copy(alpha = alpha),
                style = when (spec.sizeTier) {
                    0 -> MaterialTheme.typography.titleMedium
                    1 -> MaterialTheme.typography.titleLarge
                    else -> MaterialTheme.typography.headlineMedium
                },
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private data class WeddingInviteHeartSpec(
    val lane: Float,
    val drift: Dp,
    val durationMillis: Int,
    val delayMillis: Int,
    val travelPadding: Dp,
    val fadeIn: Float,
    val fadeOutStart: Float,
    val maxAlpha: Float,
    val symbol: String,
    val sizeTier: Int,
)

private val weddingInviteHeartSpecs = listOf(
    WeddingInviteHeartSpec(0.1f, 14.dp, 6300, 0, 176.dp, 0.16f, 0.84f, 0.76f, "♥", 2),
    WeddingInviteHeartSpec(0.22f, (-10).dp, 7200, 360, 160.dp, 0.14f, 0.88f, 0.64f, "♡", 1),
    WeddingInviteHeartSpec(0.34f, 20.dp, 5600, 680, 184.dp, 0.18f, 0.80f, 0.78f, "♥", 2),
    WeddingInviteHeartSpec(0.47f, (-18).dp, 7700, 1040, 170.dp, 0.15f, 0.89f, 0.70f, "♥", 1),
    WeddingInviteHeartSpec(0.59f, 12.dp, 6100, 1380, 190.dp, 0.13f, 0.82f, 0.68f, "♡", 1),
    WeddingInviteHeartSpec(0.72f, (-14).dp, 7000, 1760, 168.dp, 0.17f, 0.86f, 0.74f, "♥", 2),
    WeddingInviteHeartSpec(0.88f, 10.dp, 5900, 2120, 182.dp, 0.16f, 0.83f, 0.66f, "♥", 1),
    WeddingInviteHeartSpec(0.05f, (-6).dp, 8150, 2460, 158.dp, 0.14f, 0.90f, 0.56f, "♡", 0),
    WeddingInviteHeartSpec(0.28f, 16.dp, 6500, 2860, 174.dp, 0.15f, 0.84f, 0.74f, "♥", 2),
    WeddingInviteHeartSpec(0.41f, (-16).dp, 7350, 3200, 188.dp, 0.16f, 0.82f, 0.70f, "♥", 1),
    WeddingInviteHeartSpec(0.54f, 22.dp, 5750, 3560, 194.dp, 0.13f, 0.80f, 0.78f, "♡", 1),
    WeddingInviteHeartSpec(0.67f, (-12).dp, 7900, 3940, 164.dp, 0.15f, 0.88f, 0.62f, "♥", 2),
    WeddingInviteHeartSpec(0.8f, 18.dp, 6250, 4320, 180.dp, 0.16f, 0.83f, 0.76f, "♥", 2),
    WeddingInviteHeartSpec(0.93f, (-8).dp, 6800, 4660, 166.dp, 0.14f, 0.86f, 0.64f, "♡", 0),
    WeddingInviteHeartSpec(0.16f, 8.dp, 7100, 5040, 186.dp, 0.17f, 0.84f, 0.72f, "♥", 1),
    WeddingInviteHeartSpec(0.62f, (-20).dp, 7600, 5420, 176.dp, 0.15f, 0.87f, 0.68f, "♡", 1),
    WeddingInviteHeartSpec(0.76f, 14.dp, 6020, 5780, 192.dp, 0.16f, 0.82f, 0.74f, "♥", 2),
    WeddingInviteHeartSpec(0.97f, (-5).dp, 8400, 6200, 150.dp, 0.13f, 0.90f, 0.54f, "♡", 0),
)

@Composable
private fun InvitationAccessTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun InvitationNarrativeCard(isWedding: Boolean) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                if (isWedding) "This invite opens a full wedding page with timeline, venue, guests, and Kaze Pass access."
                else "This invite opens event details, schedule, venue context, and access information.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                if (isWedding) "Designed as one emotional, photo-led experience."
                else "Designed as one clean event workspace.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
            )
        }
    }
}

@Composable
internal fun CreateInvitationScreen(
    onBack: () -> Unit,
    onCreateInvitation: (InvitationPreview) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 20.dp,
    seed: InvitationDraftSeed? = null,
) {
    val scrollState = rememberScrollState()
    var eventType by remember(seed) { mutableStateOf(seed?.eventType ?: InvitationEventType.WEDDING) }
    var selectedTheme by remember(eventType) { mutableStateOf(themesForEventType(eventType).first()) }
    var selectedLinkedEvent by remember(eventType, seed) { mutableStateOf(invitationEventOptions(eventType, seed).first()) }
    var isEventMenuOpen by remember { mutableStateOf(false) }
    var isThemeSheetOpen by remember { mutableStateOf(false) }
    var generatedCode by remember { mutableStateOf(generateInvitationCode()) }
    var title by remember(seed) { mutableStateOf(seed?.eventTitle.orEmpty()) }
    var eventDate by remember(seed) { mutableStateOf(seed?.preferredDate.orEmpty()) }
    var eventTime by remember { mutableStateOf("") }
    var selectedContacts by remember { mutableStateOf(setOf<String>()) }
    var note by remember(seed) { mutableStateOf(seed?.note.orEmpty()) }
    val needsOrganizerApproval = selectedLinkedEvent.needsOrganizerApproval
    val statusLabel = if (needsOrganizerApproval) "Needs organizer approval" else "Draft invitation"
    val selectedContactLabel = if (selectedContacts.isEmpty()) {
        "No guests selected yet"
    } else {
        "${selectedContacts.size} guest${if (selectedContacts.size == 1) "" else "s"} selected"
    }

    val previewInvitation = InvitationPreview(
        title = title.ifBlank { selectedLinkedEvent.title },
        subtitle = note.ifBlank { eventType.defaultSubtitle(selectedLinkedEvent, eventDate, eventTime) },
        code = "",
        phoneLabel = selectedContactLabel,
        statusLabel = statusLabel,
        themeId = selectedTheme.id,
    )

    Box(modifier = modifier.fillMaxSize()) {
        selectedTheme.PageBackground(modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            KazeGhostButton(
                label = "Back to invitations",
                onClick = onBack,
                leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Create invitation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Choose the event type, fill the basics, then select a theme that changes the whole invitation page.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("Event type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        InvitationEventType.entries.forEach { type ->
                            SelectablePill(
                                label = type.label,
                                selected = eventType == type,
                                onClick = {
                                    eventType = type
                                    selectedLinkedEvent = invitationEventOptions(type, seed).first()
                                    title = if (seed?.eventType == type) seed.eventTitle else ""
                                },
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Linked event", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Choose the event this invitation belongs to, or create a new one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                    EventDropdownField(
                        selectedEvent = selectedLinkedEvent,
                        eventOptions = invitationEventOptions(eventType, seed),
                        expanded = isEventMenuOpen,
                        onExpandedChange = { isEventMenuOpen = it },
                        onEventSelected = { eventOption ->
                            selectedLinkedEvent = eventOption
                            title = if (eventOption.isCreateNew) "" else eventOption.title
                            isEventMenuOpen = false
                        },
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Invitation title") },
                        placeholder = { Text(selectedLinkedEvent.title) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            label = { Text("Date") },
                            placeholder = { Text("28 Apr 2026") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                        )
                        OutlinedTextField(
                            value = eventTime,
                            onValueChange = { eventTime = it },
                            label = { Text("Time") },
                            placeholder = { Text("18:00") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                "Invitation code",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                if (needsOrganizerApproval) {
                                    "The code will appear after the organizer approves and saves the invitation."
                                } else {
                                    "The code will appear after the invitation is saved."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f),
                            )
                        }
                    }
                    ContactSelectionPanel(
                        selectedContactIds = selectedContacts,
                        onContactToggle = { contactId ->
                            selectedContacts = if (contactId in selectedContacts) {
                                selectedContacts - contactId
                            } else {
                                selectedContacts + contactId
                            }
                        },
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Invitation note") },
                        placeholder = { Text(eventType.defaultSubtitle(selectedLinkedEvent, eventDate, eventTime)) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                    )
                }
            }

            InvitationDesignSelectorCard(
                eventType = eventType,
                selectedTheme = selectedTheme,
                onChangeTheme = { isThemeSheetOpen = true },
            )

            selectedTheme.Cover(
                invitation = previewInvitation,
                isActive = true,
                modifier = Modifier.fillMaxWidth(),
            )

            KazePrimaryButton(
                label = if (needsOrganizerApproval) "Create pending invitation" else "Create draft invitation",
                onClick = {
                    onCreateInvitation(
                        previewInvitation.copy(
                            statusLabel = if (needsOrganizerApproval) "Waiting organizer approval" else "Invitation ready",
                            code = if (needsOrganizerApproval) "" else generatedCode,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.CheckCircle,
            )
        }
        if (isThemeSheetOpen) {
            ThemeSelectionSheet(
                eventType = eventType,
                selectedTheme = selectedTheme,
                onThemeSelected = { selectedTheme = it },
                onDismiss = { isThemeSheetOpen = false },
                bottomContentPadding = bottomContentPadding,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun InvitationDesignSelectorCard(
    eventType: InvitationEventType,
    selectedTheme: InvitationTheme,
    onChangeTheme: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Design", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${selectedTheme.name} for ${eventType.label.lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }
            KazeSecondaryButton(
                label = "Change",
                onClick = onChangeTheme,
                leadingIcon = Icons.Default.Edit,
            )
        }
    }
}

@Composable
private fun ThemeSelectionSheet(
    eventType: InvitationEventType,
    selectedTheme: InvitationTheme,
    onThemeSelected: (InvitationTheme) -> Unit,
    onDismiss: () -> Unit,
    bottomContentPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = bottomContentPadding)
            .height(300.dp),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 44.dp, height = 4.dp)
                    .padding(bottom = 2.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Choose design", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Select a theme and preview updates immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                    )
                }
                KazeGhostButton(label = "Done", onClick = onDismiss)
            }
            themesForEventType(eventType).forEach { theme ->
                InvitationThemeOptionCard(
                    theme = theme,
                    selected = selectedTheme.id == theme.id,
                    onClick = { onThemeSelected(theme) },
                )
            }
        }
    }
}

@Composable
private fun EventDropdownField(
    selectedEvent: InvitationEventOption,
    eventOptions: List<InvitationEventOption>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onEventSelected: (InvitationEventOption) -> Unit,
) {
    Box {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onExpandedChange(true) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(selectedEvent.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(selectedEvent.supportingLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                }
                MetaPill(if (selectedEvent.isCreateNew) "New" else "Existing")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            eventOptions.forEach { event ->
                DropdownMenuItem(
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(event.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(event.supportingLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
                        }
                    },
                    onClick = { onEventSelected(event) },
                )
            }
        }
    }
}

@Composable
private fun ContactSelectionPanel(
    selectedContactIds: Set<String>,
    onContactToggle: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
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
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Guests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${selectedContactIds.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                    )
                }
                KazeSecondaryButton(
                    label = "Contacts",
                    onClick = {},
                    leadingIcon = Icons.Default.Groups,
                )
            }
            Text(
                "Select people from phone contacts or guests already registered on Kaze. This demo list will be replaced by real contacts and app users later.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                demoInvitationContacts.forEach { contact ->
                    SelectablePill(
                        label = "${contact.name} • ${contact.sourceLabel}",
                        selected = contact.id in selectedContactIds,
                        onClick = { onContactToggle(contact.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectablePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.46f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InvitationThemeOptionCard(
    theme: InvitationTheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.58f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(theme.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(theme.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
            }
            if (selected) {
                MetaPill("Selected", leadingIcon = Icons.Default.CheckCircle)
            }
        }
    }
}

private fun InvitationEventType.defaultTitle(): String = when (this) {
    InvitationEventType.WEDDING -> "Uwase x Iradukunda"
    InvitationEventType.BIRTHDAY -> "Aline's birthday"
    InvitationEventType.MEETING -> "Team strategy meeting"
    InvitationEventType.CONFERENCE -> "Kigali business summit"
    InvitationEventType.OTHER -> "Private event"
}

private fun InvitationEventType.defaultSubtitle(
    event: InvitationEventOption,
    date: String,
    time: String,
): String {
    val schedule = invitationScheduleText(date, time)
    val base = when (this) {
        InvitationEventType.WEDDING -> if (event.needsOrganizerApproval) {
            "This wedding invitation will be reviewed by the organizer before it becomes valid."
        } else {
            "Join us for the wedding celebration."
        }
        InvitationEventType.BIRTHDAY -> "You are invited to celebrate with us."
        InvitationEventType.MEETING -> "Meeting access and venue details will be shared here."
        InvitationEventType.CONFERENCE -> "Conference access and live event details will be shared here."
        InvitationEventType.OTHER -> "Event access and details will be shared here."
    }
    return if (schedule.isBlank()) base else "$base $schedule"
}

private fun invitationScheduleText(date: String, time: String): String = when {
    date.isNotBlank() && time.isNotBlank() -> "Date: $date at $time."
    date.isNotBlank() -> "Date: $date."
    time.isNotBlank() -> "Time: $time."
    else -> ""
}

private data class InvitationEventOption(
    val id: String,
    val title: String,
    val supportingLabel: String,
    val needsOrganizerApproval: Boolean,
    val isCreateNew: Boolean = false,
)

private fun invitationEventOptions(
    eventType: InvitationEventType,
    seed: InvitationDraftSeed? = null,
): List<InvitationEventOption> {
    val seedOption = seed
        ?.takeIf { it.eventType == eventType }
        ?.let {
            InvitationEventOption(
                id = "reservation_${it.venueName.lowercase().filter { char -> char.isLetterOrDigit() }}",
                title = it.eventTitle,
                supportingLabel = "${it.venueName} • ${it.guestCount} guest${if (it.guestCount == 1) "" else "s"} • ${it.sourceLabel}",
                needsOrganizerApproval = false,
            )
        }
    val options = when (eventType) {
        InvitationEventType.WEDDING -> listOf(
        InvitationEventOption("new_wedding", "Create a new wedding event", "You are the organizer for this event.", false, true),
        InvitationEventOption("uwase_iradukunda", "Uwase x Iradukunda", "Existing wedding. Organizer approval is required before guests can use the invitation.", true),
    )
        InvitationEventType.BIRTHDAY -> listOf(
        InvitationEventOption("new_birthday", "Create a birthday event", "Surprise birthdays can be created without organizer approval.", false, true),
        InvitationEventOption("aline_birthday", "Aline's birthday", "Small private event. Invitation can be created immediately.", false),
    )
        InvitationEventType.MEETING -> listOf(
        InvitationEventOption("new_meeting", "Create a meeting event", "You are the organizer for this meeting.", false, true),
        InvitationEventOption("strategy_meeting", "Team strategy meeting", "Existing meeting. Approval may be required by the organizer.", true),
    )
        InvitationEventType.CONFERENCE -> listOf(
        InvitationEventOption("new_conference", "Create a conference event", "You are the organizer for this conference.", false, true),
        InvitationEventOption("eafs", "East Africa Finance Summit", "Existing conference. Organizer approval is required.", true),
    )
        InvitationEventType.OTHER -> listOf(
        InvitationEventOption("new_other", "Create another event type", "Use this when wedding, birthday, meeting, or conference does not fit.", false, true),
    )
    }
    return if (seedOption == null) options else listOf(seedOption) + options
}

private data class InvitationContactOption(
    val id: String,
    val name: String,
    val sourceLabel: String,
)

private val demoInvitationContacts = listOf(
    InvitationContactOption("contact_aline", "Aline", "Kaze"),
    InvitationContactOption("contact_uwase", "Uwase", "Phone"),
    InvitationContactOption("contact_iradukunda", "Iradukunda", "Phone"),
    InvitationContactOption("contact_patrick", "Patrick", "Kaze"),
    InvitationContactOption("contact_claudine", "Claudine", "Phone"),
    InvitationContactOption("contact_mutesi", "Mutesi", "Kaze"),
    InvitationContactOption("contact_eric", "Eric", "Phone"),
)

private fun generateInvitationCode(length: Int = 6): String {
    val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return buildString {
        repeat(length) {
            append(alphabet[Random.nextInt(alphabet.length)])
        }
    }
}

@Composable
private fun InvitationDetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.74f),
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
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
