package dev.orestegabo.kaze.ui.stay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.orestegabo.kaze.presentation.demo.ExploreHighlight
import dev.orestegabo.kaze.presentation.demo.FollowUpOption
import dev.orestegabo.kaze.presentation.demo.LateCheckoutDraft
import dev.orestegabo.kaze.presentation.demo.LateCheckoutRequest
import dev.orestegabo.kaze.presentation.demo.ServiceOption
import dev.orestegabo.kaze.presentation.demo.ServiceRequestDraftUi
import dev.orestegabo.kaze.presentation.demo.ServiceRequestRecord
import dev.orestegabo.kaze.presentation.demo.StayMoment
import dev.orestegabo.kaze.presentation.demo.StayPrimaryAction
import dev.orestegabo.kaze.presentation.demo.StayScreen
import dev.orestegabo.kaze.presentation.demo.StayTab
import dev.orestegabo.kaze.presentation.demo.followUpOptions
import dev.orestegabo.kaze.presentation.demo.lateCheckoutOptions
import dev.orestegabo.kaze.presentation.demo.paymentOptions
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.access.StayAccessCardSection
import dev.orestegabo.kaze.ui.components.HighlightPanel
import dev.orestegabo.kaze.ui.components.InfoToken
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.components.SectionHeader
import dev.orestegabo.kaze.ui.components.SectionIntroCard
import dev.orestegabo.kaze.ui.components.SelectableInfoCard
import dev.orestegabo.kaze.domain.DigitalAccessCard
import androidx.compose.foundation.layout.IntrinsicSize

@Composable
internal fun StayHomeScreen(
    modifier: Modifier = Modifier,
    hotelDisplayName: String,
    guestName: String,
    accessProfileLabel: String,
    accessStatusLabel: String,
    accessCard: DigitalAccessCard?,
    stayMoments: List<StayMoment>,
    requestOptions: List<ServiceOption>,
    suggestionActivities: List<ExploreHighlight>,
    selectedTab: StayTab,
    activeStayScreen: StayScreen,
    lateCheckoutRequest: LateCheckoutRequest?,
    lateCheckoutDraft: LateCheckoutDraft,
    serviceRequestDraft: ServiceRequestDraftUi,
    submittedServiceRequests: List<ServiceRequestRecord>,
    onTabChange: (StayTab) -> Unit,
    onBackToStayHome: () -> Unit,
    onLateCheckoutDraftChange: (LateCheckoutDraft) -> Unit,
    onLateCheckoutSubmit: (LateCheckoutDraft) -> Unit,
    onServiceRequestDraftChange: (ServiceRequestDraftUi) -> Unit,
    onServiceRequestSubmit: (ServiceRequestDraftUi) -> Unit,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    if (activeStayScreen == StayScreen.LATE_CHECKOUT) {
        LateCheckoutScreen(
            modifier = modifier,
            draft = lateCheckoutDraft,
            existingRequest = lateCheckoutRequest,
            onBack = onBackToStayHome,
            onDraftChange = onLateCheckoutDraftChange,
            onSubmit = { onLateCheckoutSubmit(lateCheckoutDraft) },
        )
        return
    }

    if (activeStayScreen == StayScreen.SERVICE_REQUEST) {
        ServiceRequestScreen(
            modifier = modifier,
            draft = serviceRequestDraft,
            onBack = onBackToStayHome,
            onDraftChange = onServiceRequestDraftChange,
            onSubmit = { onServiceRequestSubmit(serviceRequestDraft) },
        )
        return
    }

    val activeRequestCount = submittedServiceRequests.size + if (lateCheckoutRequest != null) 1 else 0

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        val pagerState = rememberPagerState(initialPage = selectedTab.ordinal, pageCount = { StayTab.entries.size })

        LaunchedEffect(selectedTab) {
            if (pagerState.currentPage != selectedTab.ordinal) pagerState.animateScrollToPage(selectedTab.ordinal)
        }
        LaunchedEffect(pagerState.currentPage) {
            val pagerTab = StayTab.entries[pagerState.currentPage]
            if (pagerTab != selectedTab) onTabChange(pagerTab)
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
            CompactStayHeader(
                hotelName = hotelDisplayName,
                guestName = guestName,
                roomLabel = accessProfileLabel,
                stayLabel = accessStatusLabel,
            )
        }
        Spacer(Modifier.height(8.dp))
        StaySegmentedTabs(
            selectedTab = selectedTab,
            onTabChange = onTabChange,
            requestsBadgeCount = activeRequestCount,
        )
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (StayTab.entries[page]) {
                    StayTab.MY_STAY -> StayTabContent(
                        accessCard = accessCard,
                        stayMoments = stayMoments,
                        onPrimaryAction = onPrimaryAction,
                    )
                    StayTab.REQUESTS -> ServiceRequestsTab(
                        requestOptions = requestOptions,
                        lateCheckoutRequest = lateCheckoutRequest,
                        submittedServiceRequests = submittedServiceRequests,
                        onPrimaryAction = onPrimaryAction,
                    )
                    StayTab.SUGGESTIONS -> SuggestedActivitiesTab(
                        suggestionActivities = suggestionActivities,
                        onPrimaryAction = onPrimaryAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun StaySegmentedTabs(
    selectedTab: StayTab,
    onTabChange: (StayTab) -> Unit,
    requestsBadgeCount: Int = 0,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StayTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val itemShape = RoundedCornerShape(18.dp)
                Column(
                    modifier = Modifier
                        .clip(itemShape)
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                    ),
                                )
                            },
                        )
                        .clickable { onTabChange(tab) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(if (selected) 28.dp else 18.dp)
                            .height(if (selected) 4.dp else 3.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.74f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                            ),
                    )
                    Text(
                        tab.label,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                    )
                    if (tab == StayTab.REQUESTS && requestsBadgeCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = if (selected) 0.92f else 0.18f),
                        ) {
                            Text(
                                text = requestsBadgeCount.coerceAtMost(9).toString(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StayTabContent(
    accessCard: DigitalAccessCard?,
    stayMoments: List<StayMoment>,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        accessCard?.let { StayAccessCardSection(card = it) }
        StayStatusHero(onOpenRoute = { onPrimaryAction(StayPrimaryAction.OPEN_ROUTE) }, onViewFolio = { onPrimaryAction(StayPrimaryAction.VIEW_FOLIO) })
        stayMoments.forEach { moment -> StayMomentCard(moment = moment, onOpen = { onPrimaryAction(StayPrimaryAction.OpenStayMoment(moment)) }) }
    }
}

@Composable
private fun ServiceRequestsTab(
    requestOptions: List<ServiceOption>,
    lateCheckoutRequest: LateCheckoutRequest?,
    submittedServiceRequests: List<ServiceRequestRecord>,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (lateCheckoutRequest != null) {
            LateCheckoutStatusCard(request = lateCheckoutRequest, onEdit = { onPrimaryAction(StayPrimaryAction.REQUEST_LATE_CHECKOUT) })
        }
        if (submittedServiceRequests.isNotEmpty()) {
            SectionHeader("Recent requests")
            submittedServiceRequests.forEach { request ->
                ServiceRequestHistoryCard(request = request)
            }
        }
        SectionIntroCard(
            eyebrow = "Requests",
            title = "Hotel services",
            subtitle = "Ask for support without calling the desk. Requests stay visible and easy to track.",
            eyebrowColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
        )
        requestOptions.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { option ->
                    ServiceOptionCard(
                        option = option,
                        hasExistingLateCheckout = lateCheckoutRequest != null,
                        onClick = {
                            if (option.title == "Late checkout") {
                                onPrimaryAction(StayPrimaryAction.REQUEST_LATE_CHECKOUT)
                            } else {
                                onPrimaryAction(StayPrimaryAction.RequestService(option))
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ServiceOptionCard(
    option: ServiceOption,
    hasExistingLateCheckout: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = when (option.title) {
        "Fresh towels" -> KazeTheme.accents.editorialBotanical
        "In-room dining" -> KazeTheme.accents.editorialWarm
        "Laundry pickup" -> KazeTheme.accents.editorialClay
        "Concierge help" -> MaterialTheme.colorScheme.primary
        "Late checkout" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(accentColor.copy(alpha = 0.82f)),
                    )
                    Text(
                        option.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Text(
                option.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
            )
            KazeSecondaryButton(
                label = if (option.title == "Late checkout" && hasExistingLateCheckout) "Edit request" else "Request",
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ServiceRequestHistoryCard(request: ServiceRequestRecord) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF2E8B57).copy(alpha = 0.14f),
                    ) {
                        Box(
                            modifier = Modifier.size(30.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E8B57),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Text(request.option.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            request.requestedAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                        )
                    }
                }
                MetaPill(request.status)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (request.option.title == "Fresh towels" && request.quantity > 0) {
                    InfoToken(label = "Qty ${request.quantity}", accentColor = MaterialTheme.colorScheme.tertiary)
                }
            }
            if (request.locationNote.isNotBlank()) {
                Text(
                    "Location: ${request.locationNote}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                )
            }
            if (request.notes.isNotBlank()) {
                Text(
                    request.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun LateCheckoutStatusCard(request: LateCheckoutRequest, onEdit: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF2E8B57).copy(alpha = 0.14f),
                    ) {
                        Box(
                            modifier = Modifier.size(30.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E8B57),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Text("Late checkout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${request.option.checkoutTimeLabel} • ${request.option.feeLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                        )
                    }
                }
                MetaPill(request.status)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfoToken(label = request.paymentOption.label, accentColor = MaterialTheme.colorScheme.secondary)
                InfoToken(label = request.followUpOption.label, accentColor = MaterialTheme.colorScheme.primary)
            }
            if (request.notes.isNotBlank()) {
                Text(
                    request.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            KazeSecondaryButton(label = "Edit request", onClick = onEdit)
        }
    }
}

@Composable
private fun LateCheckoutScreen(
    modifier: Modifier = Modifier,
    draft: LateCheckoutDraft,
    existingRequest: LateCheckoutRequest?,
    onBack: () -> Unit,
    onDraftChange: (LateCheckoutDraft) -> Unit,
    onSubmit: () -> Unit,
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                KazeGhostButton(label = "Back", onClick = onBack)
                if (existingRequest != null) MetaPill(existingRequest.status,)
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Request late checkout", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Standard checkout is April 6, 2026 at 10:00.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    )
                    Text(
                        "Availability depends on occupancy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                    )
                }
            }
        }
        item { SectionHeader("Choose your new checkout time") }
        items(lateCheckoutOptions) { option ->
            SelectableInfoCard(
                selected = draft.option == option,
                title = option.checkoutTimeLabel,
                subtitle = "${option.feeLabel} • ${option.availabilityLabel}",
                supporting = option.summary,
                onSelect = { onDraftChange(draft.copy(option = option)) },
            )
        }
        item { SectionHeader("Choose payment preference") }
        items(paymentOptions.toList()) { payment ->
            SelectableInfoCard(
                selected = draft.paymentOption == payment,
                title = payment.label,
                subtitle = payment.confirmationLabel,
                supporting = payment.description,
                onSelect = { onDraftChange(draft.copy(paymentOption = payment)) },
            )
        }
        item { SectionHeader("How should reception follow up?") }
        items(followUpOptions.toList()) { followUp ->
            SelectableInfoCard(
                selected = draft.followUpOption == followUp,
                title = followUp.label,
                subtitle = followUp.description,
                supporting = if (followUp == FollowUpOption.COLLECT_PAYMENT_IN_ROOM) {
                    "Useful if the hotel offers discreet in-room payment collection with a receptionist or duty manager."
                } else {
                    "Keeps the guest informed without a front-desk phone call unless needed."
                },
                onSelect = { onDraftChange(draft.copy(followUpOption = followUp)) },
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Notes for reception", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = draft.notes,
                        onValueChange = { onDraftChange(draft.copy(notes = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        placeholder = { Text("Flight departs late, please advise if payment can be collected in-room.") },
                        shape = RoundedCornerShape(18.dp),
                    )
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Request summary",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            draft.option.checkoutTimeLabel,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            draft.option.feeLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SummaryRow(
                                label = "Payment",
                                value = draft.paymentOption.label,
                            )
                            SummaryRow(
                                label = "Follow-up",
                                value = draft.followUpOption.label,
                            )
                        }
                    }
                    if (draft.notes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    draft.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        }
                    }
                    Text(
                        "Reception will confirm availability and the final charge.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                    )
                }
            }
        }
        item { KazePrimaryButton(label = "Submit late checkout request", onClick = onSubmit, modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
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

@Composable
private fun ServiceRequestScreen(
    modifier: Modifier = Modifier,
    draft: ServiceRequestDraftUi,
    onBack: () -> Unit,
    onDraftChange: (ServiceRequestDraftUi) -> Unit,
    onSubmit: () -> Unit,
) {
    val isTowelRequest = draft.option.title == "Fresh towels"
    val isCustomRequest = draft.option.isCustom
    val isLaundryRequest = draft.option.title == "Laundry pickup"
    val isInRoomDiningRequest = draft.option.title == "In-room dining"
    val isConciergeRequest = draft.option.title == "Concierge help"
    val usesAssignedRoomContext = isInRoomDiningRequest || isLaundryRequest || isConciergeRequest
    val needsExplicitLocation = !isTowelRequest && !isCustomRequest && !usesAssignedRoomContext
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KazeGhostButton(label = "Back", onClick = onBack)
                MetaPill("New request")
            }
        }
        item {
            SectionIntroCard(
                eyebrow = "Service Request",
                title = draft.option.title,
                subtitle = if (isTowelRequest) {
                    "Choose how many towels you need."
                } else if (isCustomRequest) {
                    "Tell the hotel what you need."
                } else if (isLaundryRequest) {
                    "Add pickup details and send the request."
                } else if (isConciergeRequest) {
                    "Describe the help you need."
                } else {
                    "Add any details and send the request."
                },
            )
        }
        item {
            if (isTowelRequest || isCustomRequest) {
                /*Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("Fresh towels", style = MaterialTheme.typography.titleMedium)
                    }
                }*/
            } else {
                HighlightPanel(
                    title = "Service request",
                    body = "Choose the details below, then send the request to the hotel.",
                    primaryLabel = "Send request",
                    secondaryLabel = "Back to requests",
                    onPrimaryClick = onSubmit,
                    onSecondaryClick = onBack,
                )
            }
        }
        item { SectionHeader(if (isTowelRequest) "How many towels do you need?" else if (isCustomRequest) "What do you need?" else "Request details") }
        if (isTowelRequest) item {
            ServiceRequestQuantityCard(
                quantity = draft.quantity,
                itemLabel = "towels",
                onDecrease = { onDraftChange(draft.copy(quantity = (draft.quantity - 1).coerceAtLeast(1))) },
                onIncrease = { onDraftChange(draft.copy(quantity = draft.quantity + 1)) },
            )
        }
        if (isCustomRequest) {
            item {
                LuxuryNoteField(
                    value = draft.customRequest,
                    onValueChange = { onDraftChange(draft.copy(customRequest = it)) },
                    label = "Request",
                    placeholder = "Extra pillows, baby crib, adapter, room setup...",
                    minLines = 3,
                    icon = Icons.Default.Edit,
                )
            }
        }
        if (usesAssignedRoomContext) {
            item {
                // TODO replace the demo room label with the guest's actual assigned room from live stay data.
                RequestContextLabel(
                    label = "Room",
                    value = "Room 906",
                )
            }
        }
        if (needsExplicitLocation) {
            item {
                LuxuryNoteField(
                    value = draft.locationNote,
                    onValueChange = { onDraftChange(draft.copy(locationNote = it)) },
                    label = if (isLaundryRequest) "Pickup location" else "Location",
                    placeholder = if (isLaundryRequest) {
                        "Room 906, outside the door, with the concierge..."
                    } else {
                        "Room 906, pool deck cabana, lobby seating area..."
                    },
                    minLines = 2,
                    icon = Icons.Default.Place,
                )
            }
        }
        item {
            LuxuryNoteField(
                value = draft.notes,
                onValueChange = { onDraftChange(draft.copy(notes = it)) },
                label = if (isTowelRequest || isCustomRequest) "Add a note" else "Notes",
                placeholder = if (isTowelRequest) {
                    "Leave towels at the door, extra bath towels, after spa..."
                } else if (isCustomRequest) {
                    "Add timing, room, or access details..."
                } else {
                    "Hypoallergenic towels, call before entering, deliver after session..."
                },
                minLines = if (isTowelRequest || isCustomRequest) 3 else 4,
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Request summary", style = MaterialTheme.typography.titleMedium)
                    Text(draft.option.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    if (isCustomRequest && draft.customRequest.isNotBlank()) {
                        Text(
                            draft.customRequest,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.84f),
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (!isCustomRequest) {
                            InfoToken(
                                label = if (isTowelRequest) "${draft.quantity} towels" else draft.option.title,
                                accentColor = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        if (usesAssignedRoomContext) {
            Text(
                "Room 906",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
            )
        } else if (needsExplicitLocation && draft.locationNote.isNotBlank()) {
            Text(
                "Location: ${draft.locationNote}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
            )
                    }
                    if (draft.notes.isNotBlank()) {
                        Text(
                            draft.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f),
                        )
                    }
                }
            }
        }
        item {
            KazePrimaryButton(
                label = "Submit request",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ServiceRequestQuantityCard(
    quantity: Int,
    itemLabel: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Quantity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    QuantityStepperButton(
                        label = "−",
                        onClick = onDecrease,
                        edge = StepperEdge.START,
                    )
                    Text(
                        text = "$quantity",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    QuantityStepperButton(
                        label = "+",
                        onClick = onIncrease,
                        edge = StepperEdge.END,
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestContextLabel(
    label: String,
    value: String,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun QuantityStepperButton(label: String, onClick: () -> Unit, edge: StepperEdge) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = when (edge) {
            StepperEdge.START -> RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
            StepperEdge.END -> RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
        },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
    ) {
        Box(
            modifier = Modifier.size(width = 44.dp, height = 42.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private enum class StepperEdge {
    START,
    END,
}

@Composable
private fun LuxuryNoteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    minLines: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.ChatBubbleOutline,
) {
    val focusManager = LocalFocusManager.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ) {
                    Box(
                        modifier = Modifier.size(30.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Optional",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = minLines,
                    placeholder = { Text(placeholder) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SuggestedActivitiesTab(
    suggestionActivities: List<ExploreHighlight>,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FeaturedSuggestionHeader(
            onRefinePreferences = { onPrimaryAction(StayPrimaryAction.REFINE_SUGGESTIONS) },
            onSeeAgenda = { onPrimaryAction(StayPrimaryAction.SEE_FULL_AGENDA) },
        )
        val accents = listOf(
            KazeTheme.accents.editorialWarm,
            KazeTheme.accents.editorialBotanical,
            KazeTheme.accents.editorialClay,
        )
        suggestionActivities.forEachIndexed { index, suggestion ->
            SuggestionShowcaseCard(
                suggestion = suggestion,
                accentColor = accents[index % accents.size],
                onActionClick = { onPrimaryAction(StayPrimaryAction.OpenSuggestion(suggestion)) },
            )
        }
    }
}

@Composable
private fun FeaturedSuggestionHeader(onRefinePreferences: () -> Unit, onSeeAgenda: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(28.dp)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Concierge Suggestions", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Curated for tonight", style = MaterialTheme.typography.headlineSmall)
            Text("These recommendations are based on your stay, your event plan, and the pace of the hotel right now.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KazePrimaryButton(label = "Refine", onClick = onRefinePreferences)
                KazeSecondaryButton(label = "See agenda", onClick = onSeeAgenda)
            }
        }
    }
}

@Composable
private fun SuggestionShowcaseCard(suggestion: ExploreHighlight, accentColor: Color, onActionClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(26.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Box(modifier = Modifier.fillMaxWidth().background(accentColor.copy(alpha = 0.18f)).padding(horizontal = 18.dp, vertical = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaPill("Recommended",)
                    Text(suggestion.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(suggestion.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f))
                }
            }
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoToken(label = suggestion.location, accentColor = accentColor)
                    InfoToken(label = suggestion.time, accentColor = accentColor)
                }
                KazePrimaryButton(label = suggestion.cta, onClick = onActionClick, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CompactStayHeader(hotelName: String, guestName: String, roomLabel: String, stayLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                hotelName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
            )
            Text("Welcome, $guestName", style = MaterialTheme.typography.titleLarge)
            Text(roomLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
        }
        Spacer(Modifier.weight(0.05f))
        Row(
            modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color.Transparent).padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
            Text(stayLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        }
    }
}

@Composable
private fun StayStatusHero(onOpenRoute: () -> Unit, onViewFolio: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Ready now", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Open the map for your next destination or review your current hotel charges.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                KazePrimaryButton(label = "Open route", onClick = onOpenRoute)
                KazeGhostButton(label = "My charges", onClick = onViewFolio)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StayMomentCard(moment: StayMoment, onOpen: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(IntrinsicSize.Min), // Forces the Row to calculate the height of the right column
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // --- LEFT COLUMN (Timeline) ---
            Column(
                modifier = Modifier.width(70.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val timeParts = moment.time.split("•").map { it.trim() }
                    Text(
                        timeParts.firstOrNull().orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        timeParts.getOrElse(1) { moment.time },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )

                Spacer(modifier = Modifier.height(8.dp))

                // This line now fills the space dynamically
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                            shape = CircleShape
                        ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "UNTIL",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.78f),
                        )
                        Text(
                            moment.endTime,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // --- RIGHT COLUMN (Content) ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        moment.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        moment.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                        lineHeight = 20.sp
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetaPill(
                        label = moment.place,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.74f),
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    // You can use the optional color here if you updated MetaPill
                    MetaPill(
                        label = "Scheduled",
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f),
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                KazeSecondaryButton(
                    label = moment.action,
                    onClick = onOpen,
                    modifier = Modifier.fillMaxWidth() // Makes the card feel grounded
                )
            }
        }
    }
}
