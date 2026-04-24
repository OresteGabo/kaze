package dev.orestegabo.kaze.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.foundation.isSystemInDarkTheme
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

@Immutable
data class KazeAccentPalette(
    val editorialWarm: Color,
    val editorialBotanical: Color,
    val editorialClay: Color,
)

@Immutable
data class KazePassPalette(
    val cardBaseStart: Color,
    val cardBaseMiddle: Color,
    val cardBaseEnd: Color,
    val cardOverlay: Color,
    val cardOnSurface: Color,
    val cardOnSurfaceMuted: Color,
    val cardChip: Color,
    val cardChipText: Color,
)

@Immutable
data class KazeUiPalette(
    val ambientBottom: Color,
    val ambientLineStrong: Color,
    val ambientLineSoft: Color,
    val ambientCirclePrimary: Color,
    val ambientCircleSecondary: Color,
    val ambientPanelTop: Color,
    val ambientPanelBottom: Color,
    val floatingShell: Color,
    val floatingShellBorder: Color,
    val successContainerSoft: Color,
    val successContainerStrong: Color,
    val successContent: Color,
)

private val LocalBrandAssets = staticCompositionLocalOf {
    KazeBrandAssets(logoAsset = "", wordmarkAsset = "")
}

private val LocalAccentPalette = staticCompositionLocalOf {
    KazeAccentPalette(
        editorialWarm = Color(0xFFC79A52),
        editorialBotanical = Color(0xFF88A37A),
        editorialClay = Color(0xFF9A7A62),
    )
}

private val LocalPassPalette = staticCompositionLocalOf {
    KazePassPalette(
        cardBaseStart = Color(0xFF111419),
        cardBaseMiddle = Color(0xFF18242B),
        cardBaseEnd = Color(0xFF24404A),
        cardOverlay = Color(0x14FFF9F0),
        cardOnSurface = Color(0xFFFFFBF5),
        cardOnSurfaceMuted = Color(0xCCFFF8EE),
        cardChip = Color(0x2EFFF8EE),
        cardChipText = Color(0xFFFFFBF5),
    )
}

private val LocalUiPalette = staticCompositionLocalOf {
    KazeUiPalette(
        ambientBottom = Color(0xFFF0EAE0),
        ambientLineStrong = Color(0x223A6B73),
        ambientLineSoft = Color(0x1A8C6B4F),
        ambientCirclePrimary = Color(0x143A6B73),
        ambientCircleSecondary = Color(0x128C6B4F),
        ambientPanelTop = Color(0x0D3A6B73),
        ambientPanelBottom = Color(0x0A8C6B4F),
        floatingShell = Color(0xF0FFF9F0),
        floatingShellBorder = Color(0x268C6B4F),
        successContainerSoft = Color(0x1F2E8B57),
        successContainerStrong = Color(0x332E8B57),
        successContent = Color(0xFF2E8B57),
    )
}

private val LocalResolvedDarkTheme = staticCompositionLocalOf { false }

object KazeTheme {
    val hotelConfig: HotelConfig
        @Composable
        get() = LocalHotelConfig.current

    val brandAssets: KazeBrandAssets
        @Composable
        get() = LocalBrandAssets.current

    val accents: KazeAccentPalette
        @Composable
        get() = LocalAccentPalette.current

    val pass: KazePassPalette
        @Composable
        get() = LocalPassPalette.current

    val ui: KazeUiPalette
        @Composable
        get() = LocalUiPalette.current

    val isDark: Boolean
        @Composable
        get() = LocalResolvedDarkTheme.current
}

enum class KazeThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

@Composable
fun KazeTheme(
    hotelConfig: HotelConfig,
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: KazeThemeMode = KazeThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val resolvedDarkTheme = when (themeMode) {
        KazeThemeMode.SYSTEM -> darkTheme
        KazeThemeMode.LIGHT -> false
        KazeThemeMode.DARK -> true
    }
    val colorScheme = hotelConfig.branding.toColorScheme(resolvedDarkTheme)
    CompositionLocalProvider(
        LocalHotelConfig provides hotelConfig,
        LocalBrandAssets provides KazeBrandAssets(
            logoAsset = hotelConfig.branding.logoAsset,
            wordmarkAsset = hotelConfig.branding.wordmarkAsset,
        ),
        LocalAccentPalette provides KazeAccentPalette(
            editorialWarm = hotelConfig.branding.secondaryHex.toColor(),
            editorialBotanical = if (resolvedDarkTheme) hotelConfig.branding.accentHex.toColor().lighten(0.08f) else hotelConfig.branding.accentHex.toColor().copy(alpha = 0.86f),
            editorialClay = if (resolvedDarkTheme) hotelConfig.branding.primaryHex.toColor().lighten(0.14f) else hotelConfig.branding.primaryHex.toColor().copy(alpha = 0.62f),
        ),
        LocalPassPalette provides KazePassPalette(
            cardBaseStart = if (resolvedDarkTheme) hotelConfig.branding.primaryHex.toColor().darken(0.72f) else hotelConfig.branding.primaryHex.toColor().darken(0.62f),
            cardBaseMiddle = if (resolvedDarkTheme) hotelConfig.branding.primaryHex.toColor().darken(0.54f) else hotelConfig.branding.primaryHex.toColor().darken(0.42f),
            cardBaseEnd = if (resolvedDarkTheme) hotelConfig.branding.secondaryHex.toColor().darken(0.42f) else hotelConfig.branding.secondaryHex.toColor().darken(0.28f),
            cardOverlay = hotelConfig.branding.accentHex.toColor().copy(alpha = if (resolvedDarkTheme) 0.14f else 0.10f),
            cardOnSurface = Color(0xFFFFFBF5),
            cardOnSurfaceMuted = Color(0xCCFFF8EE),
            cardChip = Color(0x2EFFF8EE),
            cardChipText = Color(0xFFFFFBF5),
        ),
        LocalUiPalette provides hotelConfig.branding.toUiPalette(resolvedDarkTheme),
        LocalResolvedDarkTheme provides resolvedDarkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = hotelConfig.branding.typography.toTypography(),
            content = content,
        )
    }
}

private fun HotelBranding.toColorScheme(darkTheme: Boolean): ColorScheme {
    val primary = primaryHex.toColor()
    val secondary = secondaryHex.toColor()
    val accent = accentHex.toColor()
    val surface = surfaceHex.toColor()
    val background = backgroundHex.toColor()

    return if (darkTheme) {
        val darkBackground = background.darken(0.84f).blend(primary.darken(0.88f), 0.20f)
        val darkSurface = surface.darken(0.78f).blend(primary.darken(0.86f), 0.18f)
        val darkSurfaceVariant = darkSurface.lighten(0.08f)
        val darkOutline = darkSurfaceVariant.lighten(0.22f)
        val darkPrimary = primary.lighten(0.08f)
        val darkSecondary = secondary.lighten(0.06f)
        val darkAccent = accent.lighten(0.10f)

        darkColorScheme(
            primary = darkPrimary,
            onPrimary = darkPrimary.bestContrastingText(),
            secondary = darkSecondary,
            onSecondary = darkSecondary.bestContrastingText(),
            tertiary = darkAccent,
            onTertiary = darkAccent.bestContrastingText(),
            background = darkBackground,
            onBackground = darkBackground.bestContrastingText(),
            surface = darkSurface,
            onSurface = darkSurface.bestContrastingText(),
            primaryContainer = darkPrimary.darken(0.42f),
            onPrimaryContainer = darkPrimary.darken(0.42f).bestContrastingText(),
            secondaryContainer = darkSecondary.darken(0.48f),
            onSecondaryContainer = darkSecondary.darken(0.48f).bestContrastingText(),
            tertiaryContainer = darkAccent.darken(0.52f),
            onTertiaryContainer = darkAccent.darken(0.52f).bestContrastingText(),
            surfaceVariant = darkSurfaceVariant,
            onSurfaceVariant = darkSurfaceVariant.bestContrastingText().copy(alpha = 0.78f),
            outline = darkOutline,
        )
    } else {
        lightColorScheme(
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
            onPrimaryContainer = primary.darken(0.72f),
            secondaryContainer = secondary.copy(alpha = 0.14f),
            onSecondaryContainer = secondary.darken(0.72f),
            tertiaryContainer = accent.copy(alpha = 0.20f),
            onTertiaryContainer = accent.darken(0.72f),
            surfaceVariant = Color(0xFFF0EAE0),
            onSurfaceVariant = Color(0xFF5E5A52),
            outline = Color(0xFFD4CABB),
        )
    }
}

private fun HotelBranding.toUiPalette(darkTheme: Boolean): KazeUiPalette {
    val primary = primaryHex.toColor()
    val secondary = secondaryHex.toColor()
    val accent = accentHex.toColor()

    return if (darkTheme) {
        KazeUiPalette(
            ambientBottom = primary.darken(0.84f).blend(accent.darken(0.88f), 0.14f),
            ambientLineStrong = primary.lighten(0.18f).copy(alpha = 0.22f),
            ambientLineSoft = accent.lighten(0.18f).copy(alpha = 0.18f),
            ambientCirclePrimary = primary.lighten(0.16f).copy(alpha = 0.12f),
            ambientCircleSecondary = accent.lighten(0.14f).copy(alpha = 0.11f),
            ambientPanelTop = primary.lighten(0.12f).copy(alpha = 0.08f),
            ambientPanelBottom = accent.lighten(0.12f).copy(alpha = 0.07f),
            floatingShell = surfaceHex.toColor().darken(0.76f),
            floatingShellBorder = secondary.lighten(0.08f).copy(alpha = 0.26f),
            successContainerSoft = Color(0x1F66C38C),
            successContainerStrong = Color(0x3366C38C),
            successContent = Color(0xFF8EDFAE),
        )
    } else {
        KazeUiPalette(
            ambientBottom = Color(0xFFF0EAE0),
            ambientLineStrong = primary.copy(alpha = 0.13f),
            ambientLineSoft = accent.copy(alpha = 0.10f),
            ambientCirclePrimary = primary.copy(alpha = 0.08f),
            ambientCircleSecondary = accent.copy(alpha = 0.07f),
            ambientPanelTop = primary.copy(alpha = 0.035f),
            ambientPanelBottom = accent.copy(alpha = 0.028f),
            floatingShell = surfaceHex.toColor(),
            floatingShellBorder = secondary.copy(alpha = 0.20f),
            successContainerSoft = Color(0x1F2E8B57),
            successContainerStrong = Color(0x332E8B57),
            successContent = Color(0xFF2E8B57),
        )
    }
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

private fun Color.darken(factor: Float): Color = Color(
    red = red * (1f - factor),
    green = green * (1f - factor),
    blue = blue * (1f - factor),
    alpha = alpha,
)

private fun Color.lighten(factor: Float): Color = Color(
    red = red + ((1f - red) * factor),
    green = green + ((1f - green) * factor),
    blue = blue + ((1f - blue) * factor),
    alpha = alpha,
)

private fun Color.blend(other: Color, ratio: Float): Color = Color(
    red = red * (1f - ratio) + other.red * ratio,
    green = green * (1f - ratio) + other.green * ratio,
    blue = blue * (1f - ratio) + other.blue * ratio,
    alpha = alpha * (1f - ratio) + other.alpha * ratio,
)
