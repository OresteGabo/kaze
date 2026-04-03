package dev.orestegabo.kaze.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.orestegabo.kaze.domain.HotelConfig
import dev.orestegabo.kaze.domain.HotelBranding
import dev.orestegabo.kaze.domain.TypographySpec

private val LocalHotelConfig = staticCompositionLocalOf<HotelConfig> {
    error("HotelConfig was not provided to KazeTheme")
}

@Immutable
data class KazeBrandAssets(
    val logoAsset: String,
    val wordmarkAsset: String,
)

private val LocalBrandAssets = staticCompositionLocalOf {
    KazeBrandAssets(logoAsset = "", wordmarkAsset = "")
}

object KazeTheme {
    val hotelConfig: HotelConfig
        @Composable
        get() = LocalHotelConfig.current

    val brandAssets: KazeBrandAssets
        @Composable
        get() = LocalBrandAssets.current
}

@Composable
fun KazeTheme(
    hotelConfig: HotelConfig,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalHotelConfig provides hotelConfig,
        LocalBrandAssets provides KazeBrandAssets(
            logoAsset = hotelConfig.branding.logoAsset,
            wordmarkAsset = hotelConfig.branding.wordmarkAsset,
        ),
    ) {
        MaterialTheme(
            colorScheme = hotelConfig.branding.toColorScheme(),
            typography = hotelConfig.branding.typography.toTypography(),
            content = content,
        )
    }
}

private fun HotelBranding.toColorScheme(): ColorScheme {
    val primary = primaryHex.toColor()
    val secondary = secondaryHex.toColor()
    val accent = accentHex.toColor()
    val surface = surfaceHex.toColor()
    val background = backgroundHex.toColor()

    return lightColorScheme(
        primary = primary,
        onPrimary = primary.bestContrastingText(),
        secondary = secondary,
        onSecondary = secondary.bestContrastingText(),
        tertiary = accent,
        onTertiary = accent.bestContrastingText(),
        surface = surface,
        onSurface = surface.bestContrastingText(),
        background = background,
        onBackground = background.bestContrastingText(),
        primaryContainer = primary.copy(alpha = 0.12f),
        onPrimaryContainer = Color(0xFF183236),
        secondaryContainer = secondary.copy(alpha = 0.14f),
        onSecondaryContainer = Color(0xFF4C3317),
        tertiaryContainer = accent.copy(alpha = 0.20f),
        onTertiaryContainer = Color(0xFF5A4930),
        surfaceVariant = Color(0xFFF0EAE0),
        onSurfaceVariant = Color(0xFF5E5A52),
        outline = Color(0xFFD4CABB),
    )
}

private fun TypographySpec.toTypography(): Typography {
    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontSize = base.displayLarge.fontSize * headingScale),
        headlineMedium = base.headlineMedium.copy(fontSize = base.headlineMedium.fontSize * headingScale),
        titleLarge = base.titleLarge.copy(fontSize = base.titleLarge.fontSize * headingScale),
        bodyLarge = base.bodyLarge.copy(fontSize = base.bodyLarge.fontSize * bodyScale),
        bodyMedium = base.bodyMedium.copy(fontSize = base.bodyMedium.fontSize * bodyScale),
        labelLarge = base.labelLarge.copy(fontSize = base.labelLarge.fontSize * labelScale),
    )
}

private fun String.toColor(): Color {
    val sanitized = removePrefix("#")
    val raw = sanitized.toLong(16)
    val argb = if (sanitized.length <= 6) {
        0xFF000000 or raw
    } else {
        raw
    }
    return Color(argb)
}

private fun Color.bestContrastingText(): Color {
    val luminance = (0.299f * red) + (0.587f * green) + (0.114f * blue)
    return if (luminance > 0.55f) Color(0xFF1A1712) else Color(0xFFFFFBF5)
}
