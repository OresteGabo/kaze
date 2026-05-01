package dev.orestegabo.kaze.api

import kotlinx.serialization.Serializable

@Serializable
internal data class ApiInfoDto(
    val name: String,
    val status: String,
    val version: String,
)

@Serializable
internal data class HotelDto(
    val id: String,
    val slug: String,
    val name: String,
    val market: String,
    val timezoneId: String,
    val displayName: String,
    val city: String,
    val countryCode: String,
    val supportedLocales: List<String>,
    val activeExperiences: List<String>,
)

@Serializable
internal data class GuestProfileDto(
    val hotelId: String,
    val guestId: String,
    val stayId: String?,
    val roomId: String?,
    val fullName: String,
)

@Serializable
internal data class ItineraryDto(
    val id: String,
    val hotelId: String,
    val guestId: String,
    val stayWindow: TimeWindowDto,
    val tabs: List<ItineraryTabDto>,
)

@Serializable
internal data class ItineraryTabDto(
    val mode: String,
    val title: String,
    val sections: List<ItinerarySectionDto>,
)

@Serializable
internal data class ItinerarySectionDto(
    val id: String,
    val title: String,
    val items: List<ItineraryItemDto>,
)

@Serializable
internal data class ItineraryItemDto(
    val id: String,
    val title: String,
    val category: String,
    val status: String,
    val startIsoUtc: String,
    val endIsoUtc: String,
    val venue: VenueRefDto? = null,
    val notes: String? = null,
)

@Serializable
internal data class VenueRefDto(
    val nodeId: String,
    val floorId: String,
    val label: String,
)

@Serializable
internal data class TimeWindowDto(
    val startIsoUtc: String,
    val endIsoUtc: String,
)

@Serializable
internal data class EventDayDto(
    val id: String,
    val label: String,
    val dateIso: String,
)

@Serializable
internal data class ScheduledExperienceDto(
    val id: String,
    val dayId: String,
    val title: String,
    val description: String,
    val startIso: String,
    val endIso: String,
    val venueLabel: String,
    val hostLabel: String? = null,
)

@Serializable
internal data class AmenityHighlightDto(
    val id: String,
    val title: String,
    val description: String,
    val locationLabel: String,
    val availabilityLabel: String,
    val categoryLabel: String,
    val accessLabel: String,
    val actionLabel: String,
)

@Serializable
internal data class AmenityStatusDto(
    val id: String,
    val title: String,
    val locationLabel: String,
    val statusLabel: String,
    val hoursLabel: String,
    val openNow: Boolean,
)

@Serializable
internal data class HotelMapDto(
    val hotelId: String,
    val mapId: String,
    val name: String,
    val floors: List<FloorDto>,
)

@Serializable
internal data class FloorDto(
    val id: String,
    val label: String,
    val levelIndex: Int,
    val width: Float,
    val height: Float,
    val nodes: List<MapNodeDto>,
)

@Serializable
internal data class MapNodeDto(
    val id: String,
    val label: String,
    val kind: String,
    val x: Float,
    val y: Float,
)

@Serializable
internal data class LateCheckoutSubmissionRequest(
    val stayId: String? = null,
    val roomId: String? = null,
    val checkoutTimeIso: String,
    val feeAmountMinor: Long,
    val currencyCode: String,
    val paymentPreference: String,
    val followUpPreference: String,
    val notes: String? = null,
)

@Serializable
internal data class LateCheckoutDecisionDto(
    val requestId: String,
    val status: String,
    val approvedCheckoutTimeIso: String? = null,
    val feeAmountMinor: Long? = null,
    val currencyCode: String? = null,
    val note: String? = null,
)

@Serializable
internal data class ServiceRequestSubmissionRequest(
    val stayId: String? = null,
    val roomId: String? = null,
    val type: String,
    val note: String? = null,
)

@Serializable
internal data class ServiceRequestReceiptDto(
    val requestId: String,
    val type: String,
    val status: String,
    val note: String? = null,
)

@Serializable
internal data class ReservationDraftSubmissionRequest(
    val placeId: String,
    val serviceId: String? = null,
    val eventName: String,
    val preferredDateLabel: String,
    val guestCount: Int,
    val packageLabel: String,
    val addOns: List<String> = emptyList(),
    val paymentMethod: String,
    val note: String? = null,
)

@Serializable
internal data class ReservationResponseDto(
    val id: String,
    val reservationCode: String,
    val eventId: String,
    val placeId: String,
    val placeName: String,
    val serviceId: String? = null,
    val status: String,
    val eventName: String,
    val preferredDateLabel: String,
    val guestCount: Int,
    val packageLabel: String,
    val addOns: List<String>,
    val paymentMethod: String,
    val createdAtIso: String,
)

@Serializable
internal data class AssistantQueryRequest(
    val question: String,
)

@Serializable
internal data class AssistantAnswerDto(
    val answer: String,
    val source: String,
    val confidence: String,
)
