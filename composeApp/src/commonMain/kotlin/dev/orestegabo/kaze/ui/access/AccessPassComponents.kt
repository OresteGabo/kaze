package dev.orestegabo.kaze.ui.access

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.orestegabo.kaze.domain.AccessCardStyle
import dev.orestegabo.kaze.domain.DigitalAccessCard
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun StayAccessCardSection(
    card: DigitalAccessCard,
    hotelName: String,
) {
    var showDetails by remember { mutableStateOf(false) }

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
    val backgroundBrush = remember(card.style) { cardBackground(card.style) }
    val frameColor = remember(card.style) { cardFrameColor(card.style) }
    val accentColor = remember(card.style) { cardAccentColor(card.style) }

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
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawFuturisticPassPattern(accentColor = accentColor, frameColor = frameColor)
                drawGaboMark(
                    center = Offset(size.width * 0.82f, size.height * 0.78f),
                    scaleBase = size.width * 0.0019f,
                    tint = Color.White.copy(alpha = 0.22f),
                )
            }
            Box(
                modifier = Modifier.align(Alignment.TopEnd).size(148.dp).offset(x = 28.dp, y = (-18).dp).clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
            )
            Box(
                modifier = Modifier.align(Alignment.BottomStart).size(width = 180.dp, height = 82.dp).offset(x = (-34).dp, y = 24.dp)
                    .clip(RoundedCornerShape(topEnd = 88.dp, bottomEnd = 24.dp, topStart = 18.dp, bottomStart = 18.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
            )

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(hotelName.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.74f))
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
                        Text(card.title, style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        Text(card.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.82f))
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
                        Text(card.contextLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.58f))
                        Text(card.primaryAccessRef, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.94f))
                    }
                    Text("Tap for details", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.72f))
                }
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
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(28.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Access details", style = MaterialTheme.typography.titleLarge)
                        Text(hotelName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                    }
                    KazeGhostButton(label = "Close", onClick = onDismiss)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(modifier = Modifier.weight(1f)) { StaffQrPanel(card = card) }
                    Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AccessDetailLine(label = "Pass number", value = card.id.takeLast(8))
                        AccessDetailLine(label = "Profile", value = card.contextLabel)
                        AccessDetailLine(label = "Primary access", value = card.primaryAccessRef)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Linked access", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        card.linkedAccess.forEach { badge -> MetaPill(badge) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffQrPanel(card: DigitalAccessCard) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(22.dp)) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Canvas(modifier = Modifier.size(118.dp)) { drawPseudoQr(card.id) }
            Text("Staff scan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (inverse) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.12f),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
        )
    }
}

private fun cardBackground(style: AccessCardStyle): Brush = when (style) {
    AccessCardStyle.KazeDefault -> Brush.linearGradient(listOf(Color(0xFF111419), Color(0xFF18242B), Color(0xFF24404A)))
    is AccessCardStyle.HotelBranded -> Brush.linearGradient(listOf(Color(0xFF121416), style.supportHex.toUiColor(), style.accentHex.toUiColor().copy(alpha = 0.72f)))
    is AccessCardStyle.EventSignature -> Brush.linearGradient(listOf(Color(0xFF101318), Color(0xFF18262D), style.accentHex.toUiColor().copy(alpha = 0.48f)))
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFuturisticPassPattern(accentColor: Color, frameColor: Color) {
    val width = size.width
    val height = size.height
    drawLine(color = accentColor.copy(alpha = 0.26f), start = Offset(width * 0.08f, height * 0.22f), end = Offset(width * 0.72f, height * 0.22f), strokeWidth = 4f, cap = StrokeCap.Round)
    drawLine(color = accentColor.copy(alpha = 0.18f), start = Offset(width * 0.12f, height * 0.30f), end = Offset(width * 0.84f, height * 0.30f), strokeWidth = 2f, cap = StrokeCap.Round)

    val path = Path().apply {
        moveTo(width * 0.58f, height * 0.08f)
        lineTo(width * 0.75f, height * 0.08f)
        lineTo(width * 0.82f, height * 0.16f)
        lineTo(width * 0.94f, height * 0.16f)
        lineTo(width * 0.94f, height * 0.26f)
        lineTo(width * 0.82f, height * 0.26f)
        lineTo(width * 0.74f, height * 0.34f)
        lineTo(width * 0.56f, height * 0.34f)
    }
    drawPath(path = path, color = frameColor.copy(alpha = 0.55f), style = Stroke(width = 3f))

    repeat(6) { index ->
        val y = height * (0.56f + index * 0.055f)
        drawLine(
            color = if (index % 2 == 0) accentColor.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.12f),
            start = Offset(width * 0.08f, y),
            end = Offset(width * (0.28f + index * 0.08f), y),
            strokeWidth = if (index % 2 == 0) 3f else 2f,
            cap = StrokeCap.Round,
        )
    }

    drawCircle(color = accentColor.copy(alpha = 0.14f), radius = width * 0.14f, center = Offset(width * 0.82f, height * 0.78f), style = Stroke(width = 4f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPseudoQr(seed: String) {
    val modules = 21
    val cell = size.minDimension / modules
    val bg = Color.White
    val fg = Color(0xFF111111)
    drawRect(bg)
    fun drawFinder(x: Int, y: Int) {
        drawRect(color = fg, topLeft = Offset(x * cell, y * cell), size = androidx.compose.ui.geometry.Size(cell * 7, cell * 7))
        drawRect(color = bg, topLeft = Offset((x + 1) * cell, (y + 1) * cell), size = androidx.compose.ui.geometry.Size(cell * 5, cell * 5))
        drawRect(color = fg, topLeft = Offset((x + 2) * cell, (y + 2) * cell), size = androidx.compose.ui.geometry.Size(cell * 3, cell * 3))
    }
    drawFinder(0, 0); drawFinder(14, 0); drawFinder(0, 14)
    for (row in 0 until modules) {
        for (col in 0 until modules) {
            val inFinder = (row < 7 && col < 7) || (row < 7 && col >= 14) || (row >= 14 && col < 7)
            if (inFinder) continue
            val hash = seed.hashCode() + row * 31 + col * 17
            if ((hash and 3) == 0) {
                drawRect(color = fg, topLeft = Offset(col * cell, row * cell), size = androidx.compose.ui.geometry.Size(cell, cell))
            }
        }
    }
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
