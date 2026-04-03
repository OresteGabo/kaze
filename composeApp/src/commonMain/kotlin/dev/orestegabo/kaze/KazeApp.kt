package dev.orestegabo.kaze

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.presentation.demo.sampleHotel
import dev.orestegabo.kaze.presentation.di.rememberKazeDependencies
import dev.orestegabo.kaze.presentation.app.KazeAppViewModel
import dev.orestegabo.kaze.presentation.events.EventsActionResult
import dev.orestegabo.kaze.presentation.events.EventsViewModel
import dev.orestegabo.kaze.presentation.explore.ExploreActionResult
import dev.orestegabo.kaze.presentation.explore.ExploreViewModel
import dev.orestegabo.kaze.presentation.map.MapViewModel
import dev.orestegabo.kaze.presentation.navigation.KazeNavigator
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.presentation.stay.StayActionResult
import dev.orestegabo.kaze.presentation.stay.StayViewModel
import dev.orestegabo.kaze.ui.chrome.DemoFeedbackBanner
import dev.orestegabo.kaze.ui.chrome.KazeAmbientBackground
import dev.orestegabo.kaze.ui.chrome.KazeBottomBar
import dev.orestegabo.kaze.ui.events.EventScheduleScreen
import dev.orestegabo.kaze.ui.explore.ExploreScreen
import dev.orestegabo.kaze.ui.map.MapScreen
import dev.orestegabo.kaze.ui.stay.StayHomeScreen

@Composable
fun App() {
    val dependencies = rememberKazeDependencies()
    KazeTheme(hotelConfig = sampleHotel.config) {
        val navigator = remember { KazeNavigator() }
        val appViewModel = viewModel { KazeAppViewModel(navigator) }
        val stayViewModel = viewModel {
            StayViewModel(
                hotelId = dependencies.hotelId,
                guestIdentity = dev.orestegabo.kaze.domain.guest.GuestIdentity(
                    hotelId = dependencies.hotelId,
                    guestId = "guest_aline",
                    stayId = "stay_001",
                ),
                observeHotelContext = dependencies.observeHotelContext,
                stayRepository = dependencies.stayRepository,
                submitLateCheckoutUseCase = dependencies.submitLateCheckout,
            )
        }
        val eventsViewModel = viewModel {
            EventsViewModel(
                hotelId = dependencies.hotelId,
                experienceRepository = dependencies.experienceRepository,
            )
        }
        val exploreViewModel = viewModel {
            ExploreViewModel(
                hotelId = dependencies.hotelId,
                experienceRepository = dependencies.experienceRepository,
            )
        }
        val mapViewModel = viewModel {
            MapViewModel(
                hotelId = dependencies.hotelId,
                mapId = dependencies.mapId,
                mapRepository = dependencies.mapRepository,
            )
        }
        val uiState = appViewModel.uiState
        val stayUiState = stayViewModel.uiState
        val eventsUiState = eventsViewModel.uiState
        val exploreUiState = exploreViewModel.uiState
        val mapUiState = mapViewModel.uiState

        fun handleStayResult(result: StayActionResult?) {
            when (result) {
                null -> Unit
                is StayActionResult.Feedback -> appViewModel.showFeedback(result.message)
                is StayActionResult.NavigateToEvents -> appViewModel.openEvents()
                is StayActionResult.NavigateToMap -> appViewModel.openMapRoute(
                    route = result.route,
                    floorId = result.floorId,
                    floorLabel = result.floorLabel,
                )
            }
        }

        fun handleEventResult(result: EventsActionResult.NavigateToMap) {
            appViewModel.openMapRoute(
                route = result.route,
                floorId = result.floorId,
                floorLabel = result.floorLabel,
            )
        }

        fun handleExploreResult(result: ExploreActionResult) {
            when (result) {
                is ExploreActionResult.Feedback -> appViewModel.showFeedback(result.message)
                is ExploreActionResult.NavigateToMap -> appViewModel.openMapRoute(
                    route = result.route,
                    floorId = result.floorId,
                    floorLabel = result.floorLabel,
                )
            }
        }

        LaunchedEffect(uiState.activeMapTarget) {
            mapViewModel.applyNavigationTarget(uiState.activeMapTarget)
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                KazeAmbientBackground(modifier = Modifier.matchParentSize())
                Column(modifier = Modifier.fillMaxSize().padding(bottom = 110.dp)) {
                    DemoFeedbackBanner(
                        message = uiState.feedbackMessage,
                        onDismiss = appViewModel::dismissFeedback,
                    )

                    when (uiState.currentDestination) {
                        KazeDestination.STAY -> StayHomeScreen(
                            modifier = Modifier.weight(1f),
                            hotelDisplayName = stayUiState.hotelDisplayName,
                            guestName = stayUiState.guestName,
                            accessProfileLabel = stayUiState.accessProfileLabel,
                            accessStatusLabel = stayUiState.accessStatusLabel,
                            accessCard = stayUiState.accessCard,
                            stayMoments = stayUiState.stayMoments,
                            requestOptions = stayUiState.requestOptions,
                            suggestionActivities = stayUiState.suggestionActivities,
                            selectedTab = stayUiState.selectedTab,
                            activeStayScreen = stayUiState.activeStayScreen,
                            lateCheckoutRequest = stayUiState.lateCheckoutRequest,
                            lateCheckoutDraft = stayUiState.lateCheckoutDraft,
                            onTabChange = stayViewModel::onTabChange,
                            onBackToStayHome = stayViewModel::onBackToHome,
                            onLateCheckoutDraftChange = stayViewModel::onDraftChange,
                            onLateCheckoutSubmit = { draft -> handleStayResult(stayViewModel.submitLateCheckout(draft)) },
                            onPrimaryAction = { action -> handleStayResult(stayViewModel.handleAction(action)) },
                        )

                        KazeDestination.EVENTS -> EventScheduleScreen(
                            modifier = Modifier.weight(1f),
                            days = eventsUiState.days,
                            selectedDay = eventsUiState.selectedDay,
                            sessions = eventsUiState.sessions,
                            onDaySelected = eventsViewModel::onDaySelected,
                            onSessionAction = { handleEventResult(eventsViewModel.onSessionAction(it)) },
                        )

                        KazeDestination.EXPLORE -> ExploreScreen(
                            modifier = Modifier.weight(1f),
                            highlights = exploreUiState.highlights,
                            onHighlightAction = { handleExploreResult(exploreViewModel.onHighlightAction(it)) },
                            onHeroPrimary = { handleExploreResult(exploreViewModel.reserveExperience()) },
                            onHeroSecondary = { handleExploreResult(exploreViewModel.openPoolDeckRoute()) },
                        )

                        KazeDestination.MAP -> MapScreen(
                            modifier = Modifier.weight(1f),
                            floors = mapUiState.floors,
                            guestAccess = mapUiState.guestAccess,
                            activeRoute = mapUiState.activeRoute,
                            activeFloorId = mapUiState.selectedFloorId,
                            onFloorSelected = mapViewModel::onFloorSelected,
                            onStartNavigation = {},
                            onSwitchFloor = mapViewModel::onSwitchFloor,
                        )
                    }
                }

                KazeBottomBar(
                    currentDestination = uiState.currentDestination,
                    onDestinationSelected = appViewModel::onDestinationSelected,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
