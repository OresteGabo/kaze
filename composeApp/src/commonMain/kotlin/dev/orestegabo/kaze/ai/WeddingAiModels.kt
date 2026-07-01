package dev.orestegabo.kaze.ai

internal data class WeddingInvitationPrompt(
    val coupleNames: String,
    val weddingDate: String,
    val venueName: String,
    val language: WeddingInvitationLanguage = WeddingInvitationLanguage.ENGLISH,
    val tone: WeddingInvitationTone = WeddingInvitationTone.WARM,
    val extraNotes: String = "",
)

internal enum class WeddingInvitationLanguage(val label: String) {
    ENGLISH("English"),
    FRENCH("French"),
    KINYARWANDA("Kinyarwanda"),
}

internal enum class WeddingInvitationTone(val label: String) {
    WARM("Warm"),
    ELEGANT("Elegant"),
    JOYFUL("Joyful"),
    SHORT("Short"),
}

internal data class WeddingInvitationCopy(
    val headline: String,
    val body: String,
    val whatsappMessage: String,
    val rsvpReminder: String,
)

internal enum class EdgeAiCapability {
    GEMMA_TEXT,
    KOOG_ORCHESTRATION,
    PERSON_SEGMENTATION,
    SUBJECT_SEGMENTATION,
}

internal data class EdgeAiStatus(
    val enabled: Boolean,
    val availableCapabilities: Set<EdgeAiCapability>,
    val modelMessage: String,
) {
    val isGemmaReady: Boolean = EdgeAiCapability.GEMMA_TEXT in availableCapabilities
    val isSegmentationReady: Boolean = EdgeAiCapability.PERSON_SEGMENTATION in availableCapabilities ||
        EdgeAiCapability.SUBJECT_SEGMENTATION in availableCapabilities
}
