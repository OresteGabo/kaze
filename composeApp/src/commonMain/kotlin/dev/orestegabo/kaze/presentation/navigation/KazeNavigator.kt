package dev.orestegabo.kaze.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.orestegabo.kaze.presentation.demo.KazeDestination

internal data class MapNavigationTarget(
    val route: String = "Guest arrival to Great Rift Ballroom",
    val floorId: String = "l1",
    val floorLabel: String = "Ground floor",
)

internal data class KazeNavigationState(
    val currentDestination: KazeDestination = KazeDestination.STAY,
    val mapTarget: MapNavigationTarget = MapNavigationTarget(),
)

internal class KazeNavigator(
    initialState: KazeNavigationState = KazeNavigationState(),
) {
    var state by mutableStateOf(initialState)
        private set

    fun goTo(destination: KazeDestination) {
        state = state.copy(currentDestination = destination)
    }

    fun openEvents() {
        goTo(KazeDestination.EVENTS)
    }

    fun openMap(target: MapNavigationTarget) {
        state = state.copy(
            currentDestination = KazeDestination.MAP,
            mapTarget = target,
        )
    }
}
