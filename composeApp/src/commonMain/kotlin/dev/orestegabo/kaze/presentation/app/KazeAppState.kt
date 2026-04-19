package dev.orestegabo.kaze.presentation.app

import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.presentation.navigation.MapNavigationTarget
import dev.orestegabo.kaze.theme.KazeThemeMode

internal data class KazeAppUiState(
    val isReady: Boolean = false,
    val isOnboardingVisible: Boolean = false,
    val onboardingPage: Int = 0,
    val sessionMode: KazeSessionMode? = null,
    val sessionEmail: String = "",
    val themeMode: KazeThemeMode = KazeThemeMode.SYSTEM,
    val currentDestination: KazeDestination = KazeDestination.HOME,
    val activeMapTarget: MapNavigationTarget = MapNavigationTarget(),
    val feedbackMessage: String = "",
)

internal enum class KazeSessionMode {
    AUTHENTICATED,
    GUEST,
}
