package dev.orestegabo.kaze.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                title = "Hotel life beyond the room",
                subtitle = "Amenities, experiences, and social moments curated around the visitor's current context.",
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
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(highlight.title, style = MaterialTheme.typography.titleLarge)
            Text(highlight.description, style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(highlight.locationLabel)
                MetaPill(highlight.availabilityLabel)
                MetaPill(highlight.actionLabel)
            }
            KazeSecondaryButton(label = highlight.actionLabel, onClick = onActionClick)
        }
    }
}
