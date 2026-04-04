package dev.orestegabo.kaze.presentation.stay

import dev.orestegabo.kaze.data.repository.HotelRepository
import dev.orestegabo.kaze.data.repository.StayRepository
import dev.orestegabo.kaze.domain.Hotel
import dev.orestegabo.kaze.domain.HotelBranding
import dev.orestegabo.kaze.domain.HotelCampus
import dev.orestegabo.kaze.domain.HotelConfig
import dev.orestegabo.kaze.domain.HotelMarket
import dev.orestegabo.kaze.domain.Itinerary
import dev.orestegabo.kaze.domain.TypographySpec
import dev.orestegabo.kaze.domain.guest.GuestIdentity
import dev.orestegabo.kaze.domain.guest.LateCheckoutDecision
import dev.orestegabo.kaze.domain.guest.LateCheckoutStatus
import dev.orestegabo.kaze.domain.guest.LateCheckoutSubmission
import dev.orestegabo.kaze.domain.guest.ServiceRequestDraft
import dev.orestegabo.kaze.domain.guest.ServiceRequestReceipt
import dev.orestegabo.kaze.domain.guest.ServiceRequestStatus
import dev.orestegabo.kaze.presentation.demo.LateCheckoutDraft
import dev.orestegabo.kaze.presentation.demo.ServiceRequestDraftUi
import dev.orestegabo.kaze.presentation.demo.StayPrimaryAction
import dev.orestegabo.kaze.presentation.demo.StayScreen
import dev.orestegabo.kaze.presentation.demo.StayTab
import dev.orestegabo.kaze.usecase.ObserveHotelContextUseCase
import dev.orestegabo.kaze.usecase.SubmitLateCheckoutUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StayViewModelTest {

    @Test
    fun request_service_action_opens_request_screen_and_selects_requests_tab() {
        val repository = RecordingStayRepository()
        val viewModel = createViewModel(repository)
        val option = viewModel.uiState.requestOptions.first { it.title == "Fresh towels" }

        val result = viewModel.handleAction(StayPrimaryAction.RequestService(option))

        assertEquals(null, result)
        assertEquals(StayScreen.SERVICE_REQUEST, viewModel.uiState.activeStayScreen)
        assertEquals(StayTab.REQUESTS, viewModel.uiState.selectedTab)
        assertEquals("Fresh towels", viewModel.uiState.serviceRequestDraft.option.title)
    }

    @Test
    fun submit_service_request_saves_history_and_feedback() {
        val repository = RecordingStayRepository()
        val viewModel = createViewModel(repository)
        val towels = viewModel.uiState.requestOptions.first { it.title == "Fresh towels" }

        val result = viewModel.submitServiceRequest(
            ServiceRequestDraftUi(
                option = towels,
                quantity = 3,
                notes = "Leave them at the door",
            ),
        )

        assertIs<StayActionResult.Feedback>(result)
        assertTrue(result.message.contains("Fresh towels request sent"))
        assertEquals(StayScreen.HOME, viewModel.uiState.activeStayScreen)
        assertEquals(StayTab.REQUESTS, viewModel.uiState.selectedTab)
        assertEquals(1, viewModel.uiState.submittedServiceRequests.size)
        assertEquals(3, viewModel.uiState.submittedServiceRequests.first().quantity)
        assertEquals("Leave them at the door", viewModel.uiState.submittedServiceRequests.first().notes)
        assertNotNull(repository.lastServiceRequestDraft)
        assertEquals(dev.orestegabo.kaze.domain.guest.ServiceRequestType.TOWELS, repository.lastServiceRequestDraft?.type)
    }

    @Test
    fun submit_custom_request_combines_custom_text_and_note() {
        val repository = RecordingStayRepository()
        val viewModel = createViewModel(repository)
        val custom = viewModel.uiState.requestOptions.first { it.isCustom }

        viewModel.submitServiceRequest(
            ServiceRequestDraftUi(
                option = custom,
                customRequest = "Need a baby crib",
                notes = "Please bring it before 20:00",
            ),
        )

        val submitted = viewModel.uiState.submittedServiceRequests.first()
        assertEquals("Need a baby crib\nPlease bring it before 20:00", submitted.notes)
        assertEquals(
            "Need a baby crib\nPlease bring it before 20:00",
            repository.lastServiceRequestDraft?.note,
        )
    }

    @Test
    fun submit_late_checkout_updates_request_state() {
        val repository = RecordingStayRepository()
        val viewModel = createViewModel(repository)

        val result = viewModel.submitLateCheckout(LateCheckoutDraft())

        assertIs<StayActionResult.Feedback>(result)
        assertNotNull(viewModel.uiState.lateCheckoutRequest)
        assertEquals(StayScreen.HOME, viewModel.uiState.activeStayScreen)
        assertEquals(LateCheckoutStatus.PENDING, repository.lastLateCheckoutDecision?.status)
    }

    private fun createViewModel(repository: RecordingStayRepository): StayViewModel =
        StayViewModel(
            hotelId = "rw-kgl-marriott",
            guestIdentity = GuestIdentity(
                hotelId = "rw-kgl-marriott",
                guestId = "guest_aline",
                stayId = "stay_001",
                roomId = "room_906",
            ),
            observeHotelContext = ObserveHotelContextUseCase(
                hotelRepository = object : HotelRepository {
                    override suspend fun getHotel(hotelId: String): Hotel? = null

                    override suspend fun requireHotel(hotelId: String): Hotel = testHotel(hotelId)
                },
            ),
            stayRepository = repository,
            submitLateCheckoutUseCase = SubmitLateCheckoutUseCase(repository),
        )

    private fun testHotel(hotelId: String): Hotel =
        Hotel(
            id = hotelId,
            slug = "demo",
            name = "Demo Hotel",
            market = HotelMarket.LUXURY_HOTEL,
            timezoneId = "Africa/Kigali",
            config = HotelConfig(
                hotelId = hotelId,
                displayName = "Demo Hotel",
                branding = HotelBranding(
                    primaryHex = "#123456",
                    secondaryHex = "#654321",
                    accentHex = "#abcdef",
                    surfaceHex = "#ffffff",
                    backgroundHex = "#f7f7f7",
                    logoAsset = "logo.svg",
                    wordmarkAsset = "wordmark.svg",
                    typography = TypographySpec(),
                ),
                supportedLocales = listOf("en"),
                defaultCurrencyCode = "RWF",
            ),
            campus = HotelCampus(city = "Kigali", countryCode = "RW", buildings = emptyList()),
            activeExperiences = emptySet(),
        )

    private class RecordingStayRepository : StayRepository {
        var lastServiceRequestDraft: ServiceRequestDraft? = null
        var lastLateCheckoutDecision: LateCheckoutDecision? = null

        override suspend fun getStayItinerary(guest: GuestIdentity): Itinerary? = null

        override suspend fun submitLateCheckout(submission: LateCheckoutSubmission): LateCheckoutDecision =
            LateCheckoutDecision(
                requestId = "late_1",
                status = LateCheckoutStatus.PENDING,
            ).also { lastLateCheckoutDecision = it }

        override suspend fun submitServiceRequest(request: ServiceRequestDraft): ServiceRequestReceipt =
            ServiceRequestReceipt(
                requestId = "req_1",
                type = request.type,
                status = ServiceRequestStatus.PENDING,
                note = "The hotel will confirm it shortly.",
            ).also { lastServiceRequestDraft = request }
    }
}
