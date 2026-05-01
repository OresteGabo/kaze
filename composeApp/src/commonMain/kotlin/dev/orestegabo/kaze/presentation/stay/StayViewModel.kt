package dev.orestegabo.kaze.presentation.stay

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.orestegabo.kaze.data.repository.StayRepository
import dev.orestegabo.kaze.domain.guest.FollowUpPreference
import dev.orestegabo.kaze.domain.guest.GuestIdentity
import dev.orestegabo.kaze.domain.guest.LateCheckoutSelection
import dev.orestegabo.kaze.domain.guest.PaymentPreference
import dev.orestegabo.kaze.domain.guest.ServiceRequestDraft
import dev.orestegabo.kaze.domain.guest.ServiceRequestStatus
import dev.orestegabo.kaze.domain.guest.ServiceRequestType
import dev.orestegabo.kaze.presentation.demo.LateCheckoutDraft
import dev.orestegabo.kaze.presentation.demo.LateCheckoutRequest
import dev.orestegabo.kaze.presentation.demo.AccessContextUi
import dev.orestegabo.kaze.presentation.demo.RequestContactOption
import dev.orestegabo.kaze.presentation.demo.RequestWindowOption
import dev.orestegabo.kaze.presentation.demo.ServiceOption
import dev.orestegabo.kaze.presentation.demo.ServiceRequestDraftUi
import dev.orestegabo.kaze.presentation.demo.ServiceRequestRecord
import dev.orestegabo.kaze.presentation.demo.StayPrimaryAction
import dev.orestegabo.kaze.presentation.demo.StayScreen
import dev.orestegabo.kaze.presentation.demo.StayTab
import dev.orestegabo.kaze.presentation.demo.demoAccessContexts
import dev.orestegabo.kaze.presentation.demo.requestOptions
import dev.orestegabo.kaze.presentation.demo.sampleHotel
import dev.orestegabo.kaze.presentation.util.runImmediateSuspend
import dev.orestegabo.kaze.usecase.ObserveHotelContextUseCase
import dev.orestegabo.kaze.usecase.SubmitLateCheckoutUseCase

internal class StayViewModel(
    private val hotelId: String,
    private val guestIdentity: GuestIdentity?,
    private val observeHotelContext: ObserveHotelContextUseCase,
    private val stayRepository: StayRepository,
    private val submitLateCheckoutUseCase: SubmitLateCheckoutUseCase,
) : ViewModel() {
    private val initialAccessContext = demoAccessContexts.firstOrNull()
    private var currentGuestIdentity: GuestIdentity? = guestIdentity

    var uiState by mutableStateOf(
        StayUiState(
            hotelDisplayName = initialAccessContext?.title ?: sampleHotel.config.displayName,
            accessProfileLabel = initialAccessContext?.accessProfileLabel ?: "No active access yet",
            accessStatusLabel = initialAccessContext?.statusLabel ?: "Waiting for invites",
            accessCard = initialAccessContext?.accessCard,
            accessContexts = demoAccessContexts,
            selectedAccessContextId = initialAccessContext?.id,
            stayMoments = initialAccessContext?.moments.orEmpty(),
            requestOptions = initialAccessContext?.toServiceOptions().orEmpty(),
            suggestionActivities = initialAccessContext?.suggestions.orEmpty(),
            guestName = "Aline",
            assignedRoomLabel = currentGuestIdentity?.roomId?.let { "Room $it" }.orEmpty(),
        ),
    )
        private set

    fun applyActiveStay(
        guestIdentity: GuestIdentity?,
        hotelDisplayName: String?,
        guestName: String?,
    ) {
        currentGuestIdentity = guestIdentity
        uiState = uiState.copy(
            hotelDisplayName = hotelDisplayName?.takeIf { it.isNotBlank() } ?: uiState.hotelDisplayName,
            guestName = guestName?.takeIf { it.isNotBlank() } ?: uiState.guestName,
            assignedRoomLabel = guestIdentity?.roomId?.let { "Room $it" }.orEmpty(),
            accessStatusLabel = if (guestIdentity != null) "Active stay linked" else uiState.accessStatusLabel,
        )
    }

    fun applyPresentationContext(
        showSharedDemoAccess: Boolean,
        guestName: String,
    ) {
        val accessContexts = if (showSharedDemoAccess) demoAccessContexts else emptyList()
        val selectedAccessContext = accessContexts.firstOrNull()
        uiState = uiState.copy(
            hotelDisplayName = selectedAccessContext?.title ?: sampleHotel.config.displayName,
            accessProfileLabel = selectedAccessContext?.accessProfileLabel ?: "No linked access yet",
            accessStatusLabel = selectedAccessContext?.statusLabel ?: "Waiting for your real invitation or pass",
            accessCard = selectedAccessContext?.accessCard,
            accessContexts = accessContexts,
            selectedAccessContextId = selectedAccessContext?.id,
            stayMoments = selectedAccessContext?.moments.orEmpty(),
            requestOptions = selectedAccessContext?.toServiceOptions().orEmpty(),
            suggestionActivities = selectedAccessContext?.suggestions.orEmpty(),
            guestName = guestName,
            activeStayScreen = StayScreen.HOME,
        )
    }

    fun onTabChange(tab: StayTab) {
        uiState = uiState.copy(selectedTab = tab)
    }

    fun onAccessContextSelected(contextId: String) {
        val context = uiState.accessContexts.firstOrNull { it.id == contextId } ?: return
        uiState = uiState.copy(
            hotelDisplayName = context.title,
            accessProfileLabel = context.accessProfileLabel,
            accessStatusLabel = context.statusLabel,
            accessCard = context.accessCard,
            selectedAccessContextId = context.id,
            stayMoments = context.moments,
            requestOptions = context.toServiceOptions(),
            suggestionActivities = context.suggestions,
            activeStayScreen = StayScreen.HOME,
        )
    }

    fun onBackToHome() {
        uiState = uiState.copy(activeStayScreen = StayScreen.HOME)
    }

    fun onDraftChange(draft: LateCheckoutDraft) {
        uiState = uiState.copy(lateCheckoutDraft = draft)
    }

    fun onServiceRequestDraftChange(draft: ServiceRequestDraftUi) {
        uiState = uiState.copy(serviceRequestDraft = draft)
    }

    fun submitLateCheckout(draft: LateCheckoutDraft): StayActionResult.Feedback {
        val activeGuest = currentGuestIdentity ?: return StayActionResult.Feedback(
            "Your active stay is not linked yet. Sign in with a hotel stay before requesting late checkout.",
        )
        val decision = runImmediateSuspend {
            submitLateCheckoutUseCase(
                dev.orestegabo.kaze.domain.guest.LateCheckoutSubmission(
                    guest = activeGuest,
                    selection = LateCheckoutSelection(
                        checkoutTimeIso = draft.option.checkoutTimeLabel,
                        feeAmountMinor = draft.option.feeLabel.filter(Char::isDigit).toLongOrNull()?.times(100) ?: 0L,
                        currencyCode = sampleHotel.config.defaultCurrencyCode,
                    ),
                    paymentPreference = draft.paymentOption.toDomain(),
                    followUpPreference = draft.followUpOption.toDomain(),
                    notes = draft.notes.ifBlank { null },
                ),
            )
        }
        val request = LateCheckoutRequest(
            option = draft.option,
            paymentOption = draft.paymentOption,
            followUpOption = draft.followUpOption,
            notes = draft.notes.ifBlank { decision.note ?: "No additional notes." },
            status = "Pending front desk approval",
        )
        uiState = uiState.copy(
            lateCheckoutRequest = request,
            lateCheckoutDraft = LateCheckoutDraft(
                option = request.option,
                paymentOption = request.paymentOption,
                followUpOption = request.followUpOption,
                notes = request.notes,
            ),
            activeStayScreen = StayScreen.HOME,
        )
        return StayActionResult.Feedback(
            "Late checkout requested for ${request.option.checkoutTimeLabel}. ${request.paymentOption.confirmationLabel}.",
        )
    }

    fun submitServiceRequest(draft: ServiceRequestDraftUi): StayActionResult.Feedback {
        val activeGuest = currentGuestIdentity ?: return StayActionResult.Feedback(
            "Your active stay is not linked yet. Sign in with a hotel stay before sending room requests.",
        )
        val receipt = runImmediateSuspend {
            stayRepository.submitServiceRequest(
                ServiceRequestDraft(
                    guest = activeGuest,
                    type = draft.option.toDomainRequestType(),
                    note = buildString {
                        if (draft.option.isCustom && draft.customRequest.isNotBlank()) {
                            append(draft.customRequest.trim())
                        }
                        if (draft.notes.isNotBlank()) {
                            if (isNotEmpty()) append("\n")
                            append(draft.notes.trim())
                        }
                    }.ifBlank { null },
                ),
            )
        }

        val requestRecord = ServiceRequestRecord(
            id = receipt.requestId,
            option = draft.option,
            status = receipt.status.toDisplayLabel(),
            requestedAt = "Requested just now",
            window = draft.window,
            followUp = draft.followUp,
            quantity = draft.quantity,
            locationNote = draft.locationNote,
            notes = buildString {
                if (draft.option.isCustom && draft.customRequest.isNotBlank()) {
                    append(draft.customRequest.trim())
                }
                if (draft.notes.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append(draft.notes.trim())
                }
            },
        )

        uiState = uiState.copy(
            activeStayScreen = StayScreen.HOME,
            selectedTab = StayTab.REQUESTS,
            serviceRequestDraft = ServiceRequestDraftUi(option = draft.option),
            submittedServiceRequests = listOf(requestRecord) + uiState.submittedServiceRequests,
        )

        return StayActionResult.Feedback(
            "${draft.option.title} request sent. ${receipt.note ?: "The hotel will confirm it shortly."}",
        )
    }

    fun handleAction(action: StayPrimaryAction): StayActionResult? = when (action) {
        StayPrimaryAction.OPEN_ROUTE -> StayActionResult.NavigateToMap(
            route = "Guest arrival to Great Rift Ballroom",
            floorId = "l1",
            floorLabel = "Ground floor",
        )
        StayPrimaryAction.VIEW_FOLIO -> null
        StayPrimaryAction.SYNC_CALENDAR -> null
        StayPrimaryAction.SHARE_STAY -> null
        StayPrimaryAction.REQUEST_LATE_CHECKOUT -> {
            val draft = uiState.lateCheckoutRequest?.let {
                LateCheckoutDraft(
                    option = it.option,
                    paymentOption = it.paymentOption,
                    followUpOption = it.followUpOption,
                    notes = it.notes,
                )
            } ?: LateCheckoutDraft()
            uiState = uiState.copy(
                lateCheckoutDraft = draft,
                activeStayScreen = StayScreen.LATE_CHECKOUT,
            )
            null
        }
        StayPrimaryAction.SEE_CHECKOUT_POLICY -> null
        StayPrimaryAction.NEW_REQUEST -> null
        StayPrimaryAction.TRACK_REQUESTS -> null
        StayPrimaryAction.REFINE_SUGGESTIONS -> null
        StayPrimaryAction.SEE_FULL_AGENDA -> StayActionResult.NavigateToEvents
        is StayPrimaryAction.OpenStayMoment -> null
        is StayPrimaryAction.RequestService -> {
            uiState = uiState.copy(
                activeStayScreen = StayScreen.SERVICE_REQUEST,
                selectedTab = StayTab.REQUESTS,
                serviceRequestDraft = ServiceRequestDraftUi(option = action.option),
            )
            null
        }
        is StayPrimaryAction.OpenSuggestion -> {
            if (action.suggestion.cta == "Open route") {
                StayActionResult.NavigateToMap(
                    route = "Arrival route to ${action.suggestion.location}",
                    floorId = "l1",
                    floorLabel = action.suggestion.location,
                )
            } else {
                StayActionResult.Feedback("${action.suggestion.title} saved to your plan.")
            }
        }
    }

    private fun dev.orestegabo.kaze.presentation.demo.PaymentOption.toDomain(): PaymentPreference =
        when (this) {
            dev.orestegabo.kaze.presentation.demo.PaymentOption.CHARGE_TO_ROOM -> PaymentPreference.CHARGE_TO_ROOM
            dev.orestegabo.kaze.presentation.demo.PaymentOption.PAY_NOW_AT_RECEPTION -> PaymentPreference.PAY_AT_RECEPTION
            dev.orestegabo.kaze.presentation.demo.PaymentOption.PAY_IN_ROOM -> PaymentPreference.PAY_IN_ROOM
        }

    private fun dev.orestegabo.kaze.presentation.demo.FollowUpOption.toDomain(): FollowUpPreference =
        when (this) {
            dev.orestegabo.kaze.presentation.demo.FollowUpOption.CONFIRM_IN_APP -> FollowUpPreference.CONFIRM_IN_APP
            dev.orestegabo.kaze.presentation.demo.FollowUpOption.CALL_ROOM -> FollowUpPreference.CALL_ROOM
            dev.orestegabo.kaze.presentation.demo.FollowUpOption.COLLECT_PAYMENT_IN_ROOM -> FollowUpPreference.VISIT_ROOM
        }

    private fun ServiceOption.toDomainRequestType(): ServiceRequestType =
        when (title) {
            "Fresh towels" -> ServiceRequestType.TOWELS
            "In-room dining" -> ServiceRequestType.DINING
            "Laundry pickup" -> ServiceRequestType.LAUNDRY
            "Concierge help" -> ServiceRequestType.CONCIERGE
            "Airport transfer" -> ServiceRequestType.CONCIERGE
            "Wake-up call" -> ServiceRequestType.CONCIERGE
            else -> ServiceRequestType.HOUSEKEEPING
        }

    private fun ServiceRequestStatus.toDisplayLabel(): String =
        when (this) {
            ServiceRequestStatus.PENDING -> "Pending hotel confirmation"
            ServiceRequestStatus.ACCEPTED -> "Accepted by hotel team"
            ServiceRequestStatus.IN_PROGRESS -> "In progress"
            ServiceRequestStatus.COMPLETED -> "Completed"
            ServiceRequestStatus.DECLINED -> "Declined"
        }

    private fun AccessContextUi.toServiceOptions(): List<ServiceOption> {
        val options = services
            .filter { service -> service.requestable }
            .map { service ->
                ServiceOption(
                    title = service.title,
                    description = service.description,
                )
            }
        val customOption = requestOptions.first { it.isCustom }
        return options
            .ifEmpty { requestOptions }
            .let { scopedOptions ->
                if (scopedOptions.any { option -> option.isCustom }) {
                    scopedOptions
                } else {
                    scopedOptions + customOption
                }
            }
    }
}
