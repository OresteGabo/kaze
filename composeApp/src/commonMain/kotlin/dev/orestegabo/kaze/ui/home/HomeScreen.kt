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
import dev.orestegabo.kaze.presentation.auth.ReservationDraftSubmissionRequest
import dev.orestegabo.kaze.presentation.auth.ReservationResponse
import dev.orestegabo.kaze.presentation.app.KazeSavedPlace
import dev.orestegabo.kaze.ui.home.components.*
import dev.orestegabo.kaze.ui.states.KazeEmptyStateScreen
import dev.orestegabo.kaze.ui.stay.LateCheckoutScreen
import dev.orestegabo.kaze.ui.stay.ServiceRequestScreen
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.k_mark_raster

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    hotelDisplayName: String,
    guestName: String,
    assignedRoomLabel: String,
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
    reservationRequests: List<ReservationResponse>,
    savedPlaces: List<KazeSavedPlace>,
    personalEventCount: Int,
    suggestedEventCount: Int,
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
    onSeeEvents: () -> Unit,
    onBrowseVenues: () -> Unit,
    onToggleSavedPlace: (KazeSavedPlace) -> Unit,
    onSubmitReservation: suspend (ReservationDraftSubmissionRequest) -> ReservationResponse,
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
                assignedRoomLabel = assignedRoomLabel,
                bottomContentPadding = bottomContentPadding,
            )
            return@BoxWithConstraints
        }

        selectedServiceQuery?.let { serviceQuery ->
            HomeServiceDetailScreen(
                serviceQuery = serviceQuery,
                bottomContentPadding = bottomContentPadding,
                savedPlaceIds = savedPlaces.map { it.id }.toSet(),
                onBack = { selectedServiceQuery = null },
                onToggleSavedPlace = onToggleSavedPlace,
                onSubmitReservation = onSubmitReservation,
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
                val hasPersonalContent = accessContexts.isNotEmpty() ||
                    accessCard != null ||
                    invitations.isNotEmpty() ||
                    savedPlaces.isNotEmpty() ||
                    personalEventCount > 0
                if (!hasPersonalContent && reservationRequests.isEmpty()) {
                    KazeEmptyStateScreen(
                        modifier = Modifier.fillMaxWidth(),
                        title = if (suggestedEventCount > 0) "No personal events yet" else "No passes or events yet",
                        subtitle = if (suggestedEventCount > 0) {
                            "You have public event suggestions waiting in Events. Joined events, invitations, passes, and stay access will appear here."
                        } else {
                            "Browse venues or send a reservation request first. Your pass, invitations, events, and stay access will appear here after that."
                        },
                        actionLabel = if (suggestedEventCount > 0) "View events" else "Browse venues",
                        eyebrow = "Home",
                        tags = if (suggestedEventCount > 0) listOf("Suggestions", "Events", "Pass") else listOf("Venues", "Reservations", "Pass"),
                        brandIcon = Res.drawable.k_mark_raster,
                        onAction = {
                            if (suggestedEventCount > 0) {
                                onSeeEvents()
                            } else {
                                onBrowseVenues()
                            }
                        },
                    )
                } else if (hasPersonalContent) {
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
                    onBrowseVenues = onBrowseVenues,
                )
            }

            if (!isGuestMode && isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(modifier = Modifier.weight(1.12f)) {
                        HomeHeroCard(
                            onBrowseVenues = onBrowseVenues,
                            onSeeEvents = onSeeEvents,
                        )
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
                HomeHeroCard(
                    onBrowseVenues = onBrowseVenues,
                    onSeeEvents = onSeeEvents,
                )
                CodeEntryCard(
                    code = joinCode,
                    onCodeChange = { joinCode = it.uppercase() },
                    onSubmit = { onEnterCode(joinCode) },
                )
            }

            if (!isGuestMode) {
                ReservationRequestsSection(
                    reservations = reservationRequests,
                )
            }

            SavedPlacesSection(
                savedPlaces = savedPlaces,
            )

            HomeServiceRail(
                onOpenService = { serviceQuery -> selectedServiceQuery = serviceQuery },
            )

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
