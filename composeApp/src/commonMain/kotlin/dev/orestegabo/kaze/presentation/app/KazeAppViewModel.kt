package dev.orestegabo.kaze.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.orestegabo.kaze.platform.SecureStore
import dev.orestegabo.kaze.presentation.auth.AuthDeepLinks
import dev.orestegabo.kaze.presentation.auth.AuthGateway
import dev.orestegabo.kaze.presentation.auth.AuthSession
import dev.orestegabo.kaze.presentation.auth.DemoAuthGateway
import dev.orestegabo.kaze.presentation.auth.ExternalUrlLauncher
import dev.orestegabo.kaze.presentation.auth.NoopExternalUrlLauncher
import dev.orestegabo.kaze.presentation.auth.SocialAuthProvider
import dev.orestegabo.kaze.presentation.auth.toAuthMessage
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
    private val authGateway: AuthGateway = DemoAuthGateway,
    private val externalUrlLauncher: ExternalUrlLauncher = NoopExternalUrlLauncher,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var feedbackDismissJob: Job? = null
    private var startupJob: Job? = null
    private var startupTimeoutJob: Job? = null

    var uiState by mutableStateOf(
        KazeAppUiState(
            currentDestination = navigator.state.currentDestination,
            activeMapTarget = navigator.state.mapTarget,
        ),
    )
        private set

    init {
        loadStartupState()
        scope.launch {
            AuthDeepLinks.callbacks.collect { callback ->
                claimSocialSession(callback.loginToken)
            }
        }
    }

    fun retryStartup() {
        loadStartupState()
    }

    private fun loadStartupState() {
        startupJob?.cancel()
        startupTimeoutJob?.cancel()
        uiState = uiState.copy(
            isReady = false,
            isStartupTakingTooLong = false,
            feedbackMessage = "",
        )
        startupTimeoutJob = scope.launch {
            delay(STARTUP_TIMEOUT_MS)
            if (!uiState.isReady) {
                uiState = uiState.copy(isStartupTakingTooLong = true)
            }
        }
        startupJob = scope.launch {
            val hasSeenOnboarding = secureStore.get(HAS_SEEN_ONBOARDING_KEY) == TRUE_VALUE
            val persistedThemeMode = secureStore.get(THEME_MODE_KEY).toThemeModeOrDefault()
            val persistedSessionMode = secureStore.get(SESSION_MODE_KEY).toSessionModeOrNull()
            uiState = uiState.copy(
                isReady = true,
                isStartupTakingTooLong = false,
                isOnboardingVisible = !hasSeenOnboarding,
                onboardingPage = 0,
                sessionMode = persistedSessionMode,
                sessionEmail = secureStore.get(SESSION_EMAIL_KEY).orEmpty(),
                themeMode = persistedThemeMode,
            )
            startupTimeoutJob?.cancel()
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

    fun showSuccessCelebration(title: String, subtitle: String) {
        feedbackDismissJob?.cancel()
        uiState = uiState.copy(
            feedbackMessage = "",
            successCelebration = KazeSuccessCelebration(
                title = title,
                subtitle = subtitle,
            ),
        )
    }

    fun dismissSuccessCelebration() {
        uiState = uiState.copy(successCelebration = null)
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
        scope.launch {
            runCatching { authGateway.signIn(normalizedEmail, password) }
                .onSuccess { session ->
                    startAuthenticatedSession(session, "Welcome back. Your Kaze session is ready.")
                }
                .onFailure { showFeedback(it.toAuthMessage()) }
        }
    }

    fun createAccount(email: String, password: String) {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || password.length < MIN_PASSWORD_LENGTH) {
            showFeedback("Use an email and a password with at least $MIN_PASSWORD_LENGTH characters.")
            return
        }
        scope.launch {
            runCatching { authGateway.createAccount(normalizedEmail, password) }
                .onSuccess { session ->
                    startAuthenticatedSession(session, "Account created. Your Kaze session is ready.")
                }
                .onFailure { showFeedback(it.toAuthMessage()) }
        }
    }

    fun signInWithSocialProvider(provider: String) {
        val socialProvider = provider.toSocialAuthProvider()
        if (socialProvider == null) {
            showFeedback("$provider sign-in is not supported yet.")
            return
        }
        showFeedback("Opening ${socialProvider.displayName} sign-in...")
        scope.launch {
            runCatching { authGateway.startSocialLogin(socialProvider) }
                .onSuccess { response ->
                    if (response.authorizationUrl.isBlank()) {
                        showFeedback("${socialProvider.displayName} sign-in is not available right now. Please try another option.")
                    } else if (!externalUrlLauncher.open(response.authorizationUrl)) {
                        showFeedback("Could not open ${socialProvider.displayName}. Check your device browser settings.")
                    }
                }
                .onFailure { showFeedback(it.toAuthMessage()) }
        }
    }

    fun continueAsGuest() {
        startSession(
            mode = KazeSessionMode.GUEST,
            email = "",
            feedback = "Guest mode is open. You can browse public Kaze information.",
        )
    }

    fun logout() {
        val refreshTokenJob = scope.launch {
            val refreshToken = secureStore.get(REFRESH_TOKEN_KEY)
            runCatching { authGateway.logout(refreshToken) }
        }
        navigator.goTo(KazeDestination.HOME)
        uiState = uiState.copy(
            sessionMode = null,
            sessionEmail = "",
            currentDestination = navigator.state.currentDestination,
            activeMapTarget = navigator.state.mapTarget,
            feedbackMessage = "",
        )
        scope.launch {
            refreshTokenJob.join()
            secureStore.remove(SESSION_MODE_KEY)
            secureStore.remove(SESSION_EMAIL_KEY)
            secureStore.remove(AUTH_TOKEN_KEY)
            secureStore.remove(REFRESH_TOKEN_KEY)
        }
    }

    private fun claimSocialSession(loginToken: String) {
        showFeedback("Finishing secure sign-in...")
        scope.launch {
            runCatching { authGateway.claimSession(loginToken) }
                .onSuccess { session ->
                    startAuthenticatedSession(session, "You are signed in with your real account.")
                }
                .onFailure { showFeedback(it.toAuthMessage()) }
        }
    }

    private fun startAuthenticatedSession(
        session: AuthSession,
        feedback: String,
    ) {
        startSession(
            mode = KazeSessionMode.AUTHENTICATED,
            email = session.email,
            feedback = feedback,
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
        )
    }

    private fun startSession(
        mode: KazeSessionMode,
        email: String,
        feedback: String,
        accessToken: String? = null,
        refreshToken: String? = null,
    ) {
        navigator.goTo(KazeDestination.HOME)
        uiState = uiState.copy(
            isReady = true,
            isStartupTakingTooLong = false,
            isOnboardingVisible = false,
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
                secureStore.put(AUTH_TOKEN_KEY, accessToken ?: DEMO_AUTH_TOKEN)
                if (refreshToken.isNullOrBlank()) {
                    secureStore.remove(REFRESH_TOKEN_KEY)
                } else {
                    secureStore.put(REFRESH_TOKEN_KEY, refreshToken)
                }
            } else {
                secureStore.remove(AUTH_TOKEN_KEY)
                secureStore.remove(REFRESH_TOKEN_KEY)
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
        const val REFRESH_TOKEN_KEY = "auth.refresh_token"
        const val DEMO_AUTH_TOKEN = "demo-local-session"
        const val TRUE_VALUE = "true"
        const val FEEDBACK_DURATION_MS = 2400L
        const val STARTUP_TIMEOUT_MS = 10000L
        const val MIN_PASSWORD_LENGTH = 8
    }
}

private fun String.toSocialAuthProvider(): SocialAuthProvider? =
    when (trim().lowercase()) {
        "google" -> SocialAuthProvider.GOOGLE
        "apple" -> SocialAuthProvider.APPLE
        "facebook", "meta" -> SocialAuthProvider.FACEBOOK
        else -> null
    }
