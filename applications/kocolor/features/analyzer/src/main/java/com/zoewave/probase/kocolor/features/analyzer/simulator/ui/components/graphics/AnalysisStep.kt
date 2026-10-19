package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulationStep
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulatorEvent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlinx.coroutines.delay

private val FALLBACK_THUMBNAILS = listOf(
    "https://cdn.kocolor.com/inventory/dist/assets/hero/kc-prep-01.webp",
    "https://cdn.kocolor.com/inventory/dist/assets/hero/kc-pants-408.webp",
    "https://cdn.kocolor.com/inventory/dist/assets/hero/kc-outer-301.webp",
    "https://cdn.kocolor.com/inventory/dist/assets/hero/kc-top-102.webp"
)

@Composable
fun AnalysisStep(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    // Gather all available image URLs from inventory + fallback starter pack assets
    val availableThumbnails = remember(uiState.fullClothingInventory, uiState.fullCosmeticInventory) {
        val inventoryUrls = (uiState.fullClothingInventory.mapNotNull { it.imageUrl } +
                uiState.fullCosmeticInventory.mapNotNull { it.imageUrl })
            .filter { it.isNotBlank() }
        if (inventoryUrls.isNotEmpty()) inventoryUrls else FALLBACK_THUMBNAILS
    }

    var currentImageIndex by remember { mutableIntStateOf(0) }

    // Periodically cycle through random item thumbnails
    LaunchedEffect(availableThumbnails) {
        while (true) {
            delay(2000L)
            if (availableThumbnails.isNotEmpty()) {
                currentImageIndex = (currentImageIndex + 1) % availableThumbnails.size
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(16.dp))

        // --- Center Section: Pulsing Ring + Item Thumbnail ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f, targetValue = 1.05f,
                animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )

                val currentUrl = availableThumbnails.getOrNull(currentImageIndex)

                AnimatedContent(
                    targetState = currentUrl,
                    transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) },
                    label = "thumbnailCrossfade"
                ) { targetUrl ->
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, Color(0xFFD4AF37), CircleShape), // Gold accent
                        contentAlignment = Alignment.Center
                    ) {
                        if (targetUrl != null) {
                            AsyncImage(
                                model = targetUrl,
                                contentDescription = "KoColor Catalog Item",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = when (uiState.simulationStep) {
                                    SimulationStep.APPEARANCE_ANALYSIS -> Icons.Default.Favorite
                                    SimulationStep.ROUTINE -> Icons.Default.AutoAwesome
                                    else -> Icons.Default.Grain
                                },
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = when (uiState.simulationStep) {
                    SimulationStep.APPEARANCE_ANALYSIS -> stringResource(R.string.applications_kocolor_features_analyzer_simulator_reading_appearance)
                    SimulationStep.ROUTINE -> stringResource(R.string.applications_kocolor_features_analyzer_simulator_syncing_rituals)
                    SimulationStep.GENERATING -> stringResource(R.string.applications_kocolor_features_analyzer_simulator_architecting_style)
                    else -> stringResource(R.string.applications_kocolor_features_analyzer_simulator_magic)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.applications_kocolor_features_analyzer_simulator_synthesizing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.width(280.dp),
                textAlign = TextAlign.Center
            )
        }

        // --- Bottom Section: Current AI Engine & Pipeline Activity Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFBF8F5) // Soft luxury background
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF4CAF50), CircleShape) // Active Green Dot
                        )
                        Text(
                            text = "AI Engine",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37) // Gold
                        )
                    }

                    Text(
                        text = when (uiState.simulationStep) {
                            SimulationStep.APPEARANCE_ANALYSIS -> "Stage 1/3 • Telemetry"
                            SimulationStep.ROUTINE -> "Stage 2/3 • Ritual Sync"
                            SimulationStep.GENERATING -> "Stage 3/3 • Synthesis"
                            else -> "Processing"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = uiState.activeAiProvider.ifBlank { "Determining Active Provider..." },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalysisStepPreview() {
    MaterialTheme {
        AnalysisStep(
            uiState = StyleSimulatorUiState(
                activeAiProvider = "Gemma 4 Cloud (Free Tier) - Default",
                simulationStep = SimulationStep.APPEARANCE_ANALYSIS
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
