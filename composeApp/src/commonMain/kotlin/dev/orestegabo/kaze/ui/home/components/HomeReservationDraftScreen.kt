package dev.orestegabo.kaze.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.auth.ReservationDraftSubmissionRequest
import dev.orestegabo.kaze.presentation.auth.ReservationResponse
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.launch

@Composable
internal fun HomeReservationDraftScreen(
    content: HomeServicePageContent,
    result: HomeServiceResult,
    bottomContentPadding: Dp,
    onBack: () -> Unit,
    onSubmitReservation: suspend (ReservationDraftSubmissionRequest) -> ReservationResponse,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var eventName by rememberSaveable(result.title) { mutableStateOf("") }
    var preferredDate by rememberSaveable(result.title) { mutableStateOf("") }
    val roomOptions = remember(content.title, result.title) { defaultReservationRoomOptions(content, result) }
    var selectedRoom by rememberSaveable(result.title) {
        mutableStateOf(roomOptions.firstOrNull { it.title == result.title }?.title ?: roomOptions.firstOrNull()?.title.orEmpty())
    }
    var guests by rememberSaveable(result.title) { mutableIntStateOf(defaultGuestCount(content.title)) }
    var selectedPackage by rememberSaveable(result.title) { mutableStateOf(defaultReservationPackages(content.title).first().label) }
    var paymentMethod by rememberSaveable(result.title) { mutableStateOf(defaultPaymentMethods().first()) }
    var note by rememberSaveable(result.title) { mutableStateOf("") }
    var isSaved by rememberSaveable(result.title) { mutableStateOf(false) }
    var isSaving by rememberSaveable(result.title) { mutableStateOf(false) }
    var saveError by rememberSaveable(result.title) { mutableStateOf("") }
    var savedReservationCode by rememberSaveable(result.title) { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KazeGhostButton(
            label = "Back to details",
            onClick = onBack,
            leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )

        ReservationDraftHeroCard(content = content, result = result, isSaved = isSaved)

        if (isSaved) {
            ReservationSavedCard(
                content = content,
                result = result,
                eventName = eventName,
                preferredDate = preferredDate,
                guests = guests,
                selectedRoom = selectedRoom.takeIf { roomOptions.isNotEmpty() },
                selectedPackage = selectedPackage,
                paymentMethod = paymentMethod,
                note = note,
                reservationCode = savedReservationCode,
                onBack = onBack,
            )
            return@Column
        }

        SectionLabel("Reservation details")
        ReservationDraftField(
            value = eventName,
            onValueChange = { eventName = it },
            label = "Event name",
            placeholder = "e.g. Claire & Yves reception",
            accent = content.accent,
            singleLine = true,
        )
        ReservationDraftField(
            value = preferredDate,
            onValueChange = { preferredDate = it },
            label = "Preferred date",
            placeholder = "e.g. 24 Aug 2026",
            accent = content.accent,
            singleLine = true,
            leadingIcon = Icons.Default.CalendarMonth,
        )

        if (roomOptions.isNotEmpty()) {
            SectionLabel("Room")
            ConferenceRoomSelectionGrid(
                rooms = roomOptions,
                selectedRoom = selectedRoom,
                background = content.background,
                accent = content.accent,
                onRoomSelected = { selectedRoom = it },
            )
        }

        ReservationGuestStepper(
            guests = guests,
            accent = content.accent,
            onDecrease = { if (guests > 1) guests -= 1 },
            onIncrease = { guests += 1 },
        )

        SectionLabel("Package")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            defaultReservationPackages(content.title).forEach { option ->
                ReservationChoiceCard(
                    title = option.label,
                    subtitle = option.detail,
                    selected = selectedPackage == option.label,
                    accent = content.accent,
                    onClick = { selectedPackage = option.label },
                )
            }
        }

        SectionLabel("Note")
        ReservationDraftField(
            value = note,
            onValueChange = { note = it },
            label = "Extra note",
            placeholder = "Tell the venue anything important before they confirm.",
            accent = content.accent,
            minLines = 3,
            leadingIcon = Icons.AutoMirrored.Filled.StickyNote2,
        )

        ReservationSummaryCard(
            content = content,
            result = result,
            eventName = eventName,
            preferredDate = preferredDate,
            guests = guests,
            selectedRoom = selectedRoom.takeIf { roomOptions.isNotEmpty() },
            selectedPackage = selectedPackage,
            paymentMethod = paymentMethod,
        )

        KazePrimaryButton(
            label = if (isSaving) "Saving..." else "Save reservation request",
            onClick = {
                if (isSaving) return@KazePrimaryButton
                val normalizedEventName = eventName.trim().ifBlank { result.title }
                val normalizedPreferredDate = preferredDate.trim()
                if (normalizedPreferredDate.isBlank()) {
                    saveError = "Add a preferred date before saving."
                    return@KazePrimaryButton
                }
                val serverIds = result.reservationServerIds(content)
                isSaving = true
                saveError = ""
                coroutineScope.launch {
                    runCatching {
                        onSubmitReservation(
                            ReservationDraftSubmissionRequest(
                                placeId = serverIds.placeId,
                                serviceId = serverIds.serviceId,
                                eventName = normalizedEventName,
                                preferredDateLabel = normalizedPreferredDate,
                                selectedRoom = selectedRoom.takeIf { roomOptions.isNotEmpty() },
                                guestCount = guests,
                                packageLabel = selectedPackage,
                                addOns = emptyList(),
                                paymentMethod = paymentMethod,
                                note = note.trim().takeIf { it.isNotBlank() },
                            ),
                        )
                    }.onSuccess { response ->
                        savedReservationCode = response.reservationCode
                        isSaved = true
                    }.onFailure { throwable ->
                        saveError = throwable.message ?: "Could not save this reservation right now."
                    }
                    isSaving = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Default.CheckCircle,
        )
        if (saveError.isNotBlank()) {
            Text(
                saveError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ReservationDraftHeroCard(
    content: HomeServicePageContent,
    result: HomeServiceResult,
    isSaved: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, content.accent.copy(alpha = 0.22f)),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(122.dp)
                    .background(content.accent.copy(alpha = 0.16f)),
            ) {
                Image(
                    painter = painterResource(content.background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                ) {
                    MetaPill(
                        label = if (isSaved) "Saved" else "Draft",
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        textColor = MaterialTheme.colorScheme.onSurface,
                        leadingIcon = Icons.Default.CheckCircle,
                    )
                }
            }
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Reservation request",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = content.accent,
                )
                Text(
                    result.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    result.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetaPill(result.metaLabel)
                    MetaPill(
                        label = result.priceLabel,
                        containerColor = content.accent.copy(alpha = 0.16f),
                        textColor = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReservationGuestStepper(
    guests: Int,
    accent: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = accent.copy(alpha = 0.14f)) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.padding(9.dp).size(18.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Guests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Approximate number",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReservationRoundIconButton(icon = Icons.Default.Remove, accent = accent, onClick = onDecrease)
                Text(
                    guests.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ReservationRoundIconButton(icon = Icons.Default.Add, accent = accent, onClick = onIncrease)
            }
        }
    }
}

@Composable
private fun ReservationRoundIconButton(
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.padding(9.dp).size(18.dp),
        )
    }
}

@Composable
private fun ReservationChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    leadingIcon: ImageVector = Icons.Default.CheckCircle,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, if (selected) accent.copy(alpha = 0.42f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        tonalElevation = if (selected) 3.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = accent.copy(alpha = if (selected) 0.20f else 0.10f)) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(9.dp).size(18.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ConferenceRoomSelectionGrid(
    rooms: List<ReservationRoomOption>,
    selectedRoom: String,
    background: DrawableResource,
    accent: Color,
    onRoomSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rooms.chunked(2).forEach { rowRooms ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowRooms.forEach { room ->
                    ConferenceRoomSelectionCard(
                        room = room,
                        selected = selectedRoom == room.title,
                        background = background,
                        accent = accent,
                        onClick = { onRoomSelected(room.title) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowRooms.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ConferenceRoomSelectionCard(
    room: ReservationRoomOption,
    selected: Boolean,
    background: DrawableResource,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, if (selected) accent.copy(alpha = 0.44f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        tonalElevation = if (selected) 4.dp else 1.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.55f)
                    .background(accent.copy(alpha = 0.12f)),
            ) {
                Image(
                    painter = painterResource(background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f)),
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                ) {
                    Icon(
                        imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = if (selected) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        modifier = Modifier.padding(7.dp).size(17.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    room.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    room.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                )
                MetaPill(
                    label = room.metaLabel,
                    containerColor = accent.copy(alpha = if (selected) 0.18f else 0.10f),
                    textColor = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    room.priceLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }
        }
    }
}

@Composable
private fun ReservationChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
        border = BorderStroke(1.dp, if (selected) accent.copy(alpha = 0.38f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ReservationDraftField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    accent: Color,
    singleLine: Boolean = false,
    minLines: Int = 1,
    leadingIcon: ImageVector = Icons.Default.CheckCircle,
) {
    val focusManager = LocalFocusManager.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        tonalElevation = 2.dp,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = accent,
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun ReservationSummaryCard(
    content: HomeServicePageContent,
    result: HomeServiceResult,
    eventName: String,
    preferredDate: String,
    guests: Int,
    selectedRoom: String?,
    selectedPackage: String,
    paymentMethod: String,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = content.accent.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, content.accent.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                result.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill(if (eventName.isBlank()) "Event name later" else eventName)
                MetaPill(if (preferredDate.isBlank()) "Date later" else preferredDate)
                if (!selectedRoom.isNullOrBlank()) {
                    MetaPill(selectedRoom)
                }
                MetaPill("$guests guests")
                MetaPill(selectedPackage)
                MetaPill(paymentMethod)
            }
        }
    }
}

@Composable
private fun ReservationSavedCard(
    content: HomeServicePageContent,
    result: HomeServiceResult,
    eventName: String,
    preferredDate: String,
    guests: Int,
    selectedRoom: String?,
    selectedPackage: String,
    paymentMethod: String,
    note: String,
    reservationCode: String,
    onBack: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, content.accent.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = content.accent.copy(alpha = 0.16f)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = content.accent,
                        modifier = Modifier.padding(10.dp).size(22.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "Reservation request saved",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        if (reservationCode.isNotBlank()) "${result.title} • $reservationCode" else result.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                }
            }
            ReservationSummaryCard(
                content = content,
                result = result,
                eventName = eventName,
                preferredDate = preferredDate,
                guests = guests,
                selectedRoom = selectedRoom,
                selectedPackage = selectedPackage,
                paymentMethod = paymentMethod,
            )
            if (note.isNotBlank()) {
                Text(
                    note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                )
            }
            KazePrimaryButton(
                label = "Done",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.CheckCircle,
            )
        }
    }
}

private data class ReservationPackageOption(
    val label: String,
    val detail: String,
)

private data class ReservationRoomOption(
    val title: String,
    val detail: String,
    val metaLabel: String,
    val priceLabel: String,
)

private fun defaultReservationRoomOptions(
    content: HomeServicePageContent,
    result: HomeServiceResult,
): List<ReservationRoomOption> {
    if (content.title != "Conference rooms") return emptyList()

    val catalogRooms = content.results.map { serviceResult ->
        ReservationRoomOption(
            title = serviceResult.title,
            detail = serviceResult.subtitle,
            metaLabel = serviceResult.metaLabel,
            priceLabel = serviceResult.priceLabel,
        )
    }
    val requestedRoom = ReservationRoomOption(
        title = result.title,
        detail = result.subtitle,
        metaLabel = result.metaLabel,
        priceLabel = result.priceLabel,
    )
    return (listOf(requestedRoom) + catalogRooms)
        .distinctBy { it.title }
        .ifEmpty {
            listOf(
                ReservationRoomOption(
                    title = "Main conference room",
                    detail = "Default room choice until this institution publishes its room catalog.",
                    metaLabel = "Room • Setup",
                    priceLabel = "Availability on request",
                ),
            )
        }
}

private fun defaultReservationPackages(categoryTitle: String): List<ReservationPackageOption> {
    // TODO [IN-PROGRESS] Reservation submission is backend-backed; load packages, availability windows,
    // venue rules, and price breakdowns from a real reservation catalog API instead of UI defaults.
    return when (categoryTitle) {
        "Wedding venues" -> listOf(
            ReservationPackageOption("Venue only", "Reserve the space and coordinate services later."),
            ReservationPackageOption("Venue + guest access", "Reserve the space and prepare invitation access."),
            ReservationPackageOption("Full event starter", "Venue, guest access, and key planning add-ons."),
        )
        "Conference rooms" -> listOf(
            ReservationPackageOption("Half day", "Morning or afternoon reservation."),
            ReservationPackageOption("Full day", "One full business day with room setup."),
            ReservationPackageOption("Workshop pack", "Room, guest access, and catering preparation."),
        )
        "Apartments" -> listOf(
            ReservationPackageOption("One night", "Short stay request for one night."),
            ReservationPackageOption("Weekend stay", "Best for wedding or conference guests."),
            ReservationPackageOption("Family stay", "More space for relatives or organizers."),
        )
        else -> listOf(
            ReservationPackageOption("Standard request", "Ask the provider to confirm availability."),
            ReservationPackageOption("Event add-on", "Attach this service to an event plan."),
            ReservationPackageOption("Premium support", "Request more coordination before confirmation."),
        )
    }
}

private fun defaultPaymentMethods(): List<String> = listOf(
    "Confirm after venue approval",
)

private data class ReservationServerIds(
    val placeId: String,
    val serviceId: String?,
)

private fun HomeServiceResult.reservationServerIds(content: HomeServicePageContent): ReservationServerIds =
    // TODO [IN-PROGRESS] Reservation writes are persisted; replace this UI-side mapping with
    // place/service IDs returned by the public venue catalog API.
    when (title) {
        "Umucyo Garden Venue" ->
            ReservationServerIds("rw-rebero-umucyo-gardens", "svc_umucyo_garden")
        "Kigali Serena Hotel Garden", "Kigali Serena Hotel" ->
            ReservationServerIds("rw-kgl-serena", "svc_serena_private_dinner")
        "Singita Kwitonda Lodge" ->
            ReservationServerIds("rw-musanze-kwitonda", "svc_kwitonda_campfire")
        "Kigali Marriott Boardroom", "Kigali Marriott Hotel" ->
            ReservationServerIds("rw-kgl-marriott", "svc_marriott_boardroom")
        "Kigali Convention Centre Auditorium" ->
            ReservationServerIds("rw-kgl-convention-centre", "svc_kcc_auditorium")
        "Intare Conference Arena" ->
            ReservationServerIds("rw-kigali-intare-arena", "svc_intare_mainhall")
        "Hotel des Mille Collines" ->
            ReservationServerIds("rw-kgl-mille-collines", "svc_mille_breakfast")
        "Four Points by Sheraton Kigali" ->
            ReservationServerIds("rw-kgl-four-points", "svc_fourpoints_simba")
        "Airport VIP Pickup", "Wedding Guest Shuttle" ->
            ReservationServerIds("rw-kgl-marriott", "svc_marriott_airport")
        "Delegate Shuttle Loop" ->
            ReservationServerIds("rw-kgl-convention-centre", "svc_kcc_delegate_flow")
        "Plated Dinner Service", "Cocktail Reception Menu", "Breakfast Meeting Table" ->
            ReservationServerIds("rw-kgl-serena", "svc_serena_private_dinner")
        "Conference Photography", "Hybrid Livestream Setup", "Wedding Film Package" ->
            ReservationServerIds("rw-rebero-umucyo-gardens", "svc_umucyo_photo")
        "Reception Decor Package", "Stage And Lighting Styling", "Garden Ceremony Styling" ->
            ReservationServerIds("rw-rebero-umucyo-gardens", "svc_umucyo_garden")
        else -> when (content.title) {
            "Wedding venues" -> ReservationServerIds("rw-rebero-umucyo-gardens", "svc_umucyo_garden")
            "Conference rooms" -> ReservationServerIds("rw-kgl-marriott", "svc_marriott_boardroom")
            "Hotels" -> ReservationServerIds("rw-kgl-marriott", "svc_marriott_boardroom")
            "Transport" -> ReservationServerIds("rw-kgl-marriott", "svc_marriott_airport")
            "Catering" -> ReservationServerIds("rw-kgl-serena", "svc_serena_private_dinner")
            "Photo & video" -> ReservationServerIds("rw-rebero-umucyo-gardens", "svc_umucyo_photo")
            else -> ReservationServerIds("rw-kgl-marriott", null)
        }
    }

private fun defaultGuestCount(categoryTitle: String): Int = when (categoryTitle) {
    "Wedding venues" -> 300
    "Conference rooms" -> 60
    "Apartments" -> 2
    else -> 50
}
