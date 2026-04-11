package dev.orestegabo.kaze.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.presentation.demo.InvitationPreview
import dev.orestegabo.kaze.presentation.demo.PublicVenuePreview
import dev.orestegabo.kaze.presentation.demo.VenueCategoryPreview
import dev.orestegabo.kaze.theme.KazeThemeMode
import dev.orestegabo.kaze.ui.home.components.*

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    venueCategories: List<VenueCategoryPreview>,
    featuredVenues: List<PublicVenuePreview>,
    invitations: List<InvitationPreview>,
    onEnterCode: (String) -> Unit,
    onOpenCategory: (VenueCategoryPreview) -> Unit,
    onOpenVenue: (PublicVenuePreview) -> Unit,
    onOpenVenueMap: (PublicVenuePreview) -> Unit,
    onOpenInvitation: (InvitationPreview) -> Unit,
    onSeeAllInvitations: () -> Unit,
    themeMode: KazeThemeMode,
    onThemeModeChange: (KazeThemeMode) -> Unit,
    bottomContentPadding: Dp = 20.dp,
) {
    var joinCode by rememberSaveable { mutableStateOf("") }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var selectedServiceQuery by rememberSaveable { mutableStateOf<String?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 860.dp
        val contentMaxWidth = if (isExpanded) 1180.dp else androidx.compose.ui.unit.Dp.Unspecified
        val categoryColumns = if (maxWidth >= 1160.dp) 4 else if (isExpanded) 2 else 1
        val venueColumns = if (maxWidth >= 1180.dp) 3 else if (isExpanded) 2 else 1
        val scrollState = rememberScrollState()

        if (showSettings) {
            HomeSettingsScreen(
                bottomContentPadding = bottomContentPadding,
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                onBack = { showSettings = false },
            )
            return@BoxWithConstraints
        }

        selectedServiceQuery?.let { serviceQuery ->
            HomeServiceDetailScreen(
                serviceQuery = serviceQuery,
                bottomContentPadding = bottomContentPadding,
                onBack = { selectedServiceQuery = null },
            )
            return@BoxWithConstraints
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = bottomContentPadding)
                .then(if (contentMaxWidth != androidx.compose.ui.unit.Dp.Unspecified) Modifier.fillMaxWidth() else Modifier),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeTopBar(
                onOpenSettings = { showSettings = true },
            )

            if (isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(modifier = Modifier.weight(1.12f)) {
                        HomeHeroCard()
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
                CodeEntryCard(
                    code = joinCode,
                    onCodeChange = { joinCode = it.uppercase() },
                    onSubmit = { onEnterCode(joinCode) },
                )
            }

            HomeServiceRail(
                onOpenService = { serviceQuery -> selectedServiceQuery = serviceQuery },
            )

            InvitationSection(
                invitations = invitations,
                onOpenInvitation = onOpenInvitation,
                onSeeAll = onSeeAllInvitations,
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
                        onOpenMap = { onOpenVenueMap(venue) },
                        modifier = cardModifier,
                    )
                }
            }
        }
    }
}
