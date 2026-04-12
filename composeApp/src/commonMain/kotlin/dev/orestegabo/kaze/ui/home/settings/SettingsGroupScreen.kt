package dev.orestegabo.kaze.ui.home.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun SettingsGroupScreen(
    group: SettingsGroup,
    walletProvider: String,
    bottomContentPadding: Dp,
    onBack: () -> Unit,
    onPageSelected: (SettingsDetailPage) -> Unit,
    onLegalSelected: (LegalPage) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KazeGhostButton(
            label = "Back to settings",
            onClick = onBack,
            leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )
        SettingsHeader(
            title = group.title,
            subtitle = group.summary,
            icon = group.icon,
        )
        group.pages.forEach { page ->
            SettingsPageRow(
                page = page,
                detail = if (page == SettingsDetailPage.WALLET_PASSES) walletProvider else page.tokens.take(3).joinToString(" • "),
                onClick = { onPageSelected(page) },
            )
        }
        if (group.legalPages.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Legal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                group.legalPages.forEach { page ->
                    LegalPageRow(
                        page = page,
                        onClick = { onLegalSelected(page) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun SettingsHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = KazeTheme.accents.editorialWarm.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, KazeTheme.accents.editorialWarm.copy(alpha = 0.28f)),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KazeTheme.accents.editorialWarm,
                modifier = Modifier.padding(10.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
internal fun SettingsGroupCard(
    group: SettingsGroup,
    onClick: () -> Unit,
) {
    SettingsCard(
        title = group.title,
        subtitle = group.summary,
        icon = group.icon,
        onClick = onClick,
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            group.previewTokens.forEach { token -> MetaPill(token) }
        }
    }
}

@Composable
private fun SettingsPageRow(
    page: SettingsDetailPage,
    detail: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = KazeTheme.accents.editorialWarm.copy(alpha = 0.16f),
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = KazeTheme.accents.editorialWarm,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    page.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                )
            }
        }
    }
}
