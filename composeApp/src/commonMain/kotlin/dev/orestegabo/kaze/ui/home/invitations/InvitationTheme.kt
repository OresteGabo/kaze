package dev.orestegabo.kaze.ui.home.invitations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.ui.home.invitations.birthday.BirthdayJoyTheme
import dev.orestegabo.kaze.ui.home.invitations.conference.ConferenceSummitTheme
import dev.orestegabo.kaze.ui.home.invitations.wedding.WeddingBotanicalTheme
import dev.orestegabo.kaze.ui.home.invitations.wedding.WeddingRomanceTheme

internal enum class InvitationThemeCategory {
    WEDDING,
    BIRTHDAY,
    CONFERENCE,
}

internal enum class InvitationEventType(
    val label: String,
    val category: InvitationThemeCategory,
) {
    WEDDING("Wedding", InvitationThemeCategory.WEDDING),
    BIRTHDAY("Birthday", InvitationThemeCategory.BIRTHDAY),
    MEETING("Meeting", InvitationThemeCategory.CONFERENCE),
    CONFERENCE("Conference", InvitationThemeCategory.CONFERENCE),
    OTHER("Other", InvitationThemeCategory.CONFERENCE),
}

internal interface InvitationTheme {
    val id: String
    val name: String
    val category: InvitationThemeCategory
    val label: String
    val detailsTitle: String

    fun supportingText(isActive: Boolean): String

    @Composable
    fun PageBackground(modifier: Modifier)

    @Composable
    fun Cover(
        invitation: InvitationPreview,
        isActive: Boolean,
        modifier: Modifier,
    )
}

internal val availableInvitationThemes: List<InvitationTheme> = listOf(
    WeddingRomanceTheme,
    WeddingBotanicalTheme,
    BirthdayJoyTheme,
    ConferenceSummitTheme,
)

internal fun themesForEventType(eventType: InvitationEventType): List<InvitationTheme> =
    availableInvitationThemes.filter { it.category == eventType.category }

internal fun InvitationPreview.resolveInvitationTheme(): InvitationTheme {
    val searchable = "$title $subtitle $code".lowercase()
    themeId?.let { selectedThemeId ->
        availableInvitationThemes.firstOrNull { it.id == selectedThemeId }?.let { return it }
    }
    return when {
        "birthday" in searchable -> BirthdayJoyTheme
        isWeddingInvitation() && ("garden" in searchable || "botanical" in searchable) -> WeddingBotanicalTheme
        isWeddingInvitation() -> WeddingRomanceTheme
        else -> ConferenceSummitTheme
    }
}

internal fun InvitationPreview.isWeddingInvitation(): Boolean {
    val text = "$title $subtitle".lowercase()
    return "wedding" in text || " x " in text || "love" in code.lowercase()
}

internal fun InvitationPreview.weddingInitials(): Pair<String, String> {
    val names = title.split(" x ", " X ", "&").map { it.trim() }.filter { it.isNotBlank() }
    return Pair(names.getOrNull(0).orEmpty().initials(1).ifBlank { "A" }, names.getOrNull(1).orEmpty().initials(1).ifBlank { "B" })
}

internal fun String.initials(maxCount: Int): String = split(" ", "-", "_")
    .filter { it.isNotBlank() }
    .take(maxCount)
    .joinToString("") { it.first().uppercase() }
