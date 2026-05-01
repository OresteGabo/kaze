package dev.orestegabo.kaze.ui.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import dev.orestegabo.kaze.presentation.app.KazePrivacyConsent
import dev.orestegabo.kaze.theme.KazeThemeMode

@Composable
internal fun HomeSettingsScreen(
    bottomContentPadding: Dp,
    themeMode: KazeThemeMode,
    edgeAiEnabled: Boolean,
    privacyConsent: KazePrivacyConsent,
    sessionLabel: String,
    sessionDisplayName: String,
    sessionUsername: String,
    sessionEmail: String,
    sessionPhoneNumber: String,
    needsProfileCompletion: Boolean,
    onThemeModeChange: (KazeThemeMode) -> Unit,
    onEdgeAiEnabledChange: (Boolean) -> Unit,
    onMapAndVenueActivityConsentChange: (Boolean) -> Unit,
    onDiagnosticsConsentChange: (Boolean) -> Unit,
    onNotificationsConsentChange: (Boolean) -> Unit,
    onAnalyticsConsentChange: (Boolean) -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    dev.orestegabo.kaze.ui.home.settings.HomeSettingsScreen(
        bottomContentPadding = bottomContentPadding,
        themeMode = themeMode,
        edgeAiEnabled = edgeAiEnabled,
        privacyConsent = privacyConsent,
        sessionLabel = sessionLabel,
        sessionDisplayName = sessionDisplayName,
        sessionUsername = sessionUsername,
        sessionEmail = sessionEmail,
        sessionPhoneNumber = sessionPhoneNumber,
        needsProfileCompletion = needsProfileCompletion,
        onThemeModeChange = onThemeModeChange,
        onEdgeAiEnabledChange = onEdgeAiEnabledChange,
        onMapAndVenueActivityConsentChange = onMapAndVenueActivityConsentChange,
        onDiagnosticsConsentChange = onDiagnosticsConsentChange,
        onNotificationsConsentChange = onNotificationsConsentChange,
        onAnalyticsConsentChange = onAnalyticsConsentChange,
        onUpdateProfile = onUpdateProfile,
        onLogout = onLogout,
        onBack = onBack,
    )
}
