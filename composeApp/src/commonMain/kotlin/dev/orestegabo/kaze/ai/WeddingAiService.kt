package dev.orestegabo.kaze.ai

internal interface WeddingAiService {
    val status: EdgeAiStatus

    suspend fun generateInvitationCopy(prompt: WeddingInvitationPrompt): WeddingInvitationCopy
}

internal object LocalWeddingCopyFallback {
    fun generate(prompt: WeddingInvitationPrompt): WeddingInvitationCopy {
        val couple = prompt.coupleNames.ifBlank { "the couple" }
        val date = prompt.weddingDate.ifBlank { "our wedding day" }
        val venue = prompt.venueName.ifBlank { "our celebration venue" }
        return when (prompt.language) {
            WeddingInvitationLanguage.FRENCH -> WeddingInvitationCopy(
                headline = "$couple",
                body = "Deux chemins, une promesse, et la joie douce d'un amour qui choisit de grandir devant ceux qui lui sont chers.",
                whatsappMessage = "$couple celebrent leur amour le $date a $venue. Retrouvez les details et le RSVP avec ce lien.",
                rsvpReminder = "Petit rappel: merci de confirmer votre presence au mariage de $couple.",
            )
            WeddingInvitationLanguage.KINYARWANDA -> WeddingInvitationCopy(
                headline = "$couple",
                body = "Inkuru y'urukundo ikura iba isezerano, umutima ukabona aho utaha, n'umunsi ukagira igisobanuro gishya.",
                whatsappMessage = "$couple bizihiza urukundo rwabo ku wa $date kuri $venue. Details na RSVP biri kuri iyi link.",
                rsvpReminder = "Mwibutswe kwemeza ko muzitabira ubukwe bwa $couple.",
            )
            WeddingInvitationLanguage.ENGLISH -> WeddingInvitationCopy(
                headline = "$couple",
                body = "Two stories become one promise, carried by tenderness, laughter, and the quiet beauty of choosing each other every day.",
                whatsappMessage = "Hello, $couple invite you to their wedding on $date at $venue. Please confirm using the RSVP link.",
                rsvpReminder = "A gentle reminder to confirm your presence for $couple's wedding.",
            )
        }
    }
}

internal class NoopWeddingAiService(
    private val enabled: Boolean,
) : WeddingAiService {
    override val status: EdgeAiStatus = EdgeAiStatus(
        enabled = enabled,
        availableCapabilities = emptySet(),
        modelMessage = "On-device AI adapters are not available on this platform yet.",
    )

    override suspend fun generateInvitationCopy(prompt: WeddingInvitationPrompt): WeddingInvitationCopy =
        LocalWeddingCopyFallback.generate(prompt)
}

internal expect fun createWeddingAiService(enabled: Boolean): WeddingAiService
