package dev.orestegabo.kaze.usecase

import dev.orestegabo.kaze.data.repository.StayRepository
import dev.orestegabo.kaze.domain.guest.LateCheckoutDecision
import dev.orestegabo.kaze.domain.guest.LateCheckoutSubmission

class SubmitLateCheckoutUseCase(
    private val stayRepository: StayRepository,
) {
    suspend operator fun invoke(submission: LateCheckoutSubmission): LateCheckoutDecision =
        stayRepository.submitLateCheckout(submission)
}
