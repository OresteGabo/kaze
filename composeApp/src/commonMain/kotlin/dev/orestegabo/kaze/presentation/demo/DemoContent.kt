package dev.orestegabo.kaze.presentation.demo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.graphics.vector.ImageVector
import dev.orestegabo.kaze.domain.AccessCardStyle
import dev.orestegabo.kaze.domain.DigitalAccessCard
import dev.orestegabo.kaze.domain.ExperienceMode
import dev.orestegabo.kaze.domain.Hotel
import dev.orestegabo.kaze.domain.HotelBranding
import dev.orestegabo.kaze.domain.HotelBuilding
import dev.orestegabo.kaze.domain.HotelCampus
import dev.orestegabo.kaze.domain.HotelConfig
import dev.orestegabo.kaze.domain.HotelMarket
import dev.orestegabo.kaze.domain.TypographySpec
import dev.orestegabo.kaze.domain.map.importing.MapImportProfile
import dev.orestegabo.kaze.domain.map.importing.MapSourceFormat

internal enum class KazeDestination(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Filled.Home),
    STAY("Stay", Icons.Filled.DoorFront),
    EVENTS("Events", Icons.Filled.CalendarMonth),
    EXPLORE("Explore", Icons.Filled.Explore),
    MAP("Map", Icons.Filled.Map),
}

internal data class VenueCategoryPreview(
    val title: String,
    val description: String,
    val supportingLabel: String,
)

internal data class PublicVenuePreview(
    val name: String,
    val typeLabel: String,
    val locationLabel: String,
    val priceLabel: String,
    val capacityLabel: String,
    val accessLabel: String,
    val actionLabel: String,
)

internal data class InvitationPreview(
    val title: String,
    val subtitle: String,
    val code: String,
    val phoneLabel: String,
    val statusLabel: String,
)

internal enum class StayTab(val label: String) {
    MY_STAY("My Stay"),
    REQUESTS("Requests"),
    SUGGESTIONS("Suggestions"),
}

internal enum class StayScreen {
    HOME,
    LATE_CHECKOUT,
    SERVICE_REQUEST,
}

internal data class StayMoment(
    val time: String,
    val endTime: String,
    val title: String,
    val detail: String,
    val place: String,
    val bookingLabel: String,
    val accessLabel: String,
    val action: String,
)

internal data class ServiceOption(
    val title: String,
    val description: String,
    val isCustom: Boolean = false,
)

internal data class ServiceRequestDraftUi(
    val option: ServiceOption = requestOptions.first(),
    val window: RequestWindowOption = RequestWindowOption.AS_SOON_AS_POSSIBLE,
    val followUp: RequestContactOption = RequestContactOption.CONFIRM_IN_APP,
    val quantity: Int = 1,
    val customRequest: String = "",
    val locationNote: String = "",
    val notes: String = "",
)

internal data class ServiceRequestRecord(
    val id: String,
    val option: ServiceOption,
    val status: String,
    val requestedAt: String,
    val window: RequestWindowOption,
    val followUp: RequestContactOption,
    val quantity: Int,
    val locationNote: String,
    val notes: String,
)

internal enum class RequestWindowOption(
    val label: String,
    val description: String,
) {
    AS_SOON_AS_POSSIBLE(
        label = "As soon as possible",
        description = "Best for towels, support, or anything needed quickly.",
    ),
    WITHIN_30_MINUTES(
        label = "Within 30 minutes",
        description = "Useful when the guest is on the way back or preparing to leave.",
    ),
    THIS_EVENING(
        label = "Later today",
        description = "Send the request later in the day without an immediate visit.",
    ),
}

internal enum class RequestContactOption(
    val label: String,
    val description: String,
) {
    CONFIRM_IN_APP(
        label = "Confirm in app",
        description = "Keep the request quiet and send updates through the app.",
    ),
    CALL_ME(
        label = "Call me",
        description = "Reception or service staff can call before arriving.",
    ),
}

internal data class EventDay(
    val id: String,
    val label: String,
)

internal data class EventSession(
    val day: String,
    val time: String,
    val title: String,
    val description: String,
    val room: String,
    val host: String,
)

internal data class ExploreHighlight(
    val title: String,
    val description: String,
    val location: String,
    val time: String,
    val contextLabel: String,
    val accessLabel: String,
    val cta: String,
)

internal data class LateCheckoutOption(
    val id: String,
    val checkoutTimeLabel: String,
    val feeLabel: String,
    val availabilityLabel: String,
    val summary: String,
)

internal data class LateCheckoutRequest(
    val option: LateCheckoutOption,
    val paymentOption: PaymentOption,
    val followUpOption: FollowUpOption,
    val notes: String,
    val status: String,
)

internal data class LateCheckoutDraft(
    val option: LateCheckoutOption = lateCheckoutOptions.first(),
    val paymentOption: PaymentOption = PaymentOption.CHARGE_TO_ROOM,
    val followUpOption: FollowUpOption = FollowUpOption.CONFIRM_IN_APP,
    val notes: String = "",
)

internal enum class PaymentOption(
    val label: String,
    val description: String,
    val confirmationLabel: String,
) {
    CHARGE_TO_ROOM(
        label = "Charge to room",
        description = "Add the late checkout fee to the room folio for settlement at final checkout.",
        confirmationLabel = "Added to folio once approved",
    ),
    PAY_NOW_AT_RECEPTION(
        label = "Pay at reception",
        description = "Guest settles the fee at the front desk after approval.",
        confirmationLabel = "Payment requested at front desk",
    ),
    PAY_IN_ROOM(
        label = "Pay in room",
        description = "Reception or duty manager can come to the room with a card terminal if the property offers this service.",
        confirmationLabel = "Reception follow-up in room",
    ),
}

internal enum class FollowUpOption(
    val label: String,
    val description: String,
) {
    CONFIRM_IN_APP(
        label = "Confirm in app",
        description = "Send approval and fee confirmation silently through the app.",
    ),
    CALL_ROOM(
        label = "Call the room",
        description = "Reception should call the room once availability is confirmed.",
    ),
    COLLECT_PAYMENT_IN_ROOM(
        label = "Reception to room",
        description = "Reception can come upstairs to confirm and collect payment if hotel policy allows it.",
    ),
}

internal sealed interface StayPrimaryAction {
    data object OPEN_ROUTE : StayPrimaryAction
    data object VIEW_FOLIO : StayPrimaryAction
    data object SYNC_CALENDAR : StayPrimaryAction
    data object SHARE_STAY : StayPrimaryAction
    data object REQUEST_LATE_CHECKOUT : StayPrimaryAction
    data object SEE_CHECKOUT_POLICY : StayPrimaryAction
    data object NEW_REQUEST : StayPrimaryAction
    data object TRACK_REQUESTS : StayPrimaryAction
    data object REFINE_SUGGESTIONS : StayPrimaryAction
    data object SEE_FULL_AGENDA : StayPrimaryAction
    data class OpenStayMoment(val moment: StayMoment) : StayPrimaryAction
    data class RequestService(val option: ServiceOption) : StayPrimaryAction
    data class OpenSuggestion(val suggestion: ExploreHighlight) : StayPrimaryAction
}

internal val stayMoments = listOf(
    StayMoment(
        time = "Today • 14:00",
        endTime = "15:15",
        title = "Signature massage",
        detail = "Reserved treatment slot with a 15-minute arrival window.",
        place = "Ubumwe Spa",
        bookingLabel = "Reserved",
        accessLabel = "Already paid",
        action = "Open route",
    ),
    StayMoment(
        time = "Today • 19:30",
        endTime = "22:00",
        title = "Chef's tasting dinner",
        detail = "Window table reserved for two with vegetarian preference noted.",
        place = "Kivu Dining",
        bookingLabel = "Reserved",
        accessLabel = "Already paid",
        action = "View menu",
    ),
    StayMoment(
        time = "Tomorrow • 08:00",
        endTime = "08:30",
        title = "Airport transfer",
        detail = "Vehicle confirmed. Pickup point is the main porte-cochere.",
        place = "Front Drive",
        bookingLabel = "Booked",
        accessLabel = "Included",
        action = "Contact concierge",
    ),
)

internal val requestOptions = listOf(
    ServiceOption("Fresh towels", "Send towels and bath amenities to the room."),
    ServiceOption("Bottled water", "Bring chilled or room-temperature water to the room."),
    ServiceOption("Extra pillows", "Request extra pillows or a blanket for the room."),
    ServiceOption("Iron & board", "Have an iron and ironing board delivered to the room."),
    ServiceOption("Housekeeping touch-up", "Refresh the room while you are out or resting."),
    ServiceOption("In-room dining", "Browse menu and order directly from the app."),
    ServiceOption("Laundry pickup", "Request express or standard garment collection."),
    ServiceOption("Minibar refill", "Restock drinks and in-room refreshments."),
    ServiceOption("Airport transfer", "Ask for airport pickup or drop-off arrangements."),
    ServiceOption("Wake-up call", "Schedule a morning wake-up call from reception."),
    ServiceOption("Concierge help", "Ask for transport, reservations, or local assistance."),
    ServiceOption("Late checkout", "Stay a little longer before checkout."),
    ServiceOption("Custom request", "Ask for something else.", isCustom = true),
)

internal val lateCheckoutOptions = listOf(
    LateCheckoutOption(
        id = "checkout_12",
        checkoutTimeLabel = "12:00 checkout",
        feeLabel = "RWF 35,000",
        availabilityLabel = "High availability",
        summary = "Best for guests with an afternoon meeting or flexible airport transfer.",
    ),
    LateCheckoutOption(
        id = "checkout_14",
        checkoutTimeLabel = "14:00 checkout",
        feeLabel = "RWF 55,000",
        availabilityLabel = "Limited availability",
        summary = "Subject to housekeeping turnover and incoming arrivals.",
    ),
    LateCheckoutOption(
        id = "checkout_16",
        checkoutTimeLabel = "16:00 checkout",
        feeLabel = "RWF 80,000",
        availabilityLabel = "Suite-only review",
        summary = "Usually requires manager approval because it affects same-day room readiness.",
    ),
)

internal val paymentOptions = PaymentOption.entries
internal val followUpOptions = FollowUpOption.entries

internal val eventDays = listOf(
    EventDay("day1", "Fri 3 Apr"),
    EventDay("day2", "Sat 4 Apr"),
    EventDay("day3", "Sun 5 Apr"),
)

internal val eventSchedule = listOf(
    EventSession(
        day = "day1",
        time = "16:00 - 17:00",
        title = "Welcome reception",
        description = "Arrival gathering for summit delegates with lounge music and light bites.",
        room = "Sky Lobby",
        host = "Guest Relations",
    ),
    EventSession(
        day = "day2",
        time = "08:00 - 09:15",
        title = "Opening keynote",
        description = "Main plenary session in the Great Rift Ballroom, with map route and speaker details available from the card.",
        room = "Great Rift Ballroom",
        host = "Finance Summit",
    ),
    EventSession(
        day = "day2",
        time = "11:00 - 12:00",
        title = "Private investor roundtable",
        description = "Invitation-only gathering with live occupancy and room lookup support.",
        room = "Virunga Room",
        host = "Executive Office",
    ),
    EventSession(
        day = "day3",
        time = "10:00 - 11:30",
        title = "Farewell brunch",
        description = "Closing brunch for delegates and hotel guests who opted into the event program.",
        room = "Kivu Terrace",
        host = "Events Team",
    ),
)

internal val exploreHighlights = listOf(
    ExploreHighlight(
        title = "Infinity pool quiet hours",
        description = "A calmer pool deck period curated for business travelers between meetings.",
        location = "Pool Deck",
        time = "06:00 - 09:00",
        contextLabel = "Amenity",
        accessLabel = "Included",
        cta = "Open amenity",
    ),
    ExploreHighlight(
        title = "Lobby art walk",
        description = "A short self-guided route through the hotel’s featured Rwandan artists.",
        location = "Grand Lobby",
        time = "All day",
        contextLabel = "Self-guided",
        accessLabel = "Complimentary",
        cta = "Start route",
    ),
    ExploreHighlight(
        title = "Evening jazz set",
        description = "Soft live music in the bar, recommended for summit delegates after sessions.",
        location = "Panorama Bar",
        time = "20:00",
        contextLabel = "Evening experience",
        accessLabel = "Extra charge",
        cta = "Reserve table",
    ),
)

internal val suggestedActivities = listOf(
    ExploreHighlight(
        title = "Because you booked the spa",
        description = "A wellness tea service is available in the relaxation lounge right after your treatment.",
        location = "Ubumwe Spa",
        time = "After 15:00",
        contextLabel = "Recommended for you",
        accessLabel = "Included",
        cta = "Add to stay",
    ),
    ExploreHighlight(
        title = "Because you are attending the summit",
        description = "A networking coffee point opens 20 minutes before the keynote near the ballroom entrance.",
        location = "Great Rift Foyer",
        time = "07:40",
        contextLabel = "Recommended for you",
        accessLabel = "Included for event guests",
        cta = "Open route",
    ),
    ExploreHighlight(
        title = "Because checkout is tomorrow",
        description = "A late breakfast and pressing service bundle is available for departing guests.",
        location = "Kivu Dining",
        time = "Tomorrow morning",
        contextLabel = "Recommended for you",
        accessLabel = "Extra charge",
        cta = "Book bundle",
    ),
)

internal val publicVenueCategories = listOf(
    VenueCategoryPreview(
        title = "Hotels",
        description = "Browse stays, amenities, and guest-ready services before booking or joining.",
        supportingLabel = "Stays and guest services",
    ),
    VenueCategoryPreview(
        title = "Conference",
        description = "Find rooms, venue capacity, meeting layouts, and event-ready access control.",
        supportingLabel = "Rooms and business events",
    ),
    VenueCategoryPreview(
        title = "Wedding",
        description = "Compare venues, guest access, layout planning, and styling-ready event spaces.",
        supportingLabel = "Venues and event layouts",
    ),
    VenueCategoryPreview(
        title = "Apartments",
        description = "Explore local stays and longer-visit alternatives as the marketplace expands.",
        supportingLabel = "Coming next",
    ),
)

internal val publicVenues = listOf(
    PublicVenuePreview(
        name = "Kigali Marriott",
        typeLabel = "Hotel",
        locationLabel = "KN 3 Ave, Kigali",
        priceLabel = "From RWF 280,000",
        capacityLabel = "Rooms and event spaces",
        accessLabel = "Public browsing",
        actionLabel = "View hotel",
    ),
    PublicVenuePreview(
        name = "Great Rift Ballroom",
        typeLabel = "Conference venue",
        locationLabel = "Kigali Convention District",
        priceLabel = "From RWF 1,200,000",
        capacityLabel = "Up to 450 guests",
        accessLabel = "Quote or reservation",
        actionLabel = "View venue",
    ),
    PublicVenuePreview(
        name = "Umucyo Garden Venue",
        typeLabel = "Wedding venue",
        locationLabel = "Rebero, Kigali",
        priceLabel = "From RWF 2,400,000",
        capacityLabel = "Up to 320 guests",
        accessLabel = "Layout planning available",
        actionLabel = "View venue",
    ),
)

internal val invitationPreviews = listOf(
    InvitationPreview(
        title = "East Africa Finance Summit",
        subtitle = "Invitation waiting for confirmation",
        code = "EAFS24",
        phoneLabel = "Matched to +250 78 123 4567",
        statusLabel = "Invitation received",
    ),
    InvitationPreview(
        title = "Uwase x Iradukunda",
        subtitle = "Wedding access can be added to your Kaze Pass after confirmation.",
        code = "LOVE28",
        phoneLabel = "Invite prepared for guest phone",
        statusLabel = "Pending acceptance",
    ),
)

internal val stayAccessCard = DigitalAccessCard(
    id = "pass_rw_48392",
    title = "Kaze Pass",
    subtitle = "Conference and leisure access",
    contextLabel = "Conference guest",
    primaryAccessRef = "Summit / Dining / Pool",
    linkedAccess = listOf("Summit Entry", "Restaurant Access", "Pool Access", "Concierge Services"),
    style = AccessCardStyle.EventSignature(
        eventLabel = "East Africa Finance Summit",
        accentHex = "#67E8F9",
    ),
)

internal val sampleHotel = Hotel(
    id = "rw-kgl-marriott",
    slug = "kigali-marriott",
    name = "Kigali Marriott by Kaze",
    market = HotelMarket.LUXURY_HOTEL,
    timezoneId = "Africa/Kigali",
    config = HotelConfig(
        hotelId = "rw-kgl-marriott",
        displayName = "Kigali Marriott",
        branding = HotelBranding(
            primaryHex = "#2F6970",
            secondaryHex = "#B4874F",
            accentHex = "#D8C6A3",
            surfaceHex = "#FCF8F1",
            backgroundHex = "#F3EEE5",
            logoAsset = "branding/rw-kgl-marriott/logo.svg",
            wordmarkAsset = "branding/rw-kgl-marriott/wordmark.svg",
            typography = TypographySpec(
                headingScale = 1.05f,
                bodyScale = 1f,
                labelScale = 0.96f,
            ),
        ),
        supportedLocales = listOf("en", "fr"),
        defaultCurrencyCode = "RWF",
        mapImportProfile = MapImportProfile(
            preferredFormats = listOf(
                MapSourceFormat.SVG,
                MapSourceFormat.DXF,
            ),
            fallbackFormats = listOf(
                MapSourceFormat.IFC,
                MapSourceFormat.IFCXML,
                MapSourceFormat.GBXML,
            ),
        ),
    ),
    campus = HotelCampus(
        city = "Kigali",
        countryCode = "RW",
        buildings = listOf(
            HotelBuilding(
                id = "main-tower",
                name = "Main Tower",
                floors = listOf("l1", "l9"),
            ),
        ),
    ),
    activeExperiences = setOf(
        ExperienceMode.STAY,
        ExperienceMode.EVENT,
        ExperienceMode.EXPLORE,
        ExperienceMode.SERVICE_REQUESTS,
    ),
)
