package dev.orestegabo.kaze.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.presentation.navigation.KazeNavigator
import dev.orestegabo.kaze.presentation.navigation.MapNavigationTarget

internal class KazeAppViewModel(
    private val navigator: KazeNavigator = KazeNavigator(),
) : ViewModel() {
    var uiState by mutableStateOf(
        KazeAppUiState(
            currentDestination = navigator.state.currentDestination,
            activeMapTarget = navigator.state.mapTarget,
        ),
    )
        private set

    fun onDestinationSelected(destination: KazeDestination) {
        navigator.goTo(destination)
        syncNavigationState()
    }

    fun dismissFeedback() {
        uiState = uiState.copy(feedbackMessage = "")
    }

    fun showFeedback(message: String) {
        uiState = uiState.copy(feedbackMessage = message)
    }

    fun openMapRoute(route: String, floorId: String, floorLabel: String) {
        navigator.openMap(
            MapNavigationTarget(
                route = route,
                floorId = floorId,
                floorLabel = floorLabel,
            ),
        )
        syncNavigationState()
    }

    fun openEvents() {
        navigator.openEvents()
        syncNavigationState()
    }

    private fun syncNavigationState() {
        uiState = uiState.copy(
            currentDestination = navigator.state.currentDestination,
            activeMapTarget = navigator.state.mapTarget,
        )
    }
}
