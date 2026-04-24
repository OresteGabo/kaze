package dev.orestegabo.kaze.ui.home.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.airtel_logo
import kaze.composeapp.generated.resources.bk_logo
import kaze.composeapp.generated.resources.cash_rwanda_note_raster
import kaze.composeapp.generated.resources.k_mark_raster
import kaze.composeapp.generated.resources.momo
import kaze.composeapp.generated.resources.spenn_logo
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private enum class EditableProfileField {
    NAME,
    USERNAME,
    PHONE,
}

private data class FieldValidation(
    val isError: Boolean,
    val message: String,
)

@Composable
internal fun SettingsDetailScreen(
    page: SettingsDetailPage,
    sessionDisplayName: String,
    sessionUsername: String,
    sessionEmail: String,
    sessionPhoneNumber: String,
    needsProfileCompletion: Boolean,
    bottomContentPadding: Dp,
    onUpdateProfile: (String, String, String) -> Unit,
    onBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KazeGhostButton(
            label = "Back to settings",
            onClick = onBack,
            leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )
        SettingsHeader(
            title = page.title,
            subtitle = page.summary,
            icon = page.icon,
        )

        if (page == SettingsDetailPage.ACCOUNT) {
            PersonalDataSnapshotCard(
                displayName = sessionDisplayName,
                username = sessionUsername,
                email = sessionEmail,
                phoneNumber = sessionPhoneNumber,
                needsProfileCompletion = needsProfileCompletion,
                onUpdateProfile = onUpdateProfile,
            )
        }
        if (page == SettingsDetailPage.PRIVACY_CONTROLS) {
            DataCollectionSettingsCard()
        }
        if (page == SettingsDetailPage.PAYMENTS) {
            PaymentMethodsCard()
        }
        if (page == SettingsDetailPage.ABOUT) {
            AboutKazeBrandCard()
        }

        if (page == SettingsDetailPage.HELP) {
            HelpSupportCard()
        } else {
            SettingsSectionCard(page = page)
        }
    }
}

@Composable
private fun SettingsSectionCard(page: SettingsDetailPage) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                page.tokens.forEach { token -> MetaPill(token) }
            }
            page.sections.forEach { section ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        section.heading,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    section.body.forEach { paragraph ->
                        Text(
                            paragraph,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpSupportCard() {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill("FAQ")
                MetaPill("Partners")
                MetaPill("Payments")
                MetaPill("Support")
            }
            HelpFaqCategory(
                title = "Using Kaze",
                items = listOf(
                    "How do I use an invitation code?" to "Enter the code shared by the organizer to open the invitation and follow event updates.",
                    "What is a Kaze Pass?" to "A digital access pass for an event, venue, or stay when entry control is enabled.",
                    "Can I browse venues without signing in?" to "Yes. You can explore venues and prices first, then sign in when an action needs your account.",
                    "Where do I see my saved places?" to "Open Settings, then Activity & payments, then Saved places.",
                ),
            )
            HelpFaqCategory(
                title = "Events and venues",
                items = listOf(
                    "Can I add my wedding venue or conference room?" to "Venue owners or managers can contact Kaze to add spaces, prices, photos, maps, and booking rules.",
                    "Can Kaze be used for private events?" to "Yes. Organizers can use Kaze for invitations, guest access, event updates, and Kaze Pass entry.",
                    "Can I change event details after sending invitations?" to "Yes. Invited guests can see updated event information when the organizer changes it.",
                    "Can Kaze show a venue map?" to "Yes. Some venues can include maps so guests can find halls, rooms, entrances, or event areas.",
                ),
            )
            HelpFaqCategory(
                title = "Payments",
                items = listOf(
                    "Why does cash need confirmation?" to "Cash is paid outside the app, so the venue or hotel confirms it after receiving the money.",
                    "Who confirms payments?" to "Digital payments can be tracked by the app when supported. Cash payments are confirmed by the venue or hotel.",
                ),
            )
            HelpFaqCategory(
                title = "For businesses",
                items = listOf(
                    "How can a hotel or venue join Kaze?" to "Contact GABO at dev@kazerwanda.com or visit kazerwanda.com to discuss onboarding.",
                    "Can Kaze support other businesses?" to "Yes. Kaze can support conference rooms, wedding venues, apartments, stadiums, event spaces, and other mapped spaces.",
                    "Can a business request a custom Kaze app?" to "Yes, if agreed with GABO. Kaze is a private commercial product, so custom use needs a business agreement.",
                ),
            )
            HelpFaqCategory(
                title = "Support",
                items = listOf(
                    "What if event or venue information looks wrong?" to "Use Report a problem so the venue, organizer, or Kaze support can review it.",
                    "How do I contact support?" to "Contact support at dev@kazerwanda.com.",
                    "How do I delete my account?" to "Account deletion is handled through support so it is never triggered by accident. Contact Kaze support if you want to permanently remove your account.",
                ),
            )
        }
    }
}

@Composable
private fun HelpFaqCategory(
    title: String,
    items: List<Pair<String, String>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { (question, answer) ->
                HelpFaqItem(question = question, answer = answer)
            }
        }
    }
}

@Composable
private fun HelpFaqItem(
    question: String,
    answer: String,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                answer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun PaymentMethodsCard() {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, KazeTheme.accents.editorialWarm.copy(alpha = 0.22f)),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Accepted payments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Rwanda-first payment options for reservations, services, deposits, and event extras.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill("MTN MoMo")
                MetaPill("Airtel Money")
                MetaPill("BK / Rswitch")
                MetaPill("Cash")
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Most used in Rwanda",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                PaymentMethodRow(
                    title = "MTN MoMo",
                    status = "Verified",
                    logo = Res.drawable.momo,
                    logoSize = 34.dp,
                    subtitle = "Mobile money",
                )
                PaymentMethodRow(
                    title = "Airtel Money",
                    status = "Available",
                    logo = Res.drawable.airtel_logo,
                    subtitle = "Mobile money",
                )
                PaymentMethodRow(
                    title = "BK / Rswitch",
                    status = "Available",
                    logo = Res.drawable.bk_logo,
                    subtitle = "Bank and card rails",
                )
                Text(
                    "Also supported",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                PaymentMethodRow(
                    title = "Cash",
                    status = "Needs confirmation",
                    logo = Res.drawable.cash_rwanda_note_raster,
                    logoSize = 36.dp,
                    subtitle = "Venue confirmation",
                )
                PaymentMethodRow(
                    title = "SPENN",
                    status = "Available",
                    logo = Res.drawable.spenn_logo,
                    subtitle = "Digital wallet",
                )
                PaymentMethodRow(
                    title = "Card",
                    status = "Check required",
                    subtitle = "Depends on venue",
                )
            }
            PaymentCashNotice()
            // TODO Replace static payment method status with user account and provider verification data.
        }
    }
}

@Composable
private fun PaymentMethodRow(
    title: String,
    status: String,
    subtitle: String? = null,
    logo: DrawableResource? = null,
    logoSize: Dp = 28.dp,
    icon: ImageVector = Icons.Default.VerifiedUser,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = KazeTheme.accents.editorialBotanical.copy(alpha = 0.16f),
            ) {
                if (logo != null) {
                    Image(
                        painter = painterResource(logo),
                        contentDescription = "$title logo",
                        modifier = Modifier
                            .padding(6.dp)
                            .size(logoSize),
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = KazeTheme.accents.editorialBotanical,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                    )
                }
            }
            MetaPill(status)
        }
    }
}

@Composable
private fun PaymentCashNotice() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = KazeTheme.accents.editorialWarm.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, KazeTheme.accents.editorialWarm.copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.cash_rwanda_note_raster),
                contentDescription = "Cash payment",
                modifier = Modifier.size(44.dp),
            )
            Text(
                "Cash payments are confirmed by the venue after they receive the money.",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
            )
        }
    }
}

@Composable
private fun AboutKazeBrandCard() {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                    border = BorderStroke(1.dp, KazeTheme.accents.editorialWarm.copy(alpha = 0.16f)),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.k_mark_raster),
                        contentDescription = "Kaze logo",
                        modifier = Modifier
                            .padding(14.dp)
                            .size(34.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "Kaze by GABO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Hospitality, venues, invitations, and access passes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                    )
                }
            }
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Kaze helps people discover places, manage invitations, follow stay details, request services, view maps, and use access passes from one calm experience.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                )
                Text(
                    "Built by GABO as a private commercial product for hospitality, venues, and event experiences.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill("Version 1.0")
                MetaPill("Private product")
                MetaPill("kazerwanda.com")
            }
        }
    }
}

@Composable
private fun DataCollectionSettingsCard() {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, KazeTheme.accents.editorialWarm.copy(alpha = 0.22f)),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Data collection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "See what Kaze may collect and choose which optional data uses you allow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DataCollectionRow(
                    title = "Required account and service data",
                    description = "Profile, linked contact methods, invitations, stays, passes, and service requests needed to make the app work.",
                    status = "Required",
                )
                DataCollectionRow(
                    title = "Map and venue activity",
                    description = "Venue searches, selected maps, saved places, and recently viewed spaces used to improve navigation and recommendations.",
                    status = "Optional",
                )
                DataCollectionRow(
                    title = "Diagnostics and crash reports",
                    description = "Device, app version, errors, and crash details used to fix bugs and improve reliability.",
                    status = "Recommended",
                )
                DataCollectionRow(
                    title = "Notifications",
                    description = "Invitation alerts, event updates, stay reminders, and delivery preferences.",
                    status = "User controlled",
                )
                DataCollectionRow(
                    title = "Payments and receipts",
                    description = "Payment status, transaction references, receipts, and refund/support context when payments are enabled.",
                    status = "When used",
                )
            }
            // TODO Store consent choices and connect them to analytics, diagnostics, notifications, maps, and data export.
        }
    }
}

@Composable
private fun DataCollectionRow(
    title: String,
    description: String,
    status: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                MetaPill(status)
            }
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun PersonalDataSnapshotCard(
    displayName: String,
    username: String,
    email: String,
    phoneNumber: String,
    needsProfileCompletion: Boolean,
    onUpdateProfile: (String, String, String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var editingField by rememberSaveable { mutableStateOf<EditableProfileField?>(null) }
    var pendingSave by rememberSaveable { mutableStateOf(false) }
    var draftName by rememberSaveable { mutableStateOf(displayName) }
    var draftUsername by rememberSaveable { mutableStateOf(username) }
    var draftPhone by rememberSaveable { mutableStateOf(phoneNumber) }
    val nameFocusRequester = FocusRequester()
    val usernameFocusRequester = FocusRequester()
    val phoneFocusRequester = FocusRequester()

    LaunchedEffect(displayName, username, phoneNumber, editingField) {
        if (editingField == null) {
            draftName = displayName
            draftUsername = username
            draftPhone = phoneNumber
        }
    }

    val resolvedName = displayName.takeIf { it.isNotBlank() } ?: "Add your full name"
    val resolvedEmail = email.takeIf { it.isNotBlank() } ?: "Add your email"
    val resolvedPhone = phoneNumber.takeIf { it.isNotBlank() } ?: "Add your phone number"
    val profileStatus = if (needsProfileCompletion) "Needs completion" else "Basic profile active"
    val profileMatchesDraft = displayName == draftName.trim() &&
        username == draftUsername.trim().lowercase() &&
        phoneNumber == draftPhone.trim()
    val nameValidation = validateDisplayNameInput(draftName)
    val usernameValidation = validateUsernameInput(draftUsername)
    val phoneValidation = validatePhoneNumberInput(draftPhone)

    if (editingField != null && pendingSave && profileMatchesDraft) {
        editingField = null
        pendingSave = false
    }

    LaunchedEffect(editingField) {
        if (editingField == null) {
            focusManager.clearFocus(force = true)
        } else {
            when (editingField) {
                EditableProfileField.NAME -> nameFocusRequester.requestFocus()
                EditableProfileField.USERNAME -> usernameFocusRequester.requestFocus()
                EditableProfileField.PHONE -> phoneFocusRequester.requestFocus()
                null -> Unit
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, KazeTheme.accents.editorialWarm.copy(alpha = 0.22f)),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Personal data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Review what Kaze knows about you and manage the main contact details linked to your account.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PersonalDataRow(
                    label = "Full name",
                    value = resolvedName,
                    action = if (editingField == EditableProfileField.NAME) "Save" else "Edit",
                    icon = Icons.Default.Edit,
                    isEditing = editingField == EditableProfileField.NAME,
                    editValue = draftName,
                    onEditValueChange = { draftName = it },
                    focusRequester = nameFocusRequester,
                    editLabel = "Full name",
                    onClick = {
                        if (editingField == EditableProfileField.NAME) {
                            if (nameValidation.isError) return@PersonalDataRow
                            pendingSave = true
                            onUpdateProfile(draftName, draftUsername, draftPhone)
                        } else {
                            editingField = EditableProfileField.NAME
                            pendingSave = false
                        }
                    },
                    onCancel = {
                        editingField = null
                        pendingSave = false
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                    supportingText = nameValidation.message,
                    isError = nameValidation.isError,
                )
                PersonalDataRow(
                    label = "Linked email",
                    value = resolvedEmail,
                    action = "Verified",
                    icon = Icons.Default.Email,
                )
                PersonalDataRow(
                    label = "Username",
                    value = username.takeIf { it.isNotBlank() } ?: "Add a username",
                    action = if (editingField == EditableProfileField.USERNAME) "Save" else if (username.isBlank()) "Add" else "Edit",
                    icon = Icons.Default.AccountCircle,
                    isEditing = editingField == EditableProfileField.USERNAME,
                    editValue = draftUsername,
                    onEditValueChange = { draftUsername = it },
                    focusRequester = usernameFocusRequester,
                    editLabel = "Username",
                    onClick = {
                        if (editingField == EditableProfileField.USERNAME) {
                            if (usernameValidation.isError) return@PersonalDataRow
                            pendingSave = true
                            onUpdateProfile(draftName, draftUsername, draftPhone)
                        } else {
                            editingField = EditableProfileField.USERNAME
                            pendingSave = false
                        }
                    },
                    onCancel = {
                        editingField = null
                        pendingSave = false
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done,
                    ),
                    supportingText = usernameValidation.message,
                    isError = usernameValidation.isError,
                )
                PersonalDataRow(
                    label = "Linked phone",
                    value = resolvedPhone,
                    action = if (editingField == EditableProfileField.PHONE) "Save" else if (phoneNumber.isBlank()) "Add" else "Edit",
                    icon = Icons.Default.Phone,
                    isEditing = editingField == EditableProfileField.PHONE,
                    editValue = draftPhone,
                    onEditValueChange = { draftPhone = it },
                    focusRequester = phoneFocusRequester,
                    editLabel = "Phone number",
                    onClick = {
                        if (editingField == EditableProfileField.PHONE) {
                            if (phoneValidation.isError) return@PersonalDataRow
                            pendingSave = true
                            onUpdateProfile(draftName, draftUsername, draftPhone)
                        } else {
                            editingField = EditableProfileField.PHONE
                            pendingSave = false
                        }
                    },
                    onCancel = {
                        editingField = null
                        pendingSave = false
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done,
                    ),
                    supportingText = phoneValidation.message,
                    isError = phoneValidation.isError,
                )
                ProfileStatusRow(profileStatus = profileStatus)
            }
        }
    }
}

@Composable
private fun PersonalDataRow(
    label: String,
    value: String,
    action: String,
    icon: ImageVector,
    isEditing: Boolean = false,
    editValue: String = "",
    onEditValueChange: (String) -> Unit = {},
    focusRequester: FocusRequester? = null,
    editLabel: String = label,
    onClick: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    supportingText: String? = null,
    isError: Boolean = false,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(if (isEditing) 10.dp else 0.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isEditing && onClick != null) { onClick?.invoke() },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = KazeTheme.accents.editorialWarm.copy(alpha = 0.16f),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = KazeTheme.accents.editorialWarm,
                        modifier = Modifier.padding(8.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    if (!isEditing) {
                        Text(
                            value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                if (!isEditing) {
                    MetaPill(action)
                }
            }

            if (isEditing) {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = onEditValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { base ->
                            if (focusRequester != null) base.focusRequester(focusRequester) else base
                        },
                    label = { Text(editLabel) },
                    colors = OutlinedTextFieldDefaults.colors(),
                    keyboardOptions = keyboardOptions,
                    supportingText = supportingText?.let { message ->
                        {
                            Text(
                                message,
                                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                            )
                        }
                    },
                    isError = isError,
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    onCancel?.let { cancel ->
                        KazeGhostButton(label = "Cancel", onClick = cancel)
                    }
                    onClick?.let { save ->
                        KazePrimaryButton(label = action, onClick = save)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatusRow(profileStatus: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = KazeTheme.accents.editorialWarm.copy(alpha = 0.14f),
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = KazeTheme.accents.editorialWarm,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    "Profile status",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                Text(
                    profileStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            MetaPill("Ready")
        }
    }
}

private fun validateDisplayNameInput(input: String): FieldValidation {
    val trimmed = input.trim()
    return when {
        trimmed.isBlank() -> FieldValidation(true, "Full name is required.")
        trimmed.length < 3 -> FieldValidation(true, "Name looks incomplete.")
        else -> FieldValidation(false, "Looks good.")
    }
}

private fun validateUsernameInput(input: String): FieldValidation {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return FieldValidation(false, "Optional.")
    val normalized = trimmed.lowercase()
    return when {
        normalized.length < 3 -> FieldValidation(true, "Username is too short.")
        normalized.length > 32 -> FieldValidation(true, "Username is too long.")
        !normalized.matches(Regex("^[a-z0-9._]+$")) -> {
            FieldValidation(true, "Use only letters, numbers, dots, and underscores.")
        }
        else -> FieldValidation(false, "Username format looks good.")
    }
}

private fun validatePhoneNumberInput(input: String): FieldValidation {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return FieldValidation(false, "Optional. Use +2507..., 07..., or 7...")

    val compact = buildString(trimmed.length) {
        trimmed.forEachIndexed { index, char ->
            when {
                char.isDigit() -> append(char)
                char == '+' && index == 0 -> append(char)
            }
        }
    }

    val digitCount = compact.count { it.isDigit() }
    val isRwandaLocal = compact.startsWith("07") || compact.startsWith("7")
    val isRwandaIntl = compact.startsWith("+250") || compact.startsWith("250")

    return when {
        isRwandaLocal && digitCount < 9 -> FieldValidation(true, "Phone number looks incomplete.")
        compact.startsWith("07") && digitCount != 10 -> FieldValidation(true, "Use all 10 digits for 07... format.")
        compact.startsWith("7") && digitCount != 9 -> FieldValidation(true, "Use all 9 digits for 7... format.")
        isRwandaIntl && digitCount < 12 -> FieldValidation(true, "Phone number looks incomplete.")
        compact.startsWith("+250") && digitCount != 12 -> FieldValidation(true, "Use +250 followed by 9 digits.")
        compact.startsWith("250") && digitCount != 12 -> FieldValidation(true, "Use 250 followed by 9 digits.")
        digitCount < 8 -> FieldValidation(true, "Phone number looks incomplete.")
        digitCount > 15 -> FieldValidation(true, "Phone number is too long.")
        !compact.matches(Regex("^\\+?[0-9]+$")) -> FieldValidation(true, "Phone number format is invalid.")
        else -> FieldValidation(false, "Phone number format looks good.")
    }
}


@Composable
internal fun LegalDetailScreen(
    page: LegalPage,
    bottomContentPadding: Dp,
    onBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KazeGhostButton(
            label = "Back to settings",
            onClick = onBack,
            leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = KazeTheme.accents.editorialWarm.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, KazeTheme.accents.editorialWarm.copy(alpha = 0.28f)),
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = KazeTheme.accents.editorialWarm,
                    modifier = Modifier.padding(10.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    page.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    page.updatedLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                page.sections.forEach { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            section.heading,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        section.body.forEach { paragraph ->
                            Text(
                                paragraph,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                            )
                        }
                    }
                }
            }
        }
    }
}
