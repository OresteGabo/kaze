package dev.orestegabo.kaze.ui.stay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.ExploreHighlight
import dev.orestegabo.kaze.presentation.demo.StayPrimaryAction
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.InfoToken
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun SuggestedActivitiesTab(
    suggestionActivities: List<ExploreHighlight>,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FeaturedSuggestionHeader(
            onRefinePreferences = { onPrimaryAction(StayPrimaryAction.REFINE_SUGGESTIONS) },
            onSeeAgenda = { onPrimaryAction(StayPrimaryAction.SEE_FULL_AGENDA) },
        )
        val accents = listOf(
            KazeTheme.accents.editorialWarm,
            KazeTheme.accents.editorialBotanical,
            KazeTheme.accents.editorialClay,
        )
        suggestionActivities.forEachIndexed { index, suggestion ->
            SuggestionShowcaseCard(
                suggestion = suggestion,
                accentColor = accents[index % accents.size],
                onActionClick = { onPrimaryAction(StayPrimaryAction.OpenSuggestion(suggestion)) },
            )
        }
    }
}

@Composable
internal fun FeaturedSuggestionHeader(
    onRefinePreferences: () -> Unit,
    onSeeAgenda: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Concierge Suggestions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text("Curated for tonight", style = MaterialTheme.typography.headlineSmall)
            Text(
                "These recommendations are based on your stay, your event plan, and the pace of the hotel right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KazePrimaryButton(
                    label = "Refine",
                    onClick = onRefinePreferences,
                    leadingIcon = Icons.Default.Explore,
                )
                KazeSecondaryButton(
                    label = "See agenda",
                    onClick = onSeeAgenda,
                    leadingIcon = Icons.Default.Schedule,
                )
            }
        }
    }
}

@Composable
internal fun SuggestionShowcaseCard(
    suggestion: ExploreHighlight,
    accentColor: Color,
    onActionClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetaPill("Recommended", leadingIcon = Icons.Default.Explore)
                    Text(
                        suggestion.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        suggestion.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    )
                }
            }
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InfoToken(
                        label = suggestion.location,
                        accentColor = accentColor,
                        leadingIcon = Icons.Default.Place,
                    )
                    InfoToken(
                        label = suggestion.time,
                        accentColor = accentColor,
                        leadingIcon = Icons.Default.Schedule,
                    )
                }
                KazePrimaryButton(
                    label = suggestion.cta,
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Default.Explore,
                )
            }
        }
    }
}
