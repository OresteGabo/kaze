package dev.orestegabo.kaze.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.domain.DigitalAccessCard
import dev.orestegabo.kaze.presentation.demo.AccessContextUi
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.presentation.demo.LateCheckoutDraft
import dev.orestegabo.kaze.presentation.demo.LateCheckoutRequest
import dev.orestegabo.kaze.presentation.demo.ServiceRequestDraftUi
import dev.orestegabo.kaze.presentation.demo.ServiceRequestRecord
import dev.orestegabo.kaze.presentation.demo.StayMoment
import dev.orestegabo.kaze.presentation.demo.StayPrimaryAction
import dev.orestegabo.kaze.presentation.demo.StayScreen
import dev.orestegabo.kaze.ui.home.components.*
import dev.orestegabo.kaze.ui.states.KazeEmptyStateScreen
import dev.orestegabo.kaze.ui.stay.LateCheckoutScreen
import dev.orestegabo.kaze.ui.stay.ServiceRequestScreen

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    hotelDisplayName: String,
    guestName: String,
    accessProfileLabel: String,
    accessStatusLabel: String,
    accessCard: DigitalAccessCard?,
    accessContexts: List<AccessContextUi>,
    selectedAccessContextId: String?,
    stayMoments: List<StayMoment>,
    activeStayScreen: StayScreen,
    lateCheckoutRequest: LateCheckoutRequest?,
    lateCheckoutDraft: LateCheckoutDraft,
    serviceRequestDraft: ServiceRequestDraftUi,
    submittedServiceRequests: List<ServiceRequestRecord>,
    invitations: List<InvitationPreview>,
    isGuestMode: Boolean,
    onBackToStayHome: () -> Unit,
    onLateCheckoutDraftChange: (LateCheckoutDraft) -> Unit,
    onLateCheckoutSubmit: (LateCheckoutDraft) -> Unit,
    onServiceRequestDraftChange: (ServiceRequestDraftUi) -> Unit,
    onServiceRequestSubmit: (ServiceRequestDraftUi) -> Unit,
    onAccessContextSelected: (String) -> Unit,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
    onEnterCode: (String) -> Unit,
    onOpenInvitation: (InvitationPreview) -> Unit,
    onSeeAllInvitations: () -> Unit,
    bottomContentPadding: Dp = 20.dp,
) {
    var joinCode by rememberSaveable { mutableStateOf("") }
    var selectedServiceQuery by rememberSaveable { mutableStateOf<String?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 860.dp
        val contentMaxWidth = if (isExpanded) 1180.dp else androidx.compose.ui.unit.Dp.Unspecified
        val scrollState = rememberScrollState()

        if (activeStayScreen == StayScreen.LATE_CHECKOUT) {
            LateCheckoutScreen(
                modifier = modifier,
                draft = lateCheckoutDraft,
                existingRequest = lateCheckoutRequest,
                onBack = onBackToStayHome,
                onDraftChange = onLateCheckoutDraftChange,
                onSubmit = { onLateCheckoutSubmit(lateCheckoutDraft) },
                bottomContentPadding = bottomContentPadding,
            )
            return@BoxWithConstraints
        }

        if (activeStayScreen == StayScreen.SERVICE_REQUEST) {
            ServiceRequestScreen(
                modifier = modifier,
                draft = serviceRequestDraft,
                onBack = onBackToStayHome,
                onDraftChange = onServiceRequestDraftChange,
                onSubmit = { onServiceRequestSubmit(serviceRequestDraft) },
                bottomContentPadding = bottomContentPadding,
            )
            return@BoxWithConstraints
        }

        selectedServiceQuery?.let { serviceQuery ->
            HomeServiceDetailScreen(
                serviceQuery = serviceQuery,
                bottomContentPadding = bottomContentPadding,
                onBack = { selectedServiceQuery = null },
            )
            return@BoxWithConstraints
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding)
                .then(if (contentMaxWidth != androidx.compose.ui.unit.Dp.Unspecified) Modifier.fillMaxWidth() else Modifier),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeTopBar()

            if (!isGuestMode) {
                if (accessContexts.isEmpty() && accessCard == null && invitations.isEmpty()) {
                    KazeEmptyStateScreen(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Nothing here yet",
                        subtitle = "Your pass, invitations, and event access will appear once you join something in Kaze.",
                        actionLabel = "Open invites",
                        eyebrow = "Home",
                        tags = listOf("Pass", "Invites", "Access"),
                        icon = Icons.Outlined.BookmarkBorder,
                        onAction = onSeeAllInvitations,
                    )
                } else {
                    HomeStayDashboard(
                        hotelDisplayName = hotelDisplayName,
                        guestName = guestName,
                        accessProfileLabel = accessProfileLabel,
                        accessStatusLabel = accessStatusLabel,
                        accessCard = accessCard,
                        accessContexts = accessContexts,
                        selectedAccessContextId = selectedAccessContextId,
                        stayMoments = stayMoments,
                        activeRequestCount = submittedServiceRequests.size + if (lateCheckoutRequest != null) 1 else 0,
                        onAccessContextSelected = onAccessContextSelected,
                        onPrimaryAction = onPrimaryAction,
                    )
                }
            } else {
                GuestHomeShowcase(
                    invitations = invitations,
                    code = joinCode,
                    onCodeChange = { joinCode = it.uppercase() },
                    onSubmitCode = { onEnterCode(joinCode) },
                    onOpenInvitations = onSeeAllInvitations,
                )
            }

            if (!isGuestMode && isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(modifier = Modifier.weight(1.12f)) {
                        HomeHeroCard()
                    }
                    Box(modifier = Modifier.weight(0.82f)) {
                        CodeEntryCard(
                            code = joinCode,
                            onCodeChange = { joinCode = it.uppercase() },
                            onSubmit = { onEnterCode(joinCode) },
                        )
                    }
                }
            } else if (!isGuestMode) {
                HomeHeroCard()
                CodeEntryCard(
                    code = joinCode,
                    onCodeChange = { joinCode = it.uppercase() },
                    onSubmit = { onEnterCode(joinCode) },
                )
            }

            if (invitations.isNotEmpty()) {
                InvitationSection(
                    invitations = invitations,
                    onOpenInvitation = onOpenInvitation,
                    onSeeAll = onSeeAllInvitations,
                )
            }
        }
    }
}
