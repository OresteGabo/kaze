package dev.orestegabo.kaze.ui.stay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.orestegabo.kaze.domain.DigitalAccessCard
import dev.orestegabo.kaze.presentation.demo.StayMoment
import dev.orestegabo.kaze.presentation.demo.StayPrimaryAction
import dev.orestegabo.kaze.ui.access.StayAccessCardSection
import dev.orestegabo.kaze.ui.components.KazeGhostButton
import dev.orestegabo.kaze.ui.components.KazePrimaryButton
import dev.orestegabo.kaze.ui.components.KazeSecondaryButton
import dev.orestegabo.kaze.ui.components.MetaPill

@Composable
internal fun StayTabContent(
    accessCard: DigitalAccessCard?,
    stayMoments: List<StayMoment>,
    onPrimaryAction: (StayPrimaryAction) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        accessCard?.let { StayAccessCardSection(card = it) }
        StayStatusHero(
            onOpenRoute = { onPrimaryAction(StayPrimaryAction.OPEN_ROUTE) },
            onViewFolio = { onPrimaryAction(StayPrimaryAction.VIEW_FOLIO) },
        )
        stayMoments.forEach { moment ->
            StayMomentCard(
                moment = moment,
                onOpen = { onPrimaryAction(StayPrimaryAction.OpenStayMoment(moment)) },
            )
        }
    }
}

@Composable
internal fun CompactStayHeader(
    hotelName: String,
    guestName: String,
    roomLabel: String,
    stayLabel: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        )
        {
            Text(
                hotelName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
            )
            Text("Welcome, $guestName", style = MaterialTheme.typography.titleLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    roomLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                )
            }
        }
        Spacer(Modifier.weight(0.05f))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.Transparent)
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        )
        {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
            )
            Text(
                stayLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
internal fun StayStatusHero(
    onOpenRoute: () -> Unit,
    onViewFolio: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Ready now", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Open the map for your next destination or review your current hotel charges.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KazePrimaryButton(
                    label = "Open route",
                    onClick = onOpenRoute,
                    leadingIcon = Icons.Default.Map,
                )
                KazeGhostButton(
                    label = "My charges",
                    onClick = onViewFolio,
                    leadingIcon = Icons.Default.ReceiptLong,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StayMomentCard(
    moment: StayMoment,
    onOpen: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // LEFT COLUMN (Timeline)
            Column(
                modifier = Modifier
                    .width(70.dp)
                    .fillMaxHeight(), // This now stretches to the Row's final height
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val timeParts = moment.time.split("•").map { it.trim() }

                Text(
                    timeParts.firstOrNull().orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    timeParts.getOrElse(1) { moment.time },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // The flexible line that connects top to bottom
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                            shape = CircleShape,
                        ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom "Until" box
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("UNTIL", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp))
                        Text(moment.endTime, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // RIGHT COLUMN (Content)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), // Important so Row knows this column's height matters
                verticalArrangement = Arrangement.SpaceBetween // Pushes the button to the bottom
            ) {
                Column {
                    Text(
                        moment.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        moment.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MetaPill(label = moment.place, leadingIcon = Icons.Default.Place)
                        MetaPill(label = "Scheduled", leadingIcon = Icons.Default.Schedule)
                    }
                }

                // SPACER ensures there is a gap between text and button
                Spacer(modifier = Modifier.height(16.dp))

                // THE BUTTON - Using wrapContentHeight to prevent squishing
                KazeSecondaryButton(
                    label = moment.action,
                    onClick = onOpen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp) // Fixed height is now safe because of IntrinsicSize.Min
                        .wrapContentHeight(unbounded = true), // If the layout tries to shrink it, it will "overflow" instead of squishing
                    leadingIcon = Icons.Default.Map,
                )
            }
        }
    }
}
