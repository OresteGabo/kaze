package dev.orestegabo.kaze.ui.home.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.getPlatform
import dev.orestegabo.kaze.theme.KazeThemeMode
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton

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
    val scrollState = rememberScrollState()
    val walletProvider = currentWalletPassProvider()
    var selectedSettingsPage by rememberSaveable { mutableStateOf<SettingsDetailPage?>(null) }
    var selectedLegalPage by rememberSaveable { mutableStateOf<LegalPage?>(null) }
    var selectedSettingsGroup by rememberSaveable { mutableStateOf<SettingsGroup?>(null) }

    selectedSettingsPage?.let { page ->
        SettingsDetailScreen(
            page = page,
            bottomContentPadding = bottomContentPadding,
            onBack = { selectedSettingsPage = null },
        )
        return
    }

    selectedLegalPage?.let { page ->
        LegalDetailScreen(
            page = page,
            bottomContentPadding = bottomContentPadding,
            onBack = { selectedLegalPage = null },
        )
        return
    }

    selectedSettingsGroup?.let { group ->
        SettingsGroupScreen(
            group = group,
            walletProvider = walletProvider,
            bottomContentPadding = bottomContentPadding,
            onBack = { selectedSettingsGroup = null },
            onPageSelected = { selectedSettingsPage = it },
            onLegalSelected = { selectedLegalPage = it },
        )
        return
    }

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
            title = "On-device AI",
            subtitle = if (edgeAiEnabled) {
                "Enabled. Kaze AI runs locally on this device for supported offline tasks."
            } else {
                "Disabled. You can turn it back on when you want local AI assistance."
            },
            icon = Icons.Default.AutoAwesome,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "No server AI processing for these features.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = edgeAiEnabled,
                        onCheckedChange = onEdgeAiEnabledChange,
                    )
                }
            }
        }

        SettingsCard(
            title = "Session",
            subtitle = sessionLabel,
            icon = Icons.AutoMirrored.Filled.Logout,
        ) {
            KazeSecondaryButton(
                label = "Log out",
                onClick = onLogout,
                leadingIcon = Icons.AutoMirrored.Filled.Logout,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SettingsGroup.entries.forEach { group ->
            SettingsGroupCard(
                group = group,
                onClick = { selectedSettingsGroup = group },
            )
        }
    }
}
private fun currentWalletPassProvider(): String {
    val platformName = getPlatform().name.lowercase()
    return when {
        "android" in platformName -> "Google Wallet"
        "ios" in platformName || "iphone" in platformName || "ipad" in platformName -> "Apple Wallet"
        else -> "Device wallet"
    }
}
