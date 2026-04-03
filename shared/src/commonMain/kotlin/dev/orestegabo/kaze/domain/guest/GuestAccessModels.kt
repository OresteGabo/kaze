package dev.orestegabo.kaze.domain.guest

data class GuestIdentity(
    val hotelId: String,
    val guestId: String,
    val stayId: String? = null,
    val roomId: String? = null,
)

data class LateCheckoutSelection(
    val checkoutTimeIso: String,
    val feeAmountMinor: Long,
    val currencyCode: String,
)

data class LateCheckoutSubmission(
    val guest: GuestIdentity,
    val selection: LateCheckoutSelection,
    val paymentPreference: PaymentPreference,
    val followUpPreference: FollowUpPreference,
    val notes: String? = null,
)

data class LateCheckoutDecision(
    val requestId: String,
    val status: LateCheckoutStatus,
    val approvedCheckoutTimeIso: String? = null,
    val feeAmountMinor: Long? = null,
    val currencyCode: String? = null,
    val note: String? = null,
)

data class ServiceRequestDraft(
    val guest: GuestIdentity,
    val type: ServiceRequestType,
    val note: String? = null,
    val requestedAtIso: String? = null,
)

data class ServiceRequestReceipt(
    val requestId: String,
    val type: ServiceRequestType,
    val status: ServiceRequestStatus,
    val note: String? = null,
)

enum class PaymentPreference {
    CHARGE_TO_ROOM,
    PAY_AT_RECEPTION,
    PAY_IN_ROOM,
}

enum class FollowUpPreference {
    CONFIRM_IN_APP,
    CALL_ROOM,
    VISIT_ROOM,
}

enum class LateCheckoutStatus {
    PENDING,
    APPROVED,
    DECLINED,
    CANCELLED,
}

enum class ServiceRequestType {
    TOWELS,
    DINING,
    LAUNDRY,
    CONCIERGE,
    HOUSEKEEPING,
}

enum class ServiceRequestStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    DECLINED,
}
