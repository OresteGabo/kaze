package dev.orestegabo.kaze.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.orestegabo.kaze.platform.SecureStore
import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.presentation.navigation.KazeNavigator
import dev.orestegabo.kaze.presentation.navigation.MapNavigationTarget
import dev.orestegabo.kaze.theme.KazeThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class KazeAppViewModel(
    private val secureStore: SecureStore,
    private val navigator: KazeNavigator = KazeNavigator(),
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var feedbackDismissJob: Job? = null

    var uiState by mutableStateOf(
        KazeAppUiState(
            currentDestination = navigator.state.currentDestination,
            activeMapTarget = navigator.state.mapTarget,
        ),
    )
        private set

    init {
        scope.launch {
            val hasSeenOnboarding = secureStore.get(HAS_SEEN_ONBOARDING_KEY) == TRUE_VALUE
            val persistedThemeMode = secureStore.get(THEME_MODE_KEY).toThemeModeOrDefault()
            uiState = uiState.copy(
                isReady = true,
                isOnboardingVisible = !hasSeenOnboarding,
                onboardingPage = 0,
                themeMode = persistedThemeMode,
            )
        }
    }

    fun onOnboardingPageChanged(page: Int) {
        uiState = uiState.copy(onboardingPage = page.coerceAtLeast(0))
    }

    fun onOnboardingNext() {
        val nextPage = (uiState.onboardingPage + 1).coerceAtMost(2)
        uiState = uiState.copy(onboardingPage = nextPage)
    }

    fun skipOnboarding() {
        persistOnboardingDismissal()
    }

    fun completeOnboarding() {
        persistOnboardingDismissal()
    }

    private fun persistOnboardingDismissal() {
        uiState = uiState.copy(
            isOnboardingVisible = false,
            onboardingPage = 0,
        )
        scope.launch {
            secureStore.put(HAS_SEEN_ONBOARDING_KEY, TRUE_VALUE)
        }
    }

    fun onDestinationSelected(destination: KazeDestination) {
        navigator.goTo(destination)
        syncNavigationState()
    }

    fun dismissFeedback() {
        feedbackDismissJob?.cancel()
        uiState = uiState.copy(feedbackMessage = "")
    }

    fun showFeedback(message: String) {
        uiState = uiState.copy(feedbackMessage = message)
        feedbackDismissJob?.cancel()
        feedbackDismissJob = scope.launch {
            delay(FEEDBACK_DURATION_MS)
            uiState = uiState.copy(feedbackMessage = "")
        }
    }

    fun onThemeModeChanged(themeMode: KazeThemeMode) {
        uiState = uiState.copy(themeMode = themeMode)
        scope.launch {
            secureStore.put(THEME_MODE_KEY, themeMode.name)
        }
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

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }

    private fun String?.toThemeModeOrDefault(): KazeThemeMode =
        this?.let { value ->
            KazeThemeMode.entries.firstOrNull { mode -> mode.name == value }
        } ?: KazeThemeMode.SYSTEM

    private companion object {
        const val HAS_SEEN_ONBOARDING_KEY = "app.has_seen_onboarding"
        const val THEME_MODE_KEY = "app.theme_mode"
        const val TRUE_VALUE = "true"
        const val FEEDBACK_DURATION_MS = 2400L
    }
}
