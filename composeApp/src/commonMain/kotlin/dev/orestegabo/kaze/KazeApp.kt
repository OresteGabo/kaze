package dev.orestegabo.kaze

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.presentation.demo.InvitationState
import dev.orestegabo.kaze.presentation.demo.PublicVenuePreview
import dev.orestegabo.kaze.presentation.demo.invitationPreviews
import dev.orestegabo.kaze.presentation.demo.publicVenueCategories
import dev.orestegabo.kaze.presentation.demo.publicVenues
import dev.orestegabo.kaze.presentation.demo.sampleHotel
import dev.orestegabo.kaze.presentation.di.rememberKazeDependencies
import dev.orestegabo.kaze.presentation.app.KazeSessionMode
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
import dev.orestegabo.kaze.ui.auth.AuthEntryScreen
import dev.orestegabo.kaze.ui.chrome.DemoFeedbackBanner
import dev.orestegabo.kaze.ui.chrome.KazeAmbientBackground
import dev.orestegabo.kaze.ui.chrome.KazeBottomBar
import dev.orestegabo.kaze.ui.chrome.KazeNavigationRail
import dev.orestegabo.kaze.ui.events.EventScheduleScreen
import dev.orestegabo.kaze.ui.explore.ExploreScreen
import dev.orestegabo.kaze.ui.home.HomeScreen
import dev.orestegabo.kaze.ui.home.components.HomeSettingsScreen
import dev.orestegabo.kaze.ui.home.components.InvitationsScreen
import dev.orestegabo.kaze.ui.map.MapScreen
import dev.orestegabo.kaze.ui.onboarding.OnboardingScreen
import dev.orestegabo.kaze.ui.startup.KazeStartupScreen
import dev.orestegabo.kaze.ui.startup.KazeTemporaryDownScreen
import dev.orestegabo.kaze.ui.states.KazePermissionPrimerScreen
import dev.orestegabo.kaze.ui.states.KazePermissionPrimerType
import dev.orestegabo.kaze.ui.states.KazeSuccessCelebrationScreen
import dev.orestegabo.kaze.ui.stay.StayHomeScreen

@Composable
fun App() {
    val dependencies = rememberKazeDependencies()
    val navigator = remember { KazeNavigator() }
    val appViewModel = viewModel {
        KazeAppViewModel(
            secureStore = dependencies.platformServices.secureStore,
            navigator = navigator,
            authGateway = dependencies.authGateway,
            externalUrlLauncher = dependencies.externalUrlLauncher,
        )
    }
    val uiState = appViewModel.uiState
    KazeTheme(hotelConfig = sampleHotel.config, themeMode = uiState.themeMode) {
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
        val stayUiState = stayViewModel.uiState
        val eventsUiState = eventsViewModel.uiState
        val exploreUiState = exploreViewModel.uiState
        val mapUiState = mapViewModel.uiState
        val isGuestMode = uiState.sessionMode == KazeSessionMode.GUEST
        val visibleInvitations = invitationPreviews
        val pendingInvitationCount = visibleInvitations.count { it.state == InvitationState.ACTIVE }
        val availableDestinations = when (uiState.sessionMode) {
            KazeSessionMode.GUEST -> listOf(
                KazeDestination.EVENTS,
                KazeDestination.INVITATIONS,
                KazeDestination.HOME,
                KazeDestination.EXPLORE,
                KazeDestination.SETTINGS,
            )
            else -> listOf(
                KazeDestination.EVENTS,
                KazeDestination.INVITATIONS,
                KazeDestination.HOME,
                KazeDestination.EXPLORE,
                KazeDestination.SETTINGS,
            )
        }
        val sessionLabel = when (uiState.sessionMode) {
            KazeSessionMode.AUTHENTICATED -> uiState.sessionEmail.ifBlank { "Signed in to Kaze" }
            KazeSessionMode.GUEST -> "Browsing Kaze as a guest"
            null -> "Not signed in"
        }
        var selectedInvitation by remember { mutableStateOf<InvitationPreview?>(null) }
        var activePermissionPrimer by remember { mutableStateOf<KazePermissionPrimerType?>(null) }

        fun handleStayResult(result: StayActionResult?) {
            when (result) {
                null -> Unit
                is StayActionResult.Feedback -> appViewModel.showSuccessCelebration(
                    title = "Request sent",
                    subtitle = result.message,
                )
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

        fun openPublicBrowse(query: String) {
            val trimmedQuery = query.trim()
            appViewModel.onDestinationSelected(KazeDestination.EXPLORE)
            if (trimmedQuery.isNotEmpty()) {
                appViewModel.showFeedback("Showing venues related to \"$trimmedQuery\".")
            } else {
                appViewModel.showFeedback("Browse venues, prices, and public spaces from here.")
            }
        }

        fun handleJoinCode(code: String) {
            val trimmedCode = code.trim()
            if (trimmedCode.isBlank()) {
                appViewModel.showFeedback("Enter a short event or invitation code first.")
            } else {
                appViewModel.onDestinationSelected(KazeDestination.EVENTS)
                appViewModel.showFeedback("Code $trimmedCode matched. Review the linked event details.")
            }
        }

        fun openInvitation(invitation: InvitationPreview) {
            if (isGuestMode) {
                appViewModel.showFeedback("Log in or create an account to open private invitations.")
            } else {
                selectedInvitation = invitation
                appViewModel.onDestinationSelected(KazeDestination.INVITATIONS)
            }
        }

        fun openVenue(venue: PublicVenuePreview) {
            appViewModel.onDestinationSelected(KazeDestination.EXPLORE)
            appViewModel.showFeedback("Opening ${venue.name} in public browsing.")
        }

        fun openInvitations() {
            selectedInvitation = null
            appViewModel.onDestinationSelected(KazeDestination.INVITATIONS)
        }

        fun openEventFromInvitation(invitation: InvitationPreview) {
            appViewModel.onDestinationSelected(KazeDestination.EVENTS)
            appViewModel.showFeedback("${invitation.title} event details are open.")
        }

        fun openVenueMap(venue: PublicVenuePreview) {
            appViewModel.openMapRoute(
                route = "Route to ${venue.name}",
                floorId = "l1",
                floorLabel = venue.locationLabel,
            )
            appViewModel.showFeedback("Opening the map for ${venue.name}.")
        }

        LaunchedEffect(uiState.activeMapTarget) {
            mapViewModel.applyNavigationTarget(uiState.activeMapTarget)
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            Box(modifier = Modifier.fillMaxSize()) {
                KazeAmbientBackground(modifier = Modifier.matchParentSize())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = innerPadding.calculateStartPadding(layoutDirection),
                            top = innerPadding.calculateTopPadding(),
                            end = innerPadding.calculateEndPadding(layoutDirection),
                        ),
                ) {
                    if (!uiState.isReady) {
                        if (uiState.isStartupTakingTooLong) {
                            KazeTemporaryDownScreen(
                                modifier = Modifier.fillMaxSize(),
                                onRetry = appViewModel::retryStartup,
                                onContinueOffline = appViewModel::continueAsGuest,
                            )
                        } else {
                            KazeStartupScreen(modifier = Modifier.fillMaxSize())
                        }
                    } else if (uiState.isOnboardingVisible) {
                        OnboardingScreen(
                            modifier = Modifier.fillMaxSize(),
                            currentPage = uiState.onboardingPage,
                            onPageChange = appViewModel::onOnboardingPageChanged,
                            onSkip = appViewModel::skipOnboarding,
                            onNext = appViewModel::onOnboardingNext,
                            onGetStarted = appViewModel::completeOnboarding,
                        )
                    } else if (uiState.sessionMode == null) {
                        AuthEntryScreen(
                            modifier = Modifier.fillMaxSize(),
                            feedbackMessage = uiState.feedbackMessage,
                            onSignIn = appViewModel::signIn,
                            onCreateAccount = appViewModel::createAccount,
                            onSocialSignIn = appViewModel::signInWithSocialProvider,
                            onContinueAsGuest = appViewModel::continueAsGuest,
                        )
                    } else {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val useRail = maxWidth >= 900.dp
                            val bottomContentPadding = if (useRail) 20.dp else 116.dp
                            if (useRail) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    KazeNavigationRail(
                                        currentDestination = uiState.currentDestination,
                                        onDestinationSelected = appViewModel::onDestinationSelected,
                                        pendingInvitationCount = pendingInvitationCount,
                                        destinations = availableDestinations,
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        DemoFeedbackBanner(
                                            message = uiState.feedbackMessage,
                                        )
                                        when (uiState.currentDestination) {
                                            KazeDestination.HOME -> HomeScreen(
                                                modifier = Modifier.weight(1f),
                                                hotelDisplayName = stayUiState.hotelDisplayName,
                                                guestName = stayUiState.guestName,
                                                accessProfileLabel = stayUiState.accessProfileLabel,
                                                accessStatusLabel = stayUiState.accessStatusLabel,
                                                accessCard = stayUiState.accessCard,
                                                accessContexts = stayUiState.accessContexts,
                                                selectedAccessContextId = stayUiState.selectedAccessContextId,
                                                stayMoments = stayUiState.stayMoments,
                                                suggestionActivities = stayUiState.suggestionActivities,
                                                activeStayScreen = stayUiState.activeStayScreen,
                                                lateCheckoutRequest = stayUiState.lateCheckoutRequest,
                                                lateCheckoutDraft = stayUiState.lateCheckoutDraft,
                                                serviceRequestDraft = stayUiState.serviceRequestDraft,
                                                submittedServiceRequests = stayUiState.submittedServiceRequests,
                                                venueCategories = publicVenueCategories,
                                                featuredVenues = publicVenues,
                                                invitations = visibleInvitations,
                                                isGuestMode = isGuestMode,
                                                onBackToStayHome = stayViewModel::onBackToHome,
                                                onLateCheckoutDraftChange = stayViewModel::onDraftChange,
                                                onLateCheckoutSubmit = { draft -> handleStayResult(stayViewModel.submitLateCheckout(draft)) },
                                                onServiceRequestDraftChange = stayViewModel::onServiceRequestDraftChange,
                                                onServiceRequestSubmit = { draft -> handleStayResult(stayViewModel.submitServiceRequest(draft)) },
                                                onAccessContextSelected = stayViewModel::onAccessContextSelected,
                                                onPrimaryAction = { action -> handleStayResult(stayViewModel.handleAction(action)) },
                                                onEnterCode = ::handleJoinCode,
                                                onOpenCategory = { openPublicBrowse(it.title) },
                                                onOpenVenue = ::openVenue,
                                                onOpenVenueMap = ::openVenueMap,
                                                onOpenInvitation = ::openInvitation,
                                                onSeeAllInvitations = ::openInvitations,
                                                bottomContentPadding = bottomContentPadding,
                                            )

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
                                                serviceRequestDraft = stayUiState.serviceRequestDraft,
                                                submittedServiceRequests = stayUiState.submittedServiceRequests,
                                                onTabChange = stayViewModel::onTabChange,
                                                onBackToStayHome = stayViewModel::onBackToHome,
                                                onLateCheckoutDraftChange = stayViewModel::onDraftChange,
                                                onLateCheckoutSubmit = { draft -> handleStayResult(stayViewModel.submitLateCheckout(draft)) },
                                                onServiceRequestDraftChange = stayViewModel::onServiceRequestDraftChange,
                                                onServiceRequestSubmit = { draft -> handleStayResult(stayViewModel.submitServiceRequest(draft)) },
                                                onPrimaryAction = { action -> handleStayResult(stayViewModel.handleAction(action)) },
                                                bottomContentPadding = bottomContentPadding,
                                            )

                                            KazeDestination.EVENTS -> EventScheduleScreen(
                                                modifier = Modifier.weight(1f),
                                                days = eventsUiState.days,
                                                selectedDay = eventsUiState.selectedDay,
                                                sessions = eventsUiState.sessions,
                                                onDaySelected = eventsViewModel::onDaySelected,
                                                onSessionAction = { handleEventResult(eventsViewModel.onSessionAction(it)) },
                                                bottomContentPadding = bottomContentPadding,
                                            )

                                            KazeDestination.INVITATIONS -> InvitationsScreen(
                                                modifier = Modifier.weight(1f),
                                                invitations = visibleInvitations,
                                                onBack = { appViewModel.onDestinationSelected(KazeDestination.HOME) },
                                                selectedInvitation = selectedInvitation,
                                                onSelectedInvitationChange = { selectedInvitation = it },
                                                onOpenEvent = ::openEventFromInvitation,
                                                bottomContentPadding = bottomContentPadding,
                                            )

                                            KazeDestination.EXPLORE -> ExploreScreen(
                                                modifier = Modifier.weight(1f),
                                                highlights = exploreUiState.highlights,
                                                onHighlightAction = { handleExploreResult(exploreViewModel.onHighlightAction(it)) },
                                                onHeroPrimary = { handleExploreResult(exploreViewModel.reserveExperience()) },
                                                onHeroSecondary = { handleExploreResult(exploreViewModel.openPoolDeckRoute()) },
                                                bottomContentPadding = bottomContentPadding,
                                            )

                                            KazeDestination.MAP -> MapScreen(
                                                modifier = Modifier.weight(1f),
                                                floors = mapUiState.floors,
                                                guestAccess = mapUiState.guestAccess,
                                                activeRoute = mapUiState.activeRoute,
                                                activeFloorId = mapUiState.selectedFloorId,
                                                onFloorSelected = mapViewModel::onFloorSelected,
                                                onStartNavigation = { activePermissionPrimer = KazePermissionPrimerType.LOCATION },
                                                onSwitchFloor = mapViewModel::onSwitchFloor,
                                                bottomContentPadding = bottomContentPadding,
                                            )

                                            KazeDestination.SETTINGS -> HomeSettingsScreen(
                                                bottomContentPadding = bottomContentPadding,
                                                themeMode = uiState.themeMode,
                                                sessionLabel = sessionLabel,
                                                onThemeModeChange = appViewModel::onThemeModeChanged,
                                                onLogout = appViewModel::logout,
                                                onBack = { appViewModel.onDestinationSelected(KazeDestination.HOME) },
                                            )
                                        }
                                    }
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    DemoFeedbackBanner(
                                        message = uiState.feedbackMessage,
                                    )

                                    when (uiState.currentDestination) {
                                        KazeDestination.HOME -> HomeScreen(
                                            modifier = Modifier.weight(1f),
                                            hotelDisplayName = stayUiState.hotelDisplayName,
                                            guestName = stayUiState.guestName,
                                            accessProfileLabel = stayUiState.accessProfileLabel,
                                            accessStatusLabel = stayUiState.accessStatusLabel,
                                            accessCard = stayUiState.accessCard,
                                            accessContexts = stayUiState.accessContexts,
                                            selectedAccessContextId = stayUiState.selectedAccessContextId,
                                            stayMoments = stayUiState.stayMoments,
                                            suggestionActivities = stayUiState.suggestionActivities,
                                            activeStayScreen = stayUiState.activeStayScreen,
                                            lateCheckoutRequest = stayUiState.lateCheckoutRequest,
                                            lateCheckoutDraft = stayUiState.lateCheckoutDraft,
                                            serviceRequestDraft = stayUiState.serviceRequestDraft,
                                            submittedServiceRequests = stayUiState.submittedServiceRequests,
                                            venueCategories = publicVenueCategories,
                                            featuredVenues = publicVenues,
                                            invitations = visibleInvitations,
                                            isGuestMode = isGuestMode,
                                            onBackToStayHome = stayViewModel::onBackToHome,
                                            onLateCheckoutDraftChange = stayViewModel::onDraftChange,
                                            onLateCheckoutSubmit = { draft -> handleStayResult(stayViewModel.submitLateCheckout(draft)) },
                                            onServiceRequestDraftChange = stayViewModel::onServiceRequestDraftChange,
                                            onServiceRequestSubmit = { draft -> handleStayResult(stayViewModel.submitServiceRequest(draft)) },
                                            onAccessContextSelected = stayViewModel::onAccessContextSelected,
                                            onPrimaryAction = { action -> handleStayResult(stayViewModel.handleAction(action)) },
                                            onEnterCode = ::handleJoinCode,
                                            onOpenCategory = { openPublicBrowse(it.title) },
                                            onOpenVenue = ::openVenue,
                                            onOpenVenueMap = ::openVenueMap,
                                            onOpenInvitation = ::openInvitation,
                                            onSeeAllInvitations = ::openInvitations,
                                            bottomContentPadding = bottomContentPadding,
                                            )

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
                                            serviceRequestDraft = stayUiState.serviceRequestDraft,
                                            submittedServiceRequests = stayUiState.submittedServiceRequests,
                                            onTabChange = stayViewModel::onTabChange,
                                            onBackToStayHome = stayViewModel::onBackToHome,
                                            onLateCheckoutDraftChange = stayViewModel::onDraftChange,
                                            onLateCheckoutSubmit = { draft -> handleStayResult(stayViewModel.submitLateCheckout(draft)) },
                                            onServiceRequestDraftChange = stayViewModel::onServiceRequestDraftChange,
                                            onServiceRequestSubmit = { draft -> handleStayResult(stayViewModel.submitServiceRequest(draft)) },
                                            onPrimaryAction = { action -> handleStayResult(stayViewModel.handleAction(action)) },
                                            bottomContentPadding = bottomContentPadding,
                                        )

                                        KazeDestination.EVENTS -> EventScheduleScreen(
                                            modifier = Modifier.weight(1f),
                                            days = eventsUiState.days,
                                            selectedDay = eventsUiState.selectedDay,
                                            sessions = eventsUiState.sessions,
                                            onDaySelected = eventsViewModel::onDaySelected,
                                            onSessionAction = { handleEventResult(eventsViewModel.onSessionAction(it)) },
                                            bottomContentPadding = bottomContentPadding,
                                        )

                                        KazeDestination.INVITATIONS -> InvitationsScreen(
                                            modifier = Modifier.weight(1f),
                                            invitations = visibleInvitations,
                                            onBack = { appViewModel.onDestinationSelected(KazeDestination.HOME) },
                                            selectedInvitation = selectedInvitation,
                                            onSelectedInvitationChange = { selectedInvitation = it },
                                            onOpenEvent = ::openEventFromInvitation,
                                            bottomContentPadding = bottomContentPadding,
                                        )

                                        KazeDestination.EXPLORE -> ExploreScreen(
                                            modifier = Modifier.weight(1f),
                                            highlights = exploreUiState.highlights,
                                            onHighlightAction = { handleExploreResult(exploreViewModel.onHighlightAction(it)) },
                                            onHeroPrimary = { handleExploreResult(exploreViewModel.reserveExperience()) },
                                            onHeroSecondary = { handleExploreResult(exploreViewModel.openPoolDeckRoute()) },
                                            bottomContentPadding = bottomContentPadding,
                                        )

                                        KazeDestination.MAP -> MapScreen(
                                            modifier = Modifier.weight(1f),
                                            floors = mapUiState.floors,
                                            guestAccess = mapUiState.guestAccess,
                                            activeRoute = mapUiState.activeRoute,
                                            activeFloorId = mapUiState.selectedFloorId,
                                            onFloorSelected = mapViewModel::onFloorSelected,
                                            onStartNavigation = { activePermissionPrimer = KazePermissionPrimerType.LOCATION },
                                            onSwitchFloor = mapViewModel::onSwitchFloor,
                                            bottomContentPadding = bottomContentPadding,
                                        )

                                        KazeDestination.SETTINGS -> HomeSettingsScreen(
                                            bottomContentPadding = bottomContentPadding,
                                            themeMode = uiState.themeMode,
                                            sessionLabel = sessionLabel,
                                            onThemeModeChange = appViewModel::onThemeModeChanged,
                                            onLogout = appViewModel::logout,
                                            onBack = { appViewModel.onDestinationSelected(KazeDestination.HOME) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.isReady && !uiState.isOnboardingVisible && uiState.sessionMode != null) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val useRail = maxWidth >= 900.dp
                        if (!useRail) {
                    KazeBottomBar(
                        currentDestination = uiState.currentDestination,
                        onDestinationSelected = appViewModel::onDestinationSelected,
                        pendingInvitationCount = pendingInvitationCount,
                        destinations = availableDestinations,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                        }
                    }
                }
                uiState.successCelebration?.let { celebration ->
                    KazeSuccessCelebrationScreen(
                        modifier = Modifier.fillMaxSize(),
                        title = celebration.title,
                        subtitle = celebration.subtitle,
                        onBackHome = appViewModel::dismissSuccessCelebration,
                    )
                }
                activePermissionPrimer?.let { primerType ->
                    KazePermissionPrimerScreen(
                        modifier = Modifier.fillMaxSize(),
                        type = primerType,
                        onGrantAccess = {
                            activePermissionPrimer = null
                            appViewModel.showFeedback("Location access will be requested when live navigation is connected.")
                        },
                        onMaybeLater = {
                            activePermissionPrimer = null
                            appViewModel.showFeedback("No problem. You can still browse maps manually.")
                        },
                    )
                }
            }
        }
    }
}
