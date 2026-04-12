package dev.orestegabo.kaze.ui.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import dev.orestegabo.kaze.theme.KazeThemeMode

@Composable
internal fun HomeSettingsScreen(
    bottomContentPadding: Dp,
    themeMode: KazeThemeMode,
    onThemeModeChange: (KazeThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    dev.orestegabo.kaze.ui.home.settings.HomeSettingsScreen(
        bottomContentPadding = bottomContentPadding,
        themeMode = themeMode,
        onThemeModeChange = onThemeModeChange,
        onBack = onBack,
    )
}
