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
            val persistedSessionMode = secureStore.get(SESSION_MODE_KEY).toSessionModeOrNull()
            uiState = uiState.copy(
                isReady = true,
                isOnboardingVisible = !hasSeenOnboarding,
                onboardingPage = 0,
                sessionMode = persistedSessionMode,
                sessionEmail = secureStore.get(SESSION_EMAIL_KEY).orEmpty(),
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

    fun signIn(email: String, password: String) {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || password.isBlank()) {
            showFeedback("Enter your email and password to continue.")
            return
        }
        startSession(
            mode = KazeSessionMode.AUTHENTICATED,
            email = normalizedEmail,
            feedback = "Welcome back. Your Kaze session is ready.",
        )
    }

    fun createAccount(email: String, password: String) {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || password.length < MIN_PASSWORD_LENGTH) {
            showFeedback("Use an email and a password with at least $MIN_PASSWORD_LENGTH characters.")
            return
        }
        startSession(
            mode = KazeSessionMode.AUTHENTICATED,
            email = normalizedEmail,
            feedback = "Account created for this demo session.",
        )
    }

    fun signInWithSocialProvider(provider: String) {
        startSession(
            mode = KazeSessionMode.AUTHENTICATED,
            email = "${provider.trim().lowercase()}@kaze.demo",
            feedback = "Continuing with $provider. Your Kaze session is ready.",
        )
    }

    fun continueAsGuest() {
        startSession(
            mode = KazeSessionMode.GUEST,
            email = "",
            feedback = "Guest mode is open. You can browse public Kaze information.",
        )
    }

    fun logout() {
        navigator.goTo(KazeDestination.HOME)
        uiState = uiState.copy(
            sessionMode = null,
            sessionEmail = "",
            currentDestination = navigator.state.currentDestination,
            activeMapTarget = navigator.state.mapTarget,
            feedbackMessage = "",
        )
        scope.launch {
            secureStore.remove(SESSION_MODE_KEY)
            secureStore.remove(SESSION_EMAIL_KEY)
            secureStore.remove(AUTH_TOKEN_KEY)
        }
    }

    private fun startSession(
        mode: KazeSessionMode,
        email: String,
        feedback: String,
    ) {
        navigator.goTo(KazeDestination.HOME)
        uiState = uiState.copy(
            sessionMode = mode,
            sessionEmail = email,
            currentDestination = navigator.state.currentDestination,
            activeMapTarget = navigator.state.mapTarget,
        )
        scope.launch {
            secureStore.put(SESSION_MODE_KEY, mode.name)
            if (email.isBlank()) {
                secureStore.remove(SESSION_EMAIL_KEY)
            } else {
                secureStore.put(SESSION_EMAIL_KEY, email)
            }
            if (mode == KazeSessionMode.AUTHENTICATED) {
                secureStore.put(AUTH_TOKEN_KEY, DEMO_AUTH_TOKEN)
            } else {
                secureStore.remove(AUTH_TOKEN_KEY)
            }
        }
        showFeedback(feedback)
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

    private fun String?.toSessionModeOrNull(): KazeSessionMode? =
        this?.let { value ->
            KazeSessionMode.entries.firstOrNull { mode -> mode.name == value }
        }

    private companion object {
        const val HAS_SEEN_ONBOARDING_KEY = "app.has_seen_onboarding"
        const val THEME_MODE_KEY = "app.theme_mode"
        const val SESSION_MODE_KEY = "app.session_mode"
        const val SESSION_EMAIL_KEY = "app.session_email"
        const val AUTH_TOKEN_KEY = "auth.access_token"
        const val DEMO_AUTH_TOKEN = "demo-local-session"
        const val TRUE_VALUE = "true"
        const val FEEDBACK_DURATION_MS = 2400L
        const val MIN_PASSWORD_LENGTH = 8
    }
}
