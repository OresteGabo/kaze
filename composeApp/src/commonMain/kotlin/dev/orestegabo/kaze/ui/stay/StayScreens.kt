package dev.orestegabo.kaze.ui.stay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.domain.DigitalAccessCard
import dev.orestegabo.kaze.presentation.demo.ExploreHighlight
import dev.orestegabo.kaze.presentation.demo.LateCheckoutDraft
import dev.orestegabo.kaze.presentation.demo.LateCheckoutRequest
import dev.orestegabo.kaze.presentation.demo.ServiceOption
import dev.orestegabo.kaze.presentation.demo.ServiceRequestDraftUi
import dev.orestegabo.kaze.presentation.demo.ServiceRequestRecord
import dev.orestegabo.kaze.presentation.demo.StayMoment
import dev.orestegabo.kaze.presentation.demo.StayPrimaryAction
import dev.orestegabo.kaze.presentation.demo.StayScreen
import dev.orestegabo.kaze.presentation.demo.StayTab

@Composable
internal fun StayHomeScreen(
    modifier: Modifier = Modifier,
    hotelDisplayName: String,
    guestName: String,
    accessProfileLabel: String,
    accessStatusLabel: String,
    accessCard: DigitalAccessCard?,
    stayMoments: List<StayMoment>,
    requestOptions: List<ServiceOption>,
    suggestionActivities: List<ExploreHighlight>,
    selectedTab: StayTab,
    activeStayScreen: StayScreen,
    lateCheckoutRequest: LateCheckoutRequest?,
    lateCheckoutDraft: LateCheckoutDraft,
    serviceRequestDraft: ServiceRequestDraftUi,
    submittedServiceRequests: List<ServiceRequestRecord>,
    onTabChange: (StayTab) -> Unit,
    onBackToStayHome: () -> Unit,
    onLateCheckoutDraftChange: (LateCheckoutDraft) -> Unit,
    onLateCheckoutSubmit: (LateCheckoutDraft) -> Unit,
    onServiceRequestDraftChange: (ServiceRequestDraftUi) -> Unit,
    onServiceRequestSubmit: (ServiceRequestDraftUi) -> Unit,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
    bottomContentPadding: Dp = 20.dp,
) {
    if (activeStayScreen == StayScreen.LATE_CHECKOUT) {
        LateCheckoutScreen(
            modifier = modifier,
            draft = lateCheckoutDraft,
            existingRequest = lateCheckoutRequest,
            onBack = onBackToStayHome,
            onDraftChange = onLateCheckoutDraftChange,
            onSubmit = { onLateCheckoutSubmit(lateCheckoutDraft) },
            bottomContentPadding = bottomContentPadding,
        )
        return
    }

    if (activeStayScreen == StayScreen.SERVICE_REQUEST) {
        ServiceRequestScreen(
            modifier = modifier,
            draft = serviceRequestDraft,
            onBack = onBackToStayHome,
            onDraftChange = onServiceRequestDraftChange,
            onSubmit = { onServiceRequestSubmit(serviceRequestDraft) },
            bottomContentPadding = bottomContentPadding,
        )
        return
    }

    val activeRequestCount = submittedServiceRequests.size + if (lateCheckoutRequest != null) 1 else 0

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        val pagerState = rememberPagerState(
            initialPage = selectedTab.ordinal,
            pageCount = { StayTab.entries.size },
        )
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {

            LaunchedEffect(selectedTab) {
                if (pagerState.currentPage != selectedTab.ordinal) {
                    pagerState.animateScrollToPage(selectedTab.ordinal)
                }
            }
            LaunchedEffect(pagerState.currentPage) {
                val pagerTab = StayTab.entries[pagerState.currentPage]
                if (pagerTab != selectedTab) onTabChange(pagerTab)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
            ) {
                CompactStayHeader(
                    hotelName = hotelDisplayName,
                    guestName = guestName,
                    roomLabel = accessProfileLabel,
                    stayLabel = accessStatusLabel,
                )
            }
            Spacer(Modifier.height(8.dp))
            StaySegmentedTabs(
                selectedTab = selectedTab,
                onTabChange = onTabChange,
                requestsBadgeCount = activeRequestCount,
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    when (StayTab.entries[page]) {
                        StayTab.MY_STAY -> StayTabContent(
                            accessCard = accessCard,
                            stayMoments = stayMoments,
                            onPrimaryAction = onPrimaryAction,
                            bottomContentPadding = bottomContentPadding,
                        )
                        StayTab.REQUESTS -> ServiceRequestsTab(
                            requestOptions = requestOptions,
                            lateCheckoutRequest = lateCheckoutRequest,
                            submittedServiceRequests = submittedServiceRequests,
                            onPrimaryAction = onPrimaryAction,
                            bottomContentPadding = bottomContentPadding,
                        )
                        StayTab.SUGGESTIONS -> SuggestedActivitiesTab(
                            suggestionActivities = suggestionActivities,
                            onPrimaryAction = onPrimaryAction,
                            bottomContentPadding = bottomContentPadding,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaySegmentedTabs(
    selectedTab: StayTab,
    onTabChange: (StayTab) -> Unit,
    requestsBadgeCount: Int = 0,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        tonalElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StayTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val itemShape = RoundedCornerShape(18.dp)
                Column(
                    modifier = Modifier
                        .clip(itemShape)
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                    ),
                                )
                            },
                        )
                        .clickable { onTabChange(tab) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(if (selected) 28.dp else 18.dp)
                            .height(if (selected) 4.dp else 3.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.74f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                            ),
                    )
                    Text(
                        tab.label,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f)
                        },
                    )
                    if (tab == StayTab.REQUESTS && requestsBadgeCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = if (selected) 0.92f else 0.18f),
                        ) {
                            Text(
                                text = requestsBadgeCount.coerceAtMost(9).toString(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onSecondary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
