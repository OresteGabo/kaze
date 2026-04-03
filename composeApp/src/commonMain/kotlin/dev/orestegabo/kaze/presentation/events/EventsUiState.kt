package dev.orestegabo.kaze.presentation.events

import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience

internal data class EventsUiState(
    val days: List<EventDay> = emptyList(),
    val selectedDay: EventDay? = null,
    val sessions: List<ScheduledExperience> = emptyList(),
)

internal sealed interface EventsActionResult {
    data class NavigateToMap(
        val route: String,
        val floorId: String,
        val floorLabel: String,
    ) : EventsActionResult
}
