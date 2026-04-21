package dev.orestegabo.kaze.ui.events

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.wedding_example_hero
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun WeddingEventShowcaseScreen(
    invitation: InvitationPreview,
    onViewVenue: () -> Unit,
    bottomContentPadding: Dp = 20.dp,
) {
    val colors = MaterialTheme.colorScheme
    val ui = KazeTheme.ui

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.background,
                        ui.ambientBottom,
                        colors.surface,
                    ),
                ),
            ),
        contentPadding = PaddingValues(bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { WeddingHero(invitation = invitation) }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                WeddingPassLetter(invitation = invitation)
                WeddingTimelineSection()
                WeddingGuestsSection()
                WeddingVenueSection(onViewVenue = onViewVenue)
            }
        }
    }
}

@Composable
private fun WeddingHero(invitation: InvitationPreview) {
    val colors = MaterialTheme.colorScheme
    val pass = KazeTheme.pass
    val accents = KazeTheme.accents

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(560.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.wedding_example_hero),
            contentDescription = "Wedding hero",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.14f),
                            pass.cardBaseMiddle.copy(alpha = 0.42f),
                            pass.cardBaseStart.copy(alpha = 0.96f),
                        ),
                    ),
                ),
        )
        RomanticHeartsLayer(
            modifier = Modifier.matchParentSize(),
            heartCount = 16,
            tint = accents.editorialWarm.copy(alpha = 0.88f),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = colors.tertiary.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            ) {
                Text(
                    "Wedding day",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onTertiary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                invitation.title,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = pass.cardOnSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                "Garden vows in Kigali",
                style = MaterialTheme.typography.titleLarge,
                color = pass.cardOnSurfaceMuted,
                textAlign = TextAlign.Center,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetaPill(
                    label = "28 June 2026",
                    leadingIcon = Icons.Default.Schedule,
                    containerColor = colors.primary.copy(alpha = 0.22f),
                    textColor = colors.onPrimary,
                )
                MetaPill(
                    label = "Nyarutarama Garden",
                    leadingIcon = Icons.Default.Map,
                    containerColor = colors.tertiary.copy(alpha = 0.24f),
                    textColor = colors.onTertiary,
                )
            }
        }
    }
}

@Composable
private fun WeddingPassLetter(invitation: InvitationPreview) {
    val colors = MaterialTheme.colorScheme
    val pass = KazeTheme.pass

    Surface(
        shape = RoundedCornerShape(34.dp),
        color = colors.surface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.14f)),
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                "With love",
                style = MaterialTheme.typography.labelLarge,
                color = colors.tertiary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Join the vows, stay for the toast, and dance into the evening.",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Your pass keeps the key details close without making the day feel technical.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface.copy(alpha = 0.68f),
            )
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, pass.cardOverlay.copy(alpha = 0.95f)),
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    pass.cardBaseStart,
                                    pass.cardBaseMiddle,
                                    pass.cardBaseEnd,
                                ),
                            ),
                            RoundedCornerShape(30.dp),
                        )
                        .padding(20.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(120.dp)
                            .offset(x = 20.dp, y = 18.dp)
                            .clip(CircleShape)
                            .background(pass.cardOverlay),
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            "Kaze Pass",
                            style = MaterialTheme.typography.labelLarge,
                            color = pass.cardOnSurfaceMuted,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            invitation.code,
                            style = MaterialTheme.typography.displaySmall,
                            color = pass.cardOnSurface,
                            fontWeight = FontWeight.Black,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MetaPill(
                                label = "Ceremony",
                                leadingIcon = Icons.Default.Schedule,
                                containerColor = pass.cardChip,
                                textColor = pass.cardChipText,
                            )
                            MetaPill(
                                label = "Reception",
                                leadingIcon = Icons.Default.Schedule,
                                containerColor = pass.cardChip,
                                textColor = pass.cardChipText,
                            )
                            MetaPill(
                                label = "Venue",
                                leadingIcon = Icons.Default.Map,
                                containerColor = pass.cardChip,
                                textColor = pass.cardChipText,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeddingTimelineSection() {
    val colors = MaterialTheme.colorScheme

    WeddingSectionShell(
        eyebrow = "Day flow",
        title = "From vows to after glow",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            weddingMoments.forEach { moment ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.primaryContainer,
                    ) {
                        Text(
                            moment.time,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            moment.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            moment.detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface.copy(alpha = 0.70f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeddingGuestsSection() {
    val colors = MaterialTheme.colorScheme

    WeddingSectionShell(
        eyebrow = "People",
        title = "Guests around you",
        trailing = {
            Text(
                "184 invited",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.62f),
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            featuredWeddingGuests.forEach { guest ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colors.surfaceVariant.copy(alpha = 0.56f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(shape = CircleShape, color = colors.tertiaryContainer) {
                            Box(
                                modifier = Modifier.size(46.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    guest.initials,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                guest.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onSurface,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                guest.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.62f),
                            )
                        }
                        MetaPill(
                            label = guest.status,
                            containerColor = colors.secondaryContainer,
                            textColor = colors.onSecondaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeddingVenueSection(onViewVenue: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    WeddingSectionShell(
        eyebrow = "Place",
        title = "Nyarutarama Garden",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(
                painter = painterResource(Res.drawable.wedding_example_hero),
                contentDescription = "Venue mood",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(28.dp)),
                contentScale = ContentScale.Crop,
            )
            Text(
                "Outdoor vows, floral aisles, soft sunset light, and a reception that still feels intimate in every screenshot.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface.copy(alpha = 0.72f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KazePrimaryButton(
                    label = "View venue",
                    onClick = onViewVenue,
                    leadingIcon = Icons.Default.Map,
                    modifier = Modifier.weight(1f),
                )
                KazeSecondaryButton(
                    label = "Guest list",
                    onClick = {},
                    leadingIcon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WeddingSectionShell(
    eyebrow: String,
    title: String,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(34.dp),
        color = colors.surface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        eyebrow,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.tertiary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
                trailing?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun RomanticHeartsLayer(
    modifier: Modifier = Modifier,
    heartCount: Int,
    tint: Color,
) {
    BoxWithConstraints(modifier = modifier) {
        weddingHeartSpecs.take(heartCount).forEachIndexed { index, spec ->
            val transition = rememberInfiniteTransition(label = "wedding-heart-$index")
            val progress = transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = spec.durationMillis,
                        delayMillis = spec.delayMillis,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "wedding-heart-progress-$index",
            )
            val drift = spec.drift * progress.value
            val xOffset = (maxWidth * spec.lane) + drift
            val yOffset = maxHeight - ((maxHeight + spec.travelPadding) * progress.value)
            val alpha = when {
                progress.value < spec.fadeIn -> progress.value / spec.fadeIn
                progress.value > spec.fadeOutStart -> (1f - progress.value) / (1f - spec.fadeOutStart)
                else -> spec.maxAlpha
            }.coerceIn(0.12f, spec.maxAlpha)

            Text(
                spec.symbol,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = xOffset, y = yOffset),
                color = tint.copy(alpha = alpha),
                style = when (spec.sizeTier) {
                    0 -> MaterialTheme.typography.titleMedium
                    1 -> MaterialTheme.typography.titleLarge
                    else -> MaterialTheme.typography.headlineMedium
                },
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private data class FloatingHeartSpec(
    val lane: Float,
    val drift: Dp,
    val durationMillis: Int,
    val delayMillis: Int,
    val travelPadding: Dp,
    val fadeIn: Float,
    val fadeOutStart: Float,
    val maxAlpha: Float,
    val symbol: String,
    val sizeTier: Int,
)

private data class WeddingMoment(val time: String, val title: String, val detail: String)
private data class WeddingGuest(val initials: String, val name: String, val note: String, val status: String)

private val weddingHeartSpecs = listOf(
    FloatingHeartSpec(0.08f, 18.dp, 6100, 0, 170.dp, 0.16f, 0.82f, 0.76f, "♥", 2),
    FloatingHeartSpec(0.21f, (-12).dp, 6900, 420, 150.dp, 0.14f, 0.86f, 0.66f, "♡", 1),
    FloatingHeartSpec(0.36f, 22.dp, 5400, 700, 180.dp, 0.18f, 0.80f, 0.78f, "♥", 2),
    FloatingHeartSpec(0.49f, (-16).dp, 7600, 980, 160.dp, 0.15f, 0.88f, 0.72f, "♥", 2),
    FloatingHeartSpec(0.63f, 12.dp, 5900, 1320, 190.dp, 0.13f, 0.81f, 0.64f, "♡", 1),
    FloatingHeartSpec(0.77f, (-20).dp, 7200, 1660, 150.dp, 0.16f, 0.84f, 0.74f, "♥", 2),
    FloatingHeartSpec(0.9f, 8.dp, 5700, 2100, 175.dp, 0.17f, 0.83f, 0.68f, "♥", 1),
    FloatingHeartSpec(0.14f, (-10).dp, 8300, 2560, 155.dp, 0.14f, 0.89f, 0.60f, "♡", 0),
    FloatingHeartSpec(0.29f, 14.dp, 6300, 2920, 168.dp, 0.15f, 0.84f, 0.72f, "♥", 2),
    FloatingHeartSpec(0.43f, (-18).dp, 7000, 3260, 188.dp, 0.16f, 0.82f, 0.70f, "♥", 1),
    FloatingHeartSpec(0.58f, 20.dp, 5650, 3600, 172.dp, 0.13f, 0.80f, 0.76f, "♡", 1),
    FloatingHeartSpec(0.71f, (-14).dp, 7800, 3960, 162.dp, 0.15f, 0.87f, 0.62f, "♥", 2),
    FloatingHeartSpec(0.84f, 16.dp, 6100, 4380, 176.dp, 0.16f, 0.83f, 0.74f, "♥", 2),
    FloatingHeartSpec(0.05f, (-6).dp, 7450, 4720, 166.dp, 0.14f, 0.88f, 0.58f, "♡", 0),
    FloatingHeartSpec(0.52f, 10.dp, 6800, 5140, 194.dp, 0.18f, 0.82f, 0.72f, "♥", 1),
    FloatingHeartSpec(0.95f, (-8).dp, 6200, 5480, 164.dp, 0.13f, 0.86f, 0.66f, "♡", 0),
)

private val weddingMoments = listOf(
    WeddingMoment("15:00", "Vows in the garden", "A soft ceremony under the arch with family and close friends."),
    WeddingMoment("16:20", "Portrait hour", "Confetti, couple portraits, and keepsake family photos."),
    WeddingMoment("18:00", "Dinner and speeches", "A warm reception with first toasts and candlelit tables."),
    WeddingMoment("20:30", "After glow", "Music, dessert, and a final romantic send-off."),
)

private val featuredWeddingGuests = listOf(
    WeddingGuest("AU", "Aurore Uwimana", "Family row", "Confirmed"),
    WeddingGuest("JM", "Jean-Marie M.", "Best man", "Confirmed"),
    WeddingGuest("CN", "Clarisse N.", "Bridesmaid", "Confirmed"),
    WeddingGuest("PM", "Patrick M.", "Photo team", "Pending"),
)
