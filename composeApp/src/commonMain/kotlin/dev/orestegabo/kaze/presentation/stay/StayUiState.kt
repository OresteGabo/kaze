package dev.orestegabo.kaze.presentation.stay

import dev.orestegabo.kaze.presentation.demo.LateCheckoutDraft
import dev.orestegabo.kaze.presentation.demo.LateCheckoutRequest
import dev.orestegabo.kaze.presentation.demo.AccessContextUi
import dev.orestegabo.kaze.presentation.demo.ServiceRequestDraftUi
import dev.orestegabo.kaze.presentation.demo.ServiceRequestRecord
import dev.orestegabo.kaze.presentation.demo.ServiceOption
import dev.orestegabo.kaze.presentation.demo.StayScreen
import dev.orestegabo.kaze.presentation.demo.StayMoment
import dev.orestegabo.kaze.presentation.demo.StayTab
import dev.orestegabo.kaze.presentation.demo.ExploreHighlight
import dev.orestegabo.kaze.domain.DigitalAccessCard

internal data class StayUiState(
    val hotelDisplayName: String = "",
    val guestName: String = "Aline",
    val accessProfileLabel: String = "Conference guest",
    val accessStatusLabel: String = "Active pass",
    val accessCard: DigitalAccessCard? = null,
    val accessContexts: List<AccessContextUi> = emptyList(),
    val selectedAccessContextId: String? = null,
    val stayMoments: List<StayMoment> = emptyList(),
    val requestOptions: List<ServiceOption> = emptyList(),
    val suggestionActivities: List<ExploreHighlight> = emptyList(),
    val selectedTab: StayTab = StayTab.MY_STAY,
    val activeStayScreen: StayScreen = StayScreen.HOME,
    val lateCheckoutRequest: LateCheckoutRequest? = null,
    val lateCheckoutDraft: LateCheckoutDraft = LateCheckoutDraft(),
    val serviceRequestDraft: ServiceRequestDraftUi = ServiceRequestDraftUi(),
    val submittedServiceRequests: List<ServiceRequestRecord> = emptyList(),
)

internal sealed interface StayActionResult {
    data class Feedback(val message: String) : StayActionResult
    data class NavigateToMap(val route: String, val floorId: String, val floorLabel: String) : StayActionResult
    data object NavigateToEvents : StayActionResult
}
