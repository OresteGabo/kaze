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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.ui.ai.KazeAiAssistCard
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.components.SectionIntroCard
import dev.orestegabo.kaze.ui.home.invitations.InvitationThemeCategory
import dev.orestegabo.kaze.ui.home.invitations.resolveInvitationTheme
import dev.orestegabo.kaze.ui.states.KazeEmptyStateScreen
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.empty_event_day_subtitle
import kaze.composeapp.generated.resources.empty_event_day_title
import kaze.composeapp.generated.resources.empty_events_action
import kaze.composeapp.generated.resources.empty_events_subtitle
import kaze.composeapp.generated.resources.empty_events_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EventScheduleScreen(
    modifier: Modifier = Modifier,
    days: List<EventDay>,
    selectedDay: EventDay?,
    sessions: List<ScheduledExperience>,
    onDaySelected: (EventDay) -> Unit,
    onSessionAction: (ScheduledExperience) -> Unit,
    onEmptyAction: () -> Unit,
    eventInvitation: InvitationPreview? = null,
    onVenueAction: () -> Unit = {},
    edgeAiEnabled: Boolean,
    onAiAction: (String) -> Unit,
    bottomContentPadding: Dp = 20.dp,
) {
    if (eventInvitation?.resolveInvitationTheme()?.category == InvitationThemeCategory.WEDDING) {
        WeddingEventShowcaseScreen(
            invitation = eventInvitation,
            onViewVenue = onVenueAction,
            bottomContentPadding = bottomContentPadding,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        /*item {
            SectionIntroCard(
                eyebrow = "What's On",
                title = "East Africa Finance Summit",
                subtitle = "A clean schedule view with day switching, venue references, and direct map transitions.",
                icon = Icons.Default.CalendarMonth,
            )
        }*/

        if (edgeAiEnabled && selectedDay != null) {
            item {
                KazeAiAssistCard(
                    title = "Ask about this event",
                    subtitle = "Kaze can answer schedule, room, pass, and venue questions from cached event data when the on-device model is available.",
                    actionLabel = "Ask offline concierge",
                    onAction = { onAiAction("Offline Event Concierge") },
                    icon = Icons.Default.AutoAwesome,
                )
            }
        }

        if (days.isEmpty() || selectedDay == null) {
            item {
                KazeEmptyStateScreen(
                    modifier = Modifier.fillMaxWidth().height(420.dp),
                    title = stringResource(Res.string.empty_events_title),
                    subtitle = stringResource(Res.string.empty_events_subtitle),
                    actionLabel = stringResource(Res.string.empty_events_action),
                    eyebrow = "Event space",
                    tags = listOf("Agenda", "Rooms", "Pass"),
                    icon = Icons.Default.CalendarMonth,
                    onAction = onEmptyAction,
                )
            }
            return@LazyColumn
        }

        item {
            EventDaySwitcher(days = days, selectedDay = selectedDay, onDaySelected = onDaySelected)
        }
        item {
            Text(
                selectedDay.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        item { Text("Today's schedule", style = MaterialTheme.typography.headlineSmall) }
        if (sessions.isEmpty()) {
            item {
                KazeEmptyStateScreen(
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                    title = stringResource(Res.string.empty_event_day_title),
                    subtitle = stringResource(Res.string.empty_event_day_subtitle),
                    actionLabel = stringResource(Res.string.empty_events_action),
                    eyebrow = "Quiet day",
                    tags = listOf("Check another day", "Organizer updates"),
                    icon = Icons.Default.EventBusy,
                    onAction = onEmptyAction,
                )
            }
        } else {
            items(sessions) { session ->
                SessionCard(session = session, onOpenMap = { onSessionAction(session) })
            }
        }
    }
}

@Composable
private fun EventDaySwitcher(
    days: List<EventDay>,
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
            days.forEach { day ->
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
                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.42f),
            ),
        )
        Text(shortDay, style = MaterialTheme.typography.labelLarge, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
        Text(shortDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SessionCard(
    session: ScheduledExperience,
    onOpenMap: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${session.startIso.takeLast(9).take(5)} - ${session.endIso.takeLast(9).take(5)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(session.title, style = MaterialTheme.typography.titleLarge)
            Text(session.description, style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(session.venueLabel, leadingIcon = Icons.Default.Place)
                session.hostLabel?.let { MetaPill(it, leadingIcon = Icons.Default.CalendarMonth) }
                MetaPill("Open map", leadingIcon = Icons.Default.Map)
            }
            KazeSecondaryButton(label = "Open map", onClick = onOpenMap, leadingIcon = Icons.Default.Map)
        }
    }
}
