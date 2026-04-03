package dev.orestegabo.kaze.domain

data class DigitalAccessCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val contextLabel: String,
    val primaryAccessRef: String,
    val linkedAccess: List<String> = emptyList(),
    val style: AccessCardStyle = AccessCardStyle.KazeDefault,
)

sealed interface AccessCardStyle {
    data object KazeDefault : AccessCardStyle

    data class HotelBranded(
        val headline: String,
        val accentHex: String,
        val supportHex: String,
    ) : AccessCardStyle

    data class EventSignature(
        val eventLabel: String,
        val accentHex: String,
        val patternOpacity: Float = 0.16f,
    ) : AccessCardStyle
}
