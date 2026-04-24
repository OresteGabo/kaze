package dev.orestegabo.kaze.domain

interface ServicePlace {
    val id: String
    val name: String
    val kind: ServicePlaceKind
    val location: ServicePlaceLocation
    val serviceCatalog: List<PlaceService>
}

data class ServicePlaceLocation(
    val city: String,
    val countryCode: String,
    val addressLabel: String? = null,
    val mapId: String? = null,
)

enum class ServicePlaceKind {
    HOTEL,
    WEDDING_VENUE,
    CONFERENCE_VENUE,
    RESTAURANT,
    APARTMENT,
    STADIUM,
    EVENT_SPACE,
}

data class PlaceService(
    val id: String,
    val title: String,
    val description: String,
    val category: PlaceServiceCategory,
    val pricing: PlaceServicePricing = PlaceServicePricing.Included,
    val requestable: Boolean = true,
)

enum class PlaceServiceCategory {
    ACCESS,
    ROUTE,
    FOOD,
    DRINK,
    ROOM_CARE,
    CHECKOUT,
    FOLIO,
    AGENDA,
    SUPPORT,
    ADD_ON,
    TRANSPORT,
    MEETING_SPACE,
    MEDIA,
    WEDDING,
    DINING,
    CONFERENCE,
    MEETING,
    RETREAT,
    STAY_GROUP,
}

sealed interface PlaceServicePricing {
    data object Included : PlaceServicePricing
    data object Free : PlaceServicePricing
    data object RequiresConfirmation : PlaceServicePricing
    data class Paid(val amountLabel: String) : PlaceServicePricing
}

data class VenuePlace(
    override val id: String,
    override val name: String,
    override val kind: ServicePlaceKind,
    override val location: ServicePlaceLocation,
    override val serviceCatalog: List<PlaceService> = emptyList(),
) : ServicePlace
