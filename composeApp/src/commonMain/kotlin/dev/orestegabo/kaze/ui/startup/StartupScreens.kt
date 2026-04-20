package dev.orestegabo.kaze.ui.startup

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.orestegabo.kaze.ui.chrome.KazeAmbientBackground
import dev.orestegabo.kaze.ui.states.KazeOfflineStateCard
import kaze.composeapp.generated.resources.Res
import kaze.composeapp.generated.resources.gabo_launch_branding_raster
import kaze.composeapp.generated.resources.kaze_launch_logo_polished_raster
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun KazeStartupScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        KazeAmbientBackground(modifier = Modifier.fillMaxSize())
        Surface(
            modifier = Modifier
                .size(136.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(42.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
            shadowElevation = 18.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(Res.drawable.kaze_launch_logo_polished_raster),
                    contentDescription = "Kaze logo",
                    modifier = Modifier.size(112.dp),
                )
            }
        }
        Text(
            text = "Kaze",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 230.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
        )
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 306.dp)
                .size(22.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.58f),
        )
        GaboSignature(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
        )
    }
}

@Composable
internal fun KazeTemporaryDownScreen(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onContinueOffline: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        KazeAmbientBackground(modifier = Modifier.fillMaxSize())
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            shape = RoundedCornerShape(36.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(82.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.kaze_launch_logo_polished_raster),
                            contentDescription = "Kaze logo",
                            modifier = Modifier.size(72.dp),
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.70f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = "Temporary service delay",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                Text(
                    text = "Kaze needs a moment",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Our service may be warming up or a startup check did not finish in time.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                KazeOfflineStateCard(
                    onRetryResult = { isOnline ->
                        if (isOnline) onRetry()
                    },
                )
                OutlinedButton(onClick = onContinueOffline) {
                    Text("Continue offline")
                }
            }
        }
        GaboSignature(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        )
    }
}

@Composable
private fun GaboSignature(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.alpha(0.88f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.gabo_launch_branding_raster),
            contentDescription = "GABO logo",
            modifier = Modifier.height(38.dp),
        )
    }
}
