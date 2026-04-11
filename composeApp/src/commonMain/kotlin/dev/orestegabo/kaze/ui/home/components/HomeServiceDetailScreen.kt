package dev.orestegabo.kaze.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.MetaPill
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.kaze_bg_apartments_raster
import kaze.composeapp.generated.resources.kaze_bg_catering_raster
import kaze.composeapp.generated.resources.kaze_bg_conference_rooms_raster
import kaze.composeapp.generated.resources.kaze_bg_event_layouts_raster
import kaze.composeapp.generated.resources.kaze_bg_guest_access_raster
import kaze.composeapp.generated.resources.kaze_bg_hotels_raster
import kaze.composeapp.generated.resources.kaze_bg_photo_video_raster
import kaze.composeapp.generated.resources.kaze_bg_styling_decor_raster
import kaze.composeapp.generated.resources.kaze_bg_transport_raster
import kaze.composeapp.generated.resources.kaze_bg_wedding_venues_raster
import kaze.composeapp.generated.resources.kotlinconf_ground_floor_light_raster
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private data class HomeServicePageContent(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val background: DrawableResource,
    val accent: Color,
    val filters: List<String>,
    val highlights: List<String>,
    val isAvailable: Boolean,
    val results: List<HomeServiceResult>,
)

private data class HomeServiceResult(
    val title: String,
    val subtitle: String,
    val metaLabel: String,
    val priceLabel: String,
)

private enum class ServiceResultDetailTab(val label: String) {
    DETAILS("Details"),
    MAP("Map"),
}

@Composable
internal fun HomeServiceDetailScreen(
    serviceQuery: String,
    bottomContentPadding: Dp,
    onBack: () -> Unit,
) {
    val content = servicePageContent(serviceQuery)
    val scrollState = rememberScrollState()
    var searchQuery by rememberSaveable(serviceQuery) { mutableStateOf("") }
    var selectedFilters by rememberSaveable(serviceQuery) { mutableStateOf(emptyList<String>()) }
    var selectedResultTitle by rememberSaveable(serviceQuery) { mutableStateOf<String?>(null) }
    val filteredResults = content.results.filter { result ->
        val searchable = "${result.title} ${result.subtitle} ${result.metaLabel} ${result.priceLabel}".lowercase()
        val matchesSearch = searchQuery.isBlank() || searchQuery.lowercase() in searchable
        val matchesFilter = selectedFilters.all { filter -> filter.lowercase() in searchable }
        matchesSearch && matchesFilter
    }
    val selectedResult = content.results.firstOrNull { it.title == selectedResultTitle }

    if (selectedResult != null) {
        HomeServiceResultDetailScreen(
            content = content,
            result = selectedResult,
            bottomContentPadding = bottomContentPadding,
            onBack = { selectedResultTitle = null },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KazeGhostButton(
            label = "Back",
            onClick = onBack,
            leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )

        ServiceHeroCard(content = content)

        ServiceSearchField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search ${content.title.lowercase()}",
            accent = content.accent,
        )

        SectionLabel("Quick filters")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content.filters.forEach { filter ->
                FilterPill(
                    label = filter,
                    selected = filter in selectedFilters,
                    accent = content.accent,
                    onClick = {
                        selectedFilters = if (filter in selectedFilters) {
                            selectedFilters - filter
                        } else {
                            selectedFilters + filter
                        }
                    },
                )
            }
        }

        SectionLabel("${filteredResults.size} demo result${if (filteredResults.size == 1) "" else "s"}")
        if (filteredResults.isEmpty()) {
            EmptyServiceResultsCard(accent = content.accent)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredResults.forEach { result ->
                    ServiceResultCard(
                        result = result,
                        accent = content.accent,
                        icon = content.icon,
                        background = content.background,
                        onClick = { selectedResultTitle = result.title },
                    )
                }
            }
        }

        SectionLabel("What you can check")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content.highlights.forEach { highlight ->
                ServiceDetailItem(
                    text = highlight,
                    accent = content.accent,
                )
            }
        }
    }
}

@Composable
private fun ServiceHeroCard(content: HomeServicePageContent) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, content.accent.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = content.accent.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, content.accent.copy(alpha = 0.22f)),
                ) {
                    Box(
                        modifier = Modifier.padding(14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = content.icon,
                            contentDescription = null,
                            tint = content.accent,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        content.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        content.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                }
            }
            if (!content.isAvailable) {
                MetaPill(
                    label = "Coming soon",
                    containerColor = content.accent.copy(alpha = 0.16f),
                    textColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ServiceSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    accent: Color,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
        tonalElevation = 2.dp,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = accent,
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) accent.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f),
        border = BorderStroke(1.dp, if (selected) accent.copy(alpha = 0.44f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accent,
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ServiceResultCard(
    result: HomeServiceResult,
    accent: Color,
    icon: ImageVector,
    background: DrawableResource,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(accent.copy(alpha = 0.15f)),
            ) {
                Image(
                    painter = painterResource(background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize(),
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 14.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
                    tonalElevation = 2.dp,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(22.dp),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        result.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    MetaPill(
                        label = result.priceLabel,
                        containerColor = accent.copy(alpha = 0.16f),
                        textColor = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    result.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    MetaPill(result.metaLabel)
                }
            }
        }
    }
}

@Composable
private fun HomeServiceResultDetailScreen(
    content: HomeServicePageContent,
    result: HomeServiceResult,
    bottomContentPadding: Dp,
    onBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    var selectedTab by rememberSaveable(result.title) { mutableStateOf(ServiceResultDetailTab.DETAILS.name) }
    val activeTab = ServiceResultDetailTab.valueOf(selectedTab)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KazeGhostButton(
            label = "Back to results",
            onClick = onBack,
            leadingIcon = Icons.AutoMirrored.Filled.ArrowBack,
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, content.accent.copy(alpha = 0.20f)),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .background(content.accent.copy(alpha = 0.15f)),
                ) {
                    Image(
                        painter = painterResource(content.background),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 18.dp, bottom = 16.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, content.accent.copy(alpha = 0.22f)),
                        tonalElevation = 2.dp,
                    ) {
                        Icon(
                            imageVector = content.icon,
                            contentDescription = null,
                            tint = content.accent,
                            modifier = Modifier
                                .padding(13.dp)
                                .size(24.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            result.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            result.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MetaPill(result.metaLabel)
                        MetaPill(
                            label = result.priceLabel,
                            containerColor = content.accent.copy(alpha = 0.16f),
                            textColor = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        ServiceResultDetailTabs(
            activeTab = activeTab,
            accent = content.accent,
            onTabSelected = { selectedTab = it.name },
        )

        when (activeTab) {
            ServiceResultDetailTab.DETAILS -> {
                SectionLabel("Details")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ServiceDetailItem(
                        text = "Category: ${content.title}",
                        accent = content.accent,
                    )
                    ServiceDetailItem(
                        text = "Good match for: ${result.metaLabel}",
                        accent = content.accent,
                    )
                    ServiceDetailItem(
                        text = "Estimated price: ${result.priceLabel}",
                        accent = content.accent,
                    )
                }
            }

            ServiceResultDetailTab.MAP -> {
                SectionLabel("Map")
                ServiceResultMapCard(
                    title = result.title,
                    accent = content.accent,
                )
            }
        }
    }
}

@Composable
private fun ServiceResultDetailTabs(
    activeTab: ServiceResultDetailTab,
    accent: Color,
    onTabSelected: (ServiceResultDetailTab) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ServiceResultDetailTab.entries.forEach { tab ->
                Surface(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                    color = if (activeTab == tab) accent.copy(alpha = 0.18f) else Color.Transparent,
                    border = if (activeTab == tab) BorderStroke(1.dp, accent.copy(alpha = 0.26f)) else null,
                ) {
                    Text(
                        tab.label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (activeTab == tab) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (activeTab == tab) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceResultMapCard(
    title: String,
    accent: Color,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            ) {
                Image(
                    painter = painterResource(Res.drawable.kotlinconf_ground_floor_light_raster),
                    contentDescription = "$title map",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(10.dp),
                )
            }
            Text(
                "Temporary map preview. This will later use the venue or room map connected to this item.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun EmptyServiceResultsCard(accent: Color) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
    ) {
        Text(
            "No demo result matched that search. Try a shorter word or remove the selected filter.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun ServiceDetailItem(
    text: String,
    accent: Color,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.16f),
            ) {
                Box(
                    modifier = Modifier.padding(7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = null,
                        tint = accent,
                    )
                }
            }
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable
private fun servicePageContent(query: String): HomeServicePageContent {
    val colors = MaterialTheme.colorScheme
    return when (query) {
        "wedding venues" -> HomeServicePageContent(
            title = "Wedding venues",
            subtitle = "Find reception spaces, compare capacity, and prepare guest access before the event.",
            icon = Icons.Default.Favorite,
            background = Res.drawable.kaze_bg_wedding_venues_raster,
            accent = colors.tertiary,
            filters = listOf("Reception", "Garden", "Banquet", "Kigali", "Parking"),
            highlights = listOf("Check venue capacity and starting price.", "Preview layouts for tables, chairs, and guest flow.", "Create invitations and Kaze Pass access after booking."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Kigali Garden Pavilion", "Outdoor reception venue with garden photo zones and covered dining.", "Garden • 450 guests", "From RWF 1.8M"),
                HomeServiceResult("Umubano Grand Hall", "Banquet hall with parking, stage space, and guest entrance control.", "Banquet • Parking", "From RWF 2.4M"),
                HomeServiceResult("Lake View Wedding Lawn", "Reception lawn for sunset ceremonies and family seating layouts.", "Reception • Garden", "From RWF 1.5M"),
            ),
        )
        "conference rooms" -> HomeServicePageContent(
            title = "Conference rooms",
            subtitle = "Browse meeting spaces for workshops, board meetings, trainings, and launches.",
            icon = Icons.Default.BusinessCenter,
            background = Res.drawable.kaze_bg_conference_rooms_raster,
            accent = colors.primary,
            filters = listOf("Half day", "Full day", "Projector", "Boardroom", "Training"),
            highlights = listOf("Compare room setup, capacity, and included equipment.", "Reserve a time slot and add attendee access later.", "Request extras like catering, cleaning, or livestreaming."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Nyarutarama Boardroom", "Quiet executive room with screen, Wi-Fi, and reception desk support.", "Boardroom • Projector", "RWF 180K half day"),
                HomeServiceResult("Kigali Training Suite", "Flexible classroom setup for workshops and product launches.", "Training • Full day", "RWF 320K full day"),
                HomeServiceResult("Kivu Meeting Room", "Compact meeting room for interviews and small planning sessions.", "Half day • 18 seats", "RWF 95K half day"),
            ),
        )
        "apartments" -> HomeServicePageContent(
            title = "Apartments",
            subtitle = "Discover local stays for event guests, organizers, and families who need more space than a hotel room.",
            icon = Icons.Default.Apartment,
            background = Res.drawable.kaze_bg_apartments_raster,
            accent = colors.secondary,
            filters = listOf("Short stay", "Family", "Furnished", "Garden", "Near venue", "VIP", "Kitchen"),
            highlights = listOf("Show verified local apartments near event venues.", "Compare price, distance, and guest capacity.", "Help guests find stays without leaving Kaze."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Kacyiru Family Apartment", "Furnished two-bedroom option with a shared garden near meeting venues.", "Family • Furnished • Garden", "RWF 75K/night"),
                HomeServiceResult("Remera Short Stay Studio", "Compact studio for guests attending a weekend event.", "Short stay • Near venue", "RWF 38K/night"),
                HomeServiceResult("Kigali Heights Residence", "Premium serviced apartment for organizers or VIP guests.", "Furnished • VIP", "RWF 120K/night"),
                HomeServiceResult("Gishushu Executive Flat", "Modern one-bedroom with kitchen and quick access to conference venues.", "Kitchen • Furnished", "RWF 68K/night"),
                HomeServiceResult("Nyamirambo Family Stay", "Three-bedroom apartment for relatives traveling together for a wedding.", "Family • Kitchen", "RWF 82K/night"),
                HomeServiceResult("Kimihurura Event Guest Suite", "Walkable stay option near restaurants and central Kigali venues.", "Short stay • Near venue", "RWF 95K/night"),
            ),
        )
        "hotels" -> HomeServicePageContent(
            title = "Hotels",
            subtitle = "Explore hotel spaces and services linked to events, stays, and guest access.",
            icon = Icons.Default.Hotel,
            background = Res.drawable.kaze_bg_hotels_raster,
            accent = colors.primary,
            filters = listOf("Rooms", "Events", "Restaurant", "Pool", "Map"),
            highlights = listOf("See public hotel services before booking.", "Open venue maps when a hotel provides them.", "Connect stays, requests, and passes in one place."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Kaze Demo Hotel", "Rooms, event spaces, restaurant, and indoor guest navigation.", "Rooms • Map", "Rooms from RWF 120K"),
                HomeServiceResult("Mille Collines Business Stay", "Business hotel with meeting spaces and central Kigali access.", "Events • Restaurant", "Rooms from RWF 160K"),
                HomeServiceResult("Green Hill Boutique Hotel", "Small hotel for wedding guests and family groups.", "Pool • Rooms", "Rooms from RWF 90K"),
            ),
        )
        "event layouts" -> HomeServicePageContent(
            title = "Event layouts",
            subtitle = "Plan chair and table arrangements for weddings, conferences, and private events.",
            icon = Icons.Default.Chair,
            background = Res.drawable.kaze_bg_event_layouts_raster,
            accent = colors.secondary,
            filters = listOf("Round tables", "Classroom", "Theatre", "VIP", "Free space"),
            highlights = listOf("Preview layouts based on attendee count.", "Separate fixed seats from configurable decoration layouts.", "Switch layouts before confirming the final setup."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Round Table Wedding Layout", "Suggested arrangement for families, VIPs, and dance floor flow.", "Round tables • VIP", "300 guests"),
                HomeServiceResult("Conference Classroom Layout", "Rows with desks, projector visibility, and aisle spacing.", "Classroom • Projector", "120 guests"),
                HomeServiceResult("Theatre Launch Layout", "Dense seating plan for talks, product launches, and ceremonies.", "Theatre • Free space", "500 guests"),
            ),
        )
        "event styling" -> HomeServicePageContent(
            title = "Styling & decor",
            subtitle = "Find flowers, lights, stage styling, and event decoration teams for weddings and corporate events.",
            icon = Icons.Default.LocalFlorist,
            background = Res.drawable.kaze_bg_styling_decor_raster,
            accent = colors.tertiary,
            filters = listOf("Flowers", "Lighting", "Stage", "Wedding", "Corporate", "Traditional"),
            highlights = listOf("Request decoration services after choosing a venue.", "Compare vendor packages and availability.", "Keep decoration separate from seating layout planning."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Botanical Wedding Decor", "Floral arch, table greens, and soft aisle styling.", "Flowers • Wedding", "From RWF 650K"),
                HomeServiceResult("Corporate Stage Styling", "Branded stage setup with podium, backdrop, and lighting.", "Stage • Corporate", "From RWF 480K"),
                HomeServiceResult("Evening Light Package", "Warm uplights and entrance lighting for receptions.", "Lighting • Reception", "From RWF 300K"),
                HomeServiceResult("Traditional Ceremony Touch", "Imigongo-inspired details, woven accents, and ceremonial entrance styling.", "Traditional • Wedding", "From RWF 520K"),
                HomeServiceResult("Premium Flower Tablescape", "Centerpieces, couple table styling, and VIP table florals.", "Flowers • VIP", "From RWF 780K"),
                HomeServiceResult("Minimal Corporate Decor", "Clean stage plants, branded sign-in table, and neutral table styling.", "Corporate • Stage", "From RWF 260K"),
            ),
        )
        "catering" -> HomeServicePageContent(
            title = "Catering",
            subtitle = "Plan food and drinks for meetings, weddings, and private gatherings.",
            icon = Icons.Default.Restaurant,
            background = Res.drawable.kaze_bg_catering_raster,
            accent = colors.primary,
            filters = listOf("Buffet", "Coffee break", "Dinner", "Drinks", "Canapes", "Traditional"),
            highlights = listOf("Add meals to a venue reservation.", "Compare package options and guest counts.", "Keep catering connected to event timing."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Conference Coffee Break", "Tea, coffee, juice, pastries, and fruit service.", "Coffee break • 80 pax", "RWF 8K/person"),
                HomeServiceResult("Wedding Dinner Buffet", "Buffet service with local and continental menu options.", "Buffet • Dinner", "RWF 22K/person"),
                HomeServiceResult("Soft Drinks Package", "Water, soda, and juice service for meetings or receptions.", "Drinks • Event", "RWF 4K/person"),
                HomeServiceResult("Traditional Rwandan Menu", "Brochettes, isombe, plantain, rice, and seasonal sides.", "Traditional • Buffet", "RWF 18K/person"),
                HomeServiceResult("VIP Canapes Service", "Small bites and welcome drinks for launches or premium receptions.", "Canapes • Drinks", "RWF 15K/person"),
                HomeServiceResult("Lunch Box Meeting Pack", "Individual boxed lunch for training days and workshops.", "Lunch • Corporate", "RWF 12K/person"),
            ),
        )
        "photo video live streaming" -> HomeServicePageContent(
            title = "Photo & video",
            subtitle = "Book photography, video, drone, and livestreaming teams for weddings and conferences.",
            icon = Icons.Default.Videocam,
            background = Res.drawable.kaze_bg_photo_video_raster,
            accent = colors.secondary,
            filters = listOf("Photography", "Video", "Livestream", "Drone", "Wedding", "Conference"),
            highlights = listOf("Add media coverage to weddings or conferences.", "Compare service packages and delivery options.", "Coordinate vendor access with Kaze Pass."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Wedding Photo Team", "Two photographers for ceremony, reception, and family portraits.", "Photography • Wedding", "From RWF 500K"),
                HomeServiceResult("Conference Livestream", "Camera, audio feed, encoder, and streaming support.", "Livestream • Video", "From RWF 750K"),
                HomeServiceResult("Drone Highlight Add-on", "Short aerial clips for venues with approved outdoor access.", "Drone • Outdoor", "From RWF 220K"),
                HomeServiceResult("Full Wedding Film", "Two-camera video coverage with highlight reel and long edit.", "Video • Wedding", "From RWF 900K"),
                HomeServiceResult("Corporate Recap Package", "Photos, short social clips, and speaker recording for conferences.", "Conference • Video", "From RWF 620K"),
                HomeServiceResult("Same-Day Photo Preview", "Fast edited photo selection delivered during the event day.", "Photography • Event", "From RWF 280K"),
            ),
        )
        "event transport" -> HomeServicePageContent(
            title = "Transport",
            subtitle = "Plan guest transport, airport pickup, and event shuttle options.",
            icon = Icons.Default.DirectionsCar,
            background = Res.drawable.kaze_bg_transport_raster,
            accent = colors.tertiary,
            filters = listOf("Airport", "Shuttle", "VIP car", "Guest pickup", "Bus", "Wedding"),
            highlights = listOf("Attach pickup details to event invitations.", "Coordinate drivers with venue timing.", "Later, collect transport payments in-app."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Airport VIP Pickup", "Driver pickup for speakers, couples, or special guests.", "Airport • VIP car", "From RWF 35K"),
                HomeServiceResult("Wedding Guest Shuttle", "Shared shuttle between church, reception, and parking area.", "Shuttle • Guest pickup", "From RWF 180K"),
                HomeServiceResult("Evening Return Cars", "Scheduled return rides for late event guests.", "Guest pickup • Night", "From RWF 25K"),
                HomeServiceResult("Conference Speaker Car", "Dedicated driver for keynote speakers and panel guests.", "VIP car • Conference", "From RWF 55K"),
                HomeServiceResult("Group Bus Transfer", "Bus transfer for large guest groups between venues.", "Bus • Shuttle", "From RWF 260K"),
                HomeServiceResult("Wedding Couple Car", "Decorated premium car for the couple and photo movement.", "Wedding • VIP car", "From RWF 150K"),
            ),
        )
        "guest access" -> HomeServicePageContent(
            title = "Guest access",
            subtitle = "Create event passes and keep invitations easy to verify at the entrance.",
            icon = Icons.Default.Celebration,
            background = Res.drawable.kaze_bg_guest_access_raster,
            accent = colors.primary,
            filters = listOf("Kaze Pass", "Invitation code", "QR entry", "Guest list"),
            highlights = listOf("Generate passes after an invitation is approved.", "Use invitation codes for people joining from WhatsApp or SMS.", "Track active and archived invitations."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Wedding Guest Pass", "Digital pass for invitees with code and QR entry support.", "Kaze Pass • QR entry", "Included"),
                HomeServiceResult("Conference Check-in List", "Guest list for organizers and entry desk verification.", "Guest list • QR entry", "Included"),
                HomeServiceResult("Shared Invitation Code", "Short code guests can type after receiving it by WhatsApp or SMS.", "Invitation code • Event", "Included"),
            ),
        )
        else -> HomeServicePageContent(
            title = "Service",
            subtitle = "Explore this service and see what can be connected to your event.",
            icon = Icons.Default.Search,
            background = Res.drawable.kaze_bg_guest_access_raster,
            accent = colors.primary,
            filters = listOf("Available", "Nearby", "Popular"),
            highlights = listOf("Browse matching options.", "Compare useful details before choosing.", "Save it to your event when ready."),
            isAvailable = true,
            results = listOf(
                HomeServiceResult("Sample service", "Demo service result for this category.", "Available • Nearby", "Price varies"),
            ),
        )
    }
}
