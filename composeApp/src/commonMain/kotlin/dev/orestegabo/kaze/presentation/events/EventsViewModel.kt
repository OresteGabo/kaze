package dev.orestegabo.kaze.presentation.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
import dev.orestegabo.kaze.presentation.util.runImmediateSuspend

internal class EventsViewModel(
    private val hotelId: String,
    private val experienceRepository: ExperienceRepository,
) : ViewModel() {
    var uiState by mutableStateOf(EventsUiState())
        private set

    init {
        val days = runImmediateSuspend { experienceRepository.getEventDays(hotelId) }
        val selectedDay = days.firstOrNull()
        uiState = EventsUiState(
            days = days,
            selectedDay = selectedDay,
            sessions = selectedDay?.let(::loadSchedule).orEmpty(),
        )
    }

    fun onDaySelected(day: EventDay) {
        uiState = uiState.copy(
            selectedDay = day,
            sessions = loadSchedule(day),
        )
    }

    fun onSessionAction(session: ScheduledExperience): EventsActionResult.NavigateToMap =
        EventsActionResult.NavigateToMap(
            route = "Arrival route to ${session.venueLabel}",
            floorId = if (session.venueLabel.contains("Ballroom", ignoreCase = true)) "l9" else "l1",
            floorLabel = session.venueLabel,
        )

    private fun loadSchedule(day: EventDay): List<ScheduledExperience> =
        runImmediateSuspend { experienceRepository.getEventSchedule(hotelId, day.id) }
}
