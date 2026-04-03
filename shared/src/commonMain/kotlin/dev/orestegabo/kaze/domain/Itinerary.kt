package dev.orestegabo.kaze.domain

data class Itinerary(
    val id: String,
    val hotelId: String,
    val guestId: String,
    val stayWindow: TimeWindow,
    val tabs: List<ItineraryTab>,
)

data class ItineraryTab(
    val mode: ItineraryMode,
    val title: String,
    val sections: List<ItinerarySection>,
)

data class ItinerarySection(
    val id: String,
    val title: String,
    val items: List<ItineraryItem>,
)

data class ItineraryItem(
    val id: String,
    val title: String,
    val category: ItineraryItemCategory,
    val timeWindow: TimeWindow,
    val venue: VenueRef? = null,
    val status: ReservationStatus = ReservationStatus.CONFIRMED,
    val notes: String? = null,
    val serviceRequestAllowed: Boolean = false,
)

data class VenueRef(
    val nodeId: String,
    val floorId: String,
    val label: String,
)

data class TimeWindow(
    val startIsoUtc: String,
    val endIsoUtc: String,
)

enum class ItineraryMode {
    MY_STAY,
    WHATS_ON,
    EXPLORE,
}

enum class ItineraryItemCategory {
    CHECK_IN,
    CHECK_OUT,
    DINING,
    SPA,
    EVENT_SESSION,
    TRANSPORT,
    AMENITY,
    SERVICE_REQUEST,
}

enum class ReservationStatus {
    CONFIRMED,
    PENDING,
    COMPLETED,
    CANCELLED,
}
