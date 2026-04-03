package dev.orestegabo.kaze

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.orestegabo.kaze.domain.ExperienceMode
import dev.orestegabo.kaze.domain.DigitalAccessCard
import dev.orestegabo.kaze.domain.Hotel
import dev.orestegabo.kaze.domain.HotelBranding
import dev.orestegabo.kaze.domain.HotelCampus
import dev.orestegabo.kaze.domain.HotelBuilding
import dev.orestegabo.kaze.domain.HotelConfig
import dev.orestegabo.kaze.domain.HotelMarket
import dev.orestegabo.kaze.domain.AccessCardStyle
import dev.orestegabo.kaze.domain.TypographySpec
import dev.orestegabo.kaze.domain.map.AccessLevel
import dev.orestegabo.kaze.domain.map.AccessRule
import dev.orestegabo.kaze.domain.map.AccessStatus
import dev.orestegabo.kaze.domain.map.FloorStrokeKind
import dev.orestegabo.kaze.domain.map.MapArea
import dev.orestegabo.kaze.domain.map.MapAreaKind
import dev.orestegabo.kaze.domain.map.sampleMarriottConventionMap
import dev.orestegabo.kaze.theme.KazeTheme

@Composable
@Preview
fun App() {
    KazeTheme(hotelConfig = sampleHotel.config) {
        var currentDestination by remember { mutableStateOf(KazeDestination.STAY) }
        var selectedStayTab by remember { mutableStateOf(StayTab.MY_STAY) }
        var selectedDay by remember { mutableStateOf(eventDays.first()) }
        var activeMapRoute by remember { mutableStateOf("Guest arrival to Great Rift Ballroom") }
        var activeFloorLabel by remember { mutableStateOf("Lobby Level") }
        // TODO Replace this placeholder banner state with real user-facing confirmations from backend-driven actions.
        var feedbackMessage by remember { mutableStateOf("") }
        var lateCheckoutRequest by remember { mutableStateOf<LateCheckoutRequest?>(null) }
        var lateCheckoutDraft by remember { mutableStateOf(LateCheckoutDraft()) }
        var activeStayScreen by remember { mutableStateOf(StayScreen.HOME) }

        // TODO Replace these local state transitions with real view models, repositories, and API calls.
        fun showFeedback(message: String) {
            feedbackMessage = message
        }

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                KazeBottomBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = { currentDestination = it },
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                KazeAmbientBackground(modifier = Modifier.matchParentSize())
                Column(modifier = Modifier.fillMaxSize()) {
                    DemoFeedbackBanner(
                        message = feedbackMessage,
                        onDismiss = { feedbackMessage = "" },
                    )

                    when (currentDestination) {
                        KazeDestination.STAY -> StayHomeScreen(
                            modifier = Modifier.weight(1f),
                            selectedTab = selectedStayTab,
                            activeStayScreen = activeStayScreen,
                            lateCheckoutRequest = lateCheckoutRequest,
                            lateCheckoutDraft = lateCheckoutDraft,
                            onTabChange = { selectedStayTab = it },
                            onBackToStayHome = { activeStayScreen = StayScreen.HOME },
                            onLateCheckoutDraftChange = { lateCheckoutDraft = it },
                            onLateCheckoutSubmit = { draft ->
                                // TODO Connect this submission to hotel eligibility, approval, and payment APIs.
                                val request = LateCheckoutRequest(
                                    option = draft.option,
                                    paymentOption = draft.paymentOption,
                                    followUpOption = draft.followUpOption,
                                    notes = draft.notes.ifBlank { "No additional notes." },
                                    status = "Pending front desk approval",
                                )
                                lateCheckoutRequest = request
                                lateCheckoutDraft = LateCheckoutDraft(
                                    option = request.option,
                                    paymentOption = request.paymentOption,
                                    followUpOption = request.followUpOption,
                                    notes = request.notes,
                                )
                                activeStayScreen = StayScreen.HOME
                                showFeedback(
                                    "Late checkout requested for ${request.option.checkoutTimeLabel}. ${request.paymentOption.confirmationLabel}.",
                                )
                            },
                            onPrimaryAction = { action ->
                                when (action) {
                                    StayPrimaryAction.OPEN_ROUTE -> {
                                        activeMapRoute = "Guest arrival to Great Rift Ballroom"
                                        activeFloorLabel = "Lobby Level"
                                        currentDestination = KazeDestination.MAP
                                        showFeedback("Opened indoor map for the ballroom route.")
                                    }
                                    StayPrimaryAction.VIEW_FOLIO -> showFeedback("Showing a placeholder access and folio summary.")
                                    StayPrimaryAction.SYNC_CALENDAR -> showFeedback("Synced your stay timeline to a demo calendar.")
                                    StayPrimaryAction.SHARE_STAY -> showFeedback("Generated a demo share link for your itinerary.")
                                    StayPrimaryAction.REQUEST_LATE_CHECKOUT -> {
                                        lateCheckoutDraft = lateCheckoutRequest?.let {
                                            LateCheckoutDraft(
                                                option = it.option,
                                                paymentOption = it.paymentOption,
                                                followUpOption = it.followUpOption,
                                                notes = it.notes,
                                            )
                                        } ?: LateCheckoutDraft()
                                        activeStayScreen = StayScreen.LATE_CHECKOUT
                                    }
                                    StayPrimaryAction.SEE_CHECKOUT_POLICY -> showFeedback("Late checkout is subject to occupancy, housekeeping turnover, and room type. Demo fees range from RWF 35,000 to RWF 80,000.")
                                    StayPrimaryAction.NEW_REQUEST -> showFeedback("Opened the demo request composer.")
                                    StayPrimaryAction.TRACK_REQUESTS -> showFeedback("Showing local placeholder request statuses.")
                                    StayPrimaryAction.REFINE_SUGGESTIONS -> showFeedback("Updated suggestions using local guest preferences.")
                                    StayPrimaryAction.SEE_FULL_AGENDA -> {
                                        currentDestination = KazeDestination.EVENTS
                                        showFeedback("Jumped to the full hotel event agenda.")
                                    }
                                    is StayPrimaryAction.OpenStayMoment -> showFeedback("Opened '${action.moment.title}' with placeholder reservation details.")
                                    is StayPrimaryAction.RequestService -> {
                                        // TODO Send this action through the real service-request API.
                                        showFeedback("Created a demo '${action.option.title}' request.")
                                    }
                                    is StayPrimaryAction.OpenSuggestion -> {
                                        if (action.suggestion.cta == "Open route") {
                                            activeMapRoute = "Arrival route to ${action.suggestion.location}"
                                            activeFloorLabel = action.suggestion.location
                                            currentDestination = KazeDestination.MAP
                                            showFeedback("Opened a route to ${action.suggestion.location}.")
                                        } else {
                                            showFeedback("Applied the suggestion '${action.suggestion.title}' using local placeholder data.")
                                        }
                                    }
                                }
                            },
                        )

                        KazeDestination.EVENTS -> EventScheduleScreen(
                            modifier = Modifier.weight(1f),
                            selectedDay = selectedDay,
                            onDaySelected = {
                                selectedDay = it
                                showFeedback("Loaded the ${it.label} summit schedule.")
                            },
                            onSessionAction = { session ->
                                // TODO Deep-link this to the real session details and venue data.
                                activeMapRoute = "Arrival route to ${session.room}"
                                activeFloorLabel = session.room
                                currentDestination = KazeDestination.MAP
                                showFeedback("Opened the map route for '${session.title}'.")
                            },
                        )

                        KazeDestination.EXPLORE -> ExploreScreen(
                            modifier = Modifier.weight(1f),
                            onHighlightAction = { highlight ->
                                when (highlight.cta) {
                                    "Open amenity map", "Open amenity", "Start route" -> {
                                        activeMapRoute = "Arrival route to ${highlight.location}"
                                        activeFloorLabel = highlight.location
                                        currentDestination = KazeDestination.MAP
                                        showFeedback("Opened a route to ${highlight.location}.")
                                    }
                                    else -> showFeedback("Reserved '${highlight.title}' using demo availability.")
                                }
                            },
                            onHeroPrimary = {
                                showFeedback("Reserved tonight's highlighted experience in demo mode.")
                            },
                            onHeroSecondary = {
                                activeMapRoute = "Arrival route to Pool Deck"
                                activeFloorLabel = "Amenity Route"
                                currentDestination = KazeDestination.MAP
                                showFeedback("Opened the amenity map with a placeholder route.")
                            },
                        )

                        KazeDestination.MAP -> MapScreen(
                            modifier = Modifier.weight(1f),
                            activeRoute = activeMapRoute,
                            activeFloorId = if (activeFloorLabel == "Guest Rooms") "l9" else "l1",
                            onStartNavigation = {
                                showFeedback("Started simulated wayfinding for $activeMapRoute.")
                            },
                            onSwitchFloor = {
                                activeFloorLabel = if (activeFloorLabel == "Lobby Level") "Guest Rooms" else "Lobby Level"
                                showFeedback("Switched to the $activeFloorLabel floor in demo mode.")
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KazeAmbientBackground(modifier: Modifier = Modifier) {
    val baseTop = MaterialTheme.colorScheme.background
    val baseBottom = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
    val softLineColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)
    val circlePrimary = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val circleTertiary = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.07f)
    val topPanelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.035f)
    val bottomPanelColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.028f)

    Canvas(modifier = modifier.background(Brush.verticalGradient(listOf(baseTop, baseTop, baseBottom)))) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = topPanelColor,
            topLeft = Offset(w * 0.04f, h * 0.07f),
            size = androidx.compose.ui.geometry.Size(w * 0.92f, h * 0.24f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(42f, 42f),
        )
        drawRoundRect(
            color = bottomPanelColor,
            topLeft = Offset(w * 0.05f, h * 0.66f),
            size = androidx.compose.ui.geometry.Size(w * 0.90f, h * 0.20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(42f, 42f),
        )

        drawLine(
            color = lineColor,
            start = Offset(w * 0.08f, h * 0.10f),
            end = Offset(w * 0.72f, h * 0.10f),
            strokeWidth = 5f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = lineColor,
            start = Offset(w * 0.12f, h * 0.135f),
            end = Offset(w * 0.88f, h * 0.135f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = softLineColor,
            start = Offset(w * 0.18f, h * 0.74f),
            end = Offset(w * 0.84f, h * 0.74f),
            strokeWidth = 3f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = lineColor,
            start = Offset(w * 0.14f, h * 0.79f),
            end = Offset(w * 0.62f, h * 0.79f),
            strokeWidth = 5f,
            cap = StrokeCap.Round,
        )

        val topPath = Path().apply {
            moveTo(w * 0.62f, h * 0.06f)
            lineTo(w * 0.76f, h * 0.06f)
            lineTo(w * 0.82f, h * 0.11f)
            lineTo(w * 0.93f, h * 0.11f)
            lineTo(w * 0.93f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.18f)
            lineTo(w * 0.75f, h * 0.24f)
            lineTo(w * 0.58f, h * 0.24f)
        }
        drawPath(
            path = topPath,
            color = lineColor,
            style = Stroke(width = 3f),
        )

        val bottomPath = Path().apply {
            moveTo(w * 0.10f, h * 0.88f)
            lineTo(w * 0.26f, h * 0.88f)
            lineTo(w * 0.33f, h * 0.83f)
            lineTo(w * 0.48f, h * 0.83f)
            lineTo(w * 0.48f, h * 0.90f)
            lineTo(w * 0.34f, h * 0.90f)
            lineTo(w * 0.27f, h * 0.95f)
            lineTo(w * 0.12f, h * 0.95f)
        }
        drawPath(
            path = bottomPath,
            color = softLineColor,
            style = Stroke(width = 3f),
        )

        drawCircle(
            color = circlePrimary,
            radius = w * 0.18f,
            center = Offset(w * 0.88f, h * 0.22f),
            style = Stroke(width = 5f),
        )
        drawCircle(
            color = circleTertiary,
            radius = w * 0.22f,
            center = Offset(w * 0.12f, h * 0.82f),
            style = Stroke(width = 5f),
        )

        repeat(7) { index ->
            val y = h * (0.185f + index * 0.018f)
            drawLine(
                color = if (index % 2 == 0) lineColor else softLineColor,
                start = Offset(w * 0.11f, y),
                end = Offset(w * (0.36f + index * 0.06f), y),
                strokeWidth = if (index % 2 == 0) 3f else 2f,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun KazeBottomBar(
    currentDestination: KazeDestination,
    onDestinationSelected: (KazeDestination) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KazeDestination.entries.forEach { destination ->
                val selected = currentDestination == destination
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                        )
                        .clickable { onDestinationSelected(destination) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        destination.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun StayHomeScreen(
    modifier: Modifier = Modifier,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        val pagerState = rememberPagerState(
            initialPage = selectedTab.ordinal,
            pageCount = { StayTab.entries.size },
        )

        LaunchedEffect(selectedTab) {
            if (pagerState.currentPage != selectedTab.ordinal) {
                pagerState.animateScrollToPage(selectedTab.ordinal)
            }
        }

        LaunchedEffect(pagerState.currentPage) {
            val pagerTab = StayTab.entries[pagerState.currentPage]
            if (pagerTab != selectedTab) {
                onTabChange(pagerTab)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
        ) {
            CompactStayHeader(
                hotelName = sampleHotel.config.displayName,
                guestName = "Aline",
                roomLabel = "Conference guest",
                stayLabel = "Active pass",
            )
        }
        Spacer(Modifier.height(8.dp))
        StaySegmentedTabs(
            selectedTab = selectedTab,
            onTabChange = onTabChange,
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (StayTab.entries[page]) {
                    StayTab.MY_STAY -> StayTabContent(
                        lateCheckoutRequest = lateCheckoutRequest,
                        onPrimaryAction = onPrimaryAction,
                    )

                    StayTab.REQUESTS -> ServiceRequestsTab(
                        lateCheckoutRequest = lateCheckoutRequest,
                        onPrimaryAction = onPrimaryAction,
                    )

                    StayTab.SUGGESTIONS -> SuggestedActivitiesTab(onPrimaryAction = onPrimaryAction)
                }
            }
        }
    }
}

@Composable
private fun StaySegmentedTabs(
    selectedTab: StayTab,
    onTabChange: (StayTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StayTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Surface(
                modifier = Modifier.clickable { onTabChange(tab) },
                shape = RoundedCornerShape(18.dp),
                color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                tonalElevation = if (selected) 4.dp else 0.dp,
                shadowElevation = if (selected) 2.dp else 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            ),
                    )
                    Text(
                        text = tab.label,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EventScheduleScreen(
    modifier: Modifier = Modifier,
    selectedDay: EventDay,
    onDaySelected: (EventDay) -> Unit,
    onSessionAction: (EventSession) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionIntroCard(
                eyebrow = "What's On",
                title = "East Africa Finance Summit",
                subtitle = "A clean schedule view with day switching, venue references, and direct map transitions.",
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                eventDays.forEach { day ->
                    DaySelectorChip(
                        label = day.label,
                        selected = day == selectedDay,
                        onClick = { onDaySelected(day) },
                    )
                }
            }
        }

        items(eventSchedule.filter { it.day == selectedDay.id }) { session ->
            SessionCard(session = session, onOpenMap = { onSessionAction(session) })
        }
    }
}

@Composable
private fun ExploreScreen(
    modifier: Modifier = Modifier,
    onHighlightAction: (ExploreHighlight) -> Unit,
    onHeroPrimary: () -> Unit,
    onHeroSecondary: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionIntroCard(
                eyebrow = "Explore",
                title = "Hotel life beyond the room",
                subtitle = "Amenities, experiences, and social moments curated around the visitor's current context.",
            )
        }

        item {
            HighlightPanel(
                title = "Tonight's highlighted experiences",
                body = "Sunset jazz at the lounge, chef's table seating, and a guided art walk through the lobby collection.",
                primaryLabel = "Reserve activity",
                secondaryLabel = "Open amenity map",
                onPrimaryClick = onHeroPrimary,
                onSecondaryClick = onHeroSecondary,
            )
        }

        items(exploreHighlights) { highlight ->
            ExploreCard(highlight = highlight, onActionClick = { onHighlightAction(highlight) })
        }
    }
}

@Composable
private fun MapScreen(
    modifier: Modifier = Modifier,
    activeRoute: String,
    activeFloorId: String,
    onStartNavigation: () -> Unit,
    onSwitchFloor: () -> Unit,
) {
    var selectedFloorId by remember(activeFloorId) { mutableStateOf(activeFloorId) }
    val selectedFloor = remember(selectedFloorId) {
        sampleMarriottConventionMap.floor(selectedFloorId) ?: sampleMarriottConventionMap.floor("l1")!!
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Map",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, start = 20.dp, end = 20.dp),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FloorSelectorChip(
                modifier = Modifier.weight(1f),
                label = "Ground floor",
                selected = selectedFloorId == "l1",
                onClick = { selectedFloorId = "l1" },
            )
            FloorSelectorChip(
                modifier = Modifier.weight(1f),
                label = "First floor",
                selected = selectedFloorId == "l9",
                onClick = { selectedFloorId = "l9" },
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            activeRoute,
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            ZoomableHotelMap(
                modifier = Modifier.fillMaxSize(),
                floor = selectedFloor,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = onStartNavigation,
                modifier = Modifier.weight(1f),
            ) {
                Text("Start navigation")
            }
            OutlinedButton(
                onClick = {
                    onSwitchFloor()
                    selectedFloorId = if (selectedFloorId == "l1") "l9" else "l1"
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Switch floor")
            }
        }
    }
}

@Composable
private fun FloorSelectorChip(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Text(label)
    }
}

@Composable
private fun ZoomableHotelMap(
    modifier: Modifier = Modifier,
    floor: dev.orestegabo.kaze.domain.map.FloorLevel,
) {
    var scale by remember(floor.id) { mutableStateOf(1f) }
    var offset by remember(floor.id) { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(floor.id) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                    offset += pan
                }
            },
    ) {
        val mapWidth = maxWidth * 1.55f
        val mapHeight = maxHeight * 1.25f

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .size(mapWidth, mapHeight),
            ) {
                MapPreview(
                    modifier = Modifier.fillMaxSize(),
                    floorId = floor.id,
                    guestAccess = sampleGuestAccess,
                )
                floor.areas.forEach { area ->
                    if (!sampleGuestAccess.shouldRenderLabel(area.accessRule)) return@forEach
                    val center = area.center()
                    Text(
                        text = if (sampleGuestAccess.canAccess(area.accessRule)) area.label else "Restricted",
                        modifier = Modifier.offset {
                            val x = (center.x / floor.canvasSize.width * with(density) { mapWidth.toPx() }).toInt()
                            val y = (center.y / floor.canvasSize.height * with(density) { mapHeight.toPx() }).toInt()
                            IntOffset(x - 40, y - 10)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    tonalElevation = 4.dp,
                    shadowElevation = 6.dp,
                ) {
                    TextButton(onClick = { scale = (scale + 0.25f).coerceAtMost(4f) }) {
                        Text("+", style = MaterialTheme.typography.headlineSmall)
                    }
                }
                Surface(
                    shape = CircleShape,
                    tonalElevation = 4.dp,
                    shadowElevation = 6.dp,
                ) {
                    TextButton(onClick = { scale = (scale - 0.25f).coerceAtLeast(1f) }) {
                        Text("-", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun StayTabContent(
    lateCheckoutRequest: LateCheckoutRequest?,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StayAccessCardSection(
            card = stayAccessCard,
            hotelName = sampleHotel.config.displayName,
        )

        StayStatusHero(
            onOpenRoute = { onPrimaryAction(StayPrimaryAction.OPEN_ROUTE) },
            onViewFolio = { onPrimaryAction(StayPrimaryAction.VIEW_FOLIO) },
        )

        ConciergeInfoCard(
            title = "My itinerary",
            body = "Your confirmed access, reservations, event moments, and service plans are organized like a personalized agenda.",
            actionPrimary = "Sync to calendar",
            actionSecondary = "Share with partner",
            onPrimaryClick = { onPrimaryAction(StayPrimaryAction.SYNC_CALENDAR) },
            onSecondaryClick = { onPrimaryAction(StayPrimaryAction.SHARE_STAY) },
        )

        stayMoments.forEach { moment ->
            StayMomentCard(moment = moment, onOpen = { onPrimaryAction(StayPrimaryAction.OpenStayMoment(moment)) })
        }
    }
}

@Composable
private fun StayAccessCardSection(
    card: DigitalAccessCard,
    hotelName: String,
) {
    var showDetails by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Access pass",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            "One signature card can carry room, event, dining, wellness, or day-visitor access. Tap to reveal what is linked to it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )

        SignatureStayCard(
            card = card,
            hotelName = hotelName,
            onClick = { showDetails = true },
        )

        if (showDetails) {
            AccessCardDialog(
                card = card,
                hotelName = hotelName,
                onDismiss = { showDetails = false },
            )
        }
    }
}

@Composable
private fun SignatureStayCard(
    card: DigitalAccessCard,
    hotelName: String,
    onClick: () -> Unit,
) {
    val backgroundBrush = remember(card.style) { cardBackground(card.style) }
    val frameColor = remember(card.style) { cardFrameColor(card.style) }
    val accentColor = remember(card.style) { cardAccentColor(card.style) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush)
                .border(1.dp, frameColor, RoundedCornerShape(34.dp))
                .padding(20.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawFuturisticPassPattern(
                    accentColor = accentColor,
                    frameColor = frameColor,
                )
                drawGaboMark(
                    center = Offset(size.width * 0.82f, size.height * 0.78f),
                    scaleBase = size.width * 0.0019f,
                    tint = Color.White.copy(alpha = 0.22f),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(148.dp)
                    .offset(x = 28.dp, y = (-18).dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(width = 180.dp, height = 82.dp)
                    .offset(x = (-34).dp, y = 24.dp)
                    .clip(RoundedCornerShape(topEnd = 88.dp, bottomEnd = 24.dp, topStart = 18.dp, bottomStart = 18.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                    Text(
                        hotelName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.74f),
                    )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            when (val style = card.style) {
                                AccessCardStyle.KazeDefault -> "Kaze Access"
                                is AccessCardStyle.HotelBranded -> style.headline
                                is AccessCardStyle.EventSignature -> style.eventLabel
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = accentColor,
                        )
                        Text(
                            card.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                        )
                        Text(
                            card.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.82f),
                        )
                    }
                    CardChip(text = card.id.takeLast(8), inverse = true)
                }

                Spacer(Modifier.height(58.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            card.contextLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.58f),
                        )
                        Text(
                            card.primaryAccessRef,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.94f),
                        )
                    }
                    Text(
                        "Tap for details",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.72f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AccessCardDialog(
    card: DigitalAccessCard,
    hotelName: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Access details", style = MaterialTheme.typography.titleLarge)
                        Text(
                            hotelName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StaffQrPanel(card = card)
                    }
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AccessDetailLine(label = "Pass number", value = card.id.takeLast(8))
                        AccessDetailLine(label = "Profile", value = card.contextLabel)
                        AccessDetailLine(label = "Primary access", value = card.primaryAccessRef)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Linked access", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        card.linkedAccess.forEach { badge ->
                            MetaPill(badge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffQrPanel(card: DigitalAccessCard) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Canvas(modifier = Modifier.size(118.dp)) {
                drawPseudoQr(card.id)
            }
            Text(
                "Staff scan",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AccessDetailLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CardChip(
    text: String,
    inverse: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (inverse) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.12f),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun ServiceRequestsTab(
    lateCheckoutRequest: LateCheckoutRequest?,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (lateCheckoutRequest != null) {
            LateCheckoutStatusCard(
                request = lateCheckoutRequest,
                onEdit = { onPrimaryAction(StayPrimaryAction.REQUEST_LATE_CHECKOUT) },
            )
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

        SectionIntroCard(
            eyebrow = "Requests",
            title = "Hotel services",
            subtitle = "Ask for support without calling the desk. Requests stay visible and easy to track.",
        )

        requestOptions.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { option ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(option.title, style = MaterialTheme.typography.titleMedium)
                            Text(option.description, style = MaterialTheme.typography.bodyMedium)
                            OutlinedButton(
                                onClick = { onPrimaryAction(StayPrimaryAction.RequestService(option)) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Request")
                            }
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LateCheckoutStatusCard(
    request: LateCheckoutRequest,
    onEdit: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Late checkout request", style = MaterialTheme.typography.titleMedium)
            Text(
                "${request.option.checkoutTimeLabel} • ${request.option.feeLabel}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                request.status,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            MetaPill(request.paymentOption.label)
            MetaPill(request.followUpOption.label)
            Text(
                "Reception note: ${request.notes}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f),
            )
            OutlinedButton(onClick = onEdit) {
                Text("Edit request")
            }
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
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = onBack) {
                    Text("Back")
                }
                if (existingRequest != null) {
                    MetaPill(existingRequest.status)
                }
            }
        }

        item {
            SectionIntroCard(
                eyebrow = "Stay Extension",
                title = "Request late checkout",
                subtitle = "Review pricing, approval rules, payment preference, and reception follow-up in one quiet flow.",
            )
        }

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

        item {
            SectionHeader("Choose your new checkout time")
        }

        items(lateCheckoutOptions) { option ->
            SelectableInfoCard(
                selected = draft.option == option,
                title = option.checkoutTimeLabel,
                subtitle = "${option.feeLabel} • ${option.availabilityLabel}",
                supporting = option.summary,
                onSelect = { onDraftChange(draft.copy(option = option)) },
            )
        }

        item {
            SectionHeader("Choose payment preference")
        }

        items(paymentOptions.toList()) { payment ->
            SelectableInfoCard(
                selected = draft.paymentOption == payment,
                title = payment.label,
                subtitle = payment.confirmationLabel,
                supporting = payment.description,
                onSelect = { onDraftChange(draft.copy(paymentOption = payment)) },
            )
        }

        item {
            SectionHeader("How should reception follow up?")
        }

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
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Request summary", style = MaterialTheme.typography.titleMedium)
                    Text("${draft.option.checkoutTimeLabel} • ${draft.option.feeLabel}", style = MaterialTheme.typography.bodyLarge)
                    Text(draft.paymentOption.label, style = MaterialTheme.typography.bodyMedium)
                    Text(draft.followUpOption.label, style = MaterialTheme.typography.bodyMedium)
                    // TODO Replace this preview with server-validated fee, tax, approval SLA, and payment instructions.
                    Text(
                        "Final pricing and approval timing will be confirmed by reception.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                    )
                }
            }
        }

        item {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Submit late checkout request")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun SelectableInfoCard(
    selected: Boolean,
    title: String,
    subtitle: String,
    supporting: String,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
        onClick = onSelect,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun SuggestedActivitiesTab(onPrimaryAction: (StayPrimaryAction) -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FeaturedSuggestionHeader(
            onRefinePreferences = { onPrimaryAction(StayPrimaryAction.REFINE_SUGGESTIONS) },
            onSeeAgenda = { onPrimaryAction(StayPrimaryAction.SEE_FULL_AGENDA) },
        )

        suggestedActivities.forEachIndexed { index, suggestion ->
            SuggestionShowcaseCard(
                suggestion = suggestion,
                accentColor = suggestionAccent(index),
                onActionClick = { onPrimaryAction(StayPrimaryAction.OpenSuggestion(suggestion)) },
            )
        }
    }
}

@Composable
private fun SectionIntroCard(
    eyebrow: String,
    title: String,
    subtitle: String,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                eyebrow,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
            )
        }
    }
}

@Composable
private fun HighlightPanel(
    title: String,
    body: String,
    primaryLabel: String,
    secondaryLabel: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onPrimaryClick) {
                    Text(primaryLabel)
                }
                TextButton(onClick = onSecondaryClick) {
                    Text(secondaryLabel)
                }
            }
        }
    }
}

@Composable
private fun DaySelectorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        tonalElevation = if (selected) 3.dp else 0.dp,
        shadowElevation = if (selected) 1.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DestinationDot(selected = selected)
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun FeaturedSuggestionHeader(
    onRefinePreferences: () -> Unit,
    onSeeAgenda: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Concierge Suggestions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Curated for tonight",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                "These recommendations are based on your stay, your event plan, and the pace of the hotel right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onRefinePreferences) {
                    Text("Refine")
                }
                OutlinedButton(onClick = onSeeAgenda) {
                    Text("See agenda")
                }
            }
        }
    }
}

@Composable
private fun SuggestionShowcaseCard(
    suggestion: ExploreHighlight,
    accentColor: Color,
    onActionClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accentColor.copy(alpha = 0.18f))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaPill("Recommended")
                    Text(
                        suggestion.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        suggestion.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    )
                }
            }

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InfoToken(label = suggestion.location, accentColor = accentColor)
                    InfoToken(label = suggestion.time, accentColor = accentColor)
                }
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(suggestion.cta)
                }
            }
        }
    }
}

@Composable
private fun InfoToken(
    label: String,
    accentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accentColor.copy(alpha = 0.14f),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun suggestionAccent(index: Int): Color = when (index % 3) {
    0 -> Color(0xFFC79A52)
    1 -> Color(0xFF88A37A)
    else -> Color(0xFF9A7A62)
}

private fun cardBackground(style: AccessCardStyle): Brush = when (style) {
    AccessCardStyle.KazeDefault -> Brush.linearGradient(
        colors = listOf(
            Color(0xFF111419),
            Color(0xFF18242B),
            Color(0xFF24404A),
        ),
    )

    is AccessCardStyle.HotelBranded -> Brush.linearGradient(
        colors = listOf(
            Color(0xFF121416),
            style.supportHex.toUiColor(),
            style.accentHex.toUiColor().copy(alpha = 0.72f),
        ),
    )

    is AccessCardStyle.EventSignature -> Brush.linearGradient(
        colors = listOf(
            Color(0xFF101318),
            Color(0xFF18262D),
            style.accentHex.toUiColor().copy(alpha = 0.48f),
        ),
    )
}

private fun cardFrameColor(style: AccessCardStyle): Color = when (style) {
    AccessCardStyle.KazeDefault -> Color(0x334FA6B8)
    is AccessCardStyle.HotelBranded -> style.accentHex.toUiColor().copy(alpha = 0.44f)
    is AccessCardStyle.EventSignature -> style.accentHex.toUiColor().copy(alpha = style.patternOpacity + 0.2f)
}

private fun cardAccentColor(style: AccessCardStyle): Color = when (style) {
    AccessCardStyle.KazeDefault -> Color(0xFF9EC3CC)
    is AccessCardStyle.HotelBranded -> style.accentHex.toUiColor().copy(alpha = 0.86f)
    is AccessCardStyle.EventSignature -> style.accentHex.toUiColor().copy(alpha = 0.82f)
}

private fun String.toUiColor(): Color {
    val sanitized = removePrefix("#")
    val raw = sanitized.toLong(16)
    val argb = if (sanitized.length <= 6) {
        0xFF000000 or raw
    } else {
        raw
    }
    return Color(argb)
}

@Composable
private fun DemoFeedbackBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    if (message.isBlank()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.width(12.dp))
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun HeaderBlock(
    eyebrow: String,
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            eyebrow,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.tertiary,
        )
        Text(title, style = MaterialTheme.typography.displaySmall)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
        )
    }
}

@Composable
private fun CompactStayHeader(
    hotelName: String,
    guestName: String,
    roomLabel: String,
    stayLabel: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                hotelName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Text("Welcome, $guestName", style = MaterialTheme.typography.titleLarge)
            Text(
                roomLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
            )
        }
        Spacer(Modifier.width(12.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.Transparent)
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
            )
            Text(
                stayLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun StayStatusHero(
    onOpenRoute: () -> Unit,
    onViewFolio: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Active access profile", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Conference guest • Dining, pool, and event access connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onOpenRoute) {
                    Text("Open route")
                }
                TextButton(onClick = onViewFolio) {
                    Text("View folio")
                }
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
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onPrimaryClick) {
                    Text(actionPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(onClick = onSecondaryClick) {
                    Text(actionSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun StayMomentCard(
    moment: StayMoment,
    onOpen: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                    .align(Alignment.CenterVertically),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(moment.time, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                Text(moment.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(moment.detail, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${moment.place} • ${moment.action}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
                OutlinedButton(onClick = onOpen) {
                    Text(moment.action)
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: EventSession,
    onOpenMap: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(session.time, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
            Text(session.title, style = MaterialTheme.typography.titleLarge)
            Text(session.description, style = MaterialTheme.typography.bodyMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill(session.room)
                MetaPill(session.host)
                MetaPill("Open map")
            }
            OutlinedButton(onClick = onOpenMap) {
                Text("Open map")
            }
        }
    }
}

@Composable
private fun ExploreCard(
    highlight: ExploreHighlight,
    onActionClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(highlight.title, style = MaterialTheme.typography.titleLarge)
            Text(highlight.description, style = MaterialTheme.typography.bodyMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill(highlight.location)
                MetaPill(highlight.time)
                MetaPill(highlight.cta)
            }
            OutlinedButton(onClick = onActionClick) {
                Text(highlight.cta)
            }
        }
    }
}

@Composable
private fun MetaPill(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun DestinationDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(if (selected) 12.dp else 10.dp)
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                shape = CircleShape,
            ),
    )
}

@Composable
private fun MapPreview(
    modifier: Modifier = Modifier,
    floorId: String = "l1",
    guestAccess: GuestAccessContext = sampleGuestAccess,
) {
    val floor = remember(floorId) { sampleMarriottConventionMap.floor(floorId)!! }
    val nodeLookup = remember(floorId) { floor.nodes.associateBy { it.id } }
    val outlineColor = MaterialTheme.colorScheme.outline
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val wallColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    val roomBoundaryColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    val decorColor = MaterialTheme.colorScheme.tertiary
    val edgeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    val nodeRingColor = MaterialTheme.colorScheme.primary
    val nodeFillColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val hallwayColor = MaterialTheme.colorScheme.surfaceVariant
    val lobbyColor = MaterialTheme.colorScheme.primaryContainer
    val ballroomColor = MaterialTheme.colorScheme.secondaryContainer
    val diningColor = MaterialTheme.colorScheme.tertiaryContainer
    val serviceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val guestRoomColor = MaterialTheme.colorScheme.background
    val supportColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)

    Canvas(
        modifier = modifier
            .border(1.dp, outlineColor, RoundedCornerShape(24.dp))
            .padding(16.dp),
    ) {
        val scaleX = size.width / floor.canvasSize.width
        val scaleY = size.height / floor.canvasSize.height

        floor.areas.forEach { area ->
            if (!guestAccess.shouldRenderArea(area.accessRule)) return@forEach
            val isAccessible = guestAccess.canAccess(area.accessRule)
            val areaPath = area.toPath(scaleX = scaleX, scaleY = scaleY)
            drawPath(
                path = areaPath,
                color = areaFillColor(
                    areaKind = area.kind,
                    accessRule = area.accessRule,
                    isAccessible = isAccessible,
                    surfaceColor = surfaceColor,
                    hallwayColor = hallwayColor,
                    lobbyColor = lobbyColor,
                    ballroomColor = ballroomColor,
                    diningColor = diningColor,
                    serviceColor = serviceColor,
                    guestRoomColor = guestRoomColor,
                    supportColor = supportColor,
                ),
            )
            drawPath(
                path = areaPath,
                color = if (isAccessible) outlineColor.copy(alpha = 0.65f) else Color(0x99C64545),
                style = Stroke(width = 3f),
            )
        }

        floor.strokes.forEach { stroke ->
            if (stroke.kind == FloorStrokeKind.DECOR) return@forEach
            val pathColor = strokeColor(
                strokeKind = stroke.kind,
                secondaryColor = secondaryColor,
                wallColor = wallColor,
                roomBoundaryColor = roomBoundaryColor,
                decorColor = decorColor,
            )

            stroke.points.zipWithNext().forEach { (from, to) ->
                drawLine(
                    color = pathColor,
                    start = Offset(from.x * scaleX, from.y * scaleY),
                    end = Offset(to.x * scaleX, to.y * scaleY),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round,
                )
            }
            if (stroke.closed && stroke.points.size > 2) {
                val first = stroke.points.first()
                val last = stroke.points.last()
                drawLine(
                    color = pathColor,
                    start = Offset(last.x * scaleX, last.y * scaleY),
                    end = Offset(first.x * scaleX, first.y * scaleY),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round,
                )
            }
        }

        floor.edges.forEach { edge ->
            val from = nodeLookup.getValue(edge.fromNodeId)
            val to = nodeLookup.getValue(edge.toNodeId)
            drawLine(
                color = edgeColor,
                start = Offset(from.position.x * scaleX, from.position.y * scaleY),
                end = Offset(to.position.x * scaleX, to.position.y * scaleY),
                strokeWidth = 10f,
                cap = StrokeCap.Round,
            )
        }

        floor.nodes.forEach { node ->
            val center = Offset(node.position.x * scaleX, node.position.y * scaleY)
            drawCircle(
                color = nodeRingColor,
                radius = 10f,
                center = center,
                style = Stroke(width = 5f),
            )
            drawCircle(
                color = nodeFillColor,
                radius = 6f,
                center = center,
            )
        }
    }
}

private fun strokeColor(
    strokeKind: FloorStrokeKind,
    secondaryColor: Color,
    wallColor: Color,
    roomBoundaryColor: Color,
    decorColor: Color,
): Color = when (strokeKind) {
    FloorStrokeKind.OUTLINE -> secondaryColor
    FloorStrokeKind.WALL -> wallColor
    FloorStrokeKind.ROOM_BOUNDARY -> roomBoundaryColor
    FloorStrokeKind.DECOR -> decorColor
}

private fun areaFillColor(
    areaKind: MapAreaKind,
    accessRule: AccessRule,
    isAccessible: Boolean,
    surfaceColor: Color,
    hallwayColor: Color,
    lobbyColor: Color,
    ballroomColor: Color,
    diningColor: Color,
    serviceColor: Color,
    guestRoomColor: Color,
    supportColor: Color,
): Color {
    if (!isAccessible) {
        return when (accessRule.status) {
            AccessStatus.HIDDEN -> Color.Transparent
            AccessStatus.LIMITED -> Color(0x55D06B6B)
            AccessStatus.RESTRICTED -> Color(0x66C64545)
            AccessStatus.OPEN -> Color(0x33D06B6B)
        }
    }

    return when (areaKind) {
    MapAreaKind.HALLWAY -> hallwayColor
    MapAreaKind.LOBBY_LOUNGE -> lobbyColor
    MapAreaKind.BALLROOM -> ballroomColor
    MapAreaKind.DINING -> diningColor
    MapAreaKind.SERVICE -> serviceColor
    MapAreaKind.GUEST_ROOM -> guestRoomColor
    MapAreaKind.RECEPTION -> surfaceColor.copy(alpha = 0.95f)
    MapAreaKind.SUPPORT -> supportColor
}
}

private fun MapArea.toPath(scaleX: Float, scaleY: Float): Path {
    val path = Path()
    points.firstOrNull()?.let { first ->
        path.moveTo(first.x * scaleX, first.y * scaleY)
        points.drop(1).forEach { point ->
            path.lineTo(point.x * scaleX, point.y * scaleY)
        }
        path.close()
    }
    return path
}

private fun MapArea.center(): dev.orestegabo.kaze.domain.map.MapPoint {
    val avgX = points.map { it.x }.average().toFloat()
    val avgY = points.map { it.y }.average().toFloat()
    return dev.orestegabo.kaze.domain.map.MapPoint(avgX, avgY)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFuturisticPassPattern(
    accentColor: Color,
    frameColor: Color,
) {
    val width = size.width
    val height = size.height

    drawLine(
        color = accentColor.copy(alpha = 0.26f),
        start = Offset(width * 0.08f, height * 0.22f),
        end = Offset(width * 0.72f, height * 0.22f),
        strokeWidth = 4f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = accentColor.copy(alpha = 0.18f),
        start = Offset(width * 0.12f, height * 0.30f),
        end = Offset(width * 0.84f, height * 0.30f),
        strokeWidth = 2f,
        cap = StrokeCap.Round,
    )

    val path = Path().apply {
        moveTo(width * 0.58f, height * 0.08f)
        lineTo(width * 0.75f, height * 0.08f)
        lineTo(width * 0.82f, height * 0.16f)
        lineTo(width * 0.94f, height * 0.16f)
        lineTo(width * 0.94f, height * 0.26f)
        lineTo(width * 0.82f, height * 0.26f)
        lineTo(width * 0.74f, height * 0.34f)
        lineTo(width * 0.56f, height * 0.34f)
    }
    drawPath(
        path = path,
        color = frameColor.copy(alpha = 0.55f),
        style = Stroke(width = 3f),
    )

    repeat(6) { index ->
        val y = height * (0.56f + index * 0.055f)
        drawLine(
            color = if (index % 2 == 0) accentColor.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.12f),
            start = Offset(width * 0.08f, y),
            end = Offset(width * (0.28f + index * 0.08f), y),
            strokeWidth = if (index % 2 == 0) 3f else 2f,
            cap = StrokeCap.Round,
        )
    }

    drawCircle(
        color = accentColor.copy(alpha = 0.14f),
        radius = width * 0.14f,
        center = Offset(width * 0.82f, height * 0.78f),
        style = Stroke(width = 4f),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPseudoQr(seed: String) {
    val modules = 21
    val cell = size.minDimension / modules
    val bg = Color.White
    val fg = Color(0xFF111111)

    drawRect(bg)

    fun drawFinder(x: Int, y: Int) {
        drawRect(
            color = fg,
            topLeft = Offset(x * cell, y * cell),
            size = androidx.compose.ui.geometry.Size(cell * 7, cell * 7),
        )
        drawRect(
            color = bg,
            topLeft = Offset((x + 1) * cell, (y + 1) * cell),
            size = androidx.compose.ui.geometry.Size(cell * 5, cell * 5),
        )
        drawRect(
            color = fg,
            topLeft = Offset((x + 2) * cell, (y + 2) * cell),
            size = androidx.compose.ui.geometry.Size(cell * 3, cell * 3),
        )
    }

    drawFinder(0, 0)
    drawFinder(14, 0)
    drawFinder(0, 14)

    for (row in 0 until modules) {
        for (col in 0 until modules) {
            val inFinder = (row < 7 && col < 7) ||
                (row < 7 && col >= 14) ||
                (row >= 14 && col < 7)
            if (inFinder) continue

            val hash = seed.hashCode() + row * 31 + col * 17
            if ((hash and 3) == 0) {
                drawRect(
                    color = fg,
                    topLeft = Offset(col * cell, row * cell),
                    size = androidx.compose.ui.geometry.Size(cell, cell),
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGaboMark(
    center: Offset,
    scaleBase: Float,
    tint: Color,
) {
    val originX = center.x - 52f * scaleBase
    val originY = center.y - 50f * scaleBase
    val scale = scaleBase

    val monogram = Path().apply {
        moveTo(originX + 70f * scale, originY + 28f * scale)
        lineTo(originX + 42f * scale, originY + 28f * scale)
        lineTo(originX + 25f * scale, originY + 50f * scale)
        lineTo(originX + 42f * scale, originY + 72f * scale)
        lineTo(originX + 70f * scale, originY + 72f * scale)
        moveTo(originX + 58f * scale, originY + 50f * scale)
        lineTo(originX + 80f * scale, originY + 50f * scale)
    }

    drawPath(
        path = monogram,
        color = tint,
        style = Stroke(
            width = 9f * scale,
            cap = StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
        ),
    )

    drawCircle(
        color = tint,
        radius = 4.5f * scale,
        center = Offset(
            originX + 80f * scale,
            originY + 50f * scale,
        ),
    )
}

private data class GuestAccessContext(
    val grantedLevels: Set<AccessLevel>,
) {
    fun canAccess(rule: AccessRule): Boolean = when (rule.level) {
        AccessLevel.PUBLIC -> true
        else -> rule.level in grantedLevels
    }

    fun shouldRenderArea(rule: AccessRule): Boolean =
        !(rule.status == AccessStatus.HIDDEN && !canAccess(rule))

    fun shouldRenderLabel(rule: AccessRule): Boolean =
        !(rule.status == AccessStatus.HIDDEN && !canAccess(rule))
}

private val sampleGuestAccess = GuestAccessContext(
    grantedLevels = setOf(
        AccessLevel.PUBLIC,
        AccessLevel.IN_HOUSE_GUEST,
    ),
)

private enum class KazeDestination(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    STAY("Stay", Icons.Filled.DoorFront),
    EVENTS("Events", Icons.Filled.CalendarMonth),
    EXPLORE("Explore", Icons.Filled.Explore),
    MAP("Map", Icons.Filled.Map),
}

private enum class StayTab(val label: String) {
    MY_STAY("My Stay"),
    REQUESTS("Requests"),
    SUGGESTIONS("Suggestions"),
}

private enum class StayScreen {
    HOME,
    LATE_CHECKOUT,
}

private data class StayMoment(
    val time: String,
    val title: String,
    val detail: String,
    val place: String,
    val action: String,
)

private data class ServiceOption(
    val title: String,
    val description: String,
)

private data class EventDay(
    val id: String,
    val label: String,
)

private data class EventSession(
    val day: String,
    val time: String,
    val title: String,
    val description: String,
    val room: String,
    val host: String,
)

private data class ExploreHighlight(
    val title: String,
    val description: String,
    val location: String,
    val time: String,
    val cta: String,
)

private data class LateCheckoutOption(
    val id: String,
    val checkoutTimeLabel: String,
    val feeLabel: String,
    val availabilityLabel: String,
    val summary: String,
)

private data class LateCheckoutRequest(
    val option: LateCheckoutOption,
    val paymentOption: PaymentOption,
    val followUpOption: FollowUpOption,
    val notes: String,
    val status: String,
)

private data class LateCheckoutDraft(
    val option: LateCheckoutOption = lateCheckoutOptions.first(),
    val paymentOption: PaymentOption = PaymentOption.CHARGE_TO_ROOM,
    val followUpOption: FollowUpOption = FollowUpOption.CONFIRM_IN_APP,
    val notes: String = "",
)

private enum class PaymentOption(
    val label: String,
    val description: String,
    val confirmationLabel: String,
) {
    CHARGE_TO_ROOM(
        label = "Charge to room",
        description = "Add the late checkout fee to the room folio for settlement at final checkout.",
        confirmationLabel = "Added to folio once approved",
    ),
    PAY_NOW_AT_RECEPTION(
        label = "Pay at reception",
        description = "Guest settles the fee at the front desk after approval.",
        confirmationLabel = "Payment requested at front desk",
    ),
    PAY_IN_ROOM(
        label = "Pay in room",
        description = "Reception or duty manager can come to the room with a card terminal if the property offers this service.",
        confirmationLabel = "Reception follow-up in room",
    ),
}

private enum class FollowUpOption(
    val label: String,
    val description: String,
) {
    CONFIRM_IN_APP(
        label = "Confirm in app",
        description = "Send approval and fee confirmation silently through the app.",
    ),
    CALL_ROOM(
        label = "Call the room",
        description = "Reception should call the room once availability is confirmed.",
    ),
    COLLECT_PAYMENT_IN_ROOM(
        label = "Reception to room",
        description = "Reception can come upstairs to confirm and collect payment if hotel policy allows it.",
    ),
}

private sealed interface StayPrimaryAction {
    data object OPEN_ROUTE : StayPrimaryAction
    data object VIEW_FOLIO : StayPrimaryAction
    data object SYNC_CALENDAR : StayPrimaryAction
    data object SHARE_STAY : StayPrimaryAction
    data object REQUEST_LATE_CHECKOUT : StayPrimaryAction
    data object SEE_CHECKOUT_POLICY : StayPrimaryAction
    data object NEW_REQUEST : StayPrimaryAction
    data object TRACK_REQUESTS : StayPrimaryAction
    data object REFINE_SUGGESTIONS : StayPrimaryAction
    data object SEE_FULL_AGENDA : StayPrimaryAction
    data class OpenStayMoment(val moment: StayMoment) : StayPrimaryAction
    data class RequestService(val option: ServiceOption) : StayPrimaryAction
    data class OpenSuggestion(val suggestion: ExploreHighlight) : StayPrimaryAction
}

private val stayMoments = listOf(
    StayMoment(
        time = "Today • 14:00",
        title = "Signature massage",
        detail = "Reserved treatment slot with a 15-minute arrival window.",
        place = "Ubumwe Spa",
        action = "Open route",
    ),
    StayMoment(
        time = "Today • 19:30",
        title = "Chef's tasting dinner",
        detail = "Window table reserved for two with vegetarian preference noted.",
        place = "Kivu Dining",
        action = "View menu",
    ),
    StayMoment(
        time = "Tomorrow • 08:00",
        title = "Airport transfer",
        detail = "Vehicle confirmed. Pickup point is the main porte-cochere.",
        place = "Front Drive",
        action = "Contact concierge",
    ),
)

private val requestOptions = listOf(
    ServiceOption("Fresh towels", "Send towels and bath amenities to the room."),
    ServiceOption("In-room dining", "Browse menu and order directly from the app."),
    ServiceOption("Laundry pickup", "Request express or standard garment collection."),
    ServiceOption("Concierge help", "Ask for transport, reservations, or local assistance."),
)

private val lateCheckoutOptions = listOf(
    LateCheckoutOption(
        id = "checkout_12",
        checkoutTimeLabel = "12:00 checkout",
        feeLabel = "RWF 35,000",
        availabilityLabel = "High availability",
        summary = "Best for guests with an afternoon meeting or flexible airport transfer.",
    ),
    LateCheckoutOption(
        id = "checkout_14",
        checkoutTimeLabel = "14:00 checkout",
        feeLabel = "RWF 55,000",
        availabilityLabel = "Limited availability",
        summary = "Subject to housekeeping turnover and incoming arrivals.",
    ),
    LateCheckoutOption(
        id = "checkout_16",
        checkoutTimeLabel = "16:00 checkout",
        feeLabel = "RWF 80,000",
        availabilityLabel = "Suite-only review",
        summary = "Usually requires manager approval because it affects same-day room readiness.",
    ),
)

private val paymentOptions = PaymentOption.entries
private val followUpOptions = FollowUpOption.entries

private val eventDays = listOf(
    EventDay("day1", "Fri 3 Apr"),
    EventDay("day2", "Sat 4 Apr"),
    EventDay("day3", "Sun 5 Apr"),
)

private val eventSchedule = listOf(
    EventSession(
        day = "day1",
        time = "16:00 - 17:00",
        title = "Welcome reception",
        description = "Arrival gathering for summit delegates with lounge music and light bites.",
        room = "Sky Lobby",
        host = "Guest Relations",
    ),
    EventSession(
        day = "day2",
        time = "08:00 - 09:15",
        title = "Opening keynote",
        description = "Main plenary session in the Great Rift Ballroom, with map route and speaker details available from the card.",
        room = "Great Rift Ballroom",
        host = "Finance Summit",
    ),
    EventSession(
        day = "day2",
        time = "11:00 - 12:00",
        title = "Private investor roundtable",
        description = "Invitation-only gathering with live occupancy and room lookup support.",
        room = "Virunga Room",
        host = "Executive Office",
    ),
    EventSession(
        day = "day3",
        time = "10:00 - 11:30",
        title = "Farewell brunch",
        description = "Closing brunch for delegates and hotel guests who opted into the event program.",
        room = "Kivu Terrace",
        host = "Events Team",
    ),
)

private val exploreHighlights = listOf(
    ExploreHighlight(
        title = "Infinity pool quiet hours",
        description = "A calmer pool deck period curated for business travelers between meetings.",
        location = "Pool Deck",
        time = "06:00 - 09:00",
        cta = "Open amenity",
    ),
    ExploreHighlight(
        title = "Lobby art walk",
        description = "A short self-guided route through the hotel’s featured Rwandan artists.",
        location = "Grand Lobby",
        time = "All day",
        cta = "Start route",
    ),
    ExploreHighlight(
        title = "Evening jazz set",
        description = "Soft live music in the bar, recommended for summit delegates after sessions.",
        location = "Panorama Bar",
        time = "20:00",
        cta = "Reserve table",
    ),
)

private val suggestedActivities = listOf(
    ExploreHighlight(
        title = "Because you booked the spa",
        description = "A wellness tea service is available in the relaxation lounge right after your treatment.",
        location = "Ubumwe Spa",
        time = "After 15:00",
        cta = "Add to stay",
    ),
    ExploreHighlight(
        title = "Because you are attending the summit",
        description = "A networking coffee point opens 20 minutes before the keynote near the ballroom entrance.",
        location = "Great Rift Foyer",
        time = "07:40",
        cta = "Open route",
    ),
    ExploreHighlight(
        title = "Because checkout is tomorrow",
        description = "A late breakfast and pressing service bundle is available for departing guests.",
        location = "Kivu Dining",
        time = "Tomorrow morning",
        cta = "Book bundle",
    ),
)

private val stayAccessCard = DigitalAccessCard(
    id = "pass_rw_48392",
    title = "Kaze Pass",
    subtitle = "Conference and leisure access",
    contextLabel = "Conference guest",
    primaryAccessRef = "Summit / Dining / Pool",
    linkedAccess = listOf("Summit Entry", "Restaurant Access", "Pool Access", "Concierge Services"),
    style = AccessCardStyle.EventSignature(
        eventLabel = "East Africa Finance Summit",
        accentHex = "#67E8F9",
    ),
)

private val sampleHotel = Hotel(
    id = "rw-kgl-marriott",
    slug = "kigali-marriott",
    name = "Kigali Marriott by Kaze",
    market = HotelMarket.LUXURY_HOTEL,
    timezoneId = "Africa/Kigali",
    config = HotelConfig(
        hotelId = "rw-kgl-marriott",
        displayName = "Kigali Marriott",
        branding = HotelBranding(
            primaryHex = "#8C6A2F",
            secondaryHex = "#B28A4A",
            accentHex = "#D7B67A",
            surfaceHex = "#FFF9F1",
            backgroundHex = "#F6F1E8",
            logoAsset = "branding/rw-kgl-marriott/logo.svg",
            wordmarkAsset = "branding/rw-kgl-marriott/wordmark.svg",
            typography = TypographySpec(
                headingScale = 1.05f,
                bodyScale = 1f,
                labelScale = 0.96f,
            ),
        ),
        supportedLocales = listOf("en", "fr"),
        defaultCurrencyCode = "RWF",
    ),
    campus = HotelCampus(
        city = "Kigali",
        countryCode = "RW",
        buildings = listOf(
            HotelBuilding(
                id = "main-tower",
                name = "Main Tower",
                floors = listOf("l1", "l9"),
            )
        ),
    ),
    activeExperiences = setOf(
        ExperienceMode.STAY,
        ExperienceMode.EVENT,
        ExperienceMode.EXPLORE,
        ExperienceMode.SERVICE_REQUESTS,
    ),
)
