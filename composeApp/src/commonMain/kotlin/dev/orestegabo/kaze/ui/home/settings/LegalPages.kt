package dev.orestegabo.kaze.ui.home.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class LegalPage(
    val title: String,
    val summary: String,
    val updatedLabel: String,
    val icon: ImageVector,
    val sections: List<LegalSection>,
) {
    PRIVACY(
        title = "Privacy Policy",
        summary = "How Kaze may collect, use, and protect personal data.",
        updatedLabel = "Last updated: April 22, 2026",
        icon = Icons.Default.Policy,
        sections = listOf(
            LegalSection(
                heading = "Overview",
                body = listOf(
                    "Kaze is a digital hospitality and event platform developed by GABO for hotel guests, event attendees, organizers, venues, and related service providers.",
                    "Depending on the service setup, data may be handled by GABO, by a hotel or venue using Kaze, or by both parties under a business agreement.",
                ),
            ),
            LegalSection(
                heading = "Information Kaze may use",
                body = listOf(
                    "Kaze may process account details, social sign-in references, stay references, service requests, invitation records, RSVP choices, access passes, map selections, notification preferences, payment references, and support messages.",
                    "The exact data used depends on which features are enabled by the hotel, venue, organizer, or business using Kaze.",
                ),
            ),
            LegalSection(
                heading = "Guest mode and account mode",
                body = listOf(
                    "Some public browsing features may be available in guest mode without a full account.",
                    "Private actions such as opening personal invitations, saving protected passes, managing bookings, making certain payments, or viewing private stay details may require sign-in.",
                ),
            ),
            LegalSection(
                heading = "Payments and bookings",
                body = listOf(
                    "When payments, reservations, or booking-related actions are used, Kaze may process the data needed to show payment status, booking state, and venue or hotel confirmation details.",
                    "Kaze may support payment methods commonly used in Rwanda, including mobile money and approved bank or card rails, depending on venue setup.",
                ),
            ),
            LegalSection(
                heading = "On-device AI features",
                body = listOf(
                    "When Kaze shows an on-device AI feature, the app is designed to keep that task on the device instead of sending the content to a remote AI server.",
                    "Examples may include RSVP assistance, event explanations, or local helper features where available.",
                ),
            ),
            LegalSection(
                heading = "Sharing, retention, and rights",
                body = listOf(
                    "Relevant data may be shared with authorized hotels, venues, service providers, contractors, or legal authorities when required.",
                    "Data is kept only as long as needed for operations, support, legal obligations, business records, or the applicable customer agreement.",
                    "Depending on applicable law, users may be able to request access, correction, deletion, restriction, objection, portability, or consent withdrawal.",
                    "For privacy requests, contact GABO at dev@kazerwanda.com.",
                ),
            ),
        ),
    ),
    TERMS(
        title = "Terms of Use",
        summary = "Rules for using Kaze and authorized services.",
        updatedLabel = "Last updated: April 22, 2026",
        icon = Icons.Default.Description,
        sections = listOf(
            LegalSection(
                heading = "Acceptance",
                body = listOf(
                    "By accessing or using Kaze, you agree to the applicable terms and any separate written commercial agreement that may apply.",
                ),
            ),
            LegalSection(
                heading = "Authorized use",
                body = listOf(
                    "Kaze may be used only by authorized business customers, hotel or venue staff, approved team members, approved contractors, and end users using an authorized Kaze service.",
                    "End users may use Kaze to browse venues, manage invitations, access passes, bookings, stays, event schedules, and related services only for legitimate personal or business purposes.",
                ),
            ),
            LegalSection(
                heading = "Prohibited conduct",
                body = listOf(
                    "Users may not misuse guest data, bypass access controls, interfere with the service, copy or redistribute the product without permission, reverse engineer it except where law explicitly allows, or use it for unlawful activity.",
                ),
            ),
            LegalSection(
                heading = "Invitations, passes, and bookings",
                body = listOf(
                    "Invitation access, RSVP details, passes, venue information, room availability, and booking-related data may change when updated by the organizer, hotel, venue, or business operating the service.",
                    "A displayed pass or invitation does not create rights beyond the rules and approvals attached to that event, stay, or venue.",
                ),
            ),
            LegalSection(
                heading = "Availability and changes",
                body = listOf(
                    "Kaze may evolve over time. Features may be added, removed, limited, or refined as the product improves.",
                    "Some features may be unavailable in some countries, venues, hotels, devices, or business setups.",
                ),
            ),
            LegalSection(
                heading = "Contact",
                body = listOf("For terms questions, contact dev@kazerwanda.com or visit https://kazerwanda.com."),
            ),
        ),
    ),
    LICENSE(
        title = "License & Ownership",
        summary = "Kaze is private proprietary software, not open source.",
        updatedLabel = "Copyright 2026 GABO. All rights reserved.",
        icon = Icons.Default.VerifiedUser,
        sections = listOf(
            LegalSection(
                heading = "Status",
                body = listOf(
                    "Kaze is proprietary, private software owned by GABO. The project is not open source.",
                ),
            ),
            LegalSection(
                heading = "Restrictions",
                body = listOf(
                    "Unless GABO gives explicit written permission, nobody may copy, reproduce, modify, distribute, sublicense, publish, resell, create derivative works from, or expose the source code, design assets, business logic, or product materials to unauthorized parties.",
                ),
            ),
            LegalSection(
                heading = "Allowed access",
                body = listOf(
                    "Access is limited to the owner, approved internal team members, paid contractors, service providers, or other people operating under a valid written agreement with GABO.",
                    "Access does not transfer ownership or intellectual property rights.",
                ),
            ),
            LegalSection(
                heading = "Commercial use",
                body = listOf(
                    "Commercial use, resale, licensing, white-labeling, or customer delivery is allowed only under an explicit written business agreement authorized by GABO.",
                ),
            ),
        ),
    ),
    SECURITY(
        title = "Data & Security",
        summary = "How Kaze protects access and user data.",
        updatedLabel = "Security and access guidance",
        icon = Icons.Default.Security,
        sections = listOf(
            LegalSection(
                heading = "Security approach",
                body = listOf(
                    "Kaze uses access controls, limited internal access, secure storage practices, environment separation, audit-friendly operations, and ongoing security reviews.",
                    "No system can be guaranteed to be 100% secure, so services are reviewed before launch.",
                ),
            ),
            LegalSection(
                heading = "Authentication and access",
                body = listOf(
                    "Kaze may use email sign-in, password-based login, social sign-in providers, access tokens, refresh tokens, and session controls to protect accounts and connected services.",
                    "Access to private invitations, passes, bookings, and stay details is limited to the user or authorized business roles linked to that data.",
                ),
            ),
            LegalSection(
                heading = "Customer data",
                body = listOf(
                    "Hotel, venue, guest, invitation, booking, payment-reference, and access data is handled only by authorized people and only for legitimate business operations.",
                ),
            ),
            LegalSection(
                heading = "Third-party and infrastructure services",
                body = listOf(
                    "Kaze may rely on hosting, notifications, payment providers, maps, wallet integrations, or social sign-in providers. Each provider is reviewed for privacy, security, and contractual fit.",
                    "Infrastructure and operational choices may change over time as the product grows.",
                ),
            ),
        ),
    ),
    CONTACT(
        title = "Contact",
        summary = "Where users or partners can reach GABO.",
        updatedLabel = "Support and business contact",
        icon = Icons.AutoMirrored.Filled.ContactSupport,
        sections = listOf(
            LegalSection(
                heading = "Email",
                body = listOf("dev@kazerwanda.com"),
            ),
            LegalSection(
                heading = "Website",
                body = listOf("https://kazerwanda.com"),
            ),
            LegalSection(
                heading = "Business requests",
                body = listOf(
                    "For partnerships, customer services, paid work, licensing, or access to private project materials, contact GABO before using or sharing the product.",
                ),
            ),
        ),
    ),
}
