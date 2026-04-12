package dev.orestegabo.kaze.ui.home.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class SettingsDetailPage(
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val tokens: List<String>,
    val sections: List<LegalSection>,
) {
    ACCOUNT(
        title = "Account & identity",
        summary = "Your profile details and the identity Kaze uses for stays, events, and passes.",
        icon = Icons.Default.AccountCircle,
        tokens = listOf("Name", "Email", "Phone", "Download data"),
        sections = listOf(
            LegalSection("Profile details", listOf("Review the name, phone number, email, and profile photo used across Kaze.")),
            LegalSection("Download your data", listOf("Request a copy of your personal data, including profile details, linked contact methods, saved places, invitations, stays, and service requests where applicable.")),
            LegalSection("Identity links", listOf("See the hotel stays, event organizer roles, and venue team access connected to this account without mixing permissions.")),
            LegalSection("Account changes", listOf("Some profile changes may require phone, email, or identity verification before they are applied.")),
        ),
    ),
    LANGUAGE(
        title = "Language",
        summary = "Choose the language Kaze uses for labels, alerts, and support text.",
        icon = Icons.Default.Language,
        tokens = listOf("English", "Français", "Kinyarwanda", "Swahili"),
        sections = listOf(
            LegalSection("Current language", listOf("English is selected by default.")),
            LegalSection("Regional clarity", listOf("Language preferences help make venue searches, invitations, stay details, and support messages easier to understand.")),
            LegalSection("Content language", listOf("Some hotel, venue, or event content may still appear in the language provided by that organizer.")),
        ),
    ),
    PRIVACY_CONTROLS(
        title = "Privacy controls",
        summary = "Preferences for personalization, maps, and activity visibility.",
        icon = Icons.Default.PrivacyTip,
        tokens = listOf("Data collection", "Map preferences", "Diagnostics", "Notifications"),
        sections = listOf(
            LegalSection("What Kaze may collect", listOf("Kaze may collect required service data such as profile details, invitations, stays, access passes, requests, and payment references when those features are used.")),
            LegalSection("Optional collection", listOf("Optional data may include map activity, saved places, recently viewed venues, diagnostics, and crash reports.")),
            LegalSection("Map and venue activity", listOf("Control how map searches, saved places, and recently viewed venues are used for recommendations.")),
            LegalSection("Visibility", listOf("Event organizers may need RSVP and guest list visibility controls, while hotel guests may need stay-related privacy preferences.")),
            LegalSection("Consent choices", listOf("Choose which optional data uses are allowed for your account.")),
            LegalSection("Delete request", listOf("Ask Kaze to remove personal data that is no longer needed, subject to legal, safety, payment, and service record requirements.")),
        ),
    ),
    SECURITY(
        title = "Security",
        summary = "Controls for passes, invitations, sessions, and trusted devices.",
        icon = Icons.Default.Lock,
        tokens = listOf("Biometric lock", "Active sessions", "Trusted devices"),
        sections = listOf(
            LegalSection("App lock", listOf("Protect sensitive screens like passes, invitations, payments, and stay details with the lock options available on your device.")),
            LegalSection("Sessions", listOf("Review active sessions and sign out from devices you no longer recognize.")),
            LegalSection("Trusted devices", listOf("Trusted devices can reduce friction for returning users while keeping high-risk actions protected.")),
            LegalSection("Sensitive actions", listOf("Extra confirmation can protect payment changes, Kaze Pass access, personal data export, and data deletion requests.")),
        ),
    ),
    NOTIFICATIONS(
        title = "Notifications",
        summary = "Choose which updates Kaze can send and when they should appear.",
        icon = Icons.Default.Notifications,
        tokens = listOf("Invitations", "Event updates", "Stay reminders", "Quiet hours"),
        sections = listOf(
            LegalSection("Invitations", listOf("Receive alerts for new invitation codes, RSVP changes, accepted invitations, and Kaze Pass updates.")),
            LegalSection("Events", listOf("Receive updates when event time, venue, map, entrance rules, or organizer notes change.")),
            LegalSection("Stays", listOf("Receive stay reminders for check-in, checkout, booked services, request status, and hotel messages.")),
            LegalSection("Quiet hours", listOf("Limit non-urgent notifications during hours you choose while still allowing important access or safety updates.")),
        ),
    ),
    PAYMENTS(
        title = "Payment methods",
        summary = "Ways to pay for venues, services, and event extras.",
        icon = Icons.Default.Payments,
        tokens = listOf("Cash", "MTN MoMo", "Airtel Money", "SPENN", "BK / Rswitch", "Card"),
        sections = listOf(
            LegalSection("Payment options", listOf("Choose one of the payment methods accepted by the venue or event.")),
            LegalSection("Use cases", listOf("Payments can support conference room reservations, wedding venue deposits, decoration services, catering, transport, media services, and event access passes.")),
            LegalSection("Payment availability", listOf("Available payment methods can depend on your hotel, venue, event, and country.")),
        ),
    ),
    SAVED_PLACES(
        title = "Saved places",
        summary = "Favorite venues, apartments, hotels, and recently viewed spaces.",
        icon = Icons.Default.Bookmark,
        tokens = listOf("Favorite venues", "Saved apartments", "Recently viewed"),
        sections = listOf(
            LegalSection("Favorites", listOf("Save wedding venues, conference rooms, hotels, apartments, and service providers you want to compare.")),
            LegalSection("Recently viewed", listOf("Recently viewed places help users return quickly to spaces they checked before, especially when comparing prices and capacity.")),
            LegalSection("Collections", listOf("Group saved places into shortlists such as Wedding options, Conference rooms, Family stays, and Vendor ideas.")),
        ),
    ),
    INVITATIONS(
        title = "Invitations",
        summary = "Defaults for RSVP visibility, reminders, and archived invitations.",
        icon = Icons.Default.EventAvailable,
        tokens = listOf("RSVP visibility", "Reminder timing", "Archived invites"),
        sections = listOf(
            LegalSection("Invitation defaults", listOf("Choose default reminder timing, RSVP visibility, and whether invitation updates trigger notifications.")),
            LegalSection("Archived invitations", listOf("Past invitations are separated from active and pending invitations, with a calmer design that still allows users to recover details.")),
            LegalSection("Kaze Pass", listOf("Accepted invitations can be linked to a Kaze Pass for entry when the event uses access control.")),
        ),
    ),
    ACCESSIBILITY(
        title = "Accessibility",
        summary = "Options that make Kaze easier to read and navigate.",
        icon = Icons.Default.Accessibility,
        tokens = listOf("Text size", "High contrast", "Reduced motion"),
        sections = listOf(
            LegalSection("Readability", listOf("Text size and high contrast options can help users who struggle with small text, low contrast, or bright environments.")),
            LegalSection("Motion", listOf("Reduced motion limits decorative transitions while keeping important feedback clear.")),
            LegalSection("Comfort", listOf("These options help make Kaze easier to read, tap, and navigate.")),
        ),
    ),
    WALLET_PASSES(
        title = "Wallet passes",
        summary = "Save supported invitations and Kaze Passes to the wallet available on this device.",
        icon = Icons.Default.Wallet,
        tokens = listOf("Kaze Pass", "Invitations", "Device wallet"),
        sections = listOf(
            LegalSection("Device wallet", listOf("Kaze shows the wallet option that matches this device when pass saving is available.")),
            LegalSection("Pass updates", listOf("Saved passes can help guests and event attendees keep access information close, even when they are not inside Kaze.")),
            LegalSection("Privacy", listOf("Wallet passes may show only the details needed for entry, such as event name, date, code, or access status.")),
        ),
    ),
    HELP(
        title = "Help & support",
        summary = "Get help, report a problem, or contact support.",
        icon = Icons.AutoMirrored.Filled.Help,
        tokens = listOf("FAQ", "Report a problem", "Contact support"),
        sections = listOf(
            LegalSection("How do I use an invitation code?", listOf("Enter the code shared by the organizer to open the invitation and follow event updates.")),
            LegalSection("What is a Kaze Pass?", listOf("A Kaze Pass is a digital access pass for an event, venue, or stay when entry control is enabled.")),
            LegalSection("Can I browse venues without signing in?", listOf("Yes. You can explore venues and prices first, then sign in when an action needs your account.")),
            LegalSection("Why does cash need confirmation?", listOf("Cash is paid outside the app, so the venue or hotel confirms it after receiving the money.")),
            LegalSection("Where do I see my saved places?", listOf("Open Settings, then Activity & payments, then Saved places.")),
            LegalSection("What if event or venue information looks wrong?", listOf("Use Report a problem so the venue, organizer, or Kaze support can review it.")),
            LegalSection("How can a hotel or venue join Kaze?", listOf("Contact GABO at orestegabo@icloud.com or visit kazerwanda.com to discuss onboarding.")),
            LegalSection("Can I add my wedding venue or conference room?", listOf("Venue owners or managers can contact Kaze to add spaces, prices, photos, maps, and booking rules.")),
            LegalSection("Can Kaze be used for private events?", listOf("Yes. Organizers can use Kaze for invitations, guest access, event updates, and Kaze Pass entry.")),
            LegalSection("Can I use Kaze without a hotel stay?", listOf("Yes. Kaze can also be used for venues, invitations, apartments, events, and services.")),
            LegalSection("Who confirms payments?", listOf("Digital payments can be tracked by the app when supported. Cash payments are confirmed by the venue or hotel after receiving the money.")),
            LegalSection("Can I change event details after sending invitations?", listOf("Yes. Invited guests can see updated event information when the organizer changes it.")),
            LegalSection("Can Kaze show a venue map?", listOf("Yes. Some venues can include maps so guests can find halls, rooms, entrances, or event areas.")),
            LegalSection("Can Kaze support other businesses?", listOf("Yes. Kaze can support conference rooms, wedding venues, apartments, stadiums, event spaces, and other mapped spaces.")),
            LegalSection("Can a business request a custom Kaze app?", listOf("Yes, if agreed with GABO. Kaze is a private commercial product, so custom use needs a business agreement.")),
            LegalSection("Report a problem", listOf("Report incorrect venue information, payment issues, map problems, invitation errors, or app crashes.")),
            LegalSection("Contact", listOf("Contact support at orestegabo@icloud.com.")),
        ),
    ),
    ABOUT(
        title = "About Kaze",
        summary = "What Kaze is, who builds it, and where to get support.",
        icon = Icons.Default.Info,
        tokens = listOf("Kaze by GABO", "Hospitality", "Venues", "Events"),
        sections = listOf(
            LegalSection("What Kaze does", listOf("Kaze brings venue discovery, hotel stays, event invitations, access passes, maps, service requests, and payments into one experience.")),
            LegalSection("Who it serves", listOf("Kaze is designed for hotel guests, event attendees, organizers, venues, hotels, and service providers.")),
            LegalSection("Owner", listOf("Kaze is developed by GABO as a private commercial product.")),
            LegalSection("Contact", listOf("Website: https://kazerwanda.com", "Email: orestegabo@icloud.com")),
        ),
    ),
}
