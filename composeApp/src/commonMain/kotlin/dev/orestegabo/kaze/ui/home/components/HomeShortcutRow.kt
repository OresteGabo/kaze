package dev.orestegabo.kaze.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton

@Composable
internal fun HomeShortcutRow(
    onExploreVenues: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ShortcutPill(
            icon = Icons.Default.Search,
            label = "Venues",
            onClick = onExploreVenues,
        )
        ShortcutPill(
            icon = Icons.Default.VpnKey,
            label = "Use code",
            onClick = onExploreVenues,
        )
        ShortcutPill(
            icon = Icons.Default.Groups,
            label = "Invites",
            onClick = onExploreVenues,
        )
    }
}

@Composable
private fun ShortcutPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    KazeSecondaryButton(
        label = label,
        onClick = onClick,
        leadingIcon = icon,
    )
}
