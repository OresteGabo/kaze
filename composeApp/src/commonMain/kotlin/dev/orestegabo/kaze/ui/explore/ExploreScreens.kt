package dev.orestegabo.kaze.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.domain.experience.AmenityHighlight
import dev.orestegabo.kaze.ui.components.HighlightPanel
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.components.SectionIntroCard

@Composable
internal fun ExploreScreen(
    modifier: Modifier = Modifier,
    highlights: List<AmenityHighlight>,
    onHighlightAction: (AmenityHighlight) -> Unit,
    onHeroPrimary: () -> Unit,
    onHeroSecondary: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionIntroCard(
                eyebrow = "Explore",
                title = "Discover the hotel",
                subtitle = "Browse amenities and experiences available across the property, whether included or paid separately.",
                icon = Icons.Default.Explore,
            )
        }
        item {
            HighlightPanel(
                title = "Tonight's highlighted experiences",
                body = "Sunset jazz at the lounge, chef's table seating, and a guided art walk through the lobby collection.",
                primaryLabel = "Reserve activity",
                secondaryLabel = "Open amenity map",
                onPrimaryClick = onHeroPrimary,
                onSecondaryClick = onHeroSecondary,
            )
        }
        items(highlights) { highlight ->
            ExploreCard(highlight = highlight, onActionClick = { onHighlightAction(highlight) })
        }
    }
}

@Composable
private fun ExploreCard(
    highlight: AmenityHighlight,
    onActionClick: () -> Unit,
) {
    val icon = highlight.exploreIcon()
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Text(
                        highlight.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                    modifier = Modifier.size(52.dp),
                )
            }
            Text(highlight.description, style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(highlight.categoryLabel, leadingIcon = Icons.Default.Info)
                MetaPill(
                    highlight.accessLabel,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    leadingIcon = Icons.Default.Payments,
                )
                MetaPill(highlight.locationLabel, leadingIcon = Icons.Default.Place)
                MetaPill(highlight.availabilityLabel, leadingIcon = Icons.Default.Schedule)
                MetaPill(highlight.actionLabel, leadingIcon = if (highlight.actionLabel.contains("route", ignoreCase = true) || highlight.actionLabel.contains("map", ignoreCase = true)) Icons.Default.Map else Icons.Default.Explore)
            }
            KazeSecondaryButton(
                label = highlight.actionLabel,
                onClick = onActionClick,
                leadingIcon = if (highlight.actionLabel.contains("route", ignoreCase = true) || highlight.actionLabel.contains("map", ignoreCase = true)) Icons.Default.Map else icon,
            )
        }
    }
}

private fun AmenityHighlight.exploreIcon(): ImageVector {
    val text = "$title $description $locationLabel $actionLabel".lowercase()
    return when {
        "pool" in text || "water" in text -> Icons.Default.Waves
        "spa" in text || "wellness" in text || "massage" in text -> Icons.Default.SelfImprovement
        "bar" in text || "jazz" in text || "lounge" in text -> Icons.Default.LocalBar
        "dining" in text || "chef" in text || "restaurant" in text || "breakfast" in text -> Icons.Default.Restaurant
        "art" in text || "gallery" in text || "artists" in text -> Icons.Default.Brush
        else -> Icons.Default.Explore
    }
}
