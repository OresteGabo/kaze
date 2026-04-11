package dev.orestegabo.kaze.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.getPlatform
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.theme.KazeThemeMode
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun HomeSettingsScreen(
    bottomContentPadding: Dp,
    themeMode: KazeThemeMode,
    onThemeModeChange: (KazeThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val walletProvider = currentWalletPassProvider()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KazeGhostButton(
            label = "Back",
            onClick = onBack,
            leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Manage how Kaze works for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }

        SettingsCard(
            title = "Appearance",
            subtitle = "Use your phone theme, or switch Kaze manually.",
            icon = Icons.Default.Palette,
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KazeThemeMode.entries.forEach { mode ->
                    ThemeChoicePill(
                        label = mode.settingsLabel,
                        selected = themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                    )
                }
            }
        }

        SettingsCard(
            title = "Language",
            subtitle = "Choose the language used across the app.",
            icon = Icons.Default.Language,
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(
                    label = "English",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    leadingIcon = Icons.Default.CheckCircle,
                )
                MetaPill("Français")
                MetaPill("Kinyarwanda")
                MetaPill("Swahili")
            }
        }

        SettingsCard(
            title = "Profiles",
            subtitle = "Switch between personal, organizer, and hotel-related profiles.",
            icon = Icons.Default.AccountCircle,
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill("Guest")
                MetaPill("Organizer")
                MetaPill("Hotel team")
            }
        }

        SettingsCard(
            title = "Notifications",
            subtitle = "Control invitation alerts, event updates, and stay reminders.",
            icon = Icons.Default.Notifications,
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill("Invitations")
                MetaPill("Event changes")
                MetaPill("Stay updates")
            }
        }

        SettingsCard(
            title = "Wallet passes",
            subtitle = "Save invitations and Kaze Passes to the wallet available on this device.",
            icon = Icons.Default.Wallet,
        ) {
            MetaPill(walletProvider)
            Text(
                "Pass saving is planned for a future release.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun ThemeChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            },
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

private val KazeThemeMode.settingsLabel: String
    get() = when (this) {
        KazeThemeMode.SYSTEM -> "System"
        KazeThemeMode.LIGHT -> "Light"
        KazeThemeMode.DARK -> "Dark"
    }

private fun currentWalletPassProvider(): String {
    val platformName = getPlatform().name.lowercase()
    return when {
        "android" in platformName -> "Google Wallet"
        "ios" in platformName || "iphone" in platformName || "ipad" in platformName -> "Apple Wallet"
        else -> "Device wallet"
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    val iconAccent = KazeTheme.accents.editorialWarm
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = iconAccent.copy(alpha = 0.20f),
                        border = BorderStroke(1.dp, iconAccent.copy(alpha = 0.28f)),
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconAccent,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                }
            }
            content()
        }
    }
}
