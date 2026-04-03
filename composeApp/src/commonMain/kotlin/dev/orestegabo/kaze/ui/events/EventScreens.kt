package dev.orestegabo.kaze.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.demo.EventDay
import dev.orestegabo.kaze.demo.EventSession
import dev.orestegabo.kaze.demo.eventDays
import dev.orestegabo.kaze.demo.eventSchedule
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.components.SectionIntroCard

@Composable
internal fun EventScheduleScreen(
    modifier: Modifier = Modifier,
    selectedDay: EventDay,
    onDaySelected: (EventDay) -> Unit,
    onSessionAction: (EventSession) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionIntroCard(
                eyebrow = "What's On",
                title = "East Africa Finance Summit",
                subtitle = "A clean schedule view with day switching, venue references, and direct map transitions.",
            )
        }
        item { EventDaySwitcher(selectedDay = selectedDay, onDaySelected = onDaySelected) }
        item { Text(selectedDay.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 2.dp)) }
        item { Text("Today's schedule", style = MaterialTheme.typography.headlineSmall) }
        items(eventSchedule.filter { it.day == selectedDay.id }) { session ->
            SessionCard(session = session, onOpenMap = { onSessionAction(session) })
        }
    }
}

@Composable
private fun EventDaySwitcher(
    selectedDay: EventDay,
    onDaySelected: (EventDay) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            eventDays.forEach { day ->
                EventDayButton(day = day, selected = day == selectedDay, onClick = { onDaySelected(day) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EventDayButton(
    day: EventDay,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val parts = day.label.split(" ", limit = 2)
    val shortDay = parts.firstOrNull().orEmpty()
    val shortDate = parts.getOrNull(1).orEmpty()
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)))
                } else {
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)))
                },
            )
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier.size(if (selected) 8.dp else 6.dp).clip(CircleShape).background(
                if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.42f),
            ),
        )
        Text(shortDay, style = MaterialTheme.typography.labelLarge, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
        Text(shortDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SessionCard(
    session: EventSession,
    onOpenMap: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(session.time, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
            Text(session.title, style = MaterialTheme.typography.titleLarge)
            Text(session.description, style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(session.room)
                MetaPill(session.host)
                MetaPill("Open map")
            }
            KazeSecondaryButton(label = "Open map", onClick = onOpenMap)
        }
    }
}
