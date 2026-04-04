package dev.orestegabo.kaze.ui.stay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.ExploreHighlight
import dev.orestegabo.kaze.presentation.demo.FollowUpOption
import dev.orestegabo.kaze.presentation.demo.LateCheckoutDraft
import dev.orestegabo.kaze.presentation.demo.LateCheckoutRequest
import dev.orestegabo.kaze.presentation.demo.ServiceOption
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
    onTabChange: (StayTab) -> Unit,
    onBackToStayHome: () -> Unit,
    onLateCheckoutDraftChange: (LateCheckoutDraft) -> Unit,
    onLateCheckoutSubmit: (LateCheckoutDraft) -> Unit,
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
        StaySegmentedTabs(selectedTab = selectedTab, onTabChange = onTabChange)
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (StayTab.entries[page]) {
                    StayTab.MY_STAY -> StayTabContent(
                        hotelDisplayName = hotelDisplayName,
                        accessCard = accessCard,
                        stayMoments = stayMoments,
                        lateCheckoutRequest = lateCheckoutRequest,
                        onPrimaryAction = onPrimaryAction,
                    )
                    StayTab.REQUESTS -> ServiceRequestsTab(
                        requestOptions = requestOptions,
                        lateCheckoutRequest = lateCheckoutRequest,
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
private fun StaySegmentedTabs(selectedTab: StayTab, onTabChange: (StayTab) -> Unit) {
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
                }
            }
        }
    }
}

@Composable
private fun StayTabContent(
    hotelDisplayName: String,
    accessCard: DigitalAccessCard?,
    stayMoments: List<StayMoment>,
    lateCheckoutRequest: LateCheckoutRequest?,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        accessCard?.let { StayAccessCardSection(card = it) }
        StayStatusHero(onOpenRoute = { onPrimaryAction(StayPrimaryAction.OPEN_ROUTE) }, onViewFolio = { onPrimaryAction(StayPrimaryAction.VIEW_FOLIO) })
        ConciergeInfoCard(
            title = "My itinerary",
            body = "Your confirmed access, reservations, event moments, and service plans are organized like a personalized agenda.",
            actionPrimary = "Sync to calendar",
            actionSecondary = "Share with partner",
            onPrimaryClick = { onPrimaryAction(StayPrimaryAction.SYNC_CALENDAR) },
            onSecondaryClick = { onPrimaryAction(StayPrimaryAction.SHARE_STAY) },
        )
        stayMoments.forEach { moment -> StayMomentCard(moment = moment, onOpen = { onPrimaryAction(StayPrimaryAction.OpenStayMoment(moment)) }) }
    }
}

@Composable
private fun ServiceRequestsTab(
    requestOptions: List<ServiceOption>,
    lateCheckoutRequest: LateCheckoutRequest?,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (lateCheckoutRequest != null) {
            LateCheckoutStatusCard(request = lateCheckoutRequest, onEdit = { onPrimaryAction(StayPrimaryAction.REQUEST_LATE_CHECKOUT) })
        } else {
            HighlightPanel(
                title = "Late checkout",
                body = "Request extra time in the room, choose how you want to pay, and tell reception how to follow up.",
                primaryLabel = "Request late checkout",
                secondaryLabel = "See policy",
                onPrimaryClick = { onPrimaryAction(StayPrimaryAction.REQUEST_LATE_CHECKOUT) },
                onSecondaryClick = { onPrimaryAction(StayPrimaryAction.SEE_CHECKOUT_POLICY) },
            )
        }
        SectionIntroCard(eyebrow = "Requests", title = "Hotel services", subtitle = "Ask for support without calling the desk. Requests stay visible and easy to track.")
        requestOptions.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { option ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(option.title, style = MaterialTheme.typography.titleMedium)
                            Text(option.description, style = MaterialTheme.typography.bodyMedium)
                            KazeSecondaryButton(label = "Request", onClick = { onPrimaryAction(StayPrimaryAction.RequestService(option)) }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LateCheckoutStatusCard(request: LateCheckoutRequest, onEdit: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Late checkout request", style = MaterialTheme.typography.titleMedium)
            Text("${request.option.checkoutTimeLabel} • ${request.option.feeLabel}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(request.status, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
            MetaPill(request.paymentOption.label)
            MetaPill(request.followUpOption.label)
            Text("Reception note: ${request.notes}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f))
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
                if (existingRequest != null) MetaPill(existingRequest.status)
            }
        }
        item { SectionIntroCard(eyebrow = "Stay Extension", title = "Request late checkout", subtitle = "Review pricing, approval rules, payment preference, and reception follow-up in one quiet flow.") }
        item {
            HighlightPanel(
                title = "Current stay details",
                body = "Standard checkout is April 6, 2026 at 10:00. Late checkout remains subject to occupancy and housekeeping turnover.",
                primaryLabel = "See policy",
                secondaryLabel = "Back to stay",
                onPrimaryClick = {},
                onSecondaryClick = onBack,
            )
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
            OutlinedTextField(
                value = draft.notes,
                onValueChange = { onDraftChange(draft.copy(notes = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes for reception") },
                placeholder = { Text("Flight departs late, please advise if payment can be collected in-room.") },
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Request summary", style = MaterialTheme.typography.titleMedium)
                    Text("${draft.option.checkoutTimeLabel} • ${draft.option.feeLabel}", style = MaterialTheme.typography.bodyLarge)
                    Text(draft.paymentOption.label, style = MaterialTheme.typography.bodyMedium)
                    Text(draft.followUpOption.label, style = MaterialTheme.typography.bodyMedium)
                    Text("Final pricing and approval timing will be confirmed by reception.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f))
                }
            }
        }
        item { KazePrimaryButton(label = "Submit late checkout request", onClick = onSubmit, modifier = Modifier.fillMaxWidth()) }
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
                    MetaPill("Recommended")
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Active access profile", style = MaterialTheme.typography.titleMedium)
                Text("Conference guest • Dining, pool, and event access connected", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                KazePrimaryButton(label = "Open route", onClick = onOpenRoute)
                KazeGhostButton(label = "View folio", onClick = onViewFolio)
            }
        }
    }
}

@Composable
private fun ConciergeInfoCard(
    title: String,
    body: String,
    actionPrimary: String,
    actionSecondary: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KazePrimaryButton(label = actionPrimary, onClick = onPrimaryClick)
                KazeSecondaryButton(label = actionSecondary, onClick = onSecondaryClick)
            }
        }
    }
}

@Composable
private fun StayMomentCard(moment: StayMoment, onOpen: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(modifier = Modifier.size(12.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape).align(Alignment.CenterVertically))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(moment.time, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                Text(moment.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(moment.detail, style = MaterialTheme.typography.bodyMedium)
                Text("${moment.place} • ${moment.action}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                KazeSecondaryButton(label = moment.action, onClick = onOpen)
            }
        }
    }
}
