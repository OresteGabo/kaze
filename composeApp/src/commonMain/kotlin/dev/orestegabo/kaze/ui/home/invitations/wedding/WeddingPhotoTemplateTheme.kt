package dev.orestegabo.kaze.ui.home.invitations.wedding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.ui.components.MetaPill
import dev.orestegabo.kaze.ui.home.invitations.InvitationTemplateContract
import dev.orestegabo.kaze.ui.home.invitations.InvitationTemplateKind
import dev.orestegabo.kaze.ui.home.invitations.InvitationThemeCategory
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.wedding_example_hero
import org.jetbrains.compose.resources.painterResource

internal data class WeddingTemplatePalette(
    val start: Color,
    val middle: Color,
    val end: Color,
    val accent: Color,
    val ink: Color,
)

internal abstract class WeddingPhotoTemplateTheme(
    override val id: String,
    override val name: String,
    override val label: String,
    private val vibe: String,
    private val palette: WeddingTemplatePalette,
    private val icon: ImageVector,
    private val serif: Boolean,
    private val darkCover: Boolean,
) : InvitationTemplateContract {
    override val category = InvitationThemeCategory.WEDDING
    override val detailsTitle = "Wedding invitation"
    override val templateKind = InvitationTemplateKind.PHOTO
    override val acceptsPhoto = true
    override val previewAspectRatio = 0.72f

    override fun supportingText(isActive: Boolean): String =
        if (isActive) "Photo-ready wedding template with RSVP and venue details." else "Saved wedding invitation template."

    @Composable
    override fun PageBackground(modifier: Modifier) {
        Box(
            modifier = modifier.background(
                Brush.verticalGradient(
                    listOf(
                        palette.start.copy(alpha = 0.84f),
                        palette.end.copy(alpha = 0.72f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = palette.accent.copy(alpha = 0.12f),
                    radius = size.minDimension * 0.44f,
                    center = Offset(size.width * 0.88f, size.height * 0.12f),
                )
                drawCircle(
                    color = palette.middle.copy(alpha = 0.16f),
                    radius = size.minDimension * 0.34f,
                    center = Offset(size.width * 0.08f, size.height * 0.84f),
                )
            }
        }
    }

    @Composable
    override fun Cover(invitation: InvitationPreview, isActive: Boolean, modifier: Modifier) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, palette.accent.copy(alpha = 0.24f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(284.dp)
                    .background(Brush.linearGradient(listOf(palette.start, palette.middle, palette.end)))
            ) {
                Image(
                    painter = painterResource(Res.drawable.wedding_example_hero),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    palette.start.copy(alpha = if (darkCover) 0.30f else 0.58f),
                                    palette.middle.copy(alpha = if (darkCover) 0.42f else 0.50f),
                                    Color.Black.copy(alpha = if (darkCover) 0.72f else 0.40f),
                                ),
                            ),
                        ),
                )
                TemplatePhotoSlot(
                    palette = palette,
                    darkCover = darkCover,
                    modifier = Modifier.matchParentSize(),
                )
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        MetaPill(
                            label = label,
                            leadingIcon = icon,
                            containerColor = palette.ink.copy(alpha = if (darkCover) 0.14f else 0.08f),
                            textColor = if (darkCover) Color.White else palette.ink,
                        )
                        if (invitation.code.isNotBlank()) {
                            MetaPill(
                                label = invitation.code,
                                leadingIcon = Icons.Default.VpnKey,
                                containerColor = palette.ink.copy(alpha = if (darkCover) 0.14f else 0.08f),
                                textColor = if (darkCover) Color.White else palette.ink,
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(86.dp),
                            shape = CircleShape,
                            color = palette.accent.copy(alpha = if (darkCover) 0.42f else 0.18f),
                            border = BorderStroke(1.dp, palette.ink.copy(alpha = 0.24f)),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = if (darkCover) Color.White else palette.accent, modifier = Modifier.size(34.dp))
                            }
                        }
                        Text(
                            invitation.title,
                            style = if (serif) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                            fontFamily = if (serif) FontFamily.Serif else FontFamily.Default,
                            fontWeight = if (serif) FontWeight.Normal else FontWeight.Black,
                            color = if (darkCover) Color.White else palette.ink,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            invitation.subtitle.ifBlank { vibe },
                            style = MaterialTheme.typography.bodyMedium,
                            color = (if (darkCover) Color.White else palette.ink).copy(alpha = 0.76f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Text(
                        "Photo cutout ready",
                        style = MaterialTheme.typography.labelLarge,
                        color = (if (darkCover) Color.White else palette.ink).copy(alpha = 0.72f),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplatePhotoSlot(
    palette: WeddingTemplatePalette,
    darkCover: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = palette.accent.copy(alpha = if (darkCover) 0.28f else 0.12f),
            radius = size.minDimension * 0.34f,
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            style = Stroke(width = 5f),
        )
        drawCircle(
            color = palette.ink.copy(alpha = if (darkCover) 0.16f else 0.08f),
            radius = size.minDimension * 0.28f,
            center = Offset(size.width * 0.38f, size.height * 0.58f),
        )
        drawCircle(
            color = palette.accent.copy(alpha = if (darkCover) 0.20f else 0.12f),
            radius = size.minDimension * 0.22f,
            center = Offset(size.width * 0.68f, size.height * 0.24f),
        )
    }
}
