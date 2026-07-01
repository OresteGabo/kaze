package dev.orestegabo.kaze.ui.home.invitations.funeral

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.home.invitations.InvitationTheme
import dev.orestegabo.kaze.ui.home.invitations.InvitationThemeCategory

internal object FuneralMemorialTheme : FuneralTemplateTheme(
    id = "funeral-memorial",
    name = "Memorial",
    label = "Funeral notice",
    start = Color(0xFFF8F6F1),
    middle = Color(0xFFD9D2C6),
    end = Color(0xFF3A3430),
    accent = Color(0xFF7D756B),
    ink = Color(0xFF27231F),
    icon = Icons.Default.Church,
    darkCover = false,
)

internal object FuneralPhotoMemoryTheme : FuneralTemplateTheme(
    id = "funeral-photo-memory",
    name = "Photo Memory",
    label = "Memorial photo",
    start = Color(0xFF181A1F),
    middle = Color(0xFF444A54),
    end = Color(0xFFE7E1D6),
    accent = Color(0xFFB8AA94),
    ink = Color.White,
    icon = Icons.Default.PhotoCamera,
    darkCover = true,
)

internal open class FuneralTemplateTheme(
    override val id: String,
    override val name: String,
    override val label: String,
    private val start: Color,
    private val middle: Color,
    private val end: Color,
    private val accent: Color,
    private val ink: Color,
    private val icon: ImageVector,
    private val darkCover: Boolean,
) : InvitationTheme {
    override val category = InvitationThemeCategory.FUNERAL
    override val detailsTitle = "Funeral invitation"

    override fun supportingText(isActive: Boolean): String =
        if (isActive) "A respectful invitation for family and friends to gather in memory." else "Saved memorial details."

    @Composable
    override fun PageBackground(modifier: Modifier) {
        Box(
            modifier = modifier.background(
                Brush.verticalGradient(
                    listOf(
                        start.copy(alpha = 0.86f),
                        end.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = accent.copy(alpha = 0.12f),
                    radius = size.minDimension * 0.38f,
                    center = Offset(size.width * 0.86f, size.height * 0.12f),
                )
                drawCircle(
                    color = middle.copy(alpha = 0.10f),
                    radius = size.minDimension * 0.28f,
                    center = Offset(size.width * 0.14f, size.height * 0.82f),
                )
            }
        }
    }

    @Composable
    override fun Cover(invitation: InvitationPreview, isActive: Boolean, modifier: Modifier) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(276.dp)
                    .background(Brush.linearGradient(listOf(start, middle, end)))
                    .padding(22.dp),
            ) {
                FuneralPhotoFrame(accent = accent, ink = ink, darkCover = darkCover, modifier = Modifier.matchParentSize())
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        MetaPill(label = label, leadingIcon = icon, containerColor = ink.copy(alpha = 0.12f), textColor = ink)
                        if (invitation.code.isNotBlank()) {
                            MetaPill(label = invitation.code, leadingIcon = Icons.Default.VpnKey, containerColor = ink.copy(alpha = 0.12f), textColor = ink)
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(82.dp),
                            shape = CircleShape,
                            color = accent.copy(alpha = if (darkCover) 0.36f else 0.16f),
                            border = BorderStroke(1.dp, ink.copy(alpha = 0.22f)),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = ink.copy(alpha = 0.86f), modifier = Modifier.size(32.dp))
                            }
                        }
                        Text(
                            invitation.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Serif,
                            color = ink,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            invitation.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ink.copy(alpha = 0.72f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Text(
                        "Photo can be added",
                        style = MaterialTheme.typography.labelLarge,
                        color = ink.copy(alpha = 0.70f),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun FuneralPhotoFrame(
    accent: Color,
    ink: Color,
    darkCover: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = accent.copy(alpha = if (darkCover) 0.26f else 0.14f),
            radius = size.minDimension * 0.32f,
            center = Offset(size.width * 0.50f, size.height * 0.52f),
        )
        drawLine(
            color = ink.copy(alpha = 0.22f),
            start = Offset(size.width * 0.24f, size.height * 0.76f),
            end = Offset(size.width * 0.76f, size.height * 0.76f),
            strokeWidth = 2f,
        )
    }
}
