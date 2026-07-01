package dev.orestegabo.kaze.ui.home.invitations.wedding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.graphics.Color

internal object LuxuryNightWeddingTemplate : WeddingPhotoTemplateTheme(
    id = "wedding-luxury-night",
    name = "Luxury Night",
    label = "Evening invite",
    vibe = "premium",
    palette = WeddingTemplatePalette(
        start = Color(0xFF09090C),
        middle = Color(0xFF24202A),
        end = Color(0xFFB18B55),
        accent = Color(0xFFD7B56D),
        ink = Color.White,
    ),
    icon = Icons.Default.AutoAwesome,
    serif = true,
    darkCover = true,
)
