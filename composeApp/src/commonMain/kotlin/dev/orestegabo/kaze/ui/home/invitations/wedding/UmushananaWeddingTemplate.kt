package dev.orestegabo.kaze.ui.home.invitations.wedding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.Color

internal object UmushananaWeddingTemplate : WeddingPhotoTemplateTheme(
    id = "wedding-umushanana",
    name = "Umushanana",
    label = "Traditional invite",
    vibe = "heritage",
    palette = WeddingTemplatePalette(
        start = Color(0xFFFFF7E8),
        middle = Color(0xFFEAC37A),
        end = Color(0xFF6E1F32),
        accent = Color(0xFFB6424F),
        ink = Color(0xFF2E191D),
    ),
    icon = Icons.Default.Favorite,
    serif = true,
    darkCover = false,
)
