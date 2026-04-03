package dev.orestegabo.kaze.presentation.app

import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.presentation.navigation.MapNavigationTarget

internal data class KazeAppUiState(
    val currentDestination: KazeDestination = KazeDestination.STAY,
    val activeMapTarget: MapNavigationTarget = MapNavigationTarget(),
    val feedbackMessage: String = "",
)
