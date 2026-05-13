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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
import dev.orestegabo.kaze.presentation.auth.EventAttendancePolicy
import dev.orestegabo.kaze.presentation.auth.EventCapacityMode
import dev.orestegabo.kaze.presentation.auth.EventCreateRequest
import dev.orestegabo.kaze.presentation.auth.EventVisibility
import dev.orestegabo.kaze.presentation.auth.EventVenueOption
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.states.KazeEmptyStateScreen
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.empty_event_day_subtitle
import kaze.composeapp.generated.resources.empty_event_day_title
import kaze.composeapp.generated.resources.empty_events_action
import kaze.composeapp.generated.resources.empty_events_subtitle
import kaze.composeapp.generated.resources.empty_events_title
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
internal fun EventScheduleScreen(
    modifier: Modifier = Modifier,
    days: List<EventDay>,
    selectedDay: EventDay?,
    sessions: List<ScheduledExperience>,
    suggestedSessions: List<ScheduledExperience> = emptyList(),
    venueOptions: List<EventVenueOption> = emptyList(),
    onDaySelected: (EventDay) -> Unit,
    onSessionAction: (ScheduledExperience) -> Unit,
    onEmptyAction: () -> Unit,
    canCreateEvent: Boolean = false,
    onCreateEvent: (EventCreateRequest) -> Unit = {},
    eventInvitation: InvitationPreview? = null,
    onVenueAction: () -> Unit = {},
    edgeAiEnabled: Boolean,
    onAiAction: (String) -> Unit,
    bottomContentPadding: Dp = 20.dp,
) {
    var isCreatingEvent by rememberSaveable { mutableStateOf(false) }
    if (isCreatingEvent) {
        EventCreatePage(
            modifier = modifier,
            bottomContentPadding = bottomContentPadding,
            onBack = { isCreatingEvent = false },
            venueOptions = venueOptions,
            onCreateEvent = { request ->
                onCreateEvent(request)
                isCreatingEvent = false
            },
        )
        return
    }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (days.isEmpty() || selectedDay == null) {
                item {
                    KazeEmptyStateScreen(
                        modifier = Modifier.fillMaxWidth().height(if (suggestedSessions.isEmpty()) 420.dp else 300.dp),
                        title = stringResource(Res.string.empty_events_title),
                        subtitle = stringResource(Res.string.empty_events_subtitle),
                        actionLabel = stringResource(Res.string.empty_events_action),
                        eyebrow = "Event space",
                        tags = listOf("Agenda", "Rooms", "Pass"),
                        icon = Icons.Default.CalendarMonth,
                        onAction = onEmptyAction,
                    )
                }
                if (suggestedSessions.isNotEmpty()) {
                    item {
                        EventSuggestionSection(
                            sessions = suggestedSessions,
                            onSessionAction = onSessionAction,
                        )
                    }
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
            if (suggestedSessions.isNotEmpty()) {
                item {
                    EventSuggestionSection(
                        sessions = suggestedSessions,
                        onSessionAction = onSessionAction,
                    )
                }
            }
        }
        if (canCreateEvent) {
            FloatingActionButton(
                onClick = { isCreatingEvent = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = bottomContentPadding + 18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create event")
            }
        }
    }
}

@Composable
private fun EventCreatePage(
    modifier: Modifier,
    bottomContentPadding: Dp,
    onBack: () -> Unit,
    venueOptions: List<EventVenueOption>,
    onCreateEvent: (EventCreateRequest) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Create event", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Start private by default. You can make it public when you want discovery.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                }
                TextButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Back")
                }
            }
        }
        item {
            EventCreatePanel(venueOptions = venueOptions, onCreateEvent = onCreateEvent)
        }
    }
}

@Composable
private fun EventCreatePanel(
    venueOptions: List<EventVenueOption>,
    onCreateEvent: (EventCreateRequest) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val summaryFocusRequester = FocusRequester()
    val dateOptions = remember { buildEventDateOptions() }
    val monthOptions = remember(dateOptions) { buildEventMonthOptions(dateOptions) }
    var title by rememberSaveable { mutableStateOf("") }
    var summary by rememberSaveable { mutableStateOf("") }
    var selectedDateIso by rememberSaveable { mutableStateOf(dateOptions.first().dateIso) }
    var selectedMonthKey by rememberSaveable { mutableStateOf(dateOptions.first().monthKey) }
    var selectedHour by rememberSaveable { mutableStateOf(18) }
    var selectedMinute by rememberSaveable { mutableStateOf(0) }
    var selectedDurationMinutes by rememberSaveable { mutableStateOf(180) }
    var selectedVenueId by rememberSaveable { mutableStateOf<String?>(null) }
    var eventType by rememberSaveable { mutableStateOf("WEDDING") }
    var selectedPreset by rememberSaveable { mutableStateOf(EventAccessPreset.default) }
    var isAccessPersonalized by rememberSaveable { mutableStateOf(false) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Basics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "Default access is shared by code or QR, not listed publicly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    formError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Event name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { summaryFocusRequester.requestFocus() },
                    onDone = { focusManager.clearFocus() },
                ),
            )
            OutlinedTextField(
                value = summary,
                onValueChange = {
                    summary = it
                    formError = null
                },
                modifier = Modifier.fillMaxWidth().focusRequester(summaryFocusRequester),
                label = { Text("Short description") },
                minLines = 2,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
            EventScheduleSelector(
                dateOptions = dateOptions,
                monthOptions = monthOptions,
                selectedMonthKey = selectedMonthKey,
                selectedDateIso = selectedDateIso,
                selectedHour = selectedHour,
                selectedMinute = selectedMinute,
                selectedDurationMinutes = selectedDurationMinutes,
                onMonthSelected = { monthKey ->
                    focusManager.clearFocus()
                    selectedMonthKey = monthKey
                    selectedDateIso = dateOptions.first { it.monthKey == monthKey }.dateIso
                    formError = null
                },
                onDateSelected = {
                    focusManager.clearFocus()
                    selectedDateIso = it
                    selectedMonthKey = dateOptions.firstOrNull { option -> option.dateIso == it }?.monthKey ?: selectedMonthKey
                    formError = null
                },
                onHourSelected = {
                    focusManager.clearFocus()
                    selectedHour = it
                    formError = null
                },
                onMinuteSelected = {
                    focusManager.clearFocus()
                    selectedMinute = it
                    formError = null
                },
                onDurationSelected = {
                    focusManager.clearFocus()
                    selectedDurationMinutes = it
                    formError = null
                },
            )
            EventVenueSelector(
                venueOptions = venueOptions,
                selectedVenueId = selectedVenueId,
                onVenueSelected = {
                    focusManager.clearFocus()
                    selectedVenueId = it
                    formError = null
                },
            )
            formError?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text("Event type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("WEDDING", "CHURCH", "MEETING", "CONFERENCE", "OTHER").forEach { type ->
                    EventChoiceChip(
                        label = type.lowercase().replaceFirstChar(Char::titlecase),
                        selected = eventType == type,
                        onClick = {
                            focusManager.clearFocus()
                            eventType = type
                        },
                    )
                }
            }
            EventAccessComboBox(
                preset = selectedPreset,
                isExpanded = isAccessPersonalized,
                onToggle = {
                    focusManager.clearFocus()
                    isAccessPersonalized = !isAccessPersonalized
                },
                onPresetSelected = { preset ->
                    focusManager.clearFocus()
                    selectedPreset = preset
                },
                onDone = {
                    focusManager.clearFocus()
                    isAccessPersonalized = false
                },
            )
            KazePrimaryButton(
                label = "Create Event",
                onClick = {
                    focusManager.clearFocus()
                    if (title.isBlank()) {
                        formError = "Add an event name before creating it."
                        return@KazePrimaryButton
                    }
                    val schedule = buildSelectedEventSchedule(
                        dateIso = selectedDateIso,
                        hour = selectedHour,
                        minute = selectedMinute,
                        durationMinutes = selectedDurationMinutes,
                    )
                    onCreateEvent(
                        EventCreateRequest(
                            title = title,
                            eventType = eventType,
                            summary = summary.ifBlank { null },
                            visibility = selectedPreset.visibility.name,
                            attendancePolicy = selectedPreset.attendancePolicy.name,
                            capacityMode = selectedPreset.capacityMode.name,
                            requiresIdentity = selectedPreset.requiresIdentity,
                            startsAtIso = schedule.startsAtIso,
                            endsAtIso = schedule.endsAtIso,
                            placeId = selectedVenueId,
                        ),
                    )
                    title = ""
                    summary = ""
                    selectedDateIso = dateOptions.first().dateIso
                    selectedMonthKey = dateOptions.first().monthKey
                    selectedHour = 18
                    selectedMinute = 0
                    selectedDurationMinutes = 180
                    selectedVenueId = null
                    selectedPreset = EventAccessPreset.default
                    isAccessPersonalized = false
                    formError = null
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                leadingIcon = Icons.Default.CalendarMonth,
            )
        }
    }
}

@Composable
private fun EventScheduleSelector(
    dateOptions: List<EventDateOption>,
    monthOptions: List<EventMonthOption>,
    selectedMonthKey: String,
    selectedDateIso: String,
    selectedHour: Int,
    selectedMinute: Int,
    selectedDurationMinutes: Int,
    onMonthSelected: (String) -> Unit,
    onDateSelected: (String) -> Unit,
    onHourSelected: (Int) -> Unit,
    onMinuteSelected: (Int) -> Unit,
    onDurationSelected: (Int) -> Unit,
) {
    val monthDates = dateOptions.filter { it.monthKey == selectedMonthKey }.ifEmpty { dateOptions.take(31) }
    val selectedDate = dateOptions.firstOrNull { it.dateIso == selectedDateIso } ?: monthDates.first()
    val isAllDay = selectedDurationMinutes == ALL_DAY_EVENT_DURATION
    val schedule = buildSelectedEventSchedule(
        dateIso = selectedDate.dateIso,
        hour = selectedHour,
        minute = selectedMinute,
        durationMinutes = selectedDurationMinutes,
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Date and time", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "${selectedDate.label}, ${selectedDate.monthDayLabel}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (isAllDay) "All day" else "${selectedHour.twoDigits()}:${selectedMinute.twoDigits()} - ${schedule.endDisplayLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            selectedDurationMinutes.toDurationLabel(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                EventScheduleStep(
                    marker = "1",
                    title = "Date",
                    subtitle = "${selectedDate.label}, ${selectedDate.monthDayLabel}",
                ) {
                    Text("Month", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(monthOptions) { option ->
                            EventPickerChip(
                                label = option.label,
                                detail = option.yearLabel,
                                selected = option.monthKey == selectedMonthKey,
                                onClick = { onMonthSelected(option.monthKey) },
                            )
                        }
                    }
                    Text("Day", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(monthDates) { option ->
                            EventPickerChip(
                                label = option.label,
                                detail = option.monthDayLabel,
                                selected = option.dateIso == selectedDate.dateIso,
                                onClick = { onDateSelected(option.dateIso) },
                            )
                        }
                    }
                }
                EventScheduleStep(
                    marker = "2",
                    title = "Time",
                    subtitle = if (isAllDay) "All day starts at 00:00 and ends the next day." else "${selectedHour.twoDigits()}:${selectedMinute.twoDigits()} start",
                ) {
                    if (isAllDay) {
                        Text(
                            "Time selection is skipped for all-day events.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    } else {
                        Text("Hour", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items((0..23).toList()) { hour ->
                                EventPickerChip(
                                    label = hour.twoDigits(),
                                    selected = hour == selectedHour,
                                    onClick = { onHourSelected(hour) },
                                )
                            }
                        }
                        Text("Minute", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0, 15, 30, 45).forEach { minute ->
                                EventPickerChip(
                                    label = minute.twoDigits(),
                                    selected = minute == selectedMinute,
                                    onClick = { onMinuteSelected(minute) },
                                )
                            }
                        }
                    }
                }
                EventScheduleStep(
                    marker = "3",
                    title = "Duration",
                    subtitle = selectedDurationMinutes.toDurationLabel(),
                    isLast = true,
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(60, 120, 180, 240, 360, 480, ALL_DAY_EVENT_DURATION).forEach { duration ->
                            EventPickerChip(
                                label = duration.toDurationLabel(),
                                selected = duration == selectedDurationMinutes,
                                onClick = { onDurationSelected(duration) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventVenueSelector(
    venueOptions: List<EventVenueOption>,
    selectedVenueId: String?,
    onVenueSelected: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Location", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val selectedVenue = venueOptions.firstOrNull { it.id == selectedVenueId }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            selectedVenue?.name ?: "No venue selected",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            selectedVenue?.let { "${it.kind.toVenueKindLabel()} • ${it.city}" }
                                ?: "Pick a hotel, conference venue, or event place.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                    if (selectedVenueId != null) {
                        TextButton(onClick = { onVenueSelected(null) }) {
                            Text("Clear")
                        }
                    }
                }
                if (venueOptions.isEmpty()) {
                    Text(
                        "Venues will appear when the places catalog is available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(venueOptions) { venue ->
                            EventPickerChip(
                                label = venue.name,
                                detail = "${venue.kind.toVenueKindLabel()} • ${venue.city}",
                                selected = venue.id == selectedVenueId,
                                onClick = { onVenueSelected(venue.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventSuggestionSection(
    sessions: List<ScheduledExperience>,
    onSessionAction: (ScheduledExperience) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Suggested public events", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Open events you may want to attend.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
        )
        sessions.take(6).forEach { session ->
            SessionCard(session = session, onOpenMap = { onSessionAction(session) })
        }
    }
}

@Composable
private fun EventScheduleStep(
    marker: String,
    title: String,
    subtitle: String,
    isLast: Boolean = false,
    content: @Composable () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(26.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    marker,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)),
                )
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        textAlign = TextAlign.End,
                    )
                }
                content()
            }
        }
    }
}

@Composable
private fun EventPickerChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    detail: String? = null,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            detail?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun EventAccessComboBox(
    preset: EventAccessPreset,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onPresetSelected: (EventAccessPreset) -> Unit,
    onDone: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Access", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Surface(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AccessGlyph()
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(preset.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        preset.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Hide access options" else "Show access options",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (isExpanded) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose an access style",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    EventAccessPreset.entries.forEach { option ->
                        EventPresetOptionRow(
                            preset = option,
                            selected = preset == option,
                            onClick = { onPresetSelected(option) },
                        )
                    }
                    KazeSecondaryButton(
                        label = "Done",
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AccessGlyph() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun EventChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
        )
    }
}

@Composable
private fun EventPresetOptionRow(
    preset: EventAccessPreset,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.54f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.30f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(preset.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (selected) {
                        Text(
                            text = "Selected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Text(
                    preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
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
    val shortDay = day.label.substringBefore(' ').ifBlank { day.label }
    val shortDate = day.dateIso.toEventDayLabel()
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
            .heightIn(min = 92.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier.size(if (selected) 8.dp else 6.dp).clip(CircleShape).background(
                if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.42f),
            ),
        )
        Text(
            shortDay,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            shortDate,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

private fun String.toEventDayLabel(): String {
    val parts = split("-")
    if (parts.size != 3) return this
    val month = when (parts[1]) {
        "01" -> "Jan"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Apr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Aug"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dec"
        else -> parts[1]
    }
    return "${parts[2]} $month"
}

private data class EventDateOption(
    val dateIso: String,
    val monthKey: String,
    val label: String,
    val monthDayLabel: String,
)

private data class EventMonthOption(
    val monthKey: String,
    val label: String,
    val yearLabel: String,
)

private data class SelectedEventSchedule(
    val startsAtIso: String,
    val endsAtIso: String,
    val endDisplayLabel: String,
)

@OptIn(ExperimentalTime::class)
private fun buildEventDateOptions(): List<EventDateOption> {
    val today = EventCalendarDate.parse(Clock.System.now().toString().substringBefore("T"))
        ?: EventCalendarDate(2026, 1, 1)
    return (0..548).map { offset ->
        val date = today.plusDays(offset)
        EventDateOption(
            dateIso = date.iso,
            monthKey = date.monthKey,
            label = when (offset) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> "${date.day.twoDigits()} ${date.weekdayLabel}"
            },
            monthDayLabel = date.monthDayLabel,
        )
    }
}

private fun buildEventMonthOptions(dateOptions: List<EventDateOption>): List<EventMonthOption> =
    dateOptions
        .distinctBy(EventDateOption::monthKey)
        .mapNotNull { option ->
            EventCalendarDate.parse(option.dateIso)?.let { date ->
                EventMonthOption(
                    monthKey = option.monthKey,
                    label = date.month.monthShortLabel(),
                    yearLabel = date.year.toString(),
                )
            }
        }

private fun buildSelectedEventSchedule(
    dateIso: String,
    hour: Int,
    minute: Int,
    durationMinutes: Int,
): SelectedEventSchedule {
    val startDate = EventCalendarDate.parse(dateIso) ?: EventCalendarDate(2026, 1, 1)
    if (durationMinutes == ALL_DAY_EVENT_DURATION) {
        val endDate = startDate.plusDays(1)
        return SelectedEventSchedule(
            startsAtIso = "${startDate.iso}T00:00:00Z",
            endsAtIso = "${endDate.iso}T00:00:00Z",
            endDisplayLabel = "All day",
        )
    }
    val startTotalMinutes = hour * 60 + minute
    val endTotalMinutes = startTotalMinutes + durationMinutes
    val endDate = startDate.plusDays(endTotalMinutes / MINUTES_PER_DAY)
    val endMinuteOfDay = endTotalMinutes % MINUTES_PER_DAY
    val endHour = endMinuteOfDay / 60
    val endMinute = endMinuteOfDay % 60
    val startsAtIso = "${startDate.iso}T${hour.twoDigits()}:${minute.twoDigits()}:00Z"
    val endsAtIso = "${endDate.iso}T${endHour.twoDigits()}:${endMinute.twoDigits()}:00Z"
    val endDisplayLabel = if (endDate == startDate) {
        "${endHour.twoDigits()}:${endMinute.twoDigits()}"
    } else {
        "${endDate.monthDayLabel}, ${endHour.twoDigits()}:${endMinute.twoDigits()}"
    }
    return SelectedEventSchedule(
        startsAtIso = startsAtIso,
        endsAtIso = endsAtIso,
        endDisplayLabel = endDisplayLabel,
    )
}

private data class EventCalendarDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    val iso: String = "${year.toString().padStart(4, '0')}-${month.twoDigits()}-${day.twoDigits()}"
    val monthKey: String = "${year.toString().padStart(4, '0')}-${month.twoDigits()}"
    val monthDayLabel: String = "${day.twoDigits()} ${month.monthShortLabel()}"
    val weekdayLabel: String = weekdayName(dayOfWeekIndex())

    fun plusDays(days: Int): EventCalendarDate {
        var y = year
        var m = month
        var d = day + days
        while (d > daysInMonth(y, m)) {
            d -= daysInMonth(y, m)
            m += 1
            if (m > 12) {
                m = 1
                y += 1
            }
        }
        return EventCalendarDate(y, m, d)
    }

    private fun dayOfWeekIndex(): Int {
        var adjustedMonth = month
        var adjustedYear = year
        if (adjustedMonth < 3) {
            adjustedMonth += 12
            adjustedYear -= 1
        }
        val k = adjustedYear % 100
        val j = adjustedYear / 100
        val h = (day + ((13 * (adjustedMonth + 1)) / 5) + k + (k / 4) + (j / 4) + (5 * j)) % 7
        return (h + 5) % 7
    }

    companion object {
        fun parse(value: String): EventCalendarDate? {
            val parts = value.split("-")
            if (parts.size != 3) return null
            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull() ?: return null
            val day = parts[2].toIntOrNull() ?: return null
            if (month !in 1..12 || day !in 1..daysInMonth(year, month)) return null
            return EventCalendarDate(year, month, day)
        }
    }
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private fun Int.toDurationLabel(): String =
    when {
        this == ALL_DAY_EVENT_DURATION -> "All day"
        this < 60 -> "${this}m"
        this % 60 == 0 -> "${this / 60}h"
        else -> "${this / 60}h ${this % 60}m"
    }

private fun Int.monthShortLabel(): String =
    when (this) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> toString()
    }

private fun weekdayName(index: Int): String =
    when (index) {
        0 -> "Mon"
        1 -> "Tue"
        2 -> "Wed"
        3 -> "Thu"
        4 -> "Fri"
        5 -> "Sat"
        else -> "Sun"
    }

private fun String.toVenueKindLabel(): String =
    lowercase()
        .split("_", "-")
        .filter(String::isNotBlank)
        .joinToString(" ") { word -> word.replaceFirstChar(Char::titlecase) }
        .ifBlank { "Venue" }

private fun daysInMonth(year: Int, month: Int): Int =
    when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year.isLeapYear()) 29 else 28
        else -> 30
    }

private fun Int.isLeapYear(): Boolean =
    this % 4 == 0 && (this % 100 != 0 || this % 400 == 0)

private const val MINUTES_PER_DAY = 24 * 60
private const val ALL_DAY_EVENT_DURATION = -1

private enum class EventAccessPreset(
    val label: String,
    val description: String,
    val visibility: EventVisibility,
    val attendancePolicy: EventAttendancePolicy,
    val capacityMode: EventCapacityMode,
    val requiresIdentity: Boolean,
) {
    SHARED_CODE(
        label = "Shared code or QR",
        description = "Best default for weddings and semi-open events. Guests can join with a code or QR, but the event is not public search.",
        visibility = EventVisibility.UNLISTED,
        attendancePolicy = EventAttendancePolicy.INVITE_OR_CODE,
        capacityMode = EventCapacityMode.UNLIMITED,
        requiresIdentity = false,
    ),
    PUBLIC_OPEN(
        label = "Public and open",
        description = "Visible in public search and open to everyone. Good for church and community events.",
        visibility = EventVisibility.PUBLIC,
        attendancePolicy = EventAttendancePolicy.OPEN,
        capacityMode = EventCapacityMode.UNLIMITED,
        requiresIdentity = false,
    ),
    PUBLIC_REGISTRATION(
        label = "Public registration",
        description = "Visible in public search, but guests register or RSVP first.",
        visibility = EventVisibility.PUBLIC,
        attendancePolicy = EventAttendancePolicy.REGISTRATION_REQUIRED,
        capacityMode = EventCapacityMode.UNLIMITED,
        requiresIdentity = true,
    ),
    APPROVAL(
        label = "Request approval",
        description = "People can find or receive the event, but the organizer approves access.",
        visibility = EventVisibility.PUBLIC,
        attendancePolicy = EventAttendancePolicy.APPROVAL_REQUIRED,
        capacityMode = EventCapacityMode.LIMITED,
        requiresIdentity = true,
    ),
    PRIVATE_INVITE(
        label = "Private invite-only",
        description = "Hidden from public search. Use for meetings, VIP events, and controlled gatherings.",
        visibility = EventVisibility.PRIVATE,
        attendancePolicy = EventAttendancePolicy.INVITE_OR_CODE,
        capacityMode = EventCapacityMode.LIMITED,
        requiresIdentity = true,
    );

    companion object {
        val default: EventAccessPreset = SHARED_CODE
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
                MetaPill("Venue details", leadingIcon = Icons.Default.Place)
            }
            KazeSecondaryButton(label = "Venue details", onClick = onOpenMap, leadingIcon = Icons.Default.Place)
        }
    }
}
