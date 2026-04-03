package dev.orestegabo.kaze.ui.access

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.orestegabo.kaze.domain.AccessCardStyle
import dev.orestegabo.kaze.domain.DigitalAccessCard
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun StayAccessCardSection(
    card: DigitalAccessCard,
) {
    var showDetails by remember { mutableStateOf(false) }
    val hotelName = KazeTheme.hotelConfig.displayName

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Access pass", style = MaterialTheme.typography.titleMedium)
        Text(
            "One signature card can carry room, event, dining, wellness, or day-visitor access. Tap to reveal what is linked to it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )
        SignatureStayCard(card = card, hotelName = hotelName, onClick = { showDetails = true })
        if (showDetails) {
            AccessCardDialog(card = card, hotelName = hotelName, onDismiss = { showDetails = false })
        }
    }
}

@Composable
private fun SignatureStayCard(
    card: DigitalAccessCard,
    hotelName: String,
    onClick: () -> Unit,
) {
    val passPalette = KazeTheme.pass
    val backgroundBrush = remember(card.style, passPalette) { cardBackground(card.style, passPalette) }
    val frameColor = remember(card.style) { cardFrameColor(card.style) }
    val accentColor = remember(card.style) { cardAccentColor(card.style) }
    val onCard = passPalette.cardOnSurface
    val onCardMuted = passPalette.cardOnSurfaceMuted

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush)
                .border(1.dp, frameColor, RoundedCornerShape(34.dp))
                .padding(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(132.dp)
                    .offset(x = 16.dp, y = 20.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(108.dp)) {
                    drawGaboMark(
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        scaleBase = size.width * 0.0078f,
                        tint = onCard.copy(alpha = 0.42f),
                    )
                }
            }
            Box(
                modifier = Modifier.align(Alignment.BottomStart).size(width = 180.dp, height = 82.dp).offset(x = (-34).dp, y = 24.dp)
                    .clip(RoundedCornerShape(topEnd = 88.dp, bottomEnd = 24.dp, topStart = 18.dp, bottomStart = 18.dp))
                    .background(passPalette.cardOverlay),
            )

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(hotelName.uppercase(), style = MaterialTheme.typography.labelSmall, color = onCardMuted.copy(alpha = 0.9f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            when (val style = card.style) {
                                AccessCardStyle.KazeDefault -> "Kaze Access"
                                is AccessCardStyle.HotelBranded -> style.headline
                                is AccessCardStyle.EventSignature -> style.eventLabel
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = accentColor,
                        )
                        Text(card.title, style = MaterialTheme.typography.headlineLarge, color = onCard)
                        Text(card.subtitle, style = MaterialTheme.typography.bodyMedium, color = onCardMuted)
                    }
                    CardChip(text = card.id.takeLast(8), inverse = true)
                }

                Spacer(Modifier.height(58.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(card.contextLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = onCardMuted.copy(alpha = 0.78f))
                        Text(card.primaryAccessRef, style = MaterialTheme.typography.titleMedium, color = onCard)
                    }
                    Text("Tap for details", style = MaterialTheme.typography.labelMedium, color = onCardMuted.copy(alpha = 0.88f))
                }
            }

            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .padding(vertical = 10.dp),
            ) {
                drawFuturisticPassPattern(
                    accentColor = accentColor,
                    frameColor = frameColor,
                    onSurfaceColor = passPalette.cardOnSurface,
                )
            }
        }
    }
}

@Composable
private fun AccessCardDialog(
    card: DigitalAccessCard,
    hotelName: String,
    onDismiss: () -> Unit,
) {
    val accentColor = remember(card.style) { cardAccentColor(card.style) }

    Dialog(onDismissRequest = onDismiss) {
        // Main Container: Using a subtle blur-effect simulation
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header: More elegant and centered
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp, 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Access Details",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = hotelName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = accentColor
                    )
                }

                // QR Section: Modern "Staff Key" UI
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(accentColor.copy(alpha = 0.05f))
                        .border(1.dp, accentColor.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // QR Code Mini-Panel
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Staff Verification", style = MaterialTheme.typography.labelLarge)
                            Text(
                                "Present this for identity verification or room billing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Data Grid: cleaner lines
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccessDetailRow("Pass ID", card.id.takeLast(8))
                    AccessDetailRow("Status", card.contextLabel)
                    AccessDetailRow("Primary", card.primaryAccessRef)
                }

                // Linked Access: Make these pop
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Included Privileges",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        card.linkedAccess.forEach { badge ->
                            // Custom Pill with slight accent tint
                            MetaPill(badge)
                        }
                    }
                }

                KazeGhostButton(
                    label = "Dismiss",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AccessDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StaffQrPanel() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = "Staff scan",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp),
                )
            }
            Text("Staff scan", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Hotel staff can scan this access pass to open the guest profile.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
            )
        }
    }
}

@Composable
private fun AccessDetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun CardChip(text: String, inverse: Boolean) {
    val passPalette = KazeTheme.pass
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (inverse) passPalette.cardChip else passPalette.cardChip.copy(alpha = 0.72f),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = passPalette.cardChipText,
        )
    }
}

private fun cardBackground(style: AccessCardStyle, passPalette: dev.orestegabo.kaze.theme.KazePassPalette): Brush = when (style) {
    AccessCardStyle.KazeDefault -> Brush.linearGradient(
        listOf(passPalette.cardBaseStart, passPalette.cardBaseMiddle, passPalette.cardBaseEnd),
    )
    is AccessCardStyle.HotelBranded -> Brush.linearGradient(
        listOf(passPalette.cardBaseStart, style.supportHex.toUiColor(), style.accentHex.toUiColor().copy(alpha = 0.72f)),
    )
    is AccessCardStyle.EventSignature -> Brush.linearGradient(
        listOf(passPalette.cardBaseStart, passPalette.cardBaseMiddle, style.accentHex.toUiColor().copy(alpha = 0.48f)),
    )
}

private fun cardFrameColor(style: AccessCardStyle): Color = when (style) {
    AccessCardStyle.KazeDefault -> Color(0x334FA6B8)
    is AccessCardStyle.HotelBranded -> style.accentHex.toUiColor().copy(alpha = 0.44f)
    is AccessCardStyle.EventSignature -> style.accentHex.toUiColor().copy(alpha = style.patternOpacity + 0.2f)
}

private fun cardAccentColor(style: AccessCardStyle): Color = when (style) {
    AccessCardStyle.KazeDefault -> Color(0xFF9EC3CC)
    is AccessCardStyle.HotelBranded -> style.accentHex.toUiColor().copy(alpha = 0.86f)
    is AccessCardStyle.EventSignature -> style.accentHex.toUiColor().copy(alpha = 0.82f)
}

private fun String.toUiColor(): Color {
    val sanitized = removePrefix("#")
    val raw = sanitized.toLong(16)
    val argb = if (sanitized.length <= 6) 0xFF000000 or raw else raw
    return Color(argb)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFuturisticPassPattern(
    accentColor: Color,
    frameColor: Color,
    onSurfaceColor: Color,
) {
    val width = size.width
    val height = size.height
    drawLine(color = accentColor.copy(alpha = 0.18f), start = Offset(width * 0.08f, height * 0.12f), end = Offset(width * 0.72f, height * 0.12f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(color = accentColor.copy(alpha = 0.12f), start = Offset(width * 0.12f, height * 0.16f), end = Offset(width * 0.88f, height * 0.16f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(color = onSurfaceColor.copy(alpha = 0.10f), start = Offset(width * 0.18f, height * 0.70f), end = Offset(width * 0.84f, height * 0.70f), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(color = accentColor.copy(alpha = 0.16f), start = Offset(width * 0.14f, height * 0.76f), end = Offset(width * 0.62f, height * 0.76f), strokeWidth = 5f, cap = StrokeCap.Round)

    val topPath = Path().apply {
        moveTo(width * 0.62f, height * 0.08f)
        lineTo(width * 0.76f, height * 0.08f)
        lineTo(width * 0.82f, height * 0.13f)
        lineTo(width * 0.93f, height * 0.13f)
        lineTo(width * 0.93f, height * 0.20f)
        lineTo(width * 0.82f, height * 0.20f)
        lineTo(width * 0.75f, height * 0.26f)
        lineTo(width * 0.58f, height * 0.26f)
    }
    drawPath(path = topPath, color = frameColor.copy(alpha = 0.48f), style = Stroke(width = 3f))

    val bottomPath = Path().apply {
        moveTo(width * 0.10f, height * 0.84f)
        lineTo(width * 0.26f, height * 0.84f)
        lineTo(width * 0.33f, height * 0.79f)
        lineTo(width * 0.48f, height * 0.79f)
        lineTo(width * 0.48f, height * 0.86f)
        lineTo(width * 0.34f, height * 0.86f)
        lineTo(width * 0.27f, height * 0.91f)
        lineTo(width * 0.12f, height * 0.91f)
    }
    drawPath(path = bottomPath, color = onSurfaceColor.copy(alpha = 0.10f), style = Stroke(width = 3f))

    repeat(7) { index ->
        val y = height * (0.22f + index * 0.045f)
        drawLine(
            color = if (index % 2 == 0) accentColor.copy(alpha = 0.16f) else onSurfaceColor.copy(alpha = 0.08f),
            start = Offset(width * 0.11f, y),
            end = Offset(width * (0.36f + index * 0.06f), y),
            strokeWidth = if (index % 2 == 0) 3f else 2f,
            cap = StrokeCap.Round,
        )
    }

    drawCircle(
        color = accentColor.copy(alpha = 0.18f),
        radius = width * 0.125f,
        center = Offset(width * 0.86f, height * 0.82f),
        style = Stroke(width = 4f),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGaboMark(center: Offset, scaleBase: Float, tint: Color) {
    val originX = center.x - 52f * scaleBase
    val originY = center.y - 50f * scaleBase
    val scale = scaleBase
    val monogram = Path().apply {
        moveTo(originX + 70f * scale, originY + 28f * scale)
        lineTo(originX + 42f * scale, originY + 28f * scale)
        lineTo(originX + 25f * scale, originY + 50f * scale)
        lineTo(originX + 42f * scale, originY + 72f * scale)
        lineTo(originX + 70f * scale, originY + 72f * scale)
        moveTo(originX + 58f * scale, originY + 50f * scale)
        lineTo(originX + 80f * scale, originY + 50f * scale)
    }
    drawPath(path = monogram, color = tint, style = Stroke(width = 9f * scale, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawCircle(color = tint, radius = 4.5f * scale, center = Offset(originX + 80f * scale, originY + 50f * scale))
}
