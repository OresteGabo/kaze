package dev.orestegabo.kaze.ui.home.settings

import dev.orestegabo.kaze.theme.KazeThemeMode

internal val KazeThemeMode.settingsLabel: String
    get() = when (this) {
        KazeThemeMode.SYSTEM -> "System"
        KazeThemeMode.LIGHT -> "Light"
        KazeThemeMode.DARK -> "Dark"
    }

internal data class LegalSection(
    val heading: String,
    val body: List<String>,
)
