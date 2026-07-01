package dev.orestegabo.kaze.ui.home.invitations.wedding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.ui.graphics.Color

internal object RoseGlassWeddingTemplate : WeddingPhotoTemplateTheme(
    id = "wedding-rose-glass",
    name = "Rose Glass",
    label = "Soft invite",
    vibe = "romantic",
    palette = WeddingTemplatePalette(
        start = Color(0xFFFFEEF4),
        middle = Color(0xFFE8A3B8),
        end = Color(0xFFFFF8FA),
        accent = Color(0xFFC85B7E),
        ink = Color(0xFF3A2430),
    ),
    icon = Icons.Default.CameraAlt,
    serif = false,
    darkCover = false,
)
