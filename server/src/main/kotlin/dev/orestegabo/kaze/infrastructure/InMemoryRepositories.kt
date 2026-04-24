package dev.orestegabo.kaze.infrastructure

import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.data.repository.HotelRepository
import dev.orestegabo.kaze.data.repository.MapRepository
import dev.orestegabo.kaze.data.repository.StayRepository
import dev.orestegabo.kaze.domain.ExperienceMode
import dev.orestegabo.kaze.domain.Hotel
import dev.orestegabo.kaze.domain.HotelBranding
import dev.orestegabo.kaze.domain.HotelBuilding
import dev.orestegabo.kaze.domain.HotelCampus
import dev.orestegabo.kaze.domain.HotelConfig
import dev.orestegabo.kaze.domain.HotelMarket
import dev.orestegabo.kaze.domain.Itinerary
import dev.orestegabo.kaze.domain.ItineraryItem
import dev.orestegabo.kaze.domain.ItineraryItemCategory
import dev.orestegabo.kaze.domain.ItineraryMode
import dev.orestegabo.kaze.domain.ItinerarySection
import dev.orestegabo.kaze.domain.ItineraryTab
import dev.orestegabo.kaze.domain.ReservationStatus
import dev.orestegabo.kaze.domain.TimeWindow
import dev.orestegabo.kaze.domain.TypographySpec
import dev.orestegabo.kaze.domain.VenueRef
import dev.orestegabo.kaze.domain.experience.AmenityHighlight
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
import dev.orestegabo.kaze.domain.guest.FollowUpPreference
import dev.orestegabo.kaze.domain.guest.GuestIdentity
import dev.orestegabo.kaze.domain.guest.LateCheckoutDecision
import dev.orestegabo.kaze.domain.guest.LateCheckoutStatus
import dev.orestegabo.kaze.domain.guest.LateCheckoutSubmission
import dev.orestegabo.kaze.domain.guest.ServiceRequestDraft
import dev.orestegabo.kaze.domain.guest.ServiceRequestReceipt
import dev.orestegabo.kaze.domain.guest.ServiceRequestStatus
import dev.orestegabo.kaze.domain.map.HotelMap
import dev.orestegabo.kaze.domain.map.importing.HotelMapSourceManifest
import dev.orestegabo.kaze.domain.map.importing.MapImportProfile
import dev.orestegabo.kaze.domain.map.importing.MapSourceFormat
import dev.orestegabo.kaze.domain.map.importing.TenantScopedImportRequest
import dev.orestegabo.kaze.domain.map.sampleMarriottConventionMap
import dev.orestegabo.kaze.application.AmenityStatus
import dev.orestegabo.kaze.application.GuestProfile

internal class InMemoryHotelRepository : HotelRepository {
    private val hotels = listOf(
        Hotel(
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
                    logoAsset = "k_logo.svg",
                    wordmarkAsset = "k_logo.svg",
                    typography = TypographySpec(
                        headingScale = 1.05f,
                        bodyScale = 1f,
                        labelScale = 0.96f,
                    ),
                ),
                supportedLocales = listOf("en", "fr"),
                defaultCurrencyCode = "RWF",
                mapImportProfile = MapImportProfile(
                    preferredFormats = listOf(MapSourceFormat.SVG, MapSourceFormat.DXF),
                    fallbackFormats = listOf(MapSourceFormat.IFC, MapSourceFormat.IFCXML, MapSourceFormat.GBXML),
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
        ),
    )

    override suspend fun getHotel(hotelId: String): Hotel? =
        hotels.firstOrNull { it.id == hotelId }

    override suspend fun requireHotel(hotelId: String): Hotel =
        getHotel(hotelId) ?: error("Unknown hotel id: $hotelId")
}

internal class GuestRepository {
    private val guests = listOf(
        GuestProfile(
            hotelId = "rw-kgl-marriott",
            guestId = "guest_aline",
            fullName = "Aline Uwase",
            stayId = "stay_001",
            roomId = "room_906",
        ),
        GuestProfile(
            hotelId = "rw-kgl-marriott",
            guestId = "guest_michael",
            fullName = "Michael Nshuti",
            stayId = "stay_002",
            roomId = "room_512",
        ),
    )

    fun findGuest(hotelId: String, guestId: String): GuestProfile? =
        guests.firstOrNull { it.hotelId == hotelId && it.guestId == guestId }
}

internal class InMemoryStayRepository : StayRepository {
    private val lateCheckoutDecisions = mutableListOf<LateCheckoutDecisionRecord>()
    private val serviceRequests = mutableListOf<ServiceRequestRecord>()

    override suspend fun getStayItinerary(guest: GuestIdentity): Itinerary =
        Itinerary(
            id = "itinerary_${guest.guestId}",
            hotelId = guest.hotelId,
            guestId = guest.guestId,
            stayWindow = TimeWindow(
                startIsoUtc = "2026-04-03T10:00:00Z",
                endIsoUtc = "2026-04-06T10:00:00Z",
            ),
            tabs = listOf(
                ItineraryTab(
                    mode = ItineraryMode.MY_STAY,
                    title = "My Stay",
                    sections = listOf(
                        ItinerarySection(
                            id = "core",
                            title = "Confirmed moments",
                            items = listOf(
                                ItineraryItem(
                                    id = "spa",
                                    title = "Signature massage",
                                    category = ItineraryItemCategory.SPA,
                                    timeWindow = TimeWindow(
                                        startIsoUtc = "2026-04-04T12:00:00Z",
                                        endIsoUtc = "2026-04-04T13:15:00Z",
                                    ),
                                    venue = VenueRef(
                                        nodeId = "registration",
                                        floorId = "l1",
                                        label = "Ubumwe Spa",
                                    ),
                                    status = ReservationStatus.CONFIRMED,
                                ),
                                ItineraryItem(
                                    id = "keynote",
                                    title = "Opening keynote",
                                    category = ItineraryItemCategory.EVENT_SESSION,
                                    timeWindow = TimeWindow(
                                        startIsoUtc = "2026-04-04T08:00:00Z",
                                        endIsoUtc = "2026-04-04T09:15:00Z",
                                    ),
                                    venue = VenueRef(
                                        nodeId = "keynote-room",
                                        floorId = "l9",
                                        label = "Great Rift Ballroom",
                                    ),
                                    status = ReservationStatus.CONFIRMED,
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

    override suspend fun submitLateCheckout(submission: LateCheckoutSubmission): LateCheckoutDecision {
        val decision = LateCheckoutDecision(
            requestId = "late_${submission.guest.guestId}_${lateCheckoutDecisions.size + 1}",
            status = LateCheckoutStatus.PENDING,
            approvedCheckoutTimeIso = submission.selection.checkoutTimeIso,
            feeAmountMinor = submission.selection.feeAmountMinor,
            currencyCode = submission.selection.currencyCode,
            note = when (submission.followUpPreference) {
                FollowUpPreference.VISIT_ROOM -> "Reception will coordinate an in-room follow-up if policy allows."
                FollowUpPreference.CALL_ROOM -> "Reception will call the room once availability is confirmed."
                FollowUpPreference.CONFIRM_IN_APP -> "Approval will appear quietly in the app."
            },
        )
        lateCheckoutDecisions += LateCheckoutDecisionRecord(
            hotelId = submission.guest.hotelId,
            guestId = submission.guest.guestId,
            decision = decision,
        )
        return decision
    }

    override suspend fun submitServiceRequest(request: ServiceRequestDraft): ServiceRequestReceipt {
        val receipt = ServiceRequestReceipt(
            requestId = "service_${request.type.name.lowercase()}_${request.guest.guestId}_${serviceRequests.size + 1}",
            type = request.type,
            status = ServiceRequestStatus.PENDING,
            note = "The hotel team has received the request.",
        )
        serviceRequests += ServiceRequestRecord(
            hotelId = request.guest.hotelId,
            guestId = request.guest.guestId,
            receipt = receipt,
        )
        return receipt
    }

    fun getLateCheckoutDecisions(hotelId: String, guestId: String): List<LateCheckoutDecision> =
        lateCheckoutDecisions.filter { it.hotelId == hotelId && it.guestId == guestId }.map { it.decision }

    fun getServiceRequestReceipts(hotelId: String, guestId: String): List<ServiceRequestReceipt> =
        serviceRequests.filter { it.hotelId == hotelId && it.guestId == guestId }.map { it.receipt }
}

internal class InMemoryExperienceRepository : ExperienceRepository {
    override suspend fun getEventDays(hotelId: String): List<EventDay> =
        listOf(
            EventDay(id = "day1", label = "Fri 3 Apr", dateIso = "2026-04-03"),
            EventDay(id = "day2", label = "Sat 4 Apr", dateIso = "2026-04-04"),
            EventDay(id = "day3", label = "Sun 5 Apr", dateIso = "2026-04-05"),
        )

    override suspend fun getEventSchedule(hotelId: String, dayId: String): List<ScheduledExperience> =
        allSessions.filter { it.dayId == dayId }

    override suspend fun getAmenityHighlights(hotelId: String): List<AmenityHighlight> =
        listOf(
            /*AmenityHighlight(
                id = "amenity_pool",
                title = "Infinity pool quiet hours",
                description = "A calmer pool deck period curated for business travelers between meetings.",
                locationLabel = "Pool Deck",
                availabilityLabel = "06:00 - 09:00",
                categoryLabel = "Amenity",
                accessLabel = "Included",
                actionLabel = "Open amenity",
            ),
            AmenityHighlight(
                id = "amenity_lobby",
                title = "Lobby art walk",
                description = "A short self-guided route through the hotel’s featured Rwandan artists.",
                locationLabel = "Grand Lobby",
                availabilityLabel = "All day",
                categoryLabel = "Self-guided",
                accessLabel = "Complimentary",
                actionLabel = "Start route",
            ),
            AmenityHighlight(
                id = "amenity_jazz",
                title = "Evening jazz set",
                description = "Soft live music in the bar, recommended for summit delegates after sessions.",
                locationLabel = "Panorama Bar",
                availabilityLabel = "20:00",
                categoryLabel = "Evening experience",
                accessLabel = "Extra charge",
                actionLabel = "Reserve table",
            ),*/
        )

    private val allSessions: List<ScheduledExperience> = listOf(
        /*ScheduledExperience(
            id = "session_welcome",
            dayId = "day1",
            title = "Welcome reception",
            description = "Arrival gathering for summit delegates with lounge music and light bites.",
            startIso = "2026-04-03T16:00:00Z",
            endIso = "2026-04-03T17:00:00Z",
            venueLabel = "Sky Lobby",
            hostLabel = "Guest Relations",
        ),
        ScheduledExperience(
            id = "session_keynote",
            dayId = "day2",
            title = "Opening keynote",
            description = "Main plenary session in the Great Rift Ballroom, with map route and speaker details available from the card.",
            startIso = "2026-04-04T08:00:00Z",
            endIso = "2026-04-04T09:15:00Z",
            venueLabel = "Great Rift Ballroom",
            hostLabel = "Finance Summit",
        ),
        ScheduledExperience(
            id = "session_roundtable",
            dayId = "day2",
            title = "Private investor roundtable",
            description = "Invitation-only gathering with live occupancy and room lookup support.",
            startIso = "2026-04-04T11:00:00Z",
            endIso = "2026-04-04T12:00:00Z",
            venueLabel = "Virunga Room",
            hostLabel = "Executive Office",
        ),
        ScheduledExperience(
            id = "session_brunch",
            dayId = "day3",
            title = "Farewell brunch",
            description = "Closing brunch for delegates and hotel guests who opted into the event program.",
            startIso = "2026-04-05T10:00:00Z",
            endIso = "2026-04-05T11:30:00Z",
            venueLabel = "Kivu Terrace",
            hostLabel = "Events Team",
        ),*/
    )
}

internal class InMemoryMapRepository : MapRepository {
    private var currentMap: HotelMap = sampleMarriottConventionMap

    override suspend fun getHotelMap(hotelId: String, mapId: String): HotelMap? =
        currentMap.takeIf { it.hotelId == hotelId && it.mapId == mapId }

    override suspend fun saveHotelMap(map: HotelMap) {
        currentMap = map
    }

    override suspend fun importHotelMap(
        manifest: HotelMapSourceManifest,
        requests: List<TenantScopedImportRequest>,
    ): HotelMap = currentMap.copy(sourceManifest = manifest)
}

internal class AmenityKnowledgeRepository {
    private val amenities = listOf(
        AmenityStatus(
            id = "kitchen",
            title = "Hotel kitchen",
            locationLabel = "Back-of-house culinary service",
            statusLabel = "Open now",
            hoursLabel = "Open daily until 22:30",
            openNow = true,
        ),
        AmenityStatus(
            id = "restaurant",
            title = "Kivu Dining",
            locationLabel = "Ground floor",
            statusLabel = "Open now",
            hoursLabel = "Breakfast 06:30 - 10:30, lunch and dinner until 22:30",
            openNow = true,
        ),
        AmenityStatus(
            id = "spa",
            title = "Ubumwe Spa",
            locationLabel = "Wellness level",
            statusLabel = "Open now",
            hoursLabel = "Open daily from 09:00 to 21:00",
            openNow = true,
        ),
        AmenityStatus(
            id = "pool",
            title = "Infinity pool",
            locationLabel = "Pool Deck",
            statusLabel = "Open now",
            hoursLabel = "Open daily from 06:00 to 20:00",
            openNow = true,
        ),
    )

    fun listAmenities(hotelId: String): List<AmenityStatus> {
        require(hotelId == "rw-kgl-marriott") { "Unknown hotel id: $hotelId" }
        return amenities
    }

    fun findAmenity(hotelId: String, key: String): AmenityStatus? =
        listAmenities(hotelId).firstOrNull { amenity ->
            amenity.id == key || amenity.title.contains(key, ignoreCase = true)
        }
}

private data class LateCheckoutDecisionRecord(
    val hotelId: String,
    val guestId: String,
    val decision: LateCheckoutDecision,
)

private data class ServiceRequestRecord(
    val hotelId: String,
    val guestId: String,
    val receipt: ServiceRequestReceipt,
)
