package dev.orestegabo.kaze.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Payments
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
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.home.invitations.InvitationEventType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun HomeReservationDraftScreen(
    content: HomeServicePageContent,
    result: HomeServiceResult,
    bottomContentPadding: Dp,
    onBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    var eventName by rememberSaveable(result.title) { mutableStateOf("") }
    var preferredDate by rememberSaveable(result.title) { mutableStateOf("") }
    var guests by rememberSaveable(result.title) { mutableIntStateOf(defaultGuestCount(content.title)) }
    var selectedPackage by rememberSaveable(result.title) { mutableStateOf(defaultReservationPackages(content.title).first().label) }
    var selectedAddOns by rememberSaveable(result.title) { mutableStateOf(emptyList<String>()) }
    var paymentMethod by rememberSaveable(result.title) { mutableStateOf(defaultPaymentMethods().first()) }
    var note by rememberSaveable(result.title) { mutableStateOf("") }
    var isSaved by rememberSaveable(result.title) { mutableStateOf(false) }
    var isCreatingInvitation by rememberSaveable(result.title) { mutableStateOf(false) }
    var linkedInvitation by remember { mutableStateOf<InvitationPreview?>(null) }

    if (isCreatingInvitation) {
        CreateInvitationScreen(
            onBack = { isCreatingInvitation = false },
            onCreateInvitation = { invitation ->
                linkedInvitation = invitation
                isCreatingInvitation = false
                isSaved = true
            },
            modifier = Modifier.fillMaxSize(),
            bottomContentPadding = bottomContentPadding,
            seed = reservationInvitationSeed(
                content = content,
                result = result,
                eventName = eventName,
                preferredDate = preferredDate,
                guests = guests,
                note = note,
            ),
        )
        return
    }

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
                selectedPackage = selectedPackage,
                selectedAddOns = selectedAddOns,
                paymentMethod = paymentMethod,
                note = note,
                linkedInvitation = linkedInvitation,
                onCreateInvitation = { isCreatingInvitation = true },
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

        SectionLabel("Add-ons")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            defaultReservationAddOns(content.title).forEach { addOn ->
                ReservationChip(
                    label = addOn,
                    selected = addOn in selectedAddOns,
                    accent = content.accent,
                    onClick = {
                        selectedAddOns = if (addOn in selectedAddOns) {
                            selectedAddOns - addOn
                        } else {
                            selectedAddOns + addOn
                        }
                    },
                )
            }
        }

        SectionLabel("Payment")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            defaultPaymentMethods().forEach { method ->
                ReservationChoiceCard(
                    title = method,
                    subtitle = paymentSubtitle(method),
                    selected = paymentMethod == method,
                    accent = content.accent,
                    leadingIcon = Icons.Default.Payments,
                    onClick = { paymentMethod = method },
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
            selectedPackage = selectedPackage,
            selectedAddOns = selectedAddOns,
            paymentMethod = paymentMethod,
        )

        KazePrimaryButton(
            label = "Save reservation request",
            onClick = {
                // TODO Persist this draft through the reservation API and show the confirmed reservation code after the backend accepts it.
                // TODO Connect a saved venue reservation to event creation, invitations, Kaze Pass access, maps, and payment status.
                isSaved = true
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Default.CheckCircle,
        )
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
    selectedPackage: String,
    selectedAddOns: List<String>,
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
                MetaPill("$guests guests")
                MetaPill(selectedPackage)
                MetaPill(paymentMethod)
                selectedAddOns.forEach { MetaPill(it) }
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
    selectedPackage: String,
    selectedAddOns: List<String>,
    paymentMethod: String,
    note: String,
    linkedInvitation: InvitationPreview?,
    onCreateInvitation: () -> Unit,
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
                        result.title,
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
                selectedPackage = selectedPackage,
                selectedAddOns = selectedAddOns,
                paymentMethod = paymentMethod,
            )
            if (note.isNotBlank()) {
                Text(
                    note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                )
            }
            if (linkedInvitation != null) {
                ReservationLinkedInvitationCard(
                    invitation = linkedInvitation,
                    accent = content.accent,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KazeSecondaryButton(
                    label = if (linkedInvitation == null) "Create invitation" else "New invitation",
                    onClick = onCreateInvitation,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.CalendarMonth,
                )
                KazePrimaryButton(
                    label = "Done",
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.CheckCircle,
                )
            }
        }
    }
}

@Composable
private fun ReservationLinkedInvitationCard(
    invitation: InvitationPreview,
    accent: Color,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = accent.copy(alpha = 0.18f)) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
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
                    "Invitation connected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "${invitation.title} • ${invitation.code.ifBlank { "code after approval" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }
        }
    }
}

private data class ReservationPackageOption(
    val label: String,
    val detail: String,
)

private fun defaultReservationPackages(categoryTitle: String): List<ReservationPackageOption> {
    // TODO Load real packages, availability windows, venue rules, and price breakdowns from the Ktor API/database.
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

private fun defaultReservationAddOns(categoryTitle: String): List<String> {
    // TODO Route add-ons to the right product modules: catering, styling, cleaning, insurance, media, transport, and event layouts.
    return when (categoryTitle) {
        "Wedding venues" -> listOf("Catering", "Styling & decor", "Photo & video", "Guest access", "Insurance")
        "Conference rooms" -> listOf("Coffee break", "Projector", "Livestream", "Guest access", "Cleaning")
        "Apartments" -> listOf("Airport pickup", "Extra cleaning", "Breakfast", "Late checkout")
        "Catering" -> listOf("Soft drinks", "Coffee break", "Traditional menu", "Service team")
        "Photo & video" -> listOf("Drone", "Livestream", "Same-day preview", "Extra camera")
        "Transport" -> listOf("Airport", "Guest shuttle", "VIP car", "Evening return")
        else -> listOf("Guest access", "Cleaning", "Transport", "Support")
    }
}

private fun defaultPaymentMethods(): List<String> = listOf(
    "MTN MoMo",
    "Airtel Money",
    "BK/Rswitch",
    "Card",
    "Cash at venue",
)

private fun paymentSubtitle(method: String): String {
    // TODO Validate payment methods per platform, provider, hotel, venue, and country before showing them as available.
    return when (method) {
        "Cash at venue" -> "Confirm in Kaze after payment is received."
        "Card" -> "Useful for guests or companies that prefer card payment."
        else -> "Mobile payment confirmation can be attached later."
    }
}

private fun defaultGuestCount(categoryTitle: String): Int = when (categoryTitle) {
    "Wedding venues" -> 300
    "Conference rooms" -> 60
    "Apartments" -> 2
    else -> 50
}

private fun reservationInvitationSeed(
    content: HomeServicePageContent,
    result: HomeServiceResult,
    eventName: String,
    preferredDate: String,
    guests: Int,
    note: String,
): InvitationDraftSeed {
    // TODO Replace this heuristic with an event type returned by the reservation API when venues/services become database-backed.
    val eventType = when (content.title) {
        "Wedding venues" -> InvitationEventType.WEDDING
        "Conference rooms" -> InvitationEventType.CONFERENCE
        else -> InvitationEventType.OTHER
    }
    return InvitationDraftSeed(
        eventType = eventType,
        eventTitle = eventName.ifBlank { "${result.title} event" },
        venueName = result.title,
        preferredDate = preferredDate,
        guestCount = guests,
        sourceLabel = "Reservation request",
        note = note.ifBlank { "Linked to the reservation request for ${result.title}." },
    )
}
