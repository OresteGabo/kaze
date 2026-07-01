package dev.orestegabo.kaze.ui.home.invitations.wedding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Church
import androidx.compose.ui.graphics.Color

internal object ClassicPaperWeddingTemplate : WeddingPhotoTemplateTheme(
    id = "wedding-classic-paper",
    name = "Classic Paper",
    label = "Paper invite",
    vibe = "formal",
    palette = WeddingTemplatePalette(
        start = Color(0xFFFFFBF5),
        middle = Color(0xFFF3E3D6),
        end = Color(0xFFFFFDFA),
        accent = Color(0xFF9D6B5D),
        ink = Color(0xFF2D2524),
    ),
    icon = Icons.Default.Church,
    serif = true,
    darkCover = false,
)
