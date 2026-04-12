package dev.orestegabo.kaze.ui.home.components

import dev.orestegabo.kaze.ui.home.invitations.InvitationEventType

internal data class InvitationDraftSeed(
    val eventType: InvitationEventType,
    val eventTitle: String,
    val venueName: String,
    val preferredDate: String,
    val guestCount: Int,
    val sourceLabel: String,
    val note: String,
)
