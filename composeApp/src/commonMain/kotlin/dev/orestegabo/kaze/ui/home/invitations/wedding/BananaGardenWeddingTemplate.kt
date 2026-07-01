package dev.orestegabo.kaze.ui.home.invitations.wedding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.ui.graphics.Color

internal object BananaGardenWeddingTemplate : WeddingPhotoTemplateTheme(
    id = "wedding-banana-garden",
    name = "Banana Garden",
    label = "Garden invite",
    vibe = "outdoor",
    palette = WeddingTemplatePalette(
        start = Color(0xFFEAF6EC),
        middle = Color(0xFFCFE7D6),
        end = Color(0xFFFFF9EF),
        accent = Color(0xFF2F7A4C),
        ink = Color(0xFF173527),
    ),
    icon = Icons.Default.LocalFlorist,
    serif = false,
    darkCover = false,
)
