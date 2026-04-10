package dev.orestegabo.kaze.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.presentation.demo.PublicVenuePreview
import dev.orestegabo.kaze.presentation.demo.VenueCategoryPreview
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    venueCategories: List<VenueCategoryPreview>,
    featuredVenues: List<PublicVenuePreview>,
    invitations: List<InvitationPreview>,
    onExploreVenues: (String) -> Unit,
    onEnterCode: (String) -> Unit,
    onOpenCategory: (VenueCategoryPreview) -> Unit,
    onOpenVenue: (PublicVenuePreview) -> Unit,
    onOpenInvitation: (InvitationPreview) -> Unit,
) {
    var venueQuery by rememberSaveable { mutableStateOf("") }
    var joinCode by rememberSaveable { mutableStateOf("") }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 860.dp
        val contentMaxWidth = if (isExpanded) 1180.dp else androidx.compose.ui.unit.Dp.Unspecified
        val categoryColumns = if (maxWidth >= 1160.dp) 4 else if (isExpanded) 2 else 1
        val venueColumns = if (maxWidth >= 1180.dp) 3 else if (isExpanded) 2 else 1
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
                .then(if (contentMaxWidth != androidx.compose.ui.unit.Dp.Unspecified) Modifier.fillMaxWidth() else Modifier),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(modifier = Modifier.weight(1.12f)) {
                        HomeHeroCard()
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        VenueSearchCard(
                            query = venueQuery,
                            onQueryChange = { venueQuery = it },
                            onSearch = { onExploreVenues(venueQuery) },
                        )
                    }
                    Box(modifier = Modifier.weight(0.82f)) {
                        CodeEntryCard(
                            code = joinCode,
                            onCodeChange = { joinCode = it.uppercase() },
                            onSubmit = { onEnterCode(joinCode) },
                        )
                    }
                }
            } else {
                HomeHeroCard()
                VenueSearchCard(
                    query = venueQuery,
                    onQueryChange = { venueQuery = it },
                    onSearch = { onExploreVenues(venueQuery) },
                )
                CodeEntryCard(
                    code = joinCode,
                    onCodeChange = { joinCode = it.uppercase() },
                    onSubmit = { onEnterCode(joinCode) },
                )
            }

            HomeShortcutRow(
                onExploreVenues = { onExploreVenues(venueQuery) },
            )

            InvitationSection(
                invitations = invitations,
                onOpenInvitation = onOpenInvitation,
            )

            SectionLabel("Browse venue types")

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = categoryColumns,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                venueCategories.forEach { category ->
                    val cardModifier = if (categoryColumns == 1) Modifier.fillMaxWidth() else Modifier.weight(1f)
                    VenueCategoryCard(
                        category = category,
                        onClick = { onOpenCategory(category) },
                        modifier = cardModifier,
                    )
                }
            }

            SectionLabel("Featured venues")

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = venueColumns,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                featuredVenues.forEach { venue ->
                    val cardModifier = if (venueColumns == 1) Modifier.fillMaxWidth() else Modifier.weight(1f)
                    PublicVenueCard(
                        venue = venue,
                        onClick = { onOpenVenue(venue) },
                        modifier = cardModifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeroCard(
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        "Find your venue",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Conference rooms, wedding venues, and event spaces.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                ) {
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill("Venues", leadingIcon = Icons.Default.Search)
                MetaPill("Join with code", leadingIcon = Icons.Default.VpnKey)
                MetaPill("Invitations", leadingIcon = Icons.Default.Groups)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "Search a venue, enter a shared code, or open an invitation.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                    )
                    Text(
                        "Browse venue details and prices without signing in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeShortcutRow(
    onExploreVenues: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ShortcutPill(
            icon = Icons.Default.Search,
            label = "Venues",
            onClick = onExploreVenues,
        )
        ShortcutPill(
            icon = Icons.Default.VpnKey,
            label = "Use code",
            onClick = onExploreVenues,
        )
        ShortcutPill(
            icon = Icons.Default.Groups,
            label = "Invites",
            onClick = onExploreVenues,
        )
    }
}

@Composable
private fun ShortcutPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    KazeSecondaryButton(
        label = label,
        onClick = onClick,
        leadingIcon = icon,
    )
}

@Composable
private fun VenueSearchCard(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    CompactEntryCard(
        icon = Icons.Default.Search,
        title = "Venue search",
    ) {
        LuxurySingleLineField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Wedding venue or conference room",
            leadingIcon = Icons.Default.Search,
        )
        KazePrimaryButton(
            label = "Search",
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Default.Explore,
        )
    }
}

@Composable
private fun CodeEntryCard(
    code: String,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    CompactEntryCard(
        icon = Icons.Default.VpnKey,
        title = "Shared code",
    ) {
        LuxurySingleLineField(
            value = code,
            onValueChange = onCodeChange,
            placeholder = "EAFS24",
            leadingIcon = Icons.Default.Edit,
        )
        KazeSecondaryButton(
            label = "Open",
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Default.CalendarMonth,
            emphasized = true,
        )
    }
}

@Composable
private fun CompactEntryCard(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                ) {
                    Box(
                        modifier = Modifier.size(38.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            content()
        }
    }
}

@Composable
private fun LuxurySingleLineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
private fun InvitationSection(
    invitations: List<InvitationPreview>,
    onOpenInvitation: (InvitationPreview) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("Invitations")
        invitations.forEach { invitation ->
            InvitationCard(
                invitation = invitation,
                onClick = { onOpenInvitation(invitation) },
            )
        }
    }
}

@Composable
private fun InvitationCard(
    invitation: InvitationPreview,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        invitation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        invitation.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                    )
                }
                MetaPill(
                    invitation.statusLabel,
                    leadingIcon = Icons.Default.VpnKey,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill(invitation.code, leadingIcon = Icons.Default.Edit)
                MetaPill("Live event info", leadingIcon = Icons.Default.Groups)
            }
            KazeSecondaryButton(
                label = "Open",
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.CalendarMonth,
            )
        }
    }
}

@Composable
private fun VenueCategoryCard(
    category: VenueCategoryPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = category.categoryIcon()
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            MetaPill(category.supportingLabel, leadingIcon = icon)
            KazeSecondaryButton(
                label = "Open",
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = icon,
            )
        }
    }
}

@Composable
private fun PublicVenueCard(
    venue: PublicVenuePreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = venue.venueIcon()
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Text(
                        venue.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                    modifier = Modifier.size(54.dp),
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill(venue.typeLabel, leadingIcon = icon)
                MetaPill(venue.locationLabel, leadingIcon = Icons.Default.Place)
                MetaPill(venue.capacityLabel, leadingIcon = Icons.Default.Groups)
                MetaPill(
                    venue.priceLabel,
                    leadingIcon = Icons.Default.Payments,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Text(
                venue.accessLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
            KazePrimaryButton(
                label = "View",
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = icon,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
        )
    }
}

private fun VenueCategoryPreview.categoryIcon(): ImageVector = when (title) {
    "Hotels" -> Icons.Default.Hotel
    "Conference" -> Icons.Default.CalendarMonth
    "Wedding" -> Icons.Default.Favorite
    "Apartments" -> Icons.Default.Home
    else -> Icons.Default.Explore
}

private fun PublicVenuePreview.venueIcon(): ImageVector = when {
    typeLabel.contains("hotel", ignoreCase = true) -> Icons.Default.Hotel
    typeLabel.contains("conference", ignoreCase = true) -> Icons.Default.CalendarMonth
    typeLabel.contains("wedding", ignoreCase = true) -> Icons.Default.Favorite
    else -> Icons.Default.Explore
}
