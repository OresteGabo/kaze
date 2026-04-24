package dev.orestegabo.kaze.data.repository

import dev.orestegabo.kaze.domain.Itinerary
import dev.orestegabo.kaze.domain.guest.GuestIdentity
import dev.orestegabo.kaze.domain.guest.LateCheckoutDecision
import dev.orestegabo.kaze.domain.guest.LateCheckoutSubmission
import dev.orestegabo.kaze.domain.guest.ServiceRequestDraft
import dev.orestegabo.kaze.domain.guest.ServiceRequestReceipt

interface StayRepository {
    suspend fun getStayItinerary(guest: GuestIdentity): Itinerary?
    suspend fun submitLateCheckout(submission: LateCheckoutSubmission): LateCheckoutDecision
    suspend fun submitServiceRequest(request: ServiceRequestDraft): ServiceRequestReceipt
    suspend fun getLateCheckoutHistory(guest: GuestIdentity): List<LateCheckoutDecision>
    suspend fun getServiceRequestHistory(guest: GuestIdentity): List<ServiceRequestReceipt>
}
