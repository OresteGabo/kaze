package dev.orestegabo.kaze

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.demo.KazeDestination
import dev.orestegabo.kaze.demo.LateCheckoutDraft
import dev.orestegabo.kaze.demo.LateCheckoutRequest
import dev.orestegabo.kaze.demo.StayPrimaryAction
import dev.orestegabo.kaze.demo.StayScreen
import dev.orestegabo.kaze.demo.StayTab
import dev.orestegabo.kaze.demo.eventDays
import dev.orestegabo.kaze.demo.sampleHotel
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.chrome.DemoFeedbackBanner
import dev.orestegabo.kaze.ui.chrome.KazeAmbientBackground
import dev.orestegabo.kaze.ui.chrome.KazeBottomBar
import dev.orestegabo.kaze.ui.events.EventScheduleScreen
import dev.orestegabo.kaze.ui.explore.ExploreScreen
import dev.orestegabo.kaze.ui.map.MapScreen
import dev.orestegabo.kaze.ui.stay.StayHomeScreen

@Composable
fun App() {
    KazeTheme(hotelConfig = sampleHotel.config) {
        var currentDestination by remember { mutableStateOf(KazeDestination.STAY) }
        var selectedStayTab by remember { mutableStateOf(StayTab.MY_STAY) }
        var selectedDay by remember { mutableStateOf(eventDays.first()) }
        var activeMapRoute by remember { mutableStateOf("Guest arrival to Great Rift Ballroom") }
        var activeFloorLabel by remember { mutableStateOf("Lobby Level") }
        var feedbackMessage by remember { mutableStateOf("") }
        var lateCheckoutRequest by remember { mutableStateOf<LateCheckoutRequest?>(null) }
        var lateCheckoutDraft by remember { mutableStateOf(LateCheckoutDraft()) }
        var activeStayScreen by remember { mutableStateOf(StayScreen.HOME) }

        fun showFeedback(message: String) {
            feedbackMessage = message
        }

        fun handleStayAction(action: StayPrimaryAction) {
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
                StayPrimaryAction.SEE_FULL_AGENDA -> currentDestination = KazeDestination.EVENTS
                is StayPrimaryAction.OpenStayMoment -> Unit
                is StayPrimaryAction.RequestService -> showFeedback("${action.option.title} request sent.")
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
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                KazeAmbientBackground(modifier = Modifier.matchParentSize())
                Column(modifier = Modifier.fillMaxSize().padding(bottom = 110.dp)) {
                    DemoFeedbackBanner(message = feedbackMessage, onDismiss = { feedbackMessage = "" })

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
                                showFeedback("Late checkout requested for ${request.option.checkoutTimeLabel}. ${request.paymentOption.confirmationLabel}.")
                            },
                            onPrimaryAction = ::handleStayAction,
                        )

                        KazeDestination.EVENTS -> EventScheduleScreen(
                            modifier = Modifier.weight(1f),
                            selectedDay = selectedDay,
                            onDaySelected = { selectedDay = it },
                            onSessionAction = { session ->
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
