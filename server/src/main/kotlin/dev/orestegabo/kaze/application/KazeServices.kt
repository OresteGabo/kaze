package dev.orestegabo.kaze.application

import dev.orestegabo.kaze.api.ApiNotFoundException
import dev.orestegabo.kaze.data.repository.ExperienceRepository
import dev.orestegabo.kaze.data.repository.HotelRepository
import dev.orestegabo.kaze.data.repository.MapRepository
import dev.orestegabo.kaze.data.repository.StayRepository
import dev.orestegabo.kaze.domain.Hotel
import dev.orestegabo.kaze.domain.Itinerary
import dev.orestegabo.kaze.domain.experience.AmenityHighlight
import dev.orestegabo.kaze.domain.experience.EventDay
import dev.orestegabo.kaze.domain.experience.ScheduledExperience
import dev.orestegabo.kaze.domain.guest.FollowUpPreference
import dev.orestegabo.kaze.domain.guest.GuestIdentity
import dev.orestegabo.kaze.domain.guest.LateCheckoutDecision
import dev.orestegabo.kaze.domain.guest.LateCheckoutSelection
import dev.orestegabo.kaze.domain.guest.LateCheckoutSubmission
import dev.orestegabo.kaze.domain.guest.PaymentPreference
import dev.orestegabo.kaze.domain.guest.ServiceRequestDraft
import dev.orestegabo.kaze.domain.guest.ServiceRequestReceipt
import dev.orestegabo.kaze.domain.guest.ServiceRequestType
import dev.orestegabo.kaze.domain.map.HotelMap
import dev.orestegabo.kaze.infrastructure.AmenityKnowledgeRepository
import dev.orestegabo.kaze.infrastructure.DatabaseFactory
import dev.orestegabo.kaze.infrastructure.GuestRepository
import dev.orestegabo.kaze.infrastructure.JdbcExperienceRepository
import dev.orestegabo.kaze.infrastructure.JdbcHotelRepository
import dev.orestegabo.kaze.infrastructure.JdbcMapRepository
import dev.orestegabo.kaze.infrastructure.JdbcStayRepository

internal data class GuestProfile(
    val hotelId: String,
    val guestId: String,
    val fullName: String,
    val stayId: String?,
    val roomId: String?,
)

internal data class AmenityStatus(
    val id: String,
    val title: String,
    val locationLabel: String,
    val statusLabel: String,
    val hoursLabel: String,
    val openNow: Boolean,
)

internal data class AssistantAnswer(
    val answer: String,
    val source: String,
    val confidence: String,
)

internal class HotelQueryService(
    private val hotelRepository: HotelRepository,
) {
    suspend fun listHotels(): List<Hotel> =
        hotelRepository.listHotels()

    suspend fun getHotel(hotelId: String): Hotel =
        hotelRepository.getHotel(hotelId)
            ?: throw ApiNotFoundException("Unknown hotel id: $hotelId")
}

internal class GuestStayService(
    private val guestRepository: GuestRepository,
    private val stayRepository: StayRepository,
) {
    fun getGuest(hotelId: String, guestId: String): GuestProfile =
        guestRepository.findGuest(hotelId, guestId)
            ?: throw ApiNotFoundException("Unknown guest id: $guestId")

    suspend fun getItinerary(hotelId: String, guestId: String): Itinerary {
        val guest = getGuest(hotelId, guestId)
        return stayRepository.getStayItinerary(guest.toIdentity())
            ?: throw ApiNotFoundException("No itinerary found for guest id: $guestId")
    }

    suspend fun submitLateCheckout(
        hotelId: String,
        guestId: String,
        checkoutTimeIso: String,
        feeAmountMinor: Long,
        currencyCode: String,
        paymentPreference: String,
        followUpPreference: String,
        notes: String?,
        stayId: String?,
        roomId: String?,
    ): LateCheckoutDecision {
        val guest = getGuest(hotelId, guestId)
        return stayRepository.submitLateCheckout(
            LateCheckoutSubmission(
                guest = guest.toIdentity(
                    stayIdOverride = stayId,
                    roomIdOverride = roomId,
                ),
                selection = LateCheckoutSelection(
                    checkoutTimeIso = checkoutTimeIso,
                    feeAmountMinor = feeAmountMinor,
                    currencyCode = currencyCode,
                ),
                paymentPreference = paymentPreference.toPaymentPreference(),
                followUpPreference = followUpPreference.toFollowUpPreference(),
                notes = notes?.takeIf { it.isNotBlank() },
            ),
        )
    }

    suspend fun submitServiceRequest(
        hotelId: String,
        guestId: String,
        type: String,
        note: String?,
        stayId: String?,
        roomId: String?,
    ): ServiceRequestReceipt {
        val guest = getGuest(hotelId, guestId)
        return stayRepository.submitServiceRequest(
            ServiceRequestDraft(
                guest = guest.toIdentity(
                    stayIdOverride = stayId,
                    roomIdOverride = roomId,
                ),
                type = type.toServiceRequestType(),
                note = note?.takeIf { it.isNotBlank() },
            ),
        )
    }

    suspend fun getLateCheckoutHistory(hotelId: String, guestId: String): List<LateCheckoutDecision> {
        val guest = getGuest(hotelId, guestId)
        return stayRepository.getLateCheckoutHistory(guest.toIdentity())
    }

    suspend fun getServiceRequestHistory(hotelId: String, guestId: String): List<ServiceRequestReceipt> {
        val guest = getGuest(hotelId, guestId)
        return stayRepository.getServiceRequestHistory(guest.toIdentity())
    }
}

internal class ExperienceQueryService(
    private val experienceRepository: ExperienceRepository,
) {
    suspend fun getEventDays(hotelId: String): List<EventDay> =
        experienceRepository.getEventDays(hotelId)

    suspend fun getSchedule(hotelId: String, dayId: String): List<ScheduledExperience> =
        experienceRepository.getEventSchedule(hotelId, dayId)

    suspend fun getHighlights(hotelId: String): List<AmenityHighlight> =
        experienceRepository.getAmenityHighlights(hotelId)
}

internal class MapQueryService(
    private val mapRepository: MapRepository,
) {
    suspend fun getHotelMap(hotelId: String, mapId: String?): HotelMap =
        mapRepository.getHotelMap(hotelId, mapId)
            ?: throw ApiNotFoundException(
                if (mapId == null) {
                    "No map found for hotel id: $hotelId"
                } else {
                    "Unknown map id: $mapId for hotel id: $hotelId"
                },
            )
}

internal class AssistantService(
    private val amenityKnowledgeRepository: AmenityKnowledgeRepository,
) {
    fun listAmenityStatuses(hotelId: String): List<AmenityStatus> =
        amenityKnowledgeRepository.listAmenities(hotelId)

    fun answer(hotelId: String, question: String): AssistantAnswer {
        val normalized = question.trim().lowercase()
        require(normalized.isNotBlank()) { "Question must not be blank" }

        val kitchenStatus = amenityKnowledgeRepository.findAmenity(hotelId, "kitchen")
        val restaurantStatus = amenityKnowledgeRepository.findAmenity(hotelId, "restaurant")
        val spaStatus = amenityKnowledgeRepository.findAmenity(hotelId, "spa")
        val poolStatus = amenityKnowledgeRepository.findAmenity(hotelId, "pool")

        return when {
            normalized.contains("kitchen") || normalized.contains("food") || normalized.contains("room service") ->
                kitchenStatus?.let {
                    AssistantAnswer(
                        answer = "${it.title} is ${if (it.openNow) "open" else "closed"} right now. ${it.hoursLabel}.",
                        source = "hotel amenity hours",
                        confidence = "high",
                    )
                }

            normalized.contains("restaurant") || normalized.contains("dining") ->
                restaurantStatus?.let {
                    AssistantAnswer(
                        answer = "${it.title} is ${if (it.openNow) "open" else "closed"} right now. ${it.hoursLabel}.",
                        source = "hotel amenity hours",
                        confidence = "high",
                    )
                }

            normalized.contains("spa") ->
                spaStatus?.let {
                    AssistantAnswer(
                        answer = "${it.title} is ${if (it.openNow) "open" else "closed"} right now. ${it.hoursLabel}.",
                        source = "hotel amenity hours",
                        confidence = "high",
                    )
                }

            normalized.contains("pool") ->
                poolStatus?.let {
                    AssistantAnswer(
                        answer = "${it.title} is ${if (it.openNow) "open" else "closed"} right now. ${it.hoursLabel}.",
                        source = "hotel amenity hours",
                        confidence = "high",
                    )
                }

            normalized.contains("late checkout") ->
                AssistantAnswer(
                    answer = "Yes, you can request late checkout in the app. Final approval depends on occupancy and housekeeping availability.",
                    source = "late checkout policy",
                    confidence = "medium",
                )

            else ->
                AssistantAnswer(
                    answer = "I do not have a trusted structured answer for that yet. The safest next step is to ask reception or connect this feature to live hotel knowledge data.",
                    source = "fallback",
                    confidence = "low",
                )
        } ?: AssistantAnswer(
            answer = "I do not have a trusted structured answer for that yet. The safest next step is to ask reception or connect this feature to live hotel knowledge data.",
            source = "fallback",
            confidence = "low",
        )
    }
}

internal data class ServerDependencies(
    val hotelService: HotelQueryService,
    val guestStayService: GuestStayService,
    val experienceService: ExperienceQueryService,
    val mapService: MapQueryService,
    val assistantService: AssistantService,
)

internal fun createServerDependencies(): ServerDependencies {
    val dataSource = DatabaseFactory.dataSource
    val guestRepository = GuestRepository(dataSource)
    val hotelRepository = JdbcHotelRepository(dataSource)
    val stayRepository = JdbcStayRepository(dataSource)
    val experienceRepository = JdbcExperienceRepository(dataSource)
    val mapRepository = JdbcMapRepository(dataSource)
    val amenityKnowledgeRepository = AmenityKnowledgeRepository(dataSource)

    return ServerDependencies(
        hotelService = HotelQueryService(hotelRepository),
        guestStayService = GuestStayService(guestRepository, stayRepository),
        experienceService = ExperienceQueryService(experienceRepository),
        mapService = MapQueryService(mapRepository),
        assistantService = AssistantService(amenityKnowledgeRepository),
    )
}

private fun GuestProfile.toIdentity(
    stayIdOverride: String? = null,
    roomIdOverride: String? = null,
): GuestIdentity = GuestIdentity(
    hotelId = hotelId,
    guestId = guestId,
    stayId = stayIdOverride ?: stayId,
    roomId = roomIdOverride ?: roomId,
)

private fun String.toPaymentPreference(): PaymentPreference =
    when (uppercase()) {
        PaymentPreference.CHARGE_TO_ROOM.name -> PaymentPreference.CHARGE_TO_ROOM
        PaymentPreference.PAY_AT_RECEPTION.name -> PaymentPreference.PAY_AT_RECEPTION
        PaymentPreference.PAY_IN_ROOM.name -> PaymentPreference.PAY_IN_ROOM
        else -> throw IllegalArgumentException("Unsupported paymentPreference: $this")
    }

private fun String.toFollowUpPreference(): FollowUpPreference =
    when (uppercase()) {
        FollowUpPreference.CONFIRM_IN_APP.name -> FollowUpPreference.CONFIRM_IN_APP
        FollowUpPreference.CALL_ROOM.name -> FollowUpPreference.CALL_ROOM
        FollowUpPreference.VISIT_ROOM.name -> FollowUpPreference.VISIT_ROOM
        else -> throw IllegalArgumentException("Unsupported followUpPreference: $this")
    }

private fun String.toServiceRequestType(): ServiceRequestType =
    when (uppercase()) {
        ServiceRequestType.TOWELS.name -> ServiceRequestType.TOWELS
        ServiceRequestType.DINING.name -> ServiceRequestType.DINING
        ServiceRequestType.LAUNDRY.name -> ServiceRequestType.LAUNDRY
        ServiceRequestType.CONCIERGE.name -> ServiceRequestType.CONCIERGE
        ServiceRequestType.HOUSEKEEPING.name -> ServiceRequestType.HOUSEKEEPING
        else -> throw IllegalArgumentException("Unsupported service request type: $this")
    }
