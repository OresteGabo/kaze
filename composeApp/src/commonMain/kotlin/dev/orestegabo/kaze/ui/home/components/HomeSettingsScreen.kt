package dev.orestegabo.kaze.ui.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import dev.orestegabo.kaze.theme.KazeThemeMode

@Composable
internal fun HomeSettingsScreen(
    bottomContentPadding: Dp,
    themeMode: KazeThemeMode,
    edgeAiEnabled: Boolean,
    sessionLabel: String,
    sessionDisplayName: String,
    sessionUsername: String,
    sessionEmail: String,
    sessionPhoneNumber: String,
    needsProfileCompletion: Boolean,
    onThemeModeChange: (KazeThemeMode) -> Unit,
    onEdgeAiEnabledChange: (Boolean) -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    dev.orestegabo.kaze.ui.home.settings.HomeSettingsScreen(
        bottomContentPadding = bottomContentPadding,
        themeMode = themeMode,
        edgeAiEnabled = edgeAiEnabled,
        sessionLabel = sessionLabel,
        sessionDisplayName = sessionDisplayName,
        sessionUsername = sessionUsername,
        sessionEmail = sessionEmail,
        sessionPhoneNumber = sessionPhoneNumber,
        needsProfileCompletion = needsProfileCompletion,
        onThemeModeChange = onThemeModeChange,
        onEdgeAiEnabledChange = onEdgeAiEnabledChange,
        onUpdateProfile = onUpdateProfile,
        onLogout = onLogout,
        onBack = onBack,
    )
}
