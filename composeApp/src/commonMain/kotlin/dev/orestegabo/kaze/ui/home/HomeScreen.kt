package dev.orestegabo.kaze.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import dev.orestegabo.kaze.presentation.demo.ExploreHighlight
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.presentation.demo.LateCheckoutDraft
import dev.orestegabo.kaze.presentation.demo.LateCheckoutRequest
import dev.orestegabo.kaze.presentation.demo.PublicVenuePreview
import dev.orestegabo.kaze.presentation.demo.ServiceRequestDraftUi
import dev.orestegabo.kaze.presentation.demo.ServiceRequestRecord
import dev.orestegabo.kaze.presentation.demo.StayMoment
import dev.orestegabo.kaze.presentation.demo.StayPrimaryAction
import dev.orestegabo.kaze.presentation.demo.StayScreen
import dev.orestegabo.kaze.presentation.demo.VenueCategoryPreview
import dev.orestegabo.kaze.ui.home.components.*
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
    suggestionActivities: List<ExploreHighlight>,
    activeStayScreen: StayScreen,
    lateCheckoutRequest: LateCheckoutRequest?,
    lateCheckoutDraft: LateCheckoutDraft,
    serviceRequestDraft: ServiceRequestDraftUi,
    submittedServiceRequests: List<ServiceRequestRecord>,
    venueCategories: List<VenueCategoryPreview>,
    featuredVenues: List<PublicVenuePreview>,
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
    onOpenCategory: (VenueCategoryPreview) -> Unit,
    onOpenVenue: (PublicVenuePreview) -> Unit,
    onOpenVenueMap: (PublicVenuePreview) -> Unit,
    onOpenInvitation: (InvitationPreview) -> Unit,
    onSeeAllInvitations: () -> Unit,
    bottomContentPadding: Dp = 20.dp,
) {
    var joinCode by rememberSaveable { mutableStateOf("") }
    var selectedServiceQuery by rememberSaveable { mutableStateOf<String?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 860.dp
        val contentMaxWidth = if (isExpanded) 1180.dp else androidx.compose.ui.unit.Dp.Unspecified
        val categoryColumns = if (maxWidth >= 1160.dp) 4 else if (isExpanded) 2 else 1
        val venueColumns = if (maxWidth >= 1180.dp) 3 else if (isExpanded) 2 else 1
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
                HomeStayDashboard(
                    hotelDisplayName = hotelDisplayName,
                    guestName = guestName,
                    accessProfileLabel = accessProfileLabel,
                    accessStatusLabel = accessStatusLabel,
                    accessCard = accessCard,
                    accessContexts = accessContexts,
                    selectedAccessContextId = selectedAccessContextId,
                    stayMoments = stayMoments,
                    suggestionActivities = suggestionActivities,
                    activeRequestCount = submittedServiceRequests.size + if (lateCheckoutRequest != null) 1 else 0,
                    onAccessContextSelected = onAccessContextSelected,
                    onPrimaryAction = onPrimaryAction,
                )
            } else {
                GuestPassPreviewCard(
                    onOpenInvitations = onSeeAllInvitations,
                )
            }

            if (isExpanded) {
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
            } else {
                HomeHeroCard()
                CodeEntryCard(
                    code = joinCode,
                    onCodeChange = { joinCode = it.uppercase() },
                    onSubmit = { onEnterCode(joinCode) },
                )
            }

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

            SectionLabel("Browse venue types")

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = categoryColumns,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                venueCategories.forEach { category ->
                    val cardModifier = if (categoryColumns == 1) Modifier.fillMaxWidth() else Modifier.weight(1f)
                    VenueCategoryCard(
                        category = category,
                        onClick = { onOpenCategory(category) },
                        modifier = cardModifier,
                    )
                }
            }

            SectionLabel("Featured venues")

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = venueColumns,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                featuredVenues.forEach { venue ->
                    val cardModifier = if (venueColumns == 1) Modifier.fillMaxWidth() else Modifier.weight(1f)
                    PublicVenueCard(
                        venue = venue,
                        onClick = { onOpenVenue(venue) },
                        onOpenMap = { onOpenVenueMap(venue) },
                        modifier = cardModifier,
                    )
                }
            }
        }
    }
}
