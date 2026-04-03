package dev.orestegabo.kaze.usecase

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
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class UseCaseArchitectureTest {

    @Test
    fun observe_hotel_context_returns_required_hotel() {
        val useCase = ObserveHotelContextUseCase(
            hotelRepository = object : HotelRepository {
                override suspend fun getHotel(hotelId: String): Hotel? = null

                override suspend fun requireHotel(hotelId: String): Hotel = testHotel(hotelId)
            },
        )

        val hotel = runImmediate { useCase("rw-kgl-marriott") }

        assertEquals("rw-kgl-marriott", hotel.id)
    }

    @Test
    fun submit_late_checkout_delegates_to_repository() {
        val useCase = SubmitLateCheckoutUseCase(
            stayRepository = object : StayRepository {
                override suspend fun getStayItinerary(guest: GuestIdentity): Itinerary? = null

                override suspend fun submitLateCheckout(submission: LateCheckoutSubmission): LateCheckoutDecision =
                    LateCheckoutDecision(
                        requestId = "late_1",
                        status = LateCheckoutStatus.PENDING,
                    )

                override suspend fun submitServiceRequest(request: ServiceRequestDraft): ServiceRequestReceipt {
                    error("Unused in this test")
                }
            },
        )

        val result = runImmediate { useCase(dummySubmission()) }

        assertEquals(LateCheckoutStatus.PENDING, result.status)
    }

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

    private fun dummySubmission(): LateCheckoutSubmission =
        LateCheckoutSubmission(
            guest = GuestIdentity(hotelId = "rw-kgl-marriott", guestId = "guest_1"),
            selection = dev.orestegabo.kaze.domain.guest.LateCheckoutSelection(
                checkoutTimeIso = "2026-04-06T14:00:00Z",
                feeAmountMinor = 5500000,
                currencyCode = "RWF",
            ),
            paymentPreference = dev.orestegabo.kaze.domain.guest.PaymentPreference.CHARGE_TO_ROOM,
            followUpPreference = dev.orestegabo.kaze.domain.guest.FollowUpPreference.CONFIRM_IN_APP,
        )

    private fun <T> runImmediate(block: suspend () -> T): T {
        var outcome: Result<T>? = null
        block.startCoroutine(
            object : Continuation<T> {
                override val context = EmptyCoroutineContext

                override fun resumeWith(result: Result<T>) {
                    outcome = result
                }
            },
        )
        return outcome!!.getOrThrow()
    }
}
