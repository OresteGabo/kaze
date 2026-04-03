package dev.orestegabo.kaze.presentation.demo.repository

import dev.orestegabo.kaze.data.repository.StayRepository
import dev.orestegabo.kaze.domain.Itinerary
import dev.orestegabo.kaze.domain.ItineraryItem
import dev.orestegabo.kaze.domain.ItineraryItemCategory
import dev.orestegabo.kaze.domain.ItineraryMode
import dev.orestegabo.kaze.domain.ItinerarySection
import dev.orestegabo.kaze.domain.ItineraryTab
import dev.orestegabo.kaze.domain.ReservationStatus
import dev.orestegabo.kaze.domain.TimeWindow
import dev.orestegabo.kaze.domain.VenueRef
import dev.orestegabo.kaze.domain.guest.FollowUpPreference
import dev.orestegabo.kaze.domain.guest.GuestIdentity
import dev.orestegabo.kaze.domain.guest.LateCheckoutDecision
import dev.orestegabo.kaze.domain.guest.LateCheckoutStatus
import dev.orestegabo.kaze.domain.guest.LateCheckoutSubmission
import dev.orestegabo.kaze.domain.guest.ServiceRequestDraft
import dev.orestegabo.kaze.domain.guest.ServiceRequestReceipt
import dev.orestegabo.kaze.domain.guest.ServiceRequestStatus

internal class DemoStayRepository : StayRepository {
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

    override suspend fun submitLateCheckout(submission: LateCheckoutSubmission): LateCheckoutDecision =
        LateCheckoutDecision(
            requestId = "late_${submission.guest.guestId}",
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

    override suspend fun submitServiceRequest(request: ServiceRequestDraft): ServiceRequestReceipt =
        ServiceRequestReceipt(
            requestId = "service_${request.type.name.lowercase()}_${request.guest.guestId}",
            type = request.type,
            status = ServiceRequestStatus.PENDING,
            note = "The hotel team has received the request.",
        )
}
