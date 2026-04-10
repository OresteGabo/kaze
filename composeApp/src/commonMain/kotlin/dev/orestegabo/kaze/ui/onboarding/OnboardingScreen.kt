package dev.orestegabo.kaze.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TempleBuddhist
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.theme.KazeTheme
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.gabo_mark_raster
import kaze.composeapp.generated.resources.k_mark_raster
import org.jetbrains.compose.resources.painterResource

private enum class OnboardingLayoutMode {
    COMPACT,
    TALL_EXPANDED,
    WIDE_EXPANDED,
}

internal data class OnboardingPage(
    val eyebrow: String,
    val title: String,
    val body: String,
    val footer: String,
    val highlights: List<OnboardingFeature>,
)

internal data class OnboardingFeature(
    val icon: ImageVector,
    val title: String,
)

internal val kazeOnboardingPages = listOf(
    OnboardingPage(
        eyebrow = "Welcome to Kaze",
        title = "Your hotel in one app",
        body = "See your stay, explore hotel services, and move through the property without needing to call or visit the front desk.",
        footer = "Keep your room details, directions, and hotel services in one place during your stay.",
        highlights = listOf(
            OnboardingFeature(Icons.Default.Explore, "Stay details in one place"),
            OnboardingFeature(Icons.Default.RoomService, "Hotel services without calling"),
            OnboardingFeature(Icons.Default.SupportAgent, "Less front-desk friction"),
        ),
    ),
    OnboardingPage(
        eyebrow = "Map and Events",
        title = "Find rooms, amenities, and event spaces fast",
        body = "Use the map to move across floors, locate places like restaurants, pools, or ballrooms, and see what is happening today.",
        footer = "Check where to go next without losing time moving around the hotel.",
        highlights = listOf(
            OnboardingFeature(Icons.Default.Map, "Indoor map across floors"),
            OnboardingFeature(Icons.Default.Schedule, "Today's sessions and timing"),
            OnboardingFeature(Icons.Default.Spa, "Amenities easier to find"),
        ),
    ),
    OnboardingPage(
        eyebrow = "Requests and Access",
        title = "Request services with less back and forth",
        body = "Ask for services like late checkout, towels, dining, or help from your phone, and keep one digital pass for access when needed.",
        footer = "Send requests quietly from your phone and keep your access details ready when you need them.",
        highlights = listOf(
            OnboardingFeature(Icons.Default.RoomService, "Service requests from your room"),
            OnboardingFeature(Icons.Default.QrCode2, "One digital access pass"),
            OnboardingFeature(Icons.Default.TempleBuddhist, "Quieter guest experience"),
        ),
    ),
)

@Composable
internal fun OnboardingScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onGetStarted: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = currentPage, pageCount = { kazeOnboardingPages.size })
    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(currentPage)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentPage) onPageChange(pagerState.currentPage)
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val layoutMode = when {
            maxWidth >= 900.dp && maxWidth > maxHeight -> OnboardingLayoutMode.WIDE_EXPANDED
            maxWidth >= 700.dp -> OnboardingLayoutMode.TALL_EXPANDED
            else -> OnboardingLayoutMode.COMPACT
        }
        val horizontalPadding = when {
            maxWidth >= 1100.dp -> 48.dp
            layoutMode != OnboardingLayoutMode.COMPACT -> 32.dp
            else -> 24.dp
        }
        val contentWidth = when {
            layoutMode == OnboardingLayoutMode.WIDE_EXPANDED && maxWidth >= 1280.dp -> 1080.dp
            layoutMode == OnboardingLayoutMode.WIDE_EXPANDED && maxWidth >= 960.dp -> 920.dp
            layoutMode == OnboardingLayoutMode.TALL_EXPANDED -> 760.dp
            else -> Dp.Unspecified
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = 18.dp)
                .then(if (contentWidth != Dp.Unspecified) Modifier.widthIn(max = contentWidth).align(Alignment.TopCenter) else Modifier),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandLockup()
                KazeGhostButton(label = "Skip", onClick = onSkip)
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { pageIndex ->
                OnboardingPageCard(
                    page = kazeOnboardingPages[pageIndex],
                    pageIndex = pageIndex,
                    layoutMode = layoutMode,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(kazeOnboardingPages.size) { index ->
                    val selected = index == currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .height(8.dp)
                            .width(if (selected) 30.dp else 8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                            ),
                    )
                }
            }

            if (currentPage == kazeOnboardingPages.lastIndex) {
                KazePrimaryButton(
                    label = "Enter Kaze",
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KazeSecondaryButton(
                        label = "Later",
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                    )
                    KazePrimaryButton(
                        label = "Next",
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandLockup() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.k_mark_raster),
            contentDescription = "Kaze mark",
            modifier = Modifier.size(36.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Kaze", style = MaterialTheme.typography.titleLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.gabo_mark_raster),
                    contentDescription = "GABO mark",
                    modifier = Modifier.size(12.dp),
                    alpha = 0.5f,
                )
                Text(
                    "by GABO",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageCard(
    page: OnboardingPage,
    pageIndex: Int,
    layoutMode: OnboardingLayoutMode,
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)),
        shape = RoundedCornerShape(34.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(22.dp)) {
            OnboardingPattern(
                modifier = Modifier.matchParentSize(),
                pageIndex = pageIndex,
            )

            if (layoutMode == OnboardingLayoutMode.WIDE_EXPANDED) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PageHero(
                        pageIndex = pageIndex,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 320.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        OnboardingPageCopy(page = page, wide = true)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        PageHero(
                            pageIndex = pageIndex,
                            modifier = if (layoutMode == OnboardingLayoutMode.TALL_EXPANDED) {
                                Modifier.height(236.dp)
                            } else {
                                Modifier
                            },
                        )
                        OnboardingPageCopy(
                            page = page,
                            wide = layoutMode == OnboardingLayoutMode.TALL_EXPANDED,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageCopy(
    page: OnboardingPage,
    wide: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                page.eyebrow.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Text(
                page.title,
                style = if (wide) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                page.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            page.highlights.forEach { feature ->
                OnboardingFeatureRow(feature = feature)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f)),
            shape = RoundedCornerShape(26.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            ),
        ) {
            Text(
                text = page.footer,
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun OnboardingFeatureRow(feature: OnboardingFeature) {
    val tint = MaterialTheme.colorScheme.secondary
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = tint.copy(alpha = 0.12f),
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
            )
        }
    }
}

@Composable
private fun PageHero(
    pageIndex: Int,
    modifier: Modifier = Modifier,
) {
    val accent = when (pageIndex % 3) {
        0 -> KazeTheme.accents.editorialWarm
        1 -> KazeTheme.accents.editorialBotanical
        else -> KazeTheme.accents.editorialClay
    }
    val primaryTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    val strongPrimaryTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val softOnSurface = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(188.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.64f),
                    ),
                ),
            ),
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            drawCircle(
                color = accent.copy(alpha = 0.18f),
                radius = w * 0.22f,
                center = Offset(w * 0.84f, h * 0.24f),
                style = Stroke(width = 5f),
            )
            drawCircle(
                color = primaryTint,
                radius = w * 0.28f,
                center = Offset(w * 0.16f, h * 0.8f),
                style = Stroke(width = 4f),
            )

            drawLine(
                color = accent.copy(alpha = 0.32f),
                start = Offset(w * 0.08f, h * 0.18f),
                end = Offset(w * 0.7f, h * 0.18f),
                strokeWidth = 6f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = strongPrimaryTint,
                start = Offset(w * 0.14f, h * 0.28f),
                end = Offset(w * 0.88f, h * 0.28f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round,
            )

            val routePath = Path().apply {
                moveTo(w * 0.58f, h * 0.1f)
                lineTo(w * 0.74f, h * 0.1f)
                lineTo(w * 0.82f, h * 0.18f)
                lineTo(w * 0.94f, h * 0.18f)
                lineTo(w * 0.94f, h * 0.30f)
                lineTo(w * 0.82f, h * 0.30f)
                lineTo(w * 0.74f, h * 0.38f)
                lineTo(w * 0.52f, h * 0.38f)
            }
            drawPath(
                path = routePath,
                color = accent.copy(alpha = 0.28f),
                style = Stroke(width = 3.2f),
            )

            repeat(7) { index ->
                val y = h * (0.5f + index * 0.055f)
                drawLine(
                    color = if (index % 2 == 0) accent.copy(alpha = 0.24f) else softOnSurface,
                    start = Offset(w * 0.1f, y),
                    end = Offset(w * (0.32f + index * 0.07f), y),
                    strokeWidth = if (index % 2 == 0) 3.2f else 2f,
                    cap = StrokeCap.Round,
                )
            }
        }

        Image(
            painter = painterResource(Res.drawable.k_mark_raster),
            contentDescription = "Kaze mark",
            modifier = Modifier
                .align(Alignment.Center)
                .size(96.dp),
            alpha = 0.8f,
        )
    }
}

@Composable
private fun OnboardingPattern(
    modifier: Modifier = Modifier,
    pageIndex: Int,
) {
    val accent = when (pageIndex % 3) {
        0 -> KazeTheme.accents.editorialWarm
        1 -> KazeTheme.accents.editorialBotanical
        else -> KazeTheme.accents.editorialClay
    }
    val softOnSurface = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawLine(
            color = accent.copy(alpha = 0.12f),
            start = Offset(w * 0.08f, h * 0.12f),
            end = Offset(w * 0.44f, h * 0.12f),
            strokeWidth = 3f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = softOnSurface,
            start = Offset(w * 0.18f, h * 0.82f),
            end = Offset(w * 0.76f, h * 0.82f),
            strokeWidth = 2.4f,
            cap = StrokeCap.Round,
        )
    }
}
