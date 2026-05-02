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
import dev.orestegabo.kaze.presentation.demo.sampleHotel
import dev.orestegabo.kaze.presentation.di.rememberKazeDependencies
import dev.orestegabo.kaze.presentation.app.KazeSessionMode
import dev.orestegabo.kaze.presentation.app.KazeAppViewModel
import dev.orestegabo.kaze.domain.guest.GuestIdentity
import dev.orestegabo.kaze.presentation.events.EventsActionResult
import dev.orestegabo.kaze.presentation.events.EventsViewModel
import dev.orestegabo.kaze.presentation.explore.ExploreActionResult
import dev.orestegabo.kaze.presentation.explore.ExploreViewModel
import dev.orestegabo.kaze.presentation.map.MapViewModel
import dev.orestegabo.kaze.presentation.navigation.KazeNavigator
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
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
import dev.orestegabo.kaze.ui.home.settings.LegalDetailScreen
import dev.orestegabo.kaze.ui.home.settings.LegalPage
import dev.orestegabo.kaze.ui.map.MapScreen
import dev.orestegabo.kaze.ui.onboarding.OnboardingScreen
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
            nativeSocialAuthLauncher = dependencies.nativeSocialAuthLauncher,
        )
    }
    val uiState = appViewModel.uiState
    KazeTheme(hotelConfig = sampleHotel.config, themeMode = uiState.themeMode) {
        val stayViewModel = viewModel {
            StayViewModel(
                hotelId = dependencies.hotelId,
                guestIdentity = null,
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
        LaunchedEffect(uiState.sessionActiveStay) {
            val activeStay = uiState.sessionActiveStay
            stayViewModel.applyActiveStay(
                guestIdentity = activeStay?.let {
                    GuestIdentity(
                        hotelId = it.hotelId,
                        guestId = it.guestId,
                        stayId = it.stayId,
                        roomId = it.roomId,
                    )
                },
                hotelDisplayName = activeStay?.hotelDisplayName,
                guestName = activeStay?.guestName,
            )
        }
        val isGuestMode = uiState.sessionMode == KazeSessionMode.GUEST
        val needsProfileCompletion = uiState.sessionMode == KazeSessionMode.AUTHENTICATED &&
            (uiState.sessionDisplayName.isBlank() || uiState.sessionPhoneNumber.isBlank())
        val resolvedGuestName = when (uiState.sessionMode) {
            KazeSessionMode.AUTHENTICATED -> uiState.sessionDisplayName
                .ifBlank { uiState.sessionActiveStay?.guestName.orEmpty() }
                .takeIf { it.isNotBlank() }
                ?: uiState.sessionEmail.toDisplayNameFromEmail()
            KazeSessionMode.GUEST, null -> stayUiState.guestName
        }
        val authenticatedInvitations = uiState.sessionInvitations.map { it.toInvitationPreview() }
        val visibleInvitations = when (uiState.sessionMode) {
            KazeSessionMode.AUTHENTICATED -> authenticatedInvitations
            KazeSessionMode.GUEST, null -> emptyList()
        }
        val authenticatedEventDays = uiState.sessionEvents
            .distinctBy { it.dayId }
            .map { event ->
                EventDay(
                    id = event.dayId,
                    label = event.dayLabel,
                    dateIso = event.dateIso,
                )
            }
        var selectedAuthenticatedDayId by remember(uiState.sessionUserId) {
            mutableStateOf(authenticatedEventDays.firstOrNull()?.id)
        }
        LaunchedEffect(authenticatedEventDays.map { it.id }) {
            if (selectedAuthenticatedDayId == null || authenticatedEventDays.none { it.id == selectedAuthenticatedDayId }) {
                selectedAuthenticatedDayId = authenticatedEventDays.firstOrNull()?.id
            }
        }
        val authenticatedSelectedDay = authenticatedEventDays.firstOrNull { it.id == selectedAuthenticatedDayId }
            ?: authenticatedEventDays.firstOrNull()
        val authenticatedSessions = uiState.sessionEvents
            .filter { it.dayId == authenticatedSelectedDay?.id }
            .map { it.toScheduledExperience() }
        val eventsDays = when (uiState.sessionMode) {
            KazeSessionMode.AUTHENTICATED -> authenticatedEventDays
            KazeSessionMode.GUEST, null -> eventsUiState.days
        }
        val eventsSelectedDay = when (uiState.sessionMode) {
            KazeSessionMode.AUTHENTICATED -> authenticatedSelectedDay
            KazeSessionMode.GUEST, null -> eventsUiState.selectedDay
        }
        val eventsSessions = when (uiState.sessionMode) {
            KazeSessionMode.AUTHENTICATED -> authenticatedSessions
            KazeSessionMode.GUEST, null -> eventsUiState.sessions
        }
        val pendingInvitationCount = visibleInvitations.count { it.awaitingResponse }
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
        var selectedEventInvitation by remember { mutableStateOf<InvitationPreview?>(null) }
        var selectedLegalPage by remember { mutableStateOf<LegalPage?>(null) }
        var activePermissionPrimer by remember { mutableStateOf<KazePermissionPrimerType?>(null) }

        fun selectPrimaryDestination(destination: KazeDestination) {
            selectedEventInvitation = null
            appViewModel.onDestinationSelected(destination)
        }

        fun showMapPreviewUnavailable() {
            appViewModel.showFeedback("Indoor venue maps are still being prepared for production. Browse the venue details for now.")
        }

        fun handleStayResult(result: StayActionResult?) {
            when (result) {
                null -> Unit
                is StayActionResult.Feedback -> appViewModel.showSuccessCelebration(
                    title = "Request sent",
                    subtitle = result.message,
                )
                is StayActionResult.NavigateToEvents -> {
                    selectedEventInvitation = null
                    appViewModel.openEvents()
                }
                is StayActionResult.NavigateToMap -> showMapPreviewUnavailable()
            }
        }

        fun handleEventResult(result: EventsActionResult.NavigateToMap) {
            showMapPreviewUnavailable()
        }

        fun handleExploreResult(result: ExploreActionResult) {
            when (result) {
                is ExploreActionResult.Feedback -> appViewModel.showFeedback(result.message)
                is ExploreActionResult.NavigateToMap -> showMapPreviewUnavailable()
            }
        }

        fun openPublicBrowse(query: String) {
            val trimmedQuery = query.trim()
            selectedEventInvitation = null
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
                selectedEventInvitation = null
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
            selectedEventInvitation = null
            appViewModel.onDestinationSelected(KazeDestination.EXPLORE)
            appViewModel.showFeedback("Opening ${venue.name} in public browsing.")
        }

        fun openInvitations() {
            selectedInvitation = null
            selectedEventInvitation = null
            appViewModel.onDestinationSelected(KazeDestination.INVITATIONS)
        }

        fun openEventFromInvitation(invitation: InvitationPreview) {
            selectedEventInvitation = invitation
            appViewModel.onDestinationSelected(KazeDestination.EVENTS)
            appViewModel.showFeedback("${invitation.title} event details are open.")
        }

        fun openLegalPage(page: LegalPage) {
            selectedLegalPage = page
        }

        LaunchedEffect(uiState.activeMapTarget) {
            mapViewModel.applyNavigationTarget(uiState.activeMapTarget)
        }

        LaunchedEffect(uiState.sessionMode, resolvedGuestName) {
            stayViewModel.applyPresentationContext(
                showSharedDemoAccess = uiState.sessionMode != KazeSessionMode.AUTHENTICATED,
                guestName = resolvedGuestName,
            )
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val immersiveWeddingEvent = false
            Box(modifier = Modifier.fillMaxSize()) {
                KazeAmbientBackground(modifier = Modifier.matchParentSize())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = innerPadding.calculateStartPadding(layoutDirection),
                            top = if (immersiveWeddingEvent) 0.dp else innerPadding.calculateTopPadding(),
                            end = innerPadding.calculateEndPadding(layoutDirection),
                        ),
                ) {
                    selectedLegalPage?.let { page ->
                        LegalDetailScreen(
                            page = page,
                            bottomContentPadding = 116.dp,
                            onBack = { selectedLegalPage = null },
                        )
                        return@Box
                    }
                    if (!uiState.isReady) {
                        if (uiState.isStartupTakingTooLong) {
                            KazeTemporaryDownScreen(
                                modifier = Modifier.fillMaxSize(),
                                onRetry = appViewModel::retryStartup,
                                onContinueOffline = appViewModel::continueAsGuest,
                            )
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
                            onOpenLegalPage = ::openLegalPage,
                        )
                    } else {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val useRail = maxWidth >= 900.dp
                            val bottomContentPadding = if (useRail) 20.dp else 116.dp
                            if (useRail) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    KazeNavigationRail(
                                        currentDestination = uiState.currentDestination,
                                        onDestinationSelected = ::selectPrimaryDestination,
                                        pendingInvitationCount = pendingInvitationCount,
                                        destinations = availableDestinations,
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        when (uiState.currentDestination) {
                                            KazeDestination.HOME -> HomeScreen(
                                                modifier = Modifier.weight(1f),
                                                hotelDisplayName = stayUiState.hotelDisplayName,
                                                guestName = resolvedGuestName,
                                                assignedRoomLabel = stayUiState.assignedRoomLabel,
                                                accessProfileLabel = stayUiState.accessProfileLabel,
                                                accessStatusLabel = stayUiState.accessStatusLabel,
                                                accessCard = stayUiState.accessCard,
                                                accessContexts = stayUiState.accessContexts,
                                                selectedAccessContextId = stayUiState.selectedAccessContextId,
                                                stayMoments = stayUiState.stayMoments,
                                                activeStayScreen = stayUiState.activeStayScreen,
                                                lateCheckoutRequest = stayUiState.lateCheckoutRequest,
                                                lateCheckoutDraft = stayUiState.lateCheckoutDraft,
                                                serviceRequestDraft = stayUiState.serviceRequestDraft,
                                                submittedServiceRequests = stayUiState.submittedServiceRequests,
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
                                                onOpenInvitation = ::openInvitation,
                                                onSeeAllInvitations = ::openInvitations,
                                                onBrowseVenues = { openPublicBrowse("") },
                                                onSubmitReservation = appViewModel::submitReservation,
                                                bottomContentPadding = bottomContentPadding,
                                            )

                                            KazeDestination.STAY -> StayHomeScreen(
                                                modifier = Modifier.weight(1f),
                                                hotelDisplayName = stayUiState.hotelDisplayName,
                                                guestName = resolvedGuestName,
                                                assignedRoomLabel = stayUiState.assignedRoomLabel,
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
                                                days = eventsDays,
                                                selectedDay = eventsSelectedDay,
                                                sessions = eventsSessions,
                                                onDaySelected = { day ->
                                                    if (uiState.sessionMode == KazeSessionMode.AUTHENTICATED) {
                                                        selectedAuthenticatedDayId = day.id
                                                    } else {
                                                        eventsViewModel.onDaySelected(day)
                                                    }
                                                },
                                                onSessionAction = {
                                                    if (uiState.sessionMode == KazeSessionMode.AUTHENTICATED) {
                                                        handleEventResult(
                                                            dev.orestegabo.kaze.presentation.events.EventsActionResult.NavigateToMap(
                                                                route = "Arrival route to ${it.venueLabel}",
                                                                floorId = if (it.venueLabel.contains("Ballroom", ignoreCase = true)) "l9" else "l1",
                                                                floorLabel = it.venueLabel,
                                                            ),
                                                        )
                                                    } else {
                                                        handleEventResult(eventsViewModel.onSessionAction(it))
                                                    }
                                                },
                                                onEmptyAction = {
                                                    if (uiState.sessionMode == KazeSessionMode.AUTHENTICATED) {
                                                        appViewModel.onDestinationSelected(KazeDestination.INVITATIONS)
                                                    } else {
                                                        appViewModel.onDestinationSelected(KazeDestination.EXPLORE)
                                                        appViewModel.showFeedback("Browse public venues now. Event access appears after you join an event.")
                                                    }
                                                },
                                                eventInvitation = selectedEventInvitation,
                                                onVenueAction = ::showMapPreviewUnavailable,
                                                edgeAiEnabled = uiState.edgeAiEnabled,
                                                onAiAction = { feature ->
                                                    appViewModel.showFeedback("$feature will run on-device when the local model is installed.")
                                                },
                                                bottomContentPadding = bottomContentPadding,
                                            )

                                            KazeDestination.INVITATIONS -> InvitationsScreen(
                                                modifier = Modifier.weight(1f),
                                                invitations = visibleInvitations,
                                                isGuestMode = isGuestMode,
                                                allowInvitationCreation = false,
                                                onBack = { appViewModel.onDestinationSelected(KazeDestination.HOME) },
                                                selectedInvitation = selectedInvitation,
                                                onSelectedInvitationChange = { selectedInvitation = it },
                                                onOpenEvent = ::openEventFromInvitation,
                                                onRespondToInvitation = { invitationId, response ->
                                                    appViewModel.respondToInvitation(invitationId, response)
                                                },
                                                edgeAiEnabled = uiState.edgeAiEnabled,
                                                onAiAction = { feature ->
                                                    appViewModel.showFeedback("$feature will run on-device when the local model is installed.")
                                                },
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
                                                edgeAiEnabled = uiState.edgeAiEnabled,
                                                privacyConsent = uiState.privacyConsent,
                                                sessionLabel = sessionLabel,
                                                sessionDisplayName = uiState.sessionDisplayName,
                                                sessionUsername = uiState.sessionUsername,
                                                sessionEmail = uiState.sessionEmail,
                                                sessionPhoneNumber = uiState.sessionPhoneNumber,
                                                needsProfileCompletion = needsProfileCompletion,
                                                onThemeModeChange = appViewModel::onThemeModeChanged,
                                                onEdgeAiEnabledChange = appViewModel::onEdgeAiEnabledChanged,
                                                onMapAndVenueActivityConsentChange = appViewModel::onMapAndVenueActivityConsentChanged,
                                                onDiagnosticsConsentChange = appViewModel::onDiagnosticsConsentChanged,
                                                onNotificationsConsentChange = appViewModel::onNotificationsConsentChanged,
                                                onAnalyticsConsentChange = appViewModel::onAnalyticsConsentChanged,
                                                onUpdateProfile = appViewModel::updateProfile,
                                                onLogout = appViewModel::logout,
                                                onBack = { selectPrimaryDestination(KazeDestination.HOME) },
                                            )
                                        }
                                    }
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    when (uiState.currentDestination) {
                                        KazeDestination.HOME -> HomeScreen(
                                            modifier = Modifier.weight(1f),
                                            hotelDisplayName = stayUiState.hotelDisplayName,
                                            guestName = resolvedGuestName,
                                            assignedRoomLabel = stayUiState.assignedRoomLabel,
                                            accessProfileLabel = stayUiState.accessProfileLabel,
                                            accessStatusLabel = stayUiState.accessStatusLabel,
                                            accessCard = stayUiState.accessCard,
                                            accessContexts = stayUiState.accessContexts,
                                            selectedAccessContextId = stayUiState.selectedAccessContextId,
                                            stayMoments = stayUiState.stayMoments,
                                            activeStayScreen = stayUiState.activeStayScreen,
                                            lateCheckoutRequest = stayUiState.lateCheckoutRequest,
                                            lateCheckoutDraft = stayUiState.lateCheckoutDraft,
                                            serviceRequestDraft = stayUiState.serviceRequestDraft,
                                            submittedServiceRequests = stayUiState.submittedServiceRequests,
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
                                            onOpenInvitation = ::openInvitation,
                                            onSeeAllInvitations = ::openInvitations,
                                            onBrowseVenues = { openPublicBrowse("") },
                                            onSubmitReservation = appViewModel::submitReservation,
                                            bottomContentPadding = bottomContentPadding,
                                        )

                                        KazeDestination.STAY -> StayHomeScreen(
                                            modifier = Modifier.weight(1f),
                                            hotelDisplayName = stayUiState.hotelDisplayName,
                                            guestName = resolvedGuestName,
                                            assignedRoomLabel = stayUiState.assignedRoomLabel,
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
                                            days = eventsDays,
                                            selectedDay = eventsSelectedDay,
                                            sessions = eventsSessions,
                                            onDaySelected = { day ->
                                                if (uiState.sessionMode == KazeSessionMode.AUTHENTICATED) {
                                                    selectedAuthenticatedDayId = day.id
                                                } else {
                                                    eventsViewModel.onDaySelected(day)
                                                }
                                            },
                                            onSessionAction = {
                                                if (uiState.sessionMode == KazeSessionMode.AUTHENTICATED) {
                                                    handleEventResult(
                                                        dev.orestegabo.kaze.presentation.events.EventsActionResult.NavigateToMap(
                                                            route = "Arrival route to ${it.venueLabel}",
                                                            floorId = if (it.venueLabel.contains("Ballroom", ignoreCase = true)) "l9" else "l1",
                                                            floorLabel = it.venueLabel,
                                                        ),
                                                    )
                                                } else {
                                                    handleEventResult(eventsViewModel.onSessionAction(it))
                                                }
                                            },
                                            onEmptyAction = {
                                                if (uiState.sessionMode == KazeSessionMode.AUTHENTICATED) {
                                                    appViewModel.onDestinationSelected(KazeDestination.INVITATIONS)
                                                } else {
                                                    appViewModel.onDestinationSelected(KazeDestination.EXPLORE)
                                                    appViewModel.showFeedback("Browse public venues now. Event access appears after you join an event.")
                                                }
                                            },
                                            eventInvitation = selectedEventInvitation,
                                            onVenueAction = ::showMapPreviewUnavailable,
                                            edgeAiEnabled = uiState.edgeAiEnabled,
                                            onAiAction = { feature ->
                                                appViewModel.showFeedback("$feature will run on-device when the local model is installed.")
                                            },
                                            bottomContentPadding = bottomContentPadding,
                                        )

                                        KazeDestination.INVITATIONS -> InvitationsScreen(
                                            modifier = Modifier.weight(1f),
                                            invitations = visibleInvitations,
                                            isGuestMode = isGuestMode,
                                            allowInvitationCreation = false,
                                            onBack = { appViewModel.onDestinationSelected(KazeDestination.HOME) },
                                            selectedInvitation = selectedInvitation,
                                            onSelectedInvitationChange = { selectedInvitation = it },
                                            onOpenEvent = ::openEventFromInvitation,
                                            onRespondToInvitation = { invitationId, response ->
                                                appViewModel.respondToInvitation(invitationId, response)
                                            },
                                            edgeAiEnabled = uiState.edgeAiEnabled,
                                            onAiAction = { feature ->
                                                appViewModel.showFeedback("$feature will run on-device when the local model is installed.")
                                            },
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
                                            edgeAiEnabled = uiState.edgeAiEnabled,
                                            privacyConsent = uiState.privacyConsent,
                                            sessionLabel = sessionLabel,
                                            sessionDisplayName = uiState.sessionDisplayName,
                                            sessionUsername = uiState.sessionUsername,
                                            sessionEmail = uiState.sessionEmail,
                                            sessionPhoneNumber = uiState.sessionPhoneNumber,
                                            needsProfileCompletion = needsProfileCompletion,
                                            onThemeModeChange = appViewModel::onThemeModeChanged,
                                            onEdgeAiEnabledChange = appViewModel::onEdgeAiEnabledChanged,
                                            onMapAndVenueActivityConsentChange = appViewModel::onMapAndVenueActivityConsentChanged,
                                            onDiagnosticsConsentChange = appViewModel::onDiagnosticsConsentChanged,
                                            onNotificationsConsentChange = appViewModel::onNotificationsConsentChanged,
                                            onAnalyticsConsentChange = appViewModel::onAnalyticsConsentChanged,
                                            onUpdateProfile = appViewModel::updateProfile,
                                            onLogout = appViewModel::logout,
                                            onBack = { selectPrimaryDestination(KazeDestination.HOME) },
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
                        onDestinationSelected = ::selectPrimaryDestination,
                        pendingInvitationCount = pendingInvitationCount,
                        destinations = availableDestinations,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                        }
                    }
                }
                DemoFeedbackBanner(
                    message = uiState.feedbackMessage,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = if (immersiveWeddingEvent) 14.dp else innerPadding.calculateTopPadding() + 8.dp),
                )
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

private fun dev.orestegabo.kaze.presentation.auth.AuthInvitationSummary.toInvitationPreview(): InvitationPreview =
    InvitationPreview(
        id = id,
        title = title,
        subtitle = subtitle,
        code = code,
        phoneLabel = phoneLabel,
        statusLabel = statusLabel,
        state = when (state) {
            "PAST" -> InvitationState.PAST
            "ARCHIVED" -> InvitationState.ARCHIVED
            else -> InvitationState.ACTIVE
        },
        awaitingResponse = awaitingResponse,
    )

private fun dev.orestegabo.kaze.presentation.auth.AuthEventSummary.toScheduledExperience(): ScheduledExperience =
    ScheduledExperience(
        id = id,
        dayId = dayId,
        title = title,
        description = description,
        startIso = startIso,
        endIso = endIso,
        venueLabel = venueLabel,
        hostLabel = hostLabel,
    )

private fun String.toDisplayNameFromEmail(): String {
    val localPart = substringBefore('@').trim()
    if (localPart.isBlank()) return "Guest"
    return localPart
        .split('.', '_', '-')
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
        .ifBlank { "Guest" }
}
