package dev.orestegabo.kaze.ui.stay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Iron
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.LateCheckoutRequest
import dev.orestegabo.kaze.presentation.demo.ServiceOption
import dev.orestegabo.kaze.presentation.demo.ServiceRequestRecord
import dev.orestegabo.kaze.presentation.demo.StayPrimaryAction
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.InfoToken
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.components.SectionHeader
import dev.orestegabo.kaze.ui.components.SectionIntroCard

@Composable
internal fun ServiceRequestsTab(
    requestOptions: List<ServiceOption>,
    lateCheckoutRequest: LateCheckoutRequest?,
    submittedServiceRequests: List<ServiceRequestRecord>,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (lateCheckoutRequest != null) {
            LateCheckoutStatusCard(
                request = lateCheckoutRequest,
                onEdit = { onPrimaryAction(StayPrimaryAction.REQUEST_LATE_CHECKOUT) },
            )
        }
        if (submittedServiceRequests.isNotEmpty()) {
            SectionHeader("Recent requests")
            submittedServiceRequests.forEach { request ->
                ServiceRequestHistoryCard(request = request)
            }
        }
        SectionIntroCard(
            eyebrow = "Requests",
            title = "Hotel services",
            subtitle = "Ask for support without calling the desk. Requests stay visible and easy to track.",
            eyebrowColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            icon = Icons.Default.RoomService,
        )
        requestOptions.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowItems.forEach { option ->
                    ServiceOptionCard(
                        option = option,
                        hasExistingLateCheckout = lateCheckoutRequest != null,
                        onClick = {
                            if (option.title == "Late checkout") {
                                onPrimaryAction(StayPrimaryAction.REQUEST_LATE_CHECKOUT)
                            } else {
                                onPrimaryAction(StayPrimaryAction.RequestService(option))
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun ServiceOptionCard(
    option: ServiceOption,
    hasExistingLateCheckout: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = when (option.title) {
        "Fresh towels" -> KazeTheme.accents.editorialBotanical
        "Bottled water" -> MaterialTheme.colorScheme.primary
        "Extra pillows" -> MaterialTheme.colorScheme.secondary
        "Iron & board" -> KazeTheme.accents.editorialClay
        "Housekeeping touch-up" -> KazeTheme.accents.editorialBotanical
        "In-room dining" -> KazeTheme.accents.editorialWarm
        "Laundry pickup" -> KazeTheme.accents.editorialClay
        "Minibar refill" -> MaterialTheme.colorScheme.tertiary
        "Airport transfer" -> MaterialTheme.colorScheme.primary
        "Wake-up call" -> MaterialTheme.colorScheme.secondary
        "Concierge help" -> MaterialTheme.colorScheme.primary
        "Late checkout" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }
    val readableAccentColor = lerp(accentColor, MaterialTheme.colorScheme.onSurface, 0.28f)
    Card(
        modifier = modifier.height(248.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = 0.18f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(readableAccentColor.copy(alpha = 0.9f)),
                    )
                    Text(
                        option.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                option.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            KazeSecondaryButton(
                label = if (option.title == "Late checkout" && hasExistingLateCheckout) "Edit request" else "Request",
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = option.serviceIcon(),
                leadingIconTint = readableAccentColor,
            )
        }
    }
}

@Composable
internal fun ServiceRequestHistoryCard(request: ServiceRequestRecord) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = KazeTheme.ui.successContainerSoft,
                    ) {
                        Box(
                            modifier = Modifier.size(30.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = KazeTheme.ui.successContent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = request.option.serviceIcon(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                request.option.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            request.requestedAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                        )
                    }
                }
                MetaPill(request.status)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (request.option.title == "Fresh towels" && request.quantity > 0) {
                    InfoToken(
                        label = "Qty ${request.quantity}",
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        leadingIcon = Icons.Default.DryCleaning,
                    )
                }
            }
            if (request.locationNote.isNotBlank()) {
                Text(
                    "Location: ${request.locationNote}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                )
            }
            if (request.notes.isNotBlank()) {
                Text(
                    request.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
internal fun LateCheckoutStatusCard(
    request: LateCheckoutRequest,
    onEdit: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = KazeTheme.ui.successContainerSoft,
                    ) {
                        Box(
                            modifier = Modifier.size(30.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = KazeTheme.ui.successContent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Hotel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                "Late checkout",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            "${request.option.checkoutTimeLabel} • ${request.option.feeLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                        )
                    }
                }
                MetaPill(request.status)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfoToken(
                    label = request.paymentOption.label,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                )
                InfoToken(
                    label = request.followUpOption.label,
                    accentColor = MaterialTheme.colorScheme.primary,
                    leadingIcon = Icons.Default.ChatBubbleOutline,
                )
            }
            if (request.notes.isNotBlank()) {
                Text(
                    request.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            KazeSecondaryButton(label = "Edit request", onClick = onEdit)
        }
    }
}

internal fun ServiceOption.serviceIcon() = when (title) {
    "Fresh towels" -> Icons.Default.DryCleaning
    "Bottled water" -> Icons.Default.LocalDrink
    "Extra pillows" -> Icons.Default.Hotel
    "Iron & board" -> Icons.Default.Iron
    "Housekeeping touch-up" -> Icons.Default.CleaningServices
    "In-room dining" -> Icons.Default.RoomService
    "Laundry pickup" -> Icons.Default.LocalLaundryService
    "Minibar refill" -> Icons.Default.LocalDrink
    "Airport transfer" -> Icons.Default.Map
    "Wake-up call" -> Icons.Default.Schedule
    "Concierge help" -> Icons.Default.SupportAgent
    "Late checkout" -> Icons.Default.Hotel
    "Custom request" -> Icons.Default.Edit
    else -> Icons.Default.ChatBubbleOutline
}
