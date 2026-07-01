package dev.orestegabo.kaze.ui.home.invitations.wedding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.graphics.Color

internal object LakeKivuWeddingTemplate : WeddingPhotoTemplateTheme(
    id = "wedding-lake-kivu",
    name = "Lake Kivu",
    label = "Destination invite",
    vibe = "travel",
    palette = WeddingTemplatePalette(
        start = Color(0xFFEAF7FF),
        middle = Color(0xFF8CD1E6),
        end = Color(0xFFFFF2DA),
        accent = Color(0xFF136F86),
        ink = Color(0xFF102A38),
    ),
    icon = Icons.Default.AutoAwesome,
    serif = false,
    darkCover = false,
)
