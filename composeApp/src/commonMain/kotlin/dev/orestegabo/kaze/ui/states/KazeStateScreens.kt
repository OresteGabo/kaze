package dev.orestegabo.kaze.ui.states

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.platform.isDeviceOnline
import dev.orestegabo.kaze.ui.chrome.KazeAmbientBackground
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.empty_state_action
import kaze.composeapp.generated.resources.empty_state_subtitle
import kaze.composeapp.generated.resources.empty_state_title
import kaze.composeapp.generated.resources.ignored_screens_subtitle
import kaze.composeapp.generated.resources.ignored_screens_title
import kaze.composeapp.generated.resources.offline_state_retry
import kaze.composeapp.generated.resources.offline_state_retrying
import kaze.composeapp.generated.resources.offline_state_subtitle
import kaze.composeapp.generated.resources.offline_state_title
import kaze.composeapp.generated.resources.permission_primer_camera_subtitle
import kaze.composeapp.generated.resources.permission_primer_camera_title
import kaze.composeapp.generated.resources.permission_primer_grant
import kaze.composeapp.generated.resources.permission_primer_later
import kaze.composeapp.generated.resources.permission_primer_location_subtitle
import kaze.composeapp.generated.resources.permission_primer_location_title
import kaze.composeapp.generated.resources.success_state_home
import kaze.composeapp.generated.resources.success_state_subtitle
import kaze.composeapp.generated.resources.success_state_title
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal enum class KazePermissionPrimerType {
    CAMERA,
    LOCATION,
}

internal data class KazeIgnoredStateSuggestion(
    val title: String,
    val description: String,
    val icon: ImageVector,
)

@Composable
internal fun KazeEmptyStateScreen(
    modifier: Modifier = Modifier,
    title: String = stringResource(Res.string.empty_state_title),
    subtitle: String = stringResource(Res.string.empty_state_subtitle),
    actionLabel: String? = stringResource(Res.string.empty_state_action),
    eyebrow: String? = null,
    tags: List<String> = emptyList(),
    icon: ImageVector = Icons.Outlined.BookmarkBorder,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                    )
                )
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                RoundedCornerShape(32.dp),
            )
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (eyebrow != null) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )

        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp),
            ) {
                tags.forEach { tag ->
                    MetaPill(
                        label = tag,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    )
                }
            }
        }

        if (actionLabel != null && onAction != null) {
            KazeSecondaryButton(
                label = actionLabel,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(0.8f),
            )
        }
    }
}

@Composable
internal fun KazeOfflineStateCard(
    modifier: Modifier = Modifier,
    onRetryResult: (Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        tonalElevation = 2.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconBubble(
                icon = Icons.Outlined.WifiOff,
                contentDescription = null,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(Res.string.offline_state_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(Res.string.offline_state_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                KazeSecondaryButton(
                    label = if (isChecking) {
                        stringResource(Res.string.offline_state_retrying)
                    } else {
                        stringResource(Res.string.offline_state_retry)
                    },
                    onClick = {
                        if (isChecking) return@KazeSecondaryButton
                        isChecking = true
                        scope.launch {
                            delay(RETRY_MINIMUM_LOADING_MS)
                            val isOnline = isDeviceOnline()
                            isChecking = false
                            onRetryResult(isOnline)
                        }
                    },
                    leadingIcon = if (isChecking) null else Icons.Outlined.WifiOff,
                )
            }
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Composable
internal fun KazeSuccessCelebrationScreen(
    modifier: Modifier = Modifier,
    title: String = stringResource(Res.string.success_state_title),
    subtitle: String = stringResource(Res.string.success_state_subtitle),
    onBackHome: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.34f),
                    ),
                ),
            ),
    ) {
        CelebrationConfetti(modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            AnimatedSuccessMark()
            StateCopy(
                title = title,
                subtitle = subtitle,
            )
            KazePrimaryButton(
                label = stringResource(Res.string.success_state_home),
                onClick = onBackHome,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun KazePermissionPrimerScreen(
    modifier: Modifier = Modifier,
    type: KazePermissionPrimerType,
    onGrantAccess: () -> Unit,
    onMaybeLater: () -> Unit,
) {
    val icon = when (type) {
        KazePermissionPrimerType.CAMERA -> Icons.Outlined.PhotoCamera
        KazePermissionPrimerType.LOCATION -> Icons.Outlined.LocationOn
    }
    val title = when (type) {
        KazePermissionPrimerType.CAMERA -> Res.string.permission_primer_camera_title
        KazePermissionPrimerType.LOCATION -> Res.string.permission_primer_location_title
    }
    val subtitle = when (type) {
        KazePermissionPrimerType.CAMERA -> Res.string.permission_primer_camera_subtitle
        KazePermissionPrimerType.LOCATION -> Res.string.permission_primer_location_subtitle
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        KazeAmbientBackground(modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            PlaceholderIllustration(
                icon = icon,
                contentDescription = null,
            )
            StateCopy(
                title = stringResource(title),
                subtitle = stringResource(subtitle),
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KazePrimaryButton(
                    label = stringResource(Res.string.permission_primer_grant),
                    onClick = onGrantAccess,
                    modifier = Modifier.fillMaxWidth(),
                )
                KazeSecondaryButton(
                    label = stringResource(Res.string.permission_primer_later),
                    onClick = onMaybeLater,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
internal fun KazeIgnoredStateSuggestions(
    modifier: Modifier = Modifier,
    suggestions: List<KazeIgnoredStateSuggestion> = defaultIgnoredStateSuggestions(),
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.ignored_screens_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(Res.string.ignored_screens_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        suggestions.forEach { suggestion ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f)),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = suggestion.icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = suggestion.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = suggestion.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StateCopy(
    title: String,
    subtitle: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlaceholderIllustration(
    icon: ImageVector,
    contentDescription: String?,
) {
    val transition = rememberInfiniteTransition()
    val floatOffset by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val primaryTint = MaterialTheme.colorScheme.primary
    val secondaryTint = MaterialTheme.colorScheme.secondary
    val outlineTint = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .size(168.dp)
            .graphicsLayer { translationY = floatOffset },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.matchParentSize(),
            shape = RoundedCornerShape(48.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = primaryTint.copy(alpha = 0.10f),
                    radius = size.minDimension * 0.18f,
                    center = Offset(size.width * 0.26f, size.height * 0.26f),
                )
                drawCircle(
                    color = secondaryTint.copy(alpha = 0.08f),
                    radius = size.minDimension * 0.14f,
                    center = Offset(size.width * 0.78f, size.height * 0.74f),
                )
                drawLine(
                    color = outlineTint.copy(alpha = 0.18f),
                    start = Offset(size.width * 0.18f, size.height * 0.78f),
                    end = Offset(size.width * 0.82f, size.height * 0.78f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                )
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(58.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmptyStateTagChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
        )
    }
}

@Composable
private fun GlassIconBubble(
    icon: ImageVector,
    contentDescription: String?,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun AnimatedSuccessMark() {
    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Surface(
        modifier = Modifier
            .size(136.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(76.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun CelebrationConfetti(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val secondary = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)

    Canvas(modifier = modifier.alpha(0.86f)) {
        val w = size.width
        val h = size.height
        repeat(10) { index ->
            val x = w * (0.12f + (index % 5) * 0.19f)
            val y = h * (0.12f + (index / 5) * 0.62f)
            drawLine(
                color = if (index % 2 == 0) color else secondary,
                start = Offset(x, y),
                end = Offset(x + 22f, y + 10f),
                strokeWidth = 5f,
                cap = StrokeCap.Round,
            )
            drawOval(
                color = if (index % 2 == 0) secondary else color,
                topLeft = Offset(x + 32f, y + 24f),
                size = Size(12f, 12f),
            )
        }
    }
}

private fun defaultIgnoredStateSuggestions(): List<KazeIgnoredStateSuggestion> =
    listOf(
        KazeIgnoredStateSuggestion(
            title = "Slow connection state",
            description = "Show lightweight content first, then progressively load images and live data.",
            icon = Icons.Outlined.HourglassEmpty,
        ),
        KazeIgnoredStateSuggestion(
            title = "Expired invitation",
            description = "Explain what happened and give a clear way to request a fresh invite.",
            icon = Icons.Outlined.Schedule,
        ),
        KazeIgnoredStateSuggestion(
            title = "Search with no results",
            description = "Suggest nearby categories, spelling fixes, or popular venues instead of a dead end.",
            icon = Icons.Outlined.SearchOff,
        ),
        KazeIgnoredStateSuggestion(
            title = "Session expired",
            description = "Protect trust by explaining that the user only needs to sign in again.",
            icon = Icons.Outlined.Lock,
        ),
        KazeIgnoredStateSuggestion(
            title = "Partial failure",
            description = "If payment, maps, or photos fail while the rest works, show the working parts first.",
            icon = Icons.Outlined.ErrorOutline,
        ),
    )

private const val RETRY_MINIMUM_LOADING_MS = 700L
