package dev.orestegabo.kaze.ui.home.invitations.wedding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.graphics.Color

internal object MinimalPhotoWeddingTemplate : WeddingPhotoTemplateTheme(
    id = "wedding-photo-minimal",
    name = "Minimal Photo",
    label = "Photo invite",
    vibe = "photo-led",
    palette = WeddingTemplatePalette(
        start = Color(0xFF1D1718),
        middle = Color(0xFF74524D),
        end = Color(0xFFF8EFEA),
        accent = Color(0xFFE8A3B8),
        ink = Color.White,
    ),
    icon = Icons.Default.PhotoCamera,
    serif = true,
    darkCover = true,
)
