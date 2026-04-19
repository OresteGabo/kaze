package dev.orestegabo.kaze.presentation

import dev.orestegabo.kaze.platform.SecureStore
import dev.orestegabo.kaze.presentation.app.KazeSessionMode
import dev.orestegabo.kaze.presentation.app.KazeAppViewModel
import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.presentation.demo.repository.DemoExperienceRepository
import dev.orestegabo.kaze.presentation.demo.repository.DemoMapRepository
import dev.orestegabo.kaze.presentation.events.EventsActionResult
import dev.orestegabo.kaze.presentation.events.EventsViewModel
import dev.orestegabo.kaze.presentation.explore.ExploreActionResult
import dev.orestegabo.kaze.presentation.explore.ExploreViewModel
import dev.orestegabo.kaze.presentation.map.MapViewModel
import dev.orestegabo.kaze.theme.KazeThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FeatureViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun app_view_model_shows_onboarding_for_first_launch() = runTest(testDispatcher) {
        val secureStore = RecordingSecureStore()

        val viewModel = KazeAppViewModel(secureStore)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.isReady)
        assertTrue(viewModel.uiState.isOnboardingVisible)
        assertEquals(0, viewModel.uiState.onboardingPage)
    }

    @Test
    fun app_view_model_completes_onboarding_and_auto_dismisses_feedback() = runTest(testDispatcher) {
        val secureStore = RecordingSecureStore()
        val viewModel = KazeAppViewModel(secureStore)
        advanceUntilIdle()

        viewModel.completeOnboarding()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isOnboardingVisible)
        assertEquals(null, viewModel.uiState.sessionMode)
        assertEquals("true", secureStore.values["app.has_seen_onboarding"])

        viewModel.showFeedback("Request sent")
        assertEquals("Request sent", viewModel.uiState.feedbackMessage)

        advanceTimeBy(2400)
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.feedbackMessage)
    }

    @Test
    fun app_view_model_loads_persisted_theme_mode() = runTest(testDispatcher) {
        val secureStore = RecordingSecureStore(
            initialValues = mutableMapOf("app.theme_mode" to KazeThemeMode.DARK.name),
        )

        val viewModel = KazeAppViewModel(secureStore)
        advanceUntilIdle()

        assertEquals(KazeThemeMode.DARK, viewModel.uiState.themeMode)
    }

    @Test
    fun app_view_model_persists_theme_mode_changes() = runTest(testDispatcher) {
        val secureStore = RecordingSecureStore()
        val viewModel = KazeAppViewModel(secureStore)
        advanceUntilIdle()

        viewModel.onThemeModeChanged(KazeThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(KazeThemeMode.DARK, viewModel.uiState.themeMode)
        assertEquals(KazeThemeMode.DARK.name, secureStore.values["app.theme_mode"])
    }

    @Test
    fun app_view_model_starts_guest_session_and_logout_returns_to_entry() = runTest(testDispatcher) {
        val secureStore = RecordingSecureStore()
        val viewModel = KazeAppViewModel(secureStore)
        advanceUntilIdle()

        viewModel.continueAsGuest()
        advanceUntilIdle()

        assertEquals(KazeSessionMode.GUEST, viewModel.uiState.sessionMode)
        assertEquals(KazeSessionMode.GUEST.name, secureStore.values["app.session_mode"])
        assertEquals(null, secureStore.values["auth.access_token"])

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.sessionMode)
        assertEquals(null, secureStore.values["app.session_mode"])
        assertEquals(null, secureStore.values["app.session_email"])
    }

    @Test
    fun app_view_model_starts_authenticated_session_for_login() = runTest(testDispatcher) {
        val secureStore = RecordingSecureStore()
        val viewModel = KazeAppViewModel(secureStore)
        advanceUntilIdle()

        viewModel.signIn(" Aline@Example.com ", "Password123!")
        advanceUntilIdle()

        assertEquals(KazeSessionMode.AUTHENTICATED, viewModel.uiState.sessionMode)
        assertEquals("aline@example.com", viewModel.uiState.sessionEmail)
        assertEquals(KazeSessionMode.AUTHENTICATED.name, secureStore.values["app.session_mode"])
        assertEquals("aline@example.com", secureStore.values["app.session_email"])
        assertEquals("demo-local-session", secureStore.values["auth.access_token"])
    }

    @Test
    fun app_view_model_opens_map_route() = runTest(testDispatcher) {
        val viewModel = KazeAppViewModel(RecordingSecureStore())
        advanceUntilIdle()

        viewModel.openMapRoute(
            route = "Route to spa",
            floorId = "spa-floor",
            floorLabel = "Spa",
        )

        assertEquals(KazeDestination.MAP, viewModel.uiState.currentDestination)
        assertEquals("Route to spa", viewModel.uiState.activeMapTarget.route)
        assertEquals("spa-floor", viewModel.uiState.activeMapTarget.floorId)
        assertEquals("Spa", viewModel.uiState.activeMapTarget.floorLabel)
    }

    @Test
    fun events_view_model_loads_days_and_updates_selected_schedule() {
        val viewModel = EventsViewModel(
            hotelId = "rw-kgl-marriott",
            experienceRepository = DemoExperienceRepository(),
        )

        assertTrue(viewModel.uiState.days.isNotEmpty())
        assertNotNull(viewModel.uiState.selectedDay)
        assertTrue(viewModel.uiState.sessions.isNotEmpty())

        val nextDay = viewModel.uiState.days.last()
        viewModel.onDaySelected(nextDay)

        assertEquals(nextDay, viewModel.uiState.selectedDay)
        assertTrue(viewModel.uiState.sessions.all { it.dayId == nextDay.id })
    }

    @Test
    fun events_action_uses_ballroom_to_route_guest_to_upper_floor() {
        val viewModel = EventsViewModel(
            hotelId = "rw-kgl-marriott",
            experienceRepository = DemoExperienceRepository(),
        )
        val ballroomDay = viewModel.uiState.days.first { day ->
            viewModel.onDaySelected(day)
            viewModel.uiState.sessions.any { it.venueLabel.contains("Ballroom", ignoreCase = true) }
        }
        viewModel.onDaySelected(ballroomDay)
        val ballroomSession = viewModel.uiState.sessions.first { it.venueLabel.contains("Ballroom", ignoreCase = true) }

        val result = viewModel.onSessionAction(ballroomSession)

        assertIs<EventsActionResult.NavigateToMap>(result)
        assertEquals("l9", result.floorId)
        assertTrue(result.route.contains(ballroomSession.venueLabel))
    }

    @Test
    fun explore_view_model_routes_map_actions_and_returns_feedback_for_non_map_actions() {
        val viewModel = ExploreViewModel(
            hotelId = "rw-kgl-marriott",
            experienceRepository = DemoExperienceRepository(),
        )

        assertTrue(viewModel.uiState.highlights.isNotEmpty())

        val mapHighlight = viewModel.uiState.highlights.first {
            it.actionLabel == "Open amenity map" || it.actionLabel == "Open amenity" || it.actionLabel == "Start route"
        }
        val feedbackHighlight = viewModel.uiState.highlights.first { it.actionLabel !in setOf("Open amenity map", "Open amenity", "Start route") }

        val mapResult = viewModel.onHighlightAction(mapHighlight)
        val feedbackResult = viewModel.onHighlightAction(feedbackHighlight)

        assertIs<ExploreActionResult.NavigateToMap>(mapResult)
        assertEquals("l1", mapResult.floorId)
        assertTrue(mapResult.route.contains(mapHighlight.locationLabel))

        assertIs<ExploreActionResult.Feedback>(feedbackResult)
        assertTrue(feedbackResult.message.contains(feedbackHighlight.title))
    }

    @Test
    fun map_view_model_loads_map_and_cycles_floors() {
        val viewModel = MapViewModel(
            hotelId = "rw-kgl-marriott",
            mapId = "temporary-svg-venue",
            mapRepository = DemoMapRepository(),
        )

        assertNotNull(viewModel.uiState.hotelMap)
        assertEquals("l1", viewModel.uiState.selectedFloorId)
        assertTrue(viewModel.uiState.floors.size >= 2)

        viewModel.applyNavigationTarget(
            dev.orestegabo.kaze.presentation.navigation.MapNavigationTarget(
                route = "Arrival route to Keynote, Room 14",
                floorId = "l9",
                floorLabel = "First floor",
            ),
        )
        assertEquals("Arrival route to Keynote, Room 14", viewModel.uiState.activeRoute)
        assertEquals("l9", viewModel.uiState.selectedFloorId)

        viewModel.onSwitchFloor()
        assertEquals("l1", viewModel.uiState.selectedFloorId)
    }

    private class RecordingSecureStore(
        initialValues: MutableMap<String, String> = mutableMapOf(),
    ) : SecureStore {
        val values = initialValues

        override suspend fun put(key: String, value: String) {
            values[key] = value
        }

        override suspend fun get(key: String): String? = values[key]

        override suspend fun remove(key: String) {
            values.remove(key)
        }
    }
}
