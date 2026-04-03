package dev.orestegabo.kaze.presentation.demo.repository

import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.domain.experience.AmenityHighlight
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
import dev.orestegabo.kaze.presentation.demo.eventDays
import dev.orestegabo.kaze.presentation.demo.eventSchedule
import dev.orestegabo.kaze.presentation.demo.exploreHighlights

internal class DemoExperienceRepository : ExperienceRepository {
    override suspend fun getEventDays(hotelId: String): List<EventDay> =
        eventDays.mapIndexed { index, day ->
            EventDay(
                id = day.id,
                label = day.label,
                dateIso = "2026-04-0${index + 3}",
            )
        }

    override suspend fun getEventSchedule(hotelId: String, dayId: String): List<ScheduledExperience> =
        eventSchedule
            .filter { it.day == dayId }
            .mapIndexed { index, session ->
                ScheduledExperience(
                    id = "${dayId}_$index",
                    dayId = dayId,
                    title = session.title,
                    description = session.description,
                    startIso = "2026-04-03T08:00:00Z",
                    endIso = "2026-04-03T09:00:00Z",
                    venueLabel = session.room,
                    hostLabel = session.host,
                )
            }

    override suspend fun getAmenityHighlights(hotelId: String): List<AmenityHighlight> =
        exploreHighlights.mapIndexed { index, highlight ->
            AmenityHighlight(
                id = "amenity_$index",
                title = highlight.title,
                description = highlight.description,
                locationLabel = highlight.location,
                availabilityLabel = highlight.time,
                actionLabel = highlight.cta,
            )
        }
}
