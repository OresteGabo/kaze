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
        updatedLabel = "Last updated: April 4, 2026",
        icon = Icons.Default.Policy,
        sections = listOf(
            LegalSection(
                heading = "Overview",
                body = listOf(
                    "Kaze is a private digital hospitality platform developed by GABO for hotel guests, event attendees, day visitors, and related hospitality users.",
                    "Depending on the service setup, data may be handled by GABO, by a hotel or venue using Kaze, or by both parties under a business agreement.",
                ),
            ),
            LegalSection(
                heading = "Information Kaze may use",
                body = listOf(
                    "Kaze may process guest identity details, stay references, service requests, event invitations, access passes, map selections, device diagnostics, notification preferences, and support messages.",
                    "The exact data used depends on which features are enabled by the hotel, venue, or event organizer.",
                ),
            ),
            LegalSection(
                heading = "Why information is used",
                body = listOf(
                    "Information is used to provide guest services, show stay or event details, process requests, support venue navigation, manage access, improve reliability, and communicate important service updates.",
                ),
            ),
            LegalSection(
                heading = "Sharing and retention",
                body = listOf(
                    "Relevant data may be shared with authorized hotels, venues, service providers, contractors, or legal authorities when required.",
                    "Data is kept only as long as needed for operations, support, legal obligations, business records, or the applicable customer agreement.",
                ),
            ),
            LegalSection(
                heading = "Your rights",
                body = listOf(
                    "Depending on applicable law, users may be able to request access, correction, deletion, restriction, objection, portability, or consent withdrawal.",
                    "For privacy requests, contact GABO at orestegabo@icloud.com.",
                ),
            ),
        ),
    ),
    TERMS(
        title = "Terms of Use",
        summary = "Rules for using Kaze and authorized services.",
        updatedLabel = "Last updated: April 4, 2026",
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
                ),
            ),
            LegalSection(
                heading = "Prohibited conduct",
                body = listOf(
                    "Users may not misuse guest data, bypass access controls, interfere with the service, copy or redistribute the product without permission, reverse engineer it except where law explicitly allows, or use it for unlawful activity.",
                ),
            ),
            LegalSection(
                heading = "Availability",
                body = listOf(
                    "Kaze may evolve over time. Features may be added, removed, limited, or refined as the product improves.",
                ),
            ),
            LegalSection(
                heading = "Contact",
                body = listOf("For terms questions, contact orestegabo@icloud.com or visit https://kazerwanda.com."),
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
                heading = "Customer data",
                body = listOf(
                    "Hotel, venue, guest, invitation, and access data is handled only by authorized people and only for legitimate business operations.",
                ),
            ),
            LegalSection(
                heading = "Third-party services",
                body = listOf(
                    "Kaze may rely on hosting, analytics, crash reporting, notifications, payment providers, maps, or wallet integrations. Each provider is reviewed for privacy, security, and contractual fit.",
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
                body = listOf("orestegabo@icloud.com"),
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
