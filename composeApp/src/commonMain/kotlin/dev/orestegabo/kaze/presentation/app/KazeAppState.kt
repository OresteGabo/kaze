package dev.orestegabo.kaze.presentation.app

import dev.orestegabo.kaze.presentation.auth.AuthEventSummary
import dev.orestegabo.kaze.presentation.auth.AuthInvitationSummary
import dev.orestegabo.kaze.presentation.demo.KazeDestination
import dev.orestegabo.kaze.presentation.navigation.MapNavigationTarget
import dev.orestegabo.kaze.theme.KazeThemeMode

internal data class KazeAppUiState(
    val isReady: Boolean = false,
    val isStartupTakingTooLong: Boolean = false,
    val isOnboardingVisible: Boolean = false,
    val onboardingPage: Int = 0,
    val sessionMode: KazeSessionMode? = null,
    val sessionUserId: String = "",
    val sessionEmail: String = "",
    val sessionDisplayName: String = "",
    val sessionUsername: String = "",
    val sessionPhoneNumber: String = "",
    val sessionInvitations: List<AuthInvitationSummary> = emptyList(),
    val sessionEvents: List<AuthEventSummary> = emptyList(),
    val themeMode: KazeThemeMode = KazeThemeMode.SYSTEM,
    val edgeAiEnabled: Boolean = true,
    val privacyConsent: KazePrivacyConsent = KazePrivacyConsent(),
    val currentDestination: KazeDestination = KazeDestination.HOME,
    val activeMapTarget: MapNavigationTarget = MapNavigationTarget(),
    val feedbackMessage: String = "",
    val successCelebration: KazeSuccessCelebration? = null,
)

internal enum class KazeSessionMode {
    AUTHENTICATED,
    GUEST,
}

internal data class KazeSuccessCelebration(
    val title: String,
    val subtitle: String,
)

internal data class KazePrivacyConsent(
    val mapAndVenueActivityEnabled: Boolean = true,
    val diagnosticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
)
