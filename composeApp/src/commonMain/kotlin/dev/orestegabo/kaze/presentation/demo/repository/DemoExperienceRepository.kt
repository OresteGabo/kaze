package dev.orestegabo.kaze.presentation.demo.repository

import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.domain.experience.AmenityHighlight
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
import dev.orestegabo.kaze.presentation.demo.exploreHighlights

internal class DemoExperienceRepository : ExperienceRepository {
    override suspend fun getEventDays(hotelId: String): List<EventDay> =
        emptyList()

    override suspend fun getEventSchedule(hotelId: String, dayId: String): List<ScheduledExperience> =
        emptyList()

    override suspend fun getAmenityHighlights(hotelId: String): List<AmenityHighlight> =
        exploreHighlights.mapIndexed { index, highlight ->
            AmenityHighlight(
                id = "amenity_$index",
                title = highlight.title,
                description = highlight.description,
                locationLabel = highlight.location,
                availabilityLabel = highlight.time,
                categoryLabel = highlight.contextLabel,
                accessLabel = highlight.accessLabel,
                actionLabel = highlight.cta,
            )
        }
}
