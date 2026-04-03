package dev.orestegabo.kaze.data.repository

import dev.orestegabo.kaze.domain.experience.AmenityHighlight
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience

interface ExperienceRepository {
    suspend fun getEventDays(hotelId: String): List<EventDay>
    suspend fun getEventSchedule(hotelId: String, dayId: String): List<ScheduledExperience>
    suspend fun getAmenityHighlights(hotelId: String): List<AmenityHighlight>
}
