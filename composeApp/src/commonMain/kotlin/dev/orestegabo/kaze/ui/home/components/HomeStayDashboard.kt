package dev.orestegabo.kaze.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.domain.DigitalAccessCard
import dev.orestegabo.kaze.domain.PlaceService
import dev.orestegabo.kaze.domain.PlaceServiceCategory
import dev.orestegabo.kaze.domain.PlaceServicePricing
import dev.orestegabo.kaze.domain.ServicePlaceKind
import dev.orestegabo.kaze.presentation.demo.AccessContextUi
import dev.orestegabo.kaze.presentation.demo.ExploreHighlight
import dev.orestegabo.kaze.presentation.demo.ServiceOption
import dev.orestegabo.kaze.presentation.demo.StayMoment
import dev.orestegabo.kaze.presentation.demo.StayPrimaryAction
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.access.StayAccessCardSection
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.stay.StayMomentCard

@Composable
internal fun HomeStayDashboard(
    hotelDisplayName: String,
    guestName: String,
    accessProfileLabel: String,
    accessStatusLabel: String,
    accessCard: DigitalAccessCard?,
    accessContexts: List<AccessContextUi>,
    selectedAccessContextId: String?,
    stayMoments: List<StayMoment>,
    suggestionActivities: List<ExploreHighlight>,
    activeRequestCount: Int,
    onAccessContextSelected: (String) -> Unit,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    BoxWithConstraints {
        val isExpanded = maxWidth >= 900.dp
        val scheduleColumns = if (isExpanded) 2 else 1

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(34.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, KazeTheme.accents.editorialWarm.copy(alpha = 0.22f)),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.07f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.00f),
                            ),
                        ),
                    )
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AccessContextSwitcher(
                        contexts = accessContexts,
                        selectedContextId = selectedAccessContextId,
                        onContextSelected = onAccessContextSelected,
                    )

                    HomeGuestHeader(
                        hotelDisplayName = hotelDisplayName,
                        guestName = guestName,
                        accessProfileLabel = accessProfileLabel,
                        accessStatusLabel = accessStatusLabel,
                        activeRequestCount = activeRequestCount,
                    )

                    if (accessCard != null) {
                        if (isExpanded) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Box(modifier = Modifier.weight(1.08f)) {
                                    StayAccessCardSection(card = accessCard)
                                }
                                Box(modifier = Modifier.weight(0.92f)) {
                                    HomeReadyNowCard(onPrimaryAction = onPrimaryAction)
                                }
                            }
                        } else {
                            StayAccessCardSection(card = accessCard)
                            HomeReadyNowCard(onPrimaryAction = onPrimaryAction)
                        }
                    } else {
                        HomeReadyNowCard(onPrimaryAction = onPrimaryAction)
                    }

                    HomeQuickRequestRow(
                        services = accessContexts.firstOrNull { it.id == selectedAccessContextId }?.services.orEmpty(),
                        onPrimaryAction = onPrimaryAction,
                    )

                    if (stayMoments.isNotEmpty()) {
                        SectionLabel("Today at a glance")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            maxItemsInEachRow = scheduleColumns,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            stayMoments.take(if (isExpanded) 4 else 2).forEach { moment ->
                                StayMomentCard(
                                    moment = moment,
                                    onOpen = { onPrimaryAction(StayPrimaryAction.OpenStayMoment(moment)) },
                                    modifier = if (scheduleColumns == 1) Modifier.fillMaxWidth() else Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    HomeSuggestionStrip(
                        suggestions = suggestionActivities,
                        onPrimaryAction = onPrimaryAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeGuestHeader(
    hotelDisplayName: String,
    guestName: String,
    accessProfileLabel: String,
    accessStatusLabel: String,
    activeRequestCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                hotelDisplayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Welcome back, $guestName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Your pass, route, requests, and next moments are ready from here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill(accessProfileLabel, leadingIcon = Icons.Default.DoorFront)
                MetaPill(accessStatusLabel, leadingIcon = Icons.Default.Payments)
                if (activeRequestCount > 0) {
                    MetaPill("$activeRequestCount active request${if (activeRequestCount == 1) "" else "s"}", leadingIcon = Icons.Default.Schedule)
                }
            }
        }
        Surface(
            modifier = Modifier.padding(start = 12.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
        ) {
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun AccessContextSwitcher(
    contexts: List<AccessContextUi>,
    selectedContextId: String?,
    onContextSelected: (String) -> Unit,
) {
    if (contexts.size <= 1) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Your places today",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            contexts.forEach { context ->
                val selected = context.id == selectedContextId
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onContextSelected(context.id) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = when (context.place.kind) {
                                ServicePlaceKind.HOTEL -> Icons.Default.DoorFront
                                ServicePlaceKind.CONFERENCE_VENUE -> Icons.Default.CalendarMonth
                                ServicePlaceKind.WEDDING_VENUE -> Icons.Default.Place
                                else -> Icons.Default.Place
                            },
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            context.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeReadyNowCard(
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        "Quick actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Shortcuts for the things guests usually need during an active stay.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                }
                Surface(
                    modifier = Modifier.padding(start = 12.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            HomeQuickActionItem(
                title = "Open route",
                description = "Find your next venue, room, or event area.",
                icon = Icons.Default.Map,
                emphasized = true,
                onClick = { onPrimaryAction(StayPrimaryAction.OPEN_ROUTE) },
            )
            HomeQuickActionItem(
                title = "My charges",
                description = "Review the current folio before checkout.",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                onClick = { onPrimaryAction(StayPrimaryAction.VIEW_FOLIO) },
            )
            HomeQuickActionItem(
                title = "Late checkout",
                description = "Ask the property team for extra time.",
                icon = Icons.Default.CalendarMonth,
                onClick = { onPrimaryAction(StayPrimaryAction.REQUEST_LATE_CHECKOUT) },
            )
        }
    }
}

@Composable
private fun HomeQuickActionItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    emphasized: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (emphasized) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
        },
        border = BorderStroke(
            1.dp,
            if (emphasized) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (emphasized) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                },
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (emphasized) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                )
            }
        }
    }
}

@Composable
private fun HomeQuickRequestRow(
    services: List<PlaceService>,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val quickServices = services
        .filter { service ->
            service.requestable && service.category !in setOf(
                PlaceServiceCategory.ACCESS,
                PlaceServiceCategory.ROUTE,
                PlaceServiceCategory.FOLIO,
            )
        }
        .take(3)
    if (quickServices.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("Services at this place")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            quickServices.forEach { service ->
                HomeServiceShortcut(
                    service = service,
                    onClick = {
                        onPrimaryAction(
                            StayPrimaryAction.RequestService(
                                ServiceOption(
                                    title = service.title,
                                    description = service.description,
                                ),
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HomeServiceShortcut(
    service: PlaceService,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .widthIn(min = 180.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.13f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                imageVector = Icons.Default.DoorFront,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                service.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                service.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
            Text(
                service.pricing.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private val PlaceServicePricing.label: String
    get() = when (this) {
        PlaceServicePricing.Free -> "Free"
        PlaceServicePricing.Included -> "Included"
        is PlaceServicePricing.Paid -> amountLabel
        PlaceServicePricing.RequiresConfirmation -> "Confirm first"
    }

@Composable
private fun HomeSuggestionStrip(
    suggestions: List<ExploreHighlight>,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    if (suggestions.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("Suggested for you")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            suggestions.take(2).forEach { suggestion ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 220.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onPrimaryAction(StayPrimaryAction.OpenSuggestion(suggestion)) },
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.50f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Text(
                            suggestion.contextLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Text(
                            suggestion.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "${suggestion.location} • ${suggestion.time}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                        )
                    }
                }
            }
        }
    }
}
