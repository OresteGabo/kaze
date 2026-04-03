package dev.orestegabo.kaze.presentation

import dev.orestegabo.kaze.presentation.demo.repository.DemoExperienceRepository
import dev.orestegabo.kaze.presentation.events.EventsViewModel
import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.presentation.navigation.KazeNavigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PresentationArchitectureTest {

    @Test
    fun navigator_opens_map_destination() {
        val navigator = KazeNavigator()

        navigator.openMap(
            dev.orestegabo.kaze.presentation.navigation.MapNavigationTarget(
                route = "Arrival route to Pool Deck",
                floorId = "l1",
                floorLabel = "Pool Deck",
            ),
        )

        assertEquals(KazeDestination.MAP, navigator.state.currentDestination)
        assertEquals("Arrival route to Pool Deck", navigator.state.mapTarget.route)
    }

    @Test
    fun events_view_model_loads_days_and_sessions() {
        val viewModel = EventsViewModel(
            hotelId = "rw-kgl-marriott",
            experienceRepository = DemoExperienceRepository(),
        )

        assertTrue(viewModel.uiState.days.isNotEmpty())
        assertTrue(viewModel.uiState.sessions.isNotEmpty())
    }
}
