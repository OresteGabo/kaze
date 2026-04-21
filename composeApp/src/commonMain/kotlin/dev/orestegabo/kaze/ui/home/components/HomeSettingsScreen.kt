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
    onThemeModeChange: (KazeThemeMode) -> Unit,
    onEdgeAiEnabledChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    dev.orestegabo.kaze.ui.home.settings.HomeSettingsScreen(
        bottomContentPadding = bottomContentPadding,
        themeMode = themeMode,
        edgeAiEnabled = edgeAiEnabled,
        sessionLabel = sessionLabel,
        onThemeModeChange = onThemeModeChange,
        onEdgeAiEnabledChange = onEdgeAiEnabledChange,
        onLogout = onLogout,
        onBack = onBack,
    )
}
