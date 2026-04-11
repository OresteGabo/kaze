package dev.orestegabo.kaze.ui.stay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.FollowUpOption
import dev.orestegabo.kaze.presentation.demo.LateCheckoutDraft
import dev.orestegabo.kaze.presentation.demo.LateCheckoutRequest
import dev.orestegabo.kaze.presentation.demo.ServiceRequestDraftUi
import dev.orestegabo.kaze.presentation.demo.followUpOptions
import dev.orestegabo.kaze.presentation.demo.lateCheckoutOptions
import dev.orestegabo.kaze.presentation.demo.paymentOptions
import dev.orestegabo.kaze.ui.components.HighlightPanel
import dev.orestegabo.kaze.ui.components.InfoToken
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.components.SectionHeader
import dev.orestegabo.kaze.ui.components.SectionIntroCard
import dev.orestegabo.kaze.ui.components.SelectableInfoCard

@Composable
internal fun LateCheckoutScreen(
    modifier: Modifier = Modifier,
    draft: LateCheckoutDraft,
    existingRequest: LateCheckoutRequest?,
    onBack: () -> Unit,
    onDraftChange: (LateCheckoutDraft) -> Unit,
    onSubmit: () -> Unit,
    bottomContentPadding: Dp = 20.dp,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KazeGhostButton(label = "Back", onClick = onBack)
                if (existingRequest != null) MetaPill(existingRequest.status)
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Request late checkout", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Standard checkout is April 6, 2026 at 10:00.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    )
                    Text(
                        "Availability depends on occupancy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                    )
                }
            }
        }
        item { SectionHeader("Choose your new checkout time") }
        items(lateCheckoutOptions) { option ->
            SelectableInfoCard(
                selected = draft.option == option,
                title = option.checkoutTimeLabel,
                subtitle = "${option.feeLabel} • ${option.availabilityLabel}",
                supporting = option.summary,
                onSelect = { onDraftChange(draft.copy(option = option)) },
            )
        }
        item { SectionHeader("Choose payment preference") }
        items(paymentOptions.toList()) { payment ->
            SelectableInfoCard(
                selected = draft.paymentOption == payment,
                title = payment.label,
                subtitle = payment.confirmationLabel,
                supporting = payment.description,
                onSelect = { onDraftChange(draft.copy(paymentOption = payment)) },
            )
        }
        item { SectionHeader("How should reception follow up?") }
        items(followUpOptions.toList()) { followUp ->
            SelectableInfoCard(
                selected = draft.followUpOption == followUp,
                title = followUp.label,
                subtitle = followUp.description,
                supporting = if (followUp == FollowUpOption.COLLECT_PAYMENT_IN_ROOM) {
                    "Useful if the hotel offers discreet in-room payment collection with a receptionist or duty manager."
                } else {
                    "Keeps the guest informed without a front-desk phone call unless needed."
                },
                onSelect = { onDraftChange(draft.copy(followUpOption = followUp)) },
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Notes for reception", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = draft.notes,
                        onValueChange = { onDraftChange(draft.copy(notes = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        placeholder = { Text("Flight departs late, please advise if payment can be collected in-room.") },
                        shape = RoundedCornerShape(18.dp),
                    )
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Request summary",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            draft.option.checkoutTimeLabel,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            draft.option.feeLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SummaryRow(label = "Payment", value = draft.paymentOption.label)
                            SummaryRow(label = "Follow-up", value = draft.followUpOption.label)
                        }
                    }
                    if (draft.notes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    draft.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        }
                    }
                    Text(
                        "Reception will confirm availability and the final charge.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                    )
                }
            }
        }
        item {
            KazePrimaryButton(
                label = "Submit late checkout request",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Hotel,
            )
        }
    }
}

@Composable
internal fun SummaryRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun ServiceRequestScreen(
    modifier: Modifier = Modifier,
    draft: ServiceRequestDraftUi,
    onBack: () -> Unit,
    onDraftChange: (ServiceRequestDraftUi) -> Unit,
    onSubmit: () -> Unit,
    bottomContentPadding: Dp = 20.dp,
) {
    val isTowelRequest = draft.option.title == "Fresh towels"
    val isCustomRequest = draft.option.isCustom
    val isLaundryRequest = draft.option.title == "Laundry pickup"
    val isInRoomDiningRequest = draft.option.title == "In-room dining"
    val isConciergeRequest = draft.option.title == "Concierge help"
    val usesAssignedRoomContext = !isCustomRequest
    val needsExplicitLocation = false
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KazeGhostButton(label = "Back", onClick = onBack)
                MetaPill("New request")
            }
        }
        item {
            SectionIntroCard(
                eyebrow = "Service Request",
                title = draft.option.title,
                subtitle = when {
                    isTowelRequest -> "Choose how many towels you need."
                    isCustomRequest -> "Tell the hotel what you need."
                    isLaundryRequest -> "Add pickup details and send the request."
                    isConciergeRequest -> "Describe the help you need."
                    else -> "Add any details and send the request."
                },
                icon = when {
                    isTowelRequest -> Icons.Default.DryCleaning
                    isConciergeRequest -> Icons.Default.SupportAgent
                    isLaundryRequest -> Icons.Default.LocalLaundryService
                    isInRoomDiningRequest -> Icons.Default.RoomService
                    else -> draft.option.serviceIcon()
                },
            )
        }
        item {
            if (!isTowelRequest && !isCustomRequest) {
                HighlightPanel(
                    title = "Service request",
                    body = "Choose the details below, then send the request to the hotel.",
                    primaryLabel = "Send request",
                    secondaryLabel = "Back to requests",
                    onPrimaryClick = onSubmit,
                    onSecondaryClick = onBack,
                )
            }
        }
        item {
            SectionHeader(
                if (isTowelRequest) "How many towels do you need?"
                else if (isCustomRequest) "What do you need?"
                else "Request details",
            )
        }
        if (isTowelRequest) {
            item {
                ServiceRequestQuantityCard(
                    quantity = draft.quantity,
                    itemLabel = "towels",
                    onDecrease = { onDraftChange(draft.copy(quantity = (draft.quantity - 1).coerceAtLeast(1))) },
                    onIncrease = { onDraftChange(draft.copy(quantity = draft.quantity + 1)) },
                )
            }
        }
        if (isCustomRequest) {
            item {
                LuxuryNoteField(
                    value = draft.customRequest,
                    onValueChange = { onDraftChange(draft.copy(customRequest = it)) },
                    label = "Request",
                    placeholder = "Extra pillows, baby crib, adapter, room setup...",
                    minLines = 3,
                    icon = Icons.Default.Edit,
                )
            }
        }
        if (usesAssignedRoomContext) {
            item {
                // TODO replace the demo room label with the guest's actual assigned room from live stay data.
                RequestContextLabel(label = "Room", value = "Room 906")
            }
        }
        if (needsExplicitLocation) {
            item {
                LuxuryNoteField(
                    value = draft.locationNote,
                    onValueChange = { onDraftChange(draft.copy(locationNote = it)) },
                    label = if (isLaundryRequest) "Pickup location" else "Location",
                    placeholder = if (isLaundryRequest) {
                        "Room 906, outside the door, with the concierge..."
                    } else {
                        "Room 906, pool deck cabana, lobby seating area..."
                    },
                    minLines = 2,
                    icon = Icons.Default.Place,
                )
            }
        }
        item {
            LuxuryNoteField(
                value = draft.notes,
                onValueChange = { onDraftChange(draft.copy(notes = it)) },
                label = if (isTowelRequest || isCustomRequest) "Add a note" else "Notes",
                placeholder = when {
                    isTowelRequest -> "Leave towels at the door, extra bath towels, after spa..."
                    isCustomRequest -> "Add timing, room, or access details..."
                    else -> "Hypoallergenic towels, call before entering, deliver after session..."
                },
                minLines = if (isTowelRequest || isCustomRequest) 3 else 4,
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Request summary", style = MaterialTheme.typography.titleMedium)
                    Text(
                        draft.option.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (isCustomRequest && draft.customRequest.isNotBlank()) {
                        Text(
                            draft.customRequest,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.84f),
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (!isCustomRequest) {
                            InfoToken(
                                label = if (isTowelRequest) "${draft.quantity} towels" else draft.option.title,
                                accentColor = MaterialTheme.colorScheme.tertiary,
                                leadingIcon = draft.option.serviceIcon(),
                            )
                        }
                    }
                    if (usesAssignedRoomContext) {
                        Text(
                            "Room 906",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        )
                    } else if (needsExplicitLocation && draft.locationNote.isNotBlank()) {
                        Text(
                            "Location: ${draft.locationNote}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        )
                    }
                    if (draft.notes.isNotBlank()) {
                        Text(
                            draft.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f),
                        )
                    }
                }
            }
        }
        item {
            KazePrimaryButton(
                label = "Submit request",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = draft.option.serviceIcon(),
            )
        }
    }
}

@Composable
internal fun ServiceRequestQuantityCard(
    quantity: Int,
    itemLabel: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Quantity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuantityStepperButton(label = "−", onClick = onDecrease, edge = StepperEdge.START)
                    Text(
                        text = "$quantity",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    QuantityStepperButton(label = "+", onClick = onIncrease, edge = StepperEdge.END)
                }
            }
        }
    }
}

@Composable
internal fun RequestContextLabel(
    label: String,
    value: String,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
internal fun QuantityStepperButton(
    label: String,
    onClick: () -> Unit,
    edge: StepperEdge,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = when (edge) {
            StepperEdge.START -> RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
            StepperEdge.END -> RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
        },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
    ) {
        Box(
            modifier = Modifier.size(width = 44.dp, height = 42.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

internal enum class StepperEdge {
    START,
    END,
}

@Composable
internal fun LuxuryNoteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    minLines: Int,
    icon: ImageVector = Icons.Default.ChatBubbleOutline,
) {
    val focusManager = LocalFocusManager.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ) {
                    Box(
                        modifier = Modifier.size(30.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Optional",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = minLines,
                    placeholder = { Text(placeholder) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(20.dp),
                )
            }
        }
    }
}
