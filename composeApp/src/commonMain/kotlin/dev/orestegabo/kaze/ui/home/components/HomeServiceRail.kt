package dev.orestegabo.kaze.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private data class HomeServiceAction(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val query: String,
    val isAvailable: Boolean = true,
)

@Composable
internal fun HomeServiceRail(
    onOpenService: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val actions = listOf(
        HomeServiceAction(
            title = "Wedding venues",
            icon = Icons.Default.Favorite,
            accent = colors.tertiary,
            query = "wedding venues",
        ),
        HomeServiceAction(
            title = "Conference rooms",
            icon = Icons.Default.BusinessCenter,
            accent = colors.primary,
            query = "conference rooms",
        ),
        HomeServiceAction(
            title = "Apartments",
            icon = Icons.Default.Apartment,
            accent = colors.secondary,
            query = "apartments",
            isAvailable = false,
        ),
        HomeServiceAction(
            title = "Hotels",
            icon = Icons.Default.Hotel,
            accent = colors.primary,
            query = "hotels",
        ),
        HomeServiceAction(
            title = "Event layouts",
            icon = Icons.Default.Chair,
            accent = colors.secondary,
            query = "event layouts",
        ),
        HomeServiceAction(
            title = "Styling & decor",
            icon = Icons.Default.LocalFlorist,
            accent = colors.tertiary,
            query = "event styling",
            isAvailable = false,
        ),
        HomeServiceAction(
            title = "Catering",
            icon = Icons.Default.Restaurant,
            accent = colors.primary,
            query = "catering",
            isAvailable = false,
        ),
        HomeServiceAction(
            title = "Photo & video",
            icon = Icons.Default.Videocam,
            accent = colors.secondary,
            query = "photo video live streaming",
            isAvailable = false,
        ),
        HomeServiceAction(
            title = "Transport",
            icon = Icons.Default.DirectionsCar,
            accent = colors.tertiary,
            query = "event transport",
            isAvailable = false,
        ),
        HomeServiceAction(
            title = "Guest access",
            icon = Icons.Default.Celebration,
            accent = colors.primary,
            query = "guest access",
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("Popular services")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            actions.forEach { action ->
                HomeServiceActionCard(
                    action = action,
                    onClick = { if (action.isAvailable) onOpenService(action.query) },
                )
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun HomeServiceActionCard(
    action: HomeServiceAction,
    onClick: () -> Unit,
) {
    val alpha = if (action.isAvailable) 1f else 0.58f
    val borderAlpha = if (action.isAvailable) 0.26f else 0.14f
    Surface(
        onClick = onClick,
        enabled = action.isAvailable,
        modifier = Modifier
            .width(112.dp)
            .height(126.dp),
        shape = RoundedCornerShape(24.dp),
        color = action.accent.copy(alpha = if (action.isAvailable) 0.08f else 0.04f),
        border = BorderStroke(1.dp, action.accent.copy(alpha = borderAlpha)),
    ) {
        Box {
            if (!action.isAvailable) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        "Soon",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = action.accent.copy(alpha = if (action.isAvailable) 0.18f else 0.10f),
                ) {
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null,
                            tint = action.accent.copy(alpha = alpha),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Text(
                    action.title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (action.isAvailable) 0.82f else 0.58f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
