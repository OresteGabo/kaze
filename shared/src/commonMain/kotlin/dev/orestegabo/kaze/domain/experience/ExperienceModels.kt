package dev.orestegabo.kaze.domain.experience

data class EventDay(
    val id: String,
    val label: String,
    val dateIso: String,
)

data class ScheduledExperience(
    val id: String,
    val dayId: String,
    val title: String,
    val description: String,
    val startIso: String,
    val endIso: String,
    val venueLabel: String,
    val hostLabel: String? = null,
)

data class AmenityHighlight(
    val id: String,
    val title: String,
    val description: String,
    val locationLabel: String,
    val availabilityLabel: String,
    val categoryLabel: String,
    val accessLabel: String,
    val actionLabel: String,
)
