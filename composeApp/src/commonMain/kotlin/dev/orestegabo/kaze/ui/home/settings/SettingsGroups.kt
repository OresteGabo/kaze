package dev.orestegabo.kaze.ui.home.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class SettingsGroup(
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val previewTokens: List<String>,
    val pages: List<SettingsDetailPage>,
    val legalPages: List<LegalPage> = emptyList(),
) {
    ACCOUNT_PRIVACY(
        title = "Account & privacy",
        summary = "Profile, personal data, privacy choices, and account protection.",
        icon = Icons.Default.VerifiedUser,
        previewTokens = listOf("Identity", "Privacy", "Security"),
        pages = listOf(
            SettingsDetailPage.ACCOUNT,
            SettingsDetailPage.PRIVACY_CONTROLS,
            SettingsDetailPage.SECURITY,
        ),
    ),
    PREFERENCES(
        title = "Preferences",
        summary = "Language, alerts, accessibility, and device wallet options.",
        icon = Icons.Default.Tune,
        previewTokens = listOf("Language", "Notifications", "Accessibility", "Wallet"),
        pages = listOf(
            SettingsDetailPage.LANGUAGE,
            SettingsDetailPage.NOTIFICATIONS,
            SettingsDetailPage.ACCESSIBILITY,
            SettingsDetailPage.WALLET_PASSES,
        ),
    ),
    ACTIVITY_PAYMENTS(
        title = "Activity & payments",
        summary = "Invitations, saved places, and payment methods.",
        icon = Icons.Default.EventAvailable,
        previewTokens = listOf("Invitations", "Saved places", "Payments"),
        pages = listOf(
            SettingsDetailPage.INVITATIONS,
            SettingsDetailPage.SAVED_PLACES,
            SettingsDetailPage.PAYMENTS,
        ),
    ),
    SUPPORT_LEGAL(
        title = "Support & legal",
        summary = "Help, product details, policies, ownership, and contact.",
        icon = Icons.Default.Gavel,
        previewTokens = listOf("Help", "About", "Legal"),
        pages = listOf(
            SettingsDetailPage.HELP,
            SettingsDetailPage.ABOUT,
        ),
        legalPages = LegalPage.entries,
    ),
}
