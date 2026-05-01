package dev.orestegabo.kaze.presentation.api

import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.data.repository.HotelRepository
import dev.orestegabo.kaze.data.repository.MapRepository
import dev.orestegabo.kaze.data.repository.StayRepository
import dev.orestegabo.kaze.domain.ExperienceMode
import dev.orestegabo.kaze.domain.Hotel
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
import dev.orestegabo.kaze.domain.VenueRef
import dev.orestegabo.kaze.domain.experience.AmenityHighlight
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
import dev.orestegabo.kaze.domain.guest.FollowUpPreference
import dev.orestegabo.kaze.domain.guest.GuestIdentity
import dev.orestegabo.kaze.domain.guest.LateCheckoutDecision
import dev.orestegabo.kaze.domain.guest.LateCheckoutStatus
import dev.orestegabo.kaze.domain.guest.LateCheckoutSubmission
import dev.orestegabo.kaze.domain.guest.PaymentPreference
import dev.orestegabo.kaze.domain.guest.ServiceRequestDraft
import dev.orestegabo.kaze.domain.guest.ServiceRequestReceipt
import dev.orestegabo.kaze.domain.guest.ServiceRequestStatus
import dev.orestegabo.kaze.domain.guest.ServiceRequestType
import dev.orestegabo.kaze.domain.map.FloorLevel
import dev.orestegabo.kaze.domain.map.HotelMap
import dev.orestegabo.kaze.domain.map.MapNode
import dev.orestegabo.kaze.domain.map.MapNodeKind
import dev.orestegabo.kaze.domain.map.MapPoint
import dev.orestegabo.kaze.domain.map.MapSize
import dev.orestegabo.kaze.domain.map.importing.HotelMapSourceManifest
import dev.orestegabo.kaze.domain.map.importing.TenantScopedImportRequest
import dev.orestegabo.kaze.presentation.auth.createAuthHttpClient
import dev.orestegabo.kaze.presentation.demo.sampleHotel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable

internal fun createKazeApiRepositories(
    baseUrl: String,
): KazeApiRepositories {
    val client = createAuthHttpClient()
    return KazeApiRepositories(
        hotelRepository = KtorHotelRepository(client, baseUrl),
        stayRepository = KtorStayRepository(client, baseUrl),
        experienceRepository = KtorExperienceRepository(client, baseUrl),
        mapRepository = KtorMapRepository(client, baseUrl),
    )
}

internal data class KazeApiRepositories(
    val hotelRepository: HotelRepository,
    val stayRepository: StayRepository,
    val experienceRepository: ExperienceRepository,
    val mapRepository: MapRepository,
)

private class KtorHotelRepository(
    private val client: HttpClient,
    baseUrl: String,
) : HotelRepository {
    private val apiBaseUrl = baseUrl.trimEnd('/')

    override suspend fun listHotels(): List<Hotel> =
        client.get("$apiBaseUrl/hotels").body<List<HotelDto>>().map(HotelDto::toDomain)

    override suspend fun getHotel(hotelId: String): Hotel =
        client.get("$apiBaseUrl/hotels/$hotelId").body<HotelDto>().toDomain()

    override suspend fun requireHotel(hotelId: String): Hotel =
        getHotel(hotelId)
}

private class KtorExperienceRepository(
    private val client: HttpClient,
    baseUrl: String,
) : ExperienceRepository {
    private val apiBaseUrl = baseUrl.trimEnd('/')

    override suspend fun getEventDays(hotelId: String): List<EventDay> =
        client.get("$apiBaseUrl/hotels/$hotelId/events/days").body<List<EventDayDto>>().map(EventDayDto::toDomain)

    override suspend fun getEventSchedule(hotelId: String, dayId: String): List<ScheduledExperience> =
        client.get("$apiBaseUrl/hotels/$hotelId/events/schedule") {
            parameter("dayId", dayId)
        }.body<List<ScheduledExperienceDto>>().map(ScheduledExperienceDto::toDomain)

    override suspend fun getAmenityHighlights(hotelId: String): List<AmenityHighlight> =
        client.get("$apiBaseUrl/hotels/$hotelId/explore/highlights")
            .body<List<AmenityHighlightDto>>()
            .map(AmenityHighlightDto::toDomain)
}

private class KtorMapRepository(
    private val client: HttpClient,
    baseUrl: String,
) : MapRepository {
    private val apiBaseUrl = baseUrl.trimEnd('/')

    override suspend fun getHotelMap(hotelId: String, mapId: String?): HotelMap =
        client.get("$apiBaseUrl/hotels/$hotelId/map") {
            if (!mapId.isNullOrBlank()) {
                parameter("mapId", mapId)
            }
        }.body<HotelMapDto>().toDomain()

    override suspend fun saveHotelMap(map: HotelMap) {
        error("Saving hotel maps is not supported from the app yet.")
    }

    override suspend fun importHotelMap(
        manifest: HotelMapSourceManifest,
        requests: List<TenantScopedImportRequest>,
    ): HotelMap {
        error("Map import is not supported from the app yet.")
    }
}

private class KtorStayRepository(
    private val client: HttpClient,
    baseUrl: String,
) : StayRepository {
    private val apiBaseUrl = baseUrl.trimEnd('/')

    override suspend fun getStayItinerary(guest: GuestIdentity): Itinerary =
        client.get("$apiBaseUrl/hotels/${guest.hotelId}/guests/${guest.guestId}/itinerary")
            .body<ItineraryDto>()
            .toDomain()

    override suspend fun submitLateCheckout(submission: LateCheckoutSubmission): LateCheckoutDecision =
        client.post("$apiBaseUrl/hotels/${submission.guest.hotelId}/guests/${submission.guest.guestId}/late-checkout") {
            setBody(
                LateCheckoutSubmissionRequestDto(
                    stayId = submission.guest.stayId,
                    roomId = submission.guest.roomId,
                    checkoutTimeIso = submission.selection.checkoutTimeIso,
                    feeAmountMinor = submission.selection.feeAmountMinor,
                    currencyCode = submission.selection.currencyCode,
                    paymentPreference = submission.paymentPreference.name,
                    followUpPreference = submission.followUpPreference.name,
                    notes = submission.notes,
                ),
            )
        }.body<LateCheckoutDecisionDto>().toDomain()

    override suspend fun submitServiceRequest(request: ServiceRequestDraft): ServiceRequestReceipt =
        client.post("$apiBaseUrl/hotels/${request.guest.hotelId}/guests/${request.guest.guestId}/service-requests") {
            setBody(
                ServiceRequestSubmissionRequestDto(
                    stayId = request.guest.stayId,
                    roomId = request.guest.roomId,
                    type = request.type.name,
                    note = request.note,
                ),
            )
        }.body<ServiceRequestReceiptDto>().toDomain()

    override suspend fun getLateCheckoutHistory(guest: GuestIdentity): List<LateCheckoutDecision> =
        client.get("$apiBaseUrl/hotels/${guest.hotelId}/guests/${guest.guestId}/late-checkout")
            .body<List<LateCheckoutDecisionDto>>()
            .map(LateCheckoutDecisionDto::toDomain)

    override suspend fun getServiceRequestHistory(guest: GuestIdentity): List<ServiceRequestReceipt> =
        client.get("$apiBaseUrl/hotels/${guest.hotelId}/guests/${guest.guestId}/service-requests")
            .body<List<ServiceRequestReceiptDto>>()
            .map(ServiceRequestReceiptDto::toDomain)
}

@Serializable
private data class HotelDto(
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
private data class ItineraryDto(
    val id: String,
    val hotelId: String,
    val guestId: String,
    val stayWindow: TimeWindowDto,
    val tabs: List<ItineraryTabDto>,
)

@Serializable
private data class ItineraryTabDto(
    val mode: String,
    val title: String,
    val sections: List<ItinerarySectionDto>,
)

@Serializable
private data class ItinerarySectionDto(
    val id: String,
    val title: String,
    val items: List<ItineraryItemDto>,
)

@Serializable
private data class ItineraryItemDto(
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
private data class VenueRefDto(
    val nodeId: String,
    val floorId: String,
    val label: String,
)

@Serializable
private data class TimeWindowDto(
    val startIsoUtc: String,
    val endIsoUtc: String,
)

@Serializable
private data class EventDayDto(
    val id: String,
    val label: String,
    val dateIso: String,
)

@Serializable
private data class ScheduledExperienceDto(
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
private data class AmenityHighlightDto(
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
private data class HotelMapDto(
    val hotelId: String,
    val mapId: String,
    val name: String,
    val floors: List<FloorDto>,
)

@Serializable
private data class FloorDto(
    val id: String,
    val label: String,
    val levelIndex: Int,
    val width: Float,
    val height: Float,
    val nodes: List<MapNodeDto>,
)

@Serializable
private data class MapNodeDto(
    val id: String,
    val label: String,
    val kind: String,
    val x: Float,
    val y: Float,
)

@Serializable
private data class LateCheckoutSubmissionRequestDto(
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
private data class LateCheckoutDecisionDto(
    val requestId: String,
    val status: String,
    val approvedCheckoutTimeIso: String? = null,
    val feeAmountMinor: Long? = null,
    val currencyCode: String? = null,
    val note: String? = null,
)

@Serializable
private data class ServiceRequestSubmissionRequestDto(
    val stayId: String? = null,
    val roomId: String? = null,
    val type: String,
    val note: String? = null,
)

@Serializable
private data class ServiceRequestReceiptDto(
    val requestId: String,
    val type: String,
    val status: String,
    val note: String? = null,
)

private fun HotelDto.toDomain(): Hotel {
    val fallback = sampleHotel.takeIf { it.id == id }
    return Hotel(
        id = id,
        slug = slug,
        name = name,
        market = runCatching { HotelMarket.valueOf(market) }.getOrElse {
            fallback?.market ?: HotelMarket.LUXURY_HOTEL
        },
        timezoneId = timezoneId,
        config = (fallback?.config ?: sampleHotel.config).copy(
            hotelId = id,
            displayName = displayName,
            supportedLocales = supportedLocales,
        ),
        campus = fallback?.campus?.copy(
            city = city,
            countryCode = countryCode,
        ) ?: HotelCampus(
            city = city,
            countryCode = countryCode,
            buildings = emptyList(),
        ),
        activeExperiences = activeExperiences
            .mapNotNull { value -> runCatching { ExperienceMode.valueOf(value) }.getOrNull() }
            .toSet(),
    )
}

private fun ItineraryDto.toDomain(): Itinerary =
    Itinerary(
        id = id,
        hotelId = hotelId,
        guestId = guestId,
        stayWindow = stayWindow.toDomain(),
        tabs = tabs.map(ItineraryTabDto::toDomain),
    )

private fun ItineraryTabDto.toDomain(): ItineraryTab =
    ItineraryTab(
        mode = runCatching { ItineraryMode.valueOf(mode) }.getOrElse { ItineraryMode.MY_STAY },
        title = title,
        sections = sections.map(ItinerarySectionDto::toDomain),
    )

private fun ItinerarySectionDto.toDomain(): ItinerarySection =
    ItinerarySection(
        id = id,
        title = title,
        items = items.map(ItineraryItemDto::toDomain),
    )

private fun ItineraryItemDto.toDomain(): ItineraryItem =
    ItineraryItem(
        id = id,
        title = title,
        category = runCatching { ItineraryItemCategory.valueOf(category) }.getOrElse {
            ItineraryItemCategory.EVENT_SESSION
        },
        timeWindow = TimeWindow(startIsoUtc = startIsoUtc, endIsoUtc = endIsoUtc),
        venue = venue?.toDomain(),
        status = runCatching { ReservationStatus.valueOf(status) }.getOrElse { ReservationStatus.CONFIRMED },
        notes = notes,
    )

private fun VenueRefDto.toDomain(): VenueRef =
    VenueRef(nodeId = nodeId, floorId = floorId, label = label)

private fun TimeWindowDto.toDomain(): TimeWindow =
    TimeWindow(startIsoUtc = startIsoUtc, endIsoUtc = endIsoUtc)

private fun EventDayDto.toDomain(): EventDay =
    EventDay(id = id, label = label, dateIso = dateIso)

private fun ScheduledExperienceDto.toDomain(): ScheduledExperience =
    ScheduledExperience(
        id = id,
        dayId = dayId,
        title = title,
        description = description,
        startIso = startIso,
        endIso = endIso,
        venueLabel = venueLabel,
        hostLabel = hostLabel,
    )

private fun AmenityHighlightDto.toDomain(): AmenityHighlight =
    AmenityHighlight(
        id = id,
        title = title,
        description = description,
        locationLabel = locationLabel,
        availabilityLabel = availabilityLabel,
        categoryLabel = categoryLabel,
        accessLabel = accessLabel,
        actionLabel = actionLabel,
    )

private fun HotelMapDto.toDomain(): HotelMap =
    HotelMap(
        hotelId = hotelId,
        mapId = mapId,
        name = name,
        floors = floors.map(FloorDto::toDomain),
    )

private fun FloorDto.toDomain(): FloorLevel =
    FloorLevel(
        id = id,
        buildingId = id,
        label = label,
        levelIndex = levelIndex,
        canvasSize = MapSize(width = width, height = height),
        nodes = nodes.map { node -> node.toDomain(id) },
        edges = emptyList(),
    )

private fun MapNodeDto.toDomain(floorId: String): MapNode =
    MapNode(
        id = id,
        label = label,
        kind = runCatching { MapNodeKind.valueOf(kind) }.getOrElse { MapNodeKind.LANDMARK },
        floorId = floorId,
        position = MapPoint(x = x, y = y),
    )

private fun LateCheckoutDecisionDto.toDomain(): LateCheckoutDecision =
    LateCheckoutDecision(
        requestId = requestId,
        status = runCatching { LateCheckoutStatus.valueOf(status) }.getOrElse { LateCheckoutStatus.PENDING },
        approvedCheckoutTimeIso = approvedCheckoutTimeIso,
        feeAmountMinor = feeAmountMinor,
        currencyCode = currencyCode,
        note = note,
    )

private fun ServiceRequestReceiptDto.toDomain(): ServiceRequestReceipt =
    ServiceRequestReceipt(
        requestId = requestId,
        type = runCatching { ServiceRequestType.valueOf(type) }.getOrElse { ServiceRequestType.HOUSEKEEPING },
        status = runCatching { ServiceRequestStatus.valueOf(status) }.getOrElse { ServiceRequestStatus.PENDING },
        note = note,
    )
