package dev.orestegabo.kaze.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.orestegabo.kaze.platform.SecureStore
import dev.orestegabo.kaze.presentation.auth.AuthDeepLinks
import dev.orestegabo.kaze.presentation.auth.AuthGateway
import dev.orestegabo.kaze.presentation.auth.AuthPrivacyConsent
import dev.orestegabo.kaze.presentation.auth.AuthUser
import dev.orestegabo.kaze.presentation.auth.AuthSession
import dev.orestegabo.kaze.presentation.auth.ExternalUrlLauncher
import dev.orestegabo.kaze.presentation.auth.NativeSocialAuthLauncher
import dev.orestegabo.kaze.presentation.auth.NoopExternalUrlLauncher
import dev.orestegabo.kaze.presentation.auth.NoopAuthGateway
import dev.orestegabo.kaze.presentation.auth.NoopNativeSocialAuthLauncher
import dev.orestegabo.kaze.presentation.auth.ReservationDraftSubmissionRequest
import dev.orestegabo.kaze.presentation.auth.ReservationResponse
import dev.orestegabo.kaze.presentation.auth.SocialAuthProvider
import dev.orestegabo.kaze.presentation.auth.toProfileMessage
import dev.orestegabo.kaze.presentation.auth.toAuthMessage
import dev.orestegabo.kaze.presentation.auth.toSignupMessage
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
    private val authGateway: AuthGateway = NoopAuthGateway,
    private val externalUrlLauncher: ExternalUrlLauncher = NoopExternalUrlLauncher,
    private val nativeSocialAuthLauncher: NativeSocialAuthLauncher = NoopNativeSocialAuthLauncher,
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
            val persistedEdgeAiEnabled = secureStore.get(EDGE_AI_ENABLED_KEY)?.toBooleanStrictOrNull() ?: true
            val persistedPrivacyConsent = KazePrivacyConsent(
                mapAndVenueActivityEnabled = secureStore.get(PRIVACY_MAP_ACTIVITY_ENABLED_KEY)?.toBooleanStrictOrNull() ?: true,
                diagnosticsEnabled = secureStore.get(PRIVACY_DIAGNOSTICS_ENABLED_KEY)?.toBooleanStrictOrNull() ?: true,
                notificationsEnabled = secureStore.get(PRIVACY_NOTIFICATIONS_ENABLED_KEY)?.toBooleanStrictOrNull() ?: true,
                analyticsEnabled = secureStore.get(PRIVACY_ANALYTICS_ENABLED_KEY)?.toBooleanStrictOrNull() ?: false,
            )
            uiState = uiState.copy(
                isReady = true,
                isStartupTakingTooLong = false,
                isOnboardingVisible = !hasSeenOnboarding,
                onboardingPage = 0,
                sessionMode = persistedSessionMode,
                sessionUserId = secureStore.get(SESSION_USER_ID_KEY).orEmpty(),
                sessionEmail = secureStore.get(SESSION_EMAIL_KEY).orEmpty(),
                sessionDisplayName = secureStore.get(SESSION_DISPLAY_NAME_KEY).orEmpty(),
                sessionUsername = secureStore.get(SESSION_USERNAME_KEY).orEmpty(),
                sessionPhoneNumber = secureStore.get(SESSION_PHONE_NUMBER_KEY).orEmpty(),
                themeMode = persistedThemeMode,
                edgeAiEnabled = persistedEdgeAiEnabled,
                privacyConsent = persistedPrivacyConsent,
            )
            if (persistedSessionMode == KazeSessionMode.AUTHENTICATED) {
                refreshProfileFromServer()
            }
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

    fun onEdgeAiEnabledChanged(enabled: Boolean) {
        uiState = uiState.copy(edgeAiEnabled = enabled)
        scope.launch {
            secureStore.put(EDGE_AI_ENABLED_KEY, enabled.toString())
        }
    }

    fun onMapAndVenueActivityConsentChanged(enabled: Boolean) {
        updatePrivacyConsent(uiState.privacyConsent.copy(mapAndVenueActivityEnabled = enabled))
    }

    fun onDiagnosticsConsentChanged(enabled: Boolean) {
        updatePrivacyConsent(uiState.privacyConsent.copy(diagnosticsEnabled = enabled))
    }

    fun onNotificationsConsentChanged(enabled: Boolean) {
        updatePrivacyConsent(uiState.privacyConsent.copy(notificationsEnabled = enabled))
    }

    fun onAnalyticsConsentChanged(enabled: Boolean) {
        updatePrivacyConsent(uiState.privacyConsent.copy(analyticsEnabled = enabled))
    }

    private fun updatePrivacyConsent(consent: KazePrivacyConsent) {
        uiState = uiState.copy(
            privacyConsent = consent,
        )
        scope.launch {
            persistPrivacyConsent(consent)
            if (uiState.sessionMode == KazeSessionMode.AUTHENTICATED) {
                val accessToken = secureStore.get(AUTH_TOKEN_KEY)
                if (!accessToken.isNullOrBlank()) {
                    runCatching {
                        authGateway.updatePrivacyConsent(
                            accessToken = accessToken,
                            privacyConsent = consent,
                        )
                    }.onSuccess(::applyAuthUserState)
                }
            }
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
                    val needsProfileCompletion = session.displayName.isNullOrBlank()
                    startAuthenticatedSession(
                        session = session,
                        feedback = if (needsProfileCompletion) {
                            "Account created. Add your name and phone to complete your profile."
                        } else {
                            "Account created. Your Kaze session is ready."
                        },
                    )
                    if (needsProfileCompletion) {
                        navigator.goTo(KazeDestination.SETTINGS)
                        syncNavigationState()
                    }
                }
                .onFailure { showFeedback(it.toSignupMessage()) }
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
            val nativeResult = runCatching { nativeSocialAuthLauncher.signIn(socialProvider) }
                .onFailure { showFeedback(it.toAuthMessage()) }
                .getOrNull()

            if (nativeResult != null) {
                runCatching {
                    authGateway.signInWithCredential(
                        provider = socialProvider,
                        credential = nativeResult.credential,
                        credentialType = nativeResult.credentialType,
                        displayName = nativeResult.displayName,
                    )
                }.onSuccess { session ->
                    startAuthenticatedSession(
                        session = session,
                        feedback = "${socialProvider.displayName} sign-in is ready.",
                    )
                }.onFailure { showFeedback(it.toAuthMessage()) }
                return@launch
            }

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
            displayName = "",
            feedback = "Guest mode is open. You can browse public Kaze information.",
        )
    }

    fun logout() {
        val refreshTokenJob = scope.launch {
            val accessToken = secureStore.get(AUTH_TOKEN_KEY)
            val refreshToken = secureStore.get(REFRESH_TOKEN_KEY)
            runCatching { authGateway.logout(accessToken, refreshToken) }
        }
        navigator.goTo(KazeDestination.HOME)
        uiState = uiState.copy(
            sessionMode = null,
            sessionUserId = "",
            sessionEmail = "",
            sessionDisplayName = "",
            sessionUsername = "",
            sessionPhoneNumber = "",
            sessionInvitations = emptyList(),
            sessionEvents = emptyList(),
            sessionActiveStay = null,
            currentDestination = navigator.state.currentDestination,
            activeMapTarget = navigator.state.mapTarget,
            feedbackMessage = "",
        )
        scope.launch {
            refreshTokenJob.join()
            secureStore.remove(SESSION_MODE_KEY)
            secureStore.remove(SESSION_USER_ID_KEY)
            secureStore.remove(SESSION_EMAIL_KEY)
            secureStore.remove(SESSION_DISPLAY_NAME_KEY)
            secureStore.remove(SESSION_USERNAME_KEY)
            secureStore.remove(SESSION_PHONE_NUMBER_KEY)
            secureStore.remove(AUTH_TOKEN_KEY)
            secureStore.remove(REFRESH_TOKEN_KEY)
        }
    }

    fun updateProfile(displayName: String, username: String, phoneNumber: String) {
        val normalizedDisplayName = displayName.trim()
        if (normalizedDisplayName.isBlank()) {
            showFeedback("Add your full name before saving.")
            return
        }
        scope.launch {
            val accessToken = secureStore.get(AUTH_TOKEN_KEY)
            if (accessToken.isNullOrBlank()) {
                showFeedback("Sign in again to update your profile.")
                return@launch
            }
            runCatching {
                authGateway.updateProfile(
                    accessToken = accessToken,
                    displayName = normalizedDisplayName,
                    username = username.trim().takeIf { it.isNotBlank() },
                    phoneNumber = phoneNumber.trim().takeIf { it.isNotBlank() },
                    privacyConsent = uiState.privacyConsent,
                )
            }
                .onSuccess {
                    applyAuthUserState(it)
                    showFeedback("Profile updated.")
                }
                .onFailure { showFeedback(it.toProfileMessage()) }
        }
    }

    fun respondToInvitation(invitationId: String, response: String) {
        scope.launch {
            val accessToken = secureStore.get(AUTH_TOKEN_KEY)
            if (accessToken.isNullOrBlank()) {
                showFeedback("Sign in again to update this invitation.")
                return@launch
            }
            runCatching { authGateway.respondToInvitation(accessToken, invitationId, response) }
                .onSuccess { updated ->
                    uiState = uiState.copy(
                        sessionInvitations = uiState.sessionInvitations.map { invitation ->
                            if (invitation.id == invitationId) updated else invitation
                        },
                    )
                    showFeedback(
                        if (response.equals("accept", ignoreCase = true)) "Invitation accepted."
                        else "Invitation declined.",
                    )
                }
                .onFailure { showFeedback("Could not update this invitation right now.") }
        }
    }

    suspend fun submitReservation(request: ReservationDraftSubmissionRequest): ReservationResponse {
        val accessToken = secureStore.get(AUTH_TOKEN_KEY)
        require(!accessToken.isNullOrBlank()) { "Sign in before saving a reservation request." }
        return authGateway.submitReservation(accessToken, request).also {
            refreshSessionContentFromServer(accessToken)
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
            userId = session.userId,
            email = session.email,
            displayName = session.displayName.orEmpty(),
            username = session.username.orEmpty(),
            phoneNumber = session.phoneNumber.orEmpty(),
            privacyConsent = session.privacyConsent.toKazePrivacyConsent(),
            feedback = feedback,
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
        )
    }

    private fun startSession(
        mode: KazeSessionMode,
        userId: String = "",
        email: String,
        displayName: String,
        username: String = "",
        phoneNumber: String = "",
        privacyConsent: KazePrivacyConsent = uiState.privacyConsent,
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
            sessionUserId = userId,
            sessionEmail = email,
            sessionDisplayName = displayName,
            sessionUsername = username,
            sessionPhoneNumber = phoneNumber,
            privacyConsent = privacyConsent,
            sessionInvitations = if (mode == KazeSessionMode.AUTHENTICATED) uiState.sessionInvitations else emptyList(),
            sessionEvents = if (mode == KazeSessionMode.AUTHENTICATED) uiState.sessionEvents else emptyList(),
            sessionActiveStay = if (mode == KazeSessionMode.AUTHENTICATED) uiState.sessionActiveStay else null,
            currentDestination = navigator.state.currentDestination,
            activeMapTarget = navigator.state.mapTarget,
        )
        scope.launch {
            secureStore.put(SESSION_MODE_KEY, mode.name)
            if (userId.isBlank()) {
                secureStore.remove(SESSION_USER_ID_KEY)
            } else {
                secureStore.put(SESSION_USER_ID_KEY, userId)
            }
            if (email.isBlank()) {
                secureStore.remove(SESSION_EMAIL_KEY)
            } else {
                secureStore.put(SESSION_EMAIL_KEY, email)
            }
            if (displayName.isBlank()) {
                secureStore.remove(SESSION_DISPLAY_NAME_KEY)
            } else {
                secureStore.put(SESSION_DISPLAY_NAME_KEY, displayName)
            }
            if (username.isBlank()) {
                secureStore.remove(SESSION_USERNAME_KEY)
            } else {
                secureStore.put(SESSION_USERNAME_KEY, username)
            }
            if (phoneNumber.isBlank()) {
                secureStore.remove(SESSION_PHONE_NUMBER_KEY)
            } else {
                secureStore.put(SESSION_PHONE_NUMBER_KEY, phoneNumber)
            }
            if (mode == KazeSessionMode.AUTHENTICATED) {
                if (accessToken.isNullOrBlank()) {
                    secureStore.remove(AUTH_TOKEN_KEY)
                } else {
                    secureStore.put(AUTH_TOKEN_KEY, accessToken)
                }
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
        if (mode == KazeSessionMode.AUTHENTICATED && !accessToken.isNullOrBlank()) {
            refreshSessionContentFromServer(accessToken)
        } else {
            uiState = uiState.copy(
                sessionInvitations = emptyList(),
                sessionEvents = emptyList(),
                sessionActiveStay = null,
            )
        }
        showFeedback(feedback)
    }

    private fun refreshProfileFromServer() {
        scope.launch {
            val accessToken = secureStore.get(AUTH_TOKEN_KEY)
            if (accessToken.isNullOrBlank()) return@launch
            runCatching { authGateway.getProfile(accessToken) }
                .onSuccess { user ->
                    applyAuthUserState(user)
                    refreshSessionContentFromServer(accessToken)
                }
        }
    }

    private fun applyAuthUserState(user: AuthUser) {
        applyProfileState(
            userId = user.id,
            email = user.email,
            displayName = user.displayName.orEmpty(),
            username = user.username.orEmpty(),
            phoneNumber = user.phoneNumber.orEmpty(),
            privacyConsent = user.privacyConsent.toKazePrivacyConsent(),
        )
    }

    private fun refreshSessionContentFromServer(accessToken: String) {
        scope.launch {
            val invitations = runCatching { authGateway.getInvitations(accessToken) }.getOrDefault(emptyList())
            val events = runCatching { authGateway.getEvents(accessToken) }.getOrDefault(emptyList())
            val activeStay = runCatching { authGateway.getActiveStay(accessToken) }.getOrNull()
            uiState = uiState.copy(
                sessionInvitations = invitations,
                sessionEvents = events,
                sessionActiveStay = activeStay,
            )
        }
    }

    private fun applyProfileState(
        userId: String,
        email: String,
        displayName: String,
        username: String,
        phoneNumber: String,
        privacyConsent: KazePrivacyConsent = uiState.privacyConsent,
    ) {
        uiState = uiState.copy(
            sessionUserId = userId,
            sessionEmail = email,
            sessionDisplayName = displayName,
            sessionUsername = username,
            sessionPhoneNumber = phoneNumber,
            privacyConsent = privacyConsent,
        )
        scope.launch {
            if (userId.isBlank()) {
                secureStore.remove(SESSION_USER_ID_KEY)
            } else {
                secureStore.put(SESSION_USER_ID_KEY, userId)
            }
            if (email.isBlank()) {
                secureStore.remove(SESSION_EMAIL_KEY)
            } else {
                secureStore.put(SESSION_EMAIL_KEY, email)
            }
            if (displayName.isBlank()) {
                secureStore.remove(SESSION_DISPLAY_NAME_KEY)
            } else {
                secureStore.put(SESSION_DISPLAY_NAME_KEY, displayName)
            }
            if (username.isBlank()) {
                secureStore.remove(SESSION_USERNAME_KEY)
            } else {
                secureStore.put(SESSION_USERNAME_KEY, username)
            }
            if (phoneNumber.isBlank()) {
                secureStore.remove(SESSION_PHONE_NUMBER_KEY)
            } else {
                secureStore.put(SESSION_PHONE_NUMBER_KEY, phoneNumber)
            }
            persistPrivacyConsent(privacyConsent)
        }
    }

    private suspend fun persistPrivacyConsent(consent: KazePrivacyConsent) {
        secureStore.put(PRIVACY_MAP_ACTIVITY_ENABLED_KEY, consent.mapAndVenueActivityEnabled.toString())
        secureStore.put(PRIVACY_DIAGNOSTICS_ENABLED_KEY, consent.diagnosticsEnabled.toString())
        secureStore.put(PRIVACY_NOTIFICATIONS_ENABLED_KEY, consent.notificationsEnabled.toString())
        secureStore.put(PRIVACY_ANALYTICS_ENABLED_KEY, consent.analyticsEnabled.toString())
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
        const val EDGE_AI_ENABLED_KEY = "app.edge_ai_enabled"
        const val PRIVACY_MAP_ACTIVITY_ENABLED_KEY = "app.privacy.map_activity_enabled"
        const val PRIVACY_DIAGNOSTICS_ENABLED_KEY = "app.privacy.diagnostics_enabled"
        const val PRIVACY_NOTIFICATIONS_ENABLED_KEY = "app.privacy.notifications_enabled"
        const val PRIVACY_ANALYTICS_ENABLED_KEY = "app.privacy.analytics_enabled"
        const val SESSION_MODE_KEY = "app.session_mode"
        const val SESSION_USER_ID_KEY = "app.session_user_id"
        const val SESSION_EMAIL_KEY = "app.session_email"
        const val SESSION_DISPLAY_NAME_KEY = "app.session_display_name"
        const val SESSION_USERNAME_KEY = "app.session_username"
        const val SESSION_PHONE_NUMBER_KEY = "app.session_phone_number"
        const val AUTH_TOKEN_KEY = "auth.access_token"
        const val REFRESH_TOKEN_KEY = "auth.refresh_token"
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

private fun AuthPrivacyConsent.toKazePrivacyConsent(): KazePrivacyConsent =
    KazePrivacyConsent(
        mapAndVenueActivityEnabled = mapAndVenueActivityEnabled,
        diagnosticsEnabled = diagnosticsEnabled,
        notificationsEnabled = notificationsEnabled,
        analyticsEnabled = analyticsEnabled,
    )
