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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.presentation.demo.InvitationState
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.home.invitations.InvitationEventType
import dev.orestegabo.kaze.ui.home.invitations.InvitationTemplateContract
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
import kotlin.time.Clock

@Composable
internal fun InvitationsScreen(
    invitations: List<InvitationPreview>,
    isGuestMode: Boolean,
    allowInvitationCreation: Boolean,
    onBack: () -> Unit,
    selectedInvitation: InvitationPreview?,
    onSelectedInvitationChange: (InvitationPreview?) -> Unit,
    onOpenEvent: (InvitationPreview) -> Unit,
    onRespondToInvitation: (String, String) -> Unit,
    edgeAiEnabled: Boolean,
    onAiAction: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 20.dp,
) {
    val scrollState = rememberScrollState()
    var isCreatingInvitation by remember { mutableStateOf(false) }
    var createdInvitations by remember { mutableStateOf(emptyList<InvitationPreview>()) }
    val visibleInvitations = if (allowInvitationCreation) createdInvitations + invitations else invitations
    val activeInvitations = visibleInvitations.filter { it.state == InvitationState.ACTIVE }
    val pastInvitations = visibleInvitations.filter { it.state != InvitationState.ACTIVE }

    if (selectedInvitation != null) {
        InvitationDetailScreen(
            invitation = selectedInvitation,
            onBack = { onSelectedInvitationChange(null) },
            onOpenEvent = { onOpenEvent(selectedInvitation) },
            onRespondToInvitation = onRespondToInvitation,
            edgeAiEnabled = edgeAiEnabled,
            onAiAction = onAiAction,
            modifier = modifier,
            bottomContentPadding = bottomContentPadding,
        )
        return
    }

    if (allowInvitationCreation && isCreatingInvitation) {
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

    // TODO [IN-PROGRESS] Authenticated invitations are database-backed; guest mode still shows
    // demo invitations, and realtime websocket updates are not wired yet.
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
            if (allowInvitationCreation) {
                KazePrimaryButton(
                    label = "Create invitation",
                    onClick = { isCreatingInvitation = true },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Default.Edit,
                )
            }
        }

        if (visibleInvitations.isEmpty()) {
            KazeEmptyStateScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                title = if (isGuestMode) {
                    "Private invitations appear after sign-in"
                } else {
                    stringResource(Res.string.empty_invitations_title)
                },
                subtitle = if (isGuestMode) {
                    "Sign in or enter an event code to see invitations linked to you."
                } else if (allowInvitationCreation) {
                    stringResource(Res.string.empty_invitations_subtitle)
                } else {
                    "Invitations linked to you will appear here."
                },
                actionLabel = if (allowInvitationCreation) stringResource(Res.string.empty_invitations_action) else null,
                eyebrow = "Invitations",
                tags = if (isGuestMode) listOf("Sign in", "Event code") else listOf("RSVP", "Guests"),
                icon = Icons.Default.MarkEmailUnread,
                onAction = if (allowInvitationCreation) ({ isCreatingInvitation = true }) else null,
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
                EmptyInvitationsCard(
                    title = "No active invitations",
                    subtitle = "New invitations and pending RSVPs will appear here.",
                )
            }
            if (pastInvitations.isNotEmpty()) {
                SectionLabel("Past and archived")
                pastInvitations.forEach { invitation ->
                    InvitationCard(
                        invitation = invitation,
                        onClick = { onSelectedInvitationChange(invitation) },
                    )
                }
            } else if (visibleInvitations.isNotEmpty()) {
                SectionLabel("Past and archived")
                EmptyInvitationsCard(
                    title = "Nothing archived yet",
                    subtitle = "Older invitations will settle here once an event is over.",
                    icon = Icons.Default.History,
                )
            }
        }
    }
}

@Composable
private fun InvitationDetailScreen(
    invitation: InvitationPreview,
    onBack: () -> Unit,
    onOpenEvent: () -> Unit,
    onRespondToInvitation: (String, String) -> Unit,
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
            onRespondToInvitation = onRespondToInvitation,
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
                    if (invitation.awaitingResponse) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            KazeSecondaryButton(
                                label = "Decline",
                                onClick = { onRespondToInvitation(invitation.id, "decline") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = Icons.Default.Edit,
                            )
                            KazePrimaryButton(
                                label = "Accept",
                                onClick = { onRespondToInvitation(invitation.id, "accept") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = Icons.Default.CheckCircle,
                            )
                        }
                    }
                }
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
    onRespondToInvitation: (String, String) -> Unit,
    edgeAiEnabled: Boolean,
    onAiAction: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 20.dp,
) {
    val scrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme
    val pass = KazeTheme.pass
    val accents = KazeTheme.accents

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
                            colors.scrim.copy(alpha = 0.18f),
                            pass.cardBaseMiddle.copy(alpha = 0.48f),
                            pass.cardBaseStart.copy(alpha = 0.92f),
                            colors.surface,
                        ),
                    ),
                ),
        )
        WeddingFloatingHearts(
            modifier = Modifier.matchParentSize(),
            heartCount = 18,
            tint = pass.cardOnSurface.copy(alpha = 0.88f),
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
                color = pass.cardOnSurface.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, pass.cardOnSurface.copy(alpha = 0.18f)),
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
                            color = pass.cardOnSurface.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, pass.cardOnSurface.copy(alpha = 0.16f)),
                        ) {
                            Text(
                                invitation.statusLabel,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = pass.cardOnSurface,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            invitation.statusLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = pass.cardOnSurface.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            invitation.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = pass.cardOnSurface,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            invitation.subtitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = pass.cardOnSurfaceMuted,
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
                                label = invitation.code.ifBlank { "Code after approval" },
                                leadingIcon = Icons.Default.CalendarMonth,
                                containerColor = pass.cardOnSurface.copy(alpha = 0.14f),
                                textColor = pass.cardOnSurface,
                            )
                            MetaPill(
                                label = invitation.phoneLabel,
                                leadingIcon = Icons.Default.CheckCircle,
                                containerColor = pass.cardOnSurface.copy(alpha = 0.14f),
                                textColor = pass.cardOnSurface,
                            )
                        }
                        Text(
                            invitation.subtitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = pass.cardOnSurface.copy(alpha = 0.88f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(34.dp),
                color = colors.surface.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f)),
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "Your invitation",
                        style = MaterialTheme.typography.labelLarge,
                        color = accents.editorialWarm,
                        fontWeight = FontWeight.SemiBold,
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
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = Icons.Default.CalendarMonth,
                        )
                    }
                    if (invitation.awaitingResponse) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            KazeSecondaryButton(
                                label = "Decline",
                                onClick = { onRespondToInvitation(invitation.id, "decline") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = Icons.Default.Edit,
                            )
                            KazePrimaryButton(
                                label = "Accept",
                                onClick = { onRespondToInvitation(invitation.id, "accept") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = Icons.Default.CheckCircle,
                            )
                        }
                    }
                }
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
    val colors = MaterialTheme.colorScheme
    val accents = KazeTheme.accents
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = colors.secondaryContainer.copy(alpha = 0.38f),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = accents.editorialWarm,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun WeddingInvitationQuoteCard() {
    val colors = MaterialTheme.colorScheme
    val accents = KazeTheme.accents
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = colors.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "With love",
                style = MaterialTheme.typography.labelLarge,
                color = accents.editorialWarm,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Save the date, dress with joy, and arrive ready to celebrate.",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Ceremony in the garden. Reception at sunset.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.68f),
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
internal fun CreateInvitationScreen(
    onBack: () -> Unit,
    onCreateInvitation: (InvitationPreview) -> Unit,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 20.dp,
    seed: InvitationDraftSeed? = null,
) {
    val scrollState = rememberScrollState()
    var currentStep by remember(seed) { mutableStateOf(0) }
    var eventType by remember(seed) { mutableStateOf(seed?.eventType ?: InvitationEventType.WEDDING) }
    var selectedTheme by remember(eventType) { mutableStateOf(themesForEventType(eventType).first()) }
    var selectedLinkedEvent by remember(eventType, seed) { mutableStateOf(invitationEventOptions(eventType, seed).first()) }
    var isEventTypeMenuOpen by remember { mutableStateOf(false) }
    var generatedCode by remember { mutableStateOf(generateInvitationCode()) }
    var eventDate by remember(seed) { mutableStateOf(seed?.preferredDate.orEmpty()) }
    var eventTime by remember { mutableStateOf("") }
    var venueName by remember { mutableStateOf("") }
    var brideName by remember { mutableStateOf("") }
    //var brideGender by remember { mutableStateOf("Female") }
    var groomName by remember { mutableStateOf("") }
    //var groomGender by remember { mutableStateOf("Male") }
    var birthdayName by remember { mutableStateOf("") }
    var birthdayPlanningMode by remember { mutableStateOf("For me") }
    var funeralName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var deathDate by remember { mutableStateOf("") }
    var selectedPhotoLabel by remember { mutableStateOf("") }
    var selectedContacts by remember { mutableStateOf(setOf<String>()) }
    var note by remember(seed) { mutableStateOf(seed?.note.orEmpty()) }
    var hasEditedDescription by remember(seed) { mutableStateOf(!seed?.note.isNullOrBlank()) }
    val needsOrganizerApproval = selectedLinkedEvent.needsOrganizerApproval
    val statusLabel = if (needsOrganizerApproval) "Needs organizer approval" else "Draft invitation"
    val selectedContactLabel = selectedContacts.takeIf { it.isNotEmpty() }?.let {
        "${it.size} guest${if (it.size == 1) "" else "s"}"
    }.orEmpty()
    val draftTitle = eventType.draftTitle(
        brideName = brideName,
        groomName = groomName,
        birthdayName = birthdayName,
        funeralName = funeralName,
    )
    val generatedDescription = eventType.generatedDescriptionSuggestion(
        brideName = brideName,
        groomName = groomName,
        //brideGender = brideGender,
        //groomGender = groomGender,
        birthdayName = birthdayName,
        birthdayPlanningMode = birthdayPlanningMode,
        funeralName = funeralName,
        birthDate = birthDate,
        deathDate = deathDate,
        eventDate = eventDate,
        eventTime = eventTime,
        venueName = venueName,
    )
    LaunchedEffect(generatedDescription, hasEditedDescription) {
        if (!hasEditedDescription) {
            note = generatedDescription
        }
    }
    val draftDescription = note.ifBlank { generatedDescription }
    val creationSteps = invitationCreationStepsFor(eventType)
    val currentStepKind = creationSteps[currentStep.coerceAtMost(creationSteps.lastIndex)].kind
    val templateDetails = eventType.templateDetailsSubtitle(
        eventDate = eventDate,
        eventTime = eventTime,
        venueName = venueName,
        birthDate = birthDate,
        deathDate = deathDate,
    )

    val previewInvitation = InvitationPreview(
        title = draftTitle.ifBlank { selectedLinkedEvent.title },
        subtitle = templateDetails,
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
                label = if (currentStep == 0) "Back to invitations" else "Back",
                onClick = {
                    if (currentStep == 0) onBack() else currentStep -= 1
                },
                leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "New invitation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            InvitationCreationProgress(
                currentStep = currentStep,
                steps = creationSteps,
            )

            when (currentStepKind) {
                InvitationCreationStepKind.BASICS -> Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text("Event basics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        EventTypeDropdownField(
                            selectedType = eventType,
                            expanded = isEventTypeMenuOpen,
                            onExpandedChange = { isEventTypeMenuOpen = it },
                            onEventTypeSelected = { type ->
                                eventType = type
                                selectedLinkedEvent = invitationEventOptions(type, seed).first()
                                note = ""
                                hasEditedDescription = false
                                currentStep = 0
                                isEventTypeMenuOpen = false
                            },
                        )
                        EventIdentityFields(
                            eventType = eventType,
                            brideName = brideName,
                            onBrideNameChange = { brideName = it },
                            //brideGender = brideGender,
                            //onBrideGenderChange = { brideGender = it },
                            groomName = groomName,
                            onGroomNameChange = { groomName = it },
                            //groomGender = groomGender,
                            //onGroomGenderChange = { groomGender = it },
                            birthdayName = birthdayName,
                            onBirthdayNameChange = { birthdayName = it },
                            birthdayPlanningMode = birthdayPlanningMode,
                            onBirthdayPlanningModeChange = { birthdayPlanningMode = it },
                            funeralName = funeralName,
                            onFuneralNameChange = { funeralName = it },
                            birthDate = birthDate,
                            onBirthDateChange = { birthDate = it },
                            deathDate = deathDate,
                            onDeathDateChange = { deathDate = it },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            EventDateSelectionField(
                                value = eventDate,
                                onValueChange = { eventDate = it },
                                label = "Date",
                                modifier = Modifier.weight(1f),
                            )
                            EventTimeSelectionField(
                                value = eventTime,
                                onValueChange = { eventTime = it },
                                label = "Time",
                                modifier = Modifier.weight(1f),
                            )
                        }
                        KazeSingleLineTextField(
                            value = venueName,
                            onValueChange = { venueName = it },
                            label = if (eventType == InvitationEventType.FUNERAL) "Gathering place" else "Venue",
                            placeholder = if (eventType == InvitationEventType.WEDDING) "Muhanga Park" else "Location",
                            imeAction = ImeAction.Done,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                InvitationCreationStepKind.DESIGN -> {
                    selectedTheme.Cover(
                        invitation = previewInvitation.copy(subtitle = draftDescription, phoneLabel = ""),
                        isActive = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    InvitationMessageStep(
                        draftDescription = draftDescription,
                        generatedDescription = generatedDescription,
                        hasEditedDescription = hasEditedDescription,
                        onNoteChange = {
                            note = it
                            hasEditedDescription = true
                        },
                        onResetSuggestion = {
                            note = generatedDescription
                            hasEditedDescription = false
                        },
                    )
                    InvitationPhotoCard(
                        selectedPhotoLabel = selectedPhotoLabel,
                        onSelectPhoto = { selectedPhotoLabel = "Selected" },
                    )
                    InlineTemplateGallery(
                        eventType = eventType,
                        selectedTheme = selectedTheme,
                        previewInvitation = previewInvitation.copy(subtitle = draftDescription, phoneLabel = ""),
                        onThemeSelected = { selectedTheme = it },
                    )
                }

                InvitationCreationStepKind.MESSAGE -> InvitationMessageStep(
                    draftDescription = draftDescription,
                    generatedDescription = generatedDescription,
                    hasEditedDescription = hasEditedDescription,
                    onNoteChange = {
                        note = it
                        hasEditedDescription = true
                    },
                    onResetSuggestion = {
                        note = generatedDescription
                        hasEditedDescription = false
                    },
                )

                InvitationCreationStepKind.GUESTS -> {
                    selectedTheme.Cover(
                        invitation = previewInvitation,
                        isActive = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
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
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KazeSecondaryButton(
                    label = if (currentStep == 0) "Cancel" else "Previous",
                    onClick = {
                        if (currentStep == 0) onBack() else currentStep -= 1
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
                )
                KazePrimaryButton(
                    label = if (currentStep == creationSteps.lastIndex) {
                        if (needsOrganizerApproval) "Create pending" else "Create draft"
                    } else {
                        "Next"
                    },
                    onClick = {
                        if (currentStep < creationSteps.lastIndex) {
                            currentStep += 1
                        } else {
                            onCreateInvitation(
                                previewInvitation.copy(
                                    subtitle = draftDescription,
                                    statusLabel = if (needsOrganizerApproval) "Waiting organizer approval" else "Invitation ready",
                                    code = if (needsOrganizerApproval) "" else generatedCode,
                                ),
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = if (currentStep == creationSteps.lastIndex) Icons.Default.CheckCircle else null,
                )
            }
        }
    }
}

private enum class InvitationCreationStepKind {
    BASICS,
    DESIGN,
    MESSAGE,
    GUESTS,
}

private data class InvitationCreationStep(
    val kind: InvitationCreationStepKind,
    val title: String,
    val subtitle: String,
)

private fun invitationCreationStepsFor(eventType: InvitationEventType): List<InvitationCreationStep> = when (eventType) {
    InvitationEventType.WEDDING -> listOf(
        InvitationCreationStep(InvitationCreationStepKind.BASICS, "Basics", "Couple, date and venue"),
        InvitationCreationStep(InvitationCreationStepKind.DESIGN, "Template", "Photo invitation styles"),
        InvitationCreationStep(InvitationCreationStepKind.GUESTS, "Guests", "Recipients and final check"),
    )
    InvitationEventType.BIRTHDAY -> listOf(
        InvitationCreationStep(InvitationCreationStepKind.BASICS, "Basics", "Birthday person and place"),
        InvitationCreationStep(InvitationCreationStepKind.DESIGN, "Template", "Birthday invitation styles"),
    )
    InvitationEventType.FUNERAL -> listOf(
        InvitationCreationStep(InvitationCreationStepKind.BASICS, "Basics", "Name, dates and gathering place"),
        InvitationCreationStep(InvitationCreationStepKind.DESIGN, "Template", "Respectful notice styles"),
    )
    else -> listOf(
        InvitationCreationStep(InvitationCreationStepKind.BASICS, "Basics", "Event details"),
        InvitationCreationStep(InvitationCreationStepKind.DESIGN, "Template", "Invitation style"),
    )
}

@Composable
private fun InvitationCreationProgress(
    currentStep: Int,
    steps: List<InvitationCreationStep>,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                steps.forEachIndexed { index, _ ->
                    val isDone = index < currentStep
                    val isCurrent = index == currentStep
                    Surface(
                        modifier = Modifier.size(if (isCurrent) 32.dp else 26.dp),
                        shape = CircleShape,
                        color = when {
                            isDone || isCurrent -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
                        },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(15.dp),
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(
                                    color = if (index < currentStep) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                ),
                        )
                    }
                }
            }
            Text(
                steps[currentStep].title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun InvitationMessageStep(
    draftDescription: String,
    onNoteChange: (String) -> Unit,
    generatedDescription: String,
    hasEditedDescription: Boolean,
    onResetSuggestion: () -> Unit,
) {
    var isEditing by remember { mutableStateOf(hasEditedDescription) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = draftDescription,
                    onValueChange = onNoteChange,
                    placeholder = { Text(generatedDescription) },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KazeSecondaryButton(
                    label = "Regenerate",
                    onClick = {
                        onResetSuggestion()
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.History,
                )
                KazeSecondaryButton(
                    label = if (isEditing) "Done" else "Edit manually",
                    onClick = { isEditing = !isEditing },
                    modifier = Modifier.weight(1f),
                    leadingIcon = if (isEditing) Icons.Default.CheckCircle else Icons.Default.Edit,
                )
            }
        }
    }
}

@Composable
private fun InlineTemplateGallery(
    eventType: InvitationEventType,
    selectedTheme: InvitationTheme,
    previewInvitation: InvitationPreview,
    onThemeSelected: (InvitationTheme) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Choose a template", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            themesForEventType(eventType).chunked(2).forEach { rowThemes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    rowThemes.forEach { theme ->
                        InvitationTemplateGalleryCard(
                            theme = theme,
                            previewInvitation = previewInvitation.copy(themeId = theme.id),
                            selected = selectedTheme.id == theme.id,
                            onClick = { onThemeSelected(theme) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowThemes.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
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
            .height(640.dp),
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
                    .background(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                        RoundedCornerShape(999.dp),
                    )
                    .padding(bottom = 2.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Choose design", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                KazeGhostButton(label = "Done", onClick = onDismiss)
            }
            val previewInvitation = InvitationPreview(
                title = when (eventType) {
                    InvitationEventType.WEDDING -> "Léa & David"
                    InvitationEventType.BIRTHDAY -> "Aline"
                    InvitationEventType.FUNERAL -> "Memorial"
                    else -> eventType.label
                },
                subtitle = when (eventType) {
                    InvitationEventType.WEDDING -> "16 Juin 2026 • Château de Versailles"
                    InvitationEventType.BIRTHDAY -> "Birthday celebration"
                    InvitationEventType.FUNERAL -> "In loving memory"
                    else -> "Invitation"
                },
                code = "",
                phoneLabel = "",
                statusLabel = "",
                themeId = selectedTheme.id,
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(themesForEventType(eventType), key = { it.id }) { theme ->
                    InvitationTemplateGalleryCard(
                        theme = theme,
                        previewInvitation = previewInvitation.copy(themeId = theme.id),
                        selected = selectedTheme.id == theme.id,
                        onClick = { onThemeSelected(theme) },
                    )
                }
            }
        }
    }
}

@Composable
private fun InvitationTemplateGalleryCard(
    theme: InvitationTheme,
    previewInvitation: InvitationPreview,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val aspectRatio = (theme as? InvitationTemplateContract)?.previewAspectRatio ?: 0.72f
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 1.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio),
        ) {
            theme.Cover(
                invitation = previewInvitation,
                isActive = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.04f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.44f),
                            ),
                        ),
                    ),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
            ) {
                Icon(
                    imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(16.dp),
                    tint = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.38f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if ((theme as? InvitationTemplateContract)?.acceptsPhoto == true) "Photo template" else "Static template",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.74f),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun EventTypeDropdownField(
    selectedType: InvitationEventType,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onEventTypeSelected: (InvitationEventType) -> Unit,
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
                    Text(selectedType.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    //Text(selectedType.eventTypeHint(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                }
                Icon(Icons.Default.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            creatableInvitationEventTypes.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(type.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                           // Text(type.eventTypeHint(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
                        }
                    },
                    onClick = { onEventTypeSelected(type) },
                )
            }
        }
    }
}

@Composable
private fun KazeSingleLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
            onDone = { focusManager.clearFocus() },
        ),
    )
}

@Composable
private fun EventDateSelectionField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val today = remember { InvitationCalendarDate.today() }
    val dateOptions = remember(today) { invitationFutureDateOptions(today) }
    val selectedDate = InvitationCalendarDate.parse(value)
    val displayValue = selectedDate?.displayLabel ?: "Select date"

    CompactSelectionField(
        label = label,
        value = displayValue,
        isPlaceholder = selectedDate == null,
        icon = Icons.Default.CalendarMonth,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            dateOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(option.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(option.supportingLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f))
                        }
                    },
                    onClick = {
                        onValueChange(option.date.iso)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun EventTimeSelectionField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val timeOptions = remember { invitationTimeOptions() }
    val displayValue = value.takeIf { it in timeOptions } ?: "Select time"

    CompactSelectionField(
        label = label,
        value = displayValue,
        isPlaceholder = value !in timeOptions,
        icon = Icons.Default.AccessTime,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            timeOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun CompactSelectionField(
    label: String,
    value: String,
    isPlaceholder: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    menuContent: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(true) },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPlaceholder) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                )
            }
        }
        menuContent()
    }
}

@Composable
private fun EventIdentityFields(
    eventType: InvitationEventType,
    brideName: String,
    onBrideNameChange: (String) -> Unit,
    //brideGender: String,
    //onBrideGenderChange: (String) -> Unit,
    groomName: String,
    onGroomNameChange: (String) -> Unit,
    //groomGender: String,
    //onGroomGenderChange: (String) -> Unit,
    birthdayName: String,
    onBirthdayNameChange: (String) -> Unit,
    birthdayPlanningMode: String,
    onBirthdayPlanningModeChange: (String) -> Unit,
    funeralName: String,
    onFuneralNameChange: (String) -> Unit,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    deathDate: String,
    onDeathDateChange: (String) -> Unit,
) {
    when (eventType) {
        InvitationEventType.WEDDING -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KazeSingleLineTextField(
                    value = brideName,
                    onValueChange = onBrideNameChange,
                    label = "Bride name",
                    placeholder = "Divine",
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
                KazeSingleLineTextField(
                    value = groomName,
                    onValueChange = onGroomNameChange,
                    label = "Groom name",
                    placeholder = "Oreste",
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
            }
            /*FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                genderOptions.forEach { gender ->
                    SelectablePill(
                        label = "Bride: $gender",
                        selected = brideGender == gender,
                        onClick = { onBrideGenderChange(gender) },
                    )
                    SelectablePill(
                        label = "Groom: $gender",
                        selected = groomGender == gender,
                        onClick = { onGroomGenderChange(gender) },
                    )
                }
            }*/
        }
        InvitationEventType.BIRTHDAY -> {
            KazeSingleLineTextField(
                value = birthdayName,
                onValueChange = onBirthdayNameChange,
                label = "Birthday person",
                placeholder = "Aline",
                imeAction = ImeAction.Next,
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                birthdayPlanningModes.forEach { mode ->
                    SelectablePill(
                        label = mode,
                        selected = birthdayPlanningMode == mode,
                        onClick = { onBirthdayPlanningModeChange(mode) },
                    )
                }
            }
        }
        InvitationEventType.FUNERAL -> {
            KazeSingleLineTextField(
                value = funeralName,
                onValueChange = onFuneralNameChange,
                label = "Full name",
                placeholder = "Jean Baptiste",
                imeAction = ImeAction.Next,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KazeSingleLineTextField(
                    value = birthDate,
                    onValueChange = onBirthDateChange,
                    label = "Birth date",
                    placeholder = "1954",
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
                KazeSingleLineTextField(
                    value = deathDate,
                    onValueChange = onDeathDateChange,
                    label = "Death date",
                    placeholder = "2026",
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        InvitationEventType.MEETING,
        InvitationEventType.CONFERENCE,
        InvitationEventType.OTHER -> Unit
    }
}

@Composable
private fun InvitationPhotoCard(
    selectedPhotoLabel: String,
    onSelectPhoto: () -> Unit,
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
                Text("Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (selectedPhotoLabel.isNotBlank()) {
                    Text(
                        selectedPhotoLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                }
            }
            KazeSecondaryButton(
                label = "Upload",
                onClick = onSelectPhoto,
                leadingIcon = Icons.Default.PhotoCamera,
            )
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

private fun InvitationEventType.defaultTitle(): String = when (this) {
    InvitationEventType.WEDDING -> "Uwase x Iradukunda"
    InvitationEventType.BIRTHDAY -> "Aline's birthday"
    InvitationEventType.FUNERAL -> "Memorial service"
    InvitationEventType.MEETING -> "Team strategy meeting"
    InvitationEventType.CONFERENCE -> "Kigali business summit"
    InvitationEventType.OTHER -> "Private event"
}

private fun InvitationEventType.draftTitle(
    brideName: String,
    groomName: String,
    birthdayName: String,
    funeralName: String,
): String = when (this) {
    InvitationEventType.WEDDING -> listOf(brideName, groomName)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString(" & ")
        .ifBlank { "Wedding invitation" }
    InvitationEventType.BIRTHDAY -> birthdayName.trim().ifBlank { "Birthday celebration" }
    InvitationEventType.FUNERAL -> funeralName.trim().ifBlank { "Memorial invitation" }
    InvitationEventType.MEETING,
    InvitationEventType.CONFERENCE,
    InvitationEventType.OTHER -> defaultTitle()
}
/*
private fun InvitationEventType.eventTypeHint(): String = when (this) {
    InvitationEventType.WEDDING -> "Two names, venue, RSVP and photo template."
    InvitationEventType.BIRTHDAY -> "One person, simple celebration page and RSVP."
    InvitationEventType.FUNERAL -> "Name, life dates, gathering place and respectful notice."
    InvitationEventType.MEETING -> "Meeting access and venue details."
    InvitationEventType.CONFERENCE -> "Conference access and public details."
    InvitationEventType.OTHER -> "General invitation."
}*/

private fun InvitationEventType.generatedDescriptionSuggestion(
    brideName: String,
    groomName: String,
    //brideGender: String,
    //groomGender: String,
    birthdayName: String,
    birthdayPlanningMode: String,
    funeralName: String,
    birthDate: String,
    deathDate: String,
    eventDate: String,
    eventTime: String,
    venueName: String,
): String {
    val schedule = invitationScheduleText(eventDate, eventTime)
    val venue = venueName.trim().takeIf { it.isNotBlank() }?.let { " at $it" }.orEmpty()
    return when (this) {
        InvitationEventType.WEDDING -> {
            val couple = listOf(brideName, groomName).map { it.trim() }.filter { it.isNotBlank() }.joinToString(" and ").ifBlank { "the couple" }
            "$couple, two hearts choosing the same road, begin a new chapter shaped by tenderness, laughter, and a love meant to be witnessed."
        }
        InvitationEventType.BIRTHDAY -> {
            val person = birthdayName.trim().ifBlank { "our guest of honor" }
            val surprise = if (birthdayPlanningMode == "For a friend") "A warm surprise, a bright smile, and a day made for joy." else "A bright day for laughter, music, and joy."
            "$person. $surprise $schedule$venue"
        }
        InvitationEventType.FUNERAL -> {
            val person = funeralName.trim().ifBlank { "our loved one" }
            val dates = listOf(birthDate.trim(), deathDate.trim()).filter { it.isNotBlank() }.joinToString(" - ")
            val lifeDates = if (dates.isBlank()) "" else " ($dates)"
            "In memory of $person$lifeDates, a life held with love, gratitude, and quiet remembrance. $schedule$venue"
        }
        InvitationEventType.MEETING,
        InvitationEventType.CONFERENCE,
        InvitationEventType.OTHER -> defaultSubtitle(
            event = invitationEventOptions(this).first(),
            date = eventDate,
            time = eventTime,
        )
    }.replace("  ", " ").trim()
}

private fun InvitationEventType.templateDetailsSubtitle(
    eventDate: String,
    eventTime: String,
    venueName: String,
    birthDate: String,
    deathDate: String,
): String {
    val dateTime = listOf(eventDate.trim(), eventTime.trim()).filter { it.isNotBlank() }.joinToString(" • ")
    val venue = venueName.trim()
    return when (this) {
        InvitationEventType.WEDDING -> listOf(dateTime, venue).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Wedding invitation" }
        InvitationEventType.BIRTHDAY -> listOf(dateTime, venue).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Birthday invitation" }
        InvitationEventType.FUNERAL -> {
            val dates = listOf(birthDate.trim(), deathDate.trim()).filter { it.isNotBlank() }.joinToString(" - ")
            listOf(dates, venue, dateTime).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Memorial invitation" }
        }
        InvitationEventType.MEETING,
        InvitationEventType.CONFERENCE,
        InvitationEventType.OTHER -> listOf(dateTime, venue).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Invitation" }
    }
}

private fun InvitationEventType.defaultSubtitle(
    event: InvitationEventOption,
    date: String,
    time: String,
): String {
    val schedule = invitationScheduleText(date, time)
    val base = when (this) {
        InvitationEventType.WEDDING -> if (event.needsOrganizerApproval) {
            "Awaiting organizer approval."
        } else {
            "A love story, a promise, and the beginning of a shared forever."
        }
        InvitationEventType.BIRTHDAY -> "A bright day for laughter, music, and joy."
        InvitationEventType.FUNERAL -> "A quiet gathering in remembrance."
        InvitationEventType.MEETING -> "Meeting details."
        InvitationEventType.CONFERENCE -> "Conference details."
        InvitationEventType.OTHER -> "Event details."
    }
    return if (schedule.isBlank()) base else "$base $schedule"
}

private fun invitationScheduleText(date: String, time: String): String = when {
    date.isNotBlank() && time.isNotBlank() -> "$date • $time"
    date.isNotBlank() -> date
    time.isNotBlank() -> time
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
        InvitationEventType.FUNERAL -> listOf(
        InvitationEventOption("new_funeral", "Create a funeral event", "A respectful notice for family and friends.", false, true),
        InvitationEventOption("memorial_service", "Memorial service", "Existing memorial invitation.", false),
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

private val creatableInvitationEventTypes = listOf(
    InvitationEventType.WEDDING,
    InvitationEventType.BIRTHDAY,
    InvitationEventType.FUNERAL,
)

private val genderOptions = listOf("Female", "Male")

private val birthdayPlanningModes = listOf("For me", "For a friend")

private data class InvitationDateOption(
    val date: InvitationCalendarDate,
    val label: String,
    val supportingLabel: String,
)

private data class InvitationCalendarDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    val iso: String = "${year.toString().padStart(4, '0')}-${month.inviteTwoDigits()}-${day.inviteTwoDigits()}"
    val displayLabel: String = "${day.inviteTwoDigits()} ${month.inviteMonthShortLabel()} $year"
    val weekdayLabel: String = inviteWeekdayName(dayOfWeekIndex())

    fun plusDays(days: Int): InvitationCalendarDate {
        var nextYear = year
        var nextMonth = month
        var nextDay = day + days
        while (nextDay > inviteDaysInMonth(nextYear, nextMonth)) {
            nextDay -= inviteDaysInMonth(nextYear, nextMonth)
            nextMonth += 1
            if (nextMonth > 12) {
                nextMonth = 1
                nextYear += 1
            }
        }
        return InvitationCalendarDate(nextYear, nextMonth, nextDay)
    }

    fun plusMonths(months: Int): InvitationCalendarDate {
        val monthIndex = (year * 12 + (month - 1)) + months
        val newYear = monthIndex / 12
        val newMonth = monthIndex % 12 + 1
        val newDay = day.coerceAtMost(inviteDaysInMonth(newYear, newMonth))
        return InvitationCalendarDate(newYear, newMonth, newDay)
    }

    private fun dayOfWeekIndex(): Int {
        var adjustedMonth = month
        var adjustedYear = year
        if (adjustedMonth < 3) {
            adjustedMonth += 12
            adjustedYear -= 1
        }
        val k = adjustedYear % 100
        val j = adjustedYear / 100
        val h = (day + ((13 * (adjustedMonth + 1)) / 5) + k + (k / 4) + (j / 4) + (5 * j)) % 7
        return (h + 5) % 7
    }

    companion object {
        fun today(): InvitationCalendarDate =
            parse(Clock.System.now().toString().substringBefore("T")) ?: InvitationCalendarDate(2026, 6, 25)

        fun parse(value: String): InvitationCalendarDate? {
            val parts = value.split("-")
            if (parts.size != 3) return null
            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull() ?: return null
            val day = parts[2].toIntOrNull() ?: return null
            if (month !in 1..12 || day !in 1..inviteDaysInMonth(year, month)) return null
            return InvitationCalendarDate(year, month, day)
        }
    }
}

private fun invitationFutureDateOptions(today: InvitationCalendarDate): List<InvitationDateOption> {
    val nearDates = (0..14).map { today.plusDays(it) }
    val monthDates = (1..6).map { today.plusMonths(it) }
    return (nearDates + monthDates)
        .distinctBy { it.iso }
        .mapIndexed { index, date ->
            val label = when (index) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> date.displayLabel
            }
            InvitationDateOption(
                date = date,
                label = label,
                supportingLabel = "${date.weekdayLabel} • ${date.iso}",
            )
        }
}

private fun invitationTimeOptions(): List<String> =
    (6..23).flatMap { hour ->
        listOf(0, 30).map { minute -> "${hour.inviteTwoDigits()}:${minute.inviteTwoDigits()}" }
    }

private fun Int.inviteTwoDigits(): String = toString().padStart(2, '0')

private fun Int.inviteMonthShortLabel(): String =
    when (this) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> toString()
    }

private fun inviteWeekdayName(index: Int): String =
    when (index) {
        0 -> "Mon"
        1 -> "Tue"
        2 -> "Wed"
        3 -> "Thu"
        4 -> "Fri"
        5 -> "Sat"
        else -> "Sun"
    }

private fun inviteDaysInMonth(year: Int, month: Int): Int =
    when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year.inviteIsLeapYear()) 29 else 28
        else -> 30
    }

private fun Int.inviteIsLeapYear(): Boolean =
    this % 4 == 0 && (this % 100 != 0 || this % 400 == 0)

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
    title: String = "No invitations yet",
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.MarkEmailUnread,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                }
            }
        }
    }
}
