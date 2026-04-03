package dev.orestegabo.kaze

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.kotlinconf_first_floor_dark_raster
import kaze.composeapp.generated.resources.kotlinconf_first_floor_light_raster
import kaze.composeapp.generated.resources.kotlinconf_ground_floor_dark_raster
import kaze.composeapp.generated.resources.kotlinconf_ground_floor_light_raster
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import dev.orestegabo.kaze.domain.AccessCardStyle
import dev.orestegabo.kaze.domain.DigitalAccessCard
import dev.orestegabo.kaze.domain.TypographySpec
import dev.orestegabo.kaze.domain.map.MapArea
import dev.orestegabo.kaze.demo.EventSession
import dev.orestegabo.kaze.demo.EventDay
import dev.orestegabo.kaze.demo.ExploreHighlight
import dev.orestegabo.kaze.demo.FollowUpOption
import dev.orestegabo.kaze.demo.KazeDestination
import dev.orestegabo.kaze.demo.LateCheckoutDraft
import dev.orestegabo.kaze.demo.LateCheckoutOption
import dev.orestegabo.kaze.demo.LateCheckoutRequest
import dev.orestegabo.kaze.demo.PaymentOption
import dev.orestegabo.kaze.demo.ServiceOption
import dev.orestegabo.kaze.demo.StayMoment
import dev.orestegabo.kaze.demo.StayPrimaryAction
import dev.orestegabo.kaze.demo.StayScreen
import dev.orestegabo.kaze.demo.StayTab
import dev.orestegabo.kaze.demo.eventDays
import dev.orestegabo.kaze.demo.eventSchedule
import dev.orestegabo.kaze.demo.exploreHighlights
import dev.orestegabo.kaze.demo.followUpOptions
import dev.orestegabo.kaze.demo.lateCheckoutOptions
import dev.orestegabo.kaze.demo.paymentOptions
import dev.orestegabo.kaze.demo.requestOptions
import dev.orestegabo.kaze.demo.sampleHotel
import dev.orestegabo.kaze.demo.stayAccessCard
import dev.orestegabo.kaze.demo.stayMoments
import dev.orestegabo.kaze.demo.suggestedActivities
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.map.MapScreen

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
                .background(MaterialTheme.colorScheme.background),
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                KazeAmbientBackground(modifier = Modifier.matchParentSize())
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 110.dp),
                ) {
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
                                    }
                                    StayPrimaryAction.VIEW_FOLIO -> Unit
                                    StayPrimaryAction.SYNC_CALENDAR -> Unit
                                    StayPrimaryAction.SHARE_STAY -> Unit
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
                                    StayPrimaryAction.SEE_CHECKOUT_POLICY -> Unit
                                    StayPrimaryAction.NEW_REQUEST -> Unit
                                    StayPrimaryAction.TRACK_REQUESTS -> Unit
                                    StayPrimaryAction.REFINE_SUGGESTIONS -> Unit
                                    StayPrimaryAction.SEE_FULL_AGENDA -> {
                                        currentDestination = KazeDestination.EVENTS
                                    }
                                    is StayPrimaryAction.OpenStayMoment -> Unit
                                    is StayPrimaryAction.RequestService -> {
                                        // TODO Send this action through the real service-request API.
                                        showFeedback("${action.option.title} request sent.")
                                    }
                                    is StayPrimaryAction.OpenSuggestion -> {
                                        if (action.suggestion.cta == "Open route") {
                                            activeMapRoute = "Arrival route to ${action.suggestion.location}"
                                            activeFloorLabel = action.suggestion.location
                                            currentDestination = KazeDestination.MAP
                                        } else {
                                            showFeedback("${action.suggestion.title} saved to your plan.")
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
                            },
                            onSessionAction = { session ->
                                // TODO Deep-link this to the real session details and venue data.
                                activeMapRoute = "Arrival route to ${session.room}"
                                activeFloorLabel = session.room
                                currentDestination = KazeDestination.MAP
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
                                    }
                                    else -> showFeedback("${highlight.title} reserved.")
                                }
                            },
                            onHeroPrimary = { showFeedback("Experience reserved.") },
                            onHeroSecondary = {
                                activeMapRoute = "Arrival route to Pool Deck"
                                activeFloorLabel = "Amenity Route"
                                currentDestination = KazeDestination.MAP
                            },
                        )

                        KazeDestination.MAP -> MapScreen(
                            modifier = Modifier.weight(1f),
                            activeRoute = activeMapRoute,
                            activeFloorId = if (activeFloorLabel == "Guest Rooms") "l9" else "l1",
                            onStartNavigation = {},
                            onSwitchFloor = {
                                activeFloorLabel = if (activeFloorLabel == "Lobby Level") "Guest Rooms" else "Lobby Level"
                            },
                        )
                    }
                }

                KazeBottomBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = { currentDestination = it },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
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
    modifier: Modifier = Modifier,
    currentDestination: KazeDestination,
    onDestinationSelected: (KazeDestination) -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 14.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 8.dp,
        shadowElevation = 14.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                Color.Transparent,
                            )
                        )
                    ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KazeDestination.entries.forEach { destination ->
                    val selected = currentDestination == destination
                    val itemShape = RoundedCornerShape(
                        topStart = 22.dp,
                        topEnd = 12.dp,
                        bottomEnd = 22.dp,
                        bottomStart = 12.dp,
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(itemShape)
                            .background(
                                if (selected) {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f),
                                            Color.Transparent,
                                        )
                                    )
                                }
                            )
                            .border(
                                1.dp,
                                if (selected) {
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.26f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                },
                                itemShape,
                            )
                            .clickable { onDestinationSelected(destination) }
                            .padding(horizontal = 6.dp, vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (selected) {
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                                    }
                                ),
                        )

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) {
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.78f),
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f),
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                            )
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
                                },
                            )
                        }

                        Text(
                            destination.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                            },
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StayTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Surface(
                modifier = Modifier.clickable { onTabChange(tab) },
                shape = RoundedCornerShape(18.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                tonalElevation = if (selected) 6.dp else 0.dp,
                shadowElevation = if (selected) 4.dp else 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            ),
                    )
                    Text(
                        text = tab.label,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
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
            EventDaySwitcher(
                selectedDay = selectedDay,
                onDaySelected = onDaySelected,
            )
        }

        item {
            Text(
                selectedDay.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        item {
            Text(
                "Today's schedule",
                style = MaterialTheme.typography.headlineSmall,
            )
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
                    KazeGhostButton(label = "Close", onClick = onDismiss)
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
                            KazeSecondaryButton(
                                label = "Request",
                                onClick = { onPrimaryAction(StayPrimaryAction.RequestService(option)) },
                                modifier = Modifier.fillMaxWidth(),
                            )
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
                KazeGhostButton(label = "Back", onClick = onBack)
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
            KazePrimaryButton(
                label = "Submit late checkout request",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
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
                KazePrimaryButton(label = primaryLabel, onClick = onPrimaryClick)
                KazeGhostButton(label = secondaryLabel, onClick = onSecondaryClick)
            }
        }
    }
}

@Composable
private fun EventDaySwitcher(
    selectedDay: EventDay,
    onDaySelected: (EventDay) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            eventDays.forEach { day ->
                EventDayButton(
                    day = day,
                    selected = day == selectedDay,
                    onClick = { onDaySelected(day) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun EventDayButton(
    day: EventDay,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val parts = day.label.split(" ", limit = 2)
    val shortDay = parts.firstOrNull().orEmpty()
    val shortDate = parts.getOrNull(1).orEmpty()
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        )
                    )
                }
            )
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.42f),
                ),
        )
        Text(
            text = shortDay,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
        )
        Text(
            text = shortDate,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        )
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
                KazePrimaryButton(label = "Refine", onClick = onRefinePreferences)
                KazeSecondaryButton(label = "See agenda", onClick = onSeeAgenda)
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
                KazePrimaryButton(
                    label = suggestion.cta,
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                )
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
            KazeGhostButton(label = "Dismiss", onClick = onDismiss)
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
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
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
                KazeSecondaryButton(label = moment.action, onClick = onOpen)
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
            KazeSecondaryButton(label = "Open map", onClick = onOpenMap)
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
            KazeSecondaryButton(label = highlight.cta, onClick = onActionClick)
        }
    }
}

@Composable
internal fun KazePrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f),
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun KazeSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
) {
    val containerColor = if (emphasized) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    }
    val borderColor = if (emphasized) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    }
    val textColor = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun KazeGhostButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun KazeRoundButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
