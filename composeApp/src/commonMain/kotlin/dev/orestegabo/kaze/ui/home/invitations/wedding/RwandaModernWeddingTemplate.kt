package dev.orestegabo.kaze.ui.home.invitations.wedding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.Color

internal object RwandaModernWeddingTemplate : WeddingPhotoTemplateTheme(
    id = "wedding-rwanda-modern",
    name = "Rwanda Modern",
    label = "Modern invite",
    vibe = "local",
    palette = WeddingTemplatePalette(
        start = Color(0xFFFAF7F2),
        middle = Color(0xFFFFD9C8),
        end = Color(0xFFD9F1EA),
        accent = Color(0xFF00A36C),
        ink = Color(0xFF172B38),
    ),
    icon = Icons.Default.Favorite,
    serif = false,
    darkCover = false,
)
