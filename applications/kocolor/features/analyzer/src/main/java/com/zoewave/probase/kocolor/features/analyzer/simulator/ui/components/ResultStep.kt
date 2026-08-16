package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulatorEvent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.*
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.PlaceholderResultCard
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.ResultCard
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun ResultStep(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Header & Rationale
        item {
            Column {
                Text(
                    text = stringResource(R.string.applications_kocolor_features_analyzer_simulator_blueprint),
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isLocalResult) Color.Gray else Color.Green)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isLocalResult) stringResource(R.string.applications_kocolor_features_analyzer_simulator_local_calc) else stringResource(R.string.applications_kocolor_features_analyzer_simulator_ai_optimized),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                uiState.rationale?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // 2. The Visual Blueprint (Side-by-Side)
        item {
            VisualBlueprintSection(
                data = uiState.visualBlueprintData,
                initialTab = uiState.selectedResultTab,
                onTabSelected = { onEvent(SimulatorEvent.SelectResultTab(it)) }
            )
        }

        // 3. The Atelier List
        item {
            val label = when (uiState.selectedResultTab) {
                ResultTab.FACE -> "COSMETIC ATELIER"
                ResultTab.CLOTHES -> "CLOTHING ATELIER"
                ResultTab.NAILS -> "NAIL ATELIER"
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                fontWeight = FontWeight.Black,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }

        when (uiState.selectedResultTab) {
            ResultTab.CLOTHES -> {
                if (uiState.recommendedClothing.isEmpty()) {
                    items(3) { i ->
                        PlaceholderResultCard(label = when (i) {
                            0 -> "Top"
                            1 -> "Bottom"
                            else -> "Shoes"
                        })
                    }
                } else {
                    items(uiState.recommendedClothing) { item ->
                        ResultCard(clothingItem = item, onEvent = {}, navTo = navTo)
                    }
                }
            }
            ResultTab.NAILS -> {
                val nailItems = uiState.recommendedCosmetics.filter { it.macroCategory == MacroCategory.NAILS }
                if (nailItems.isEmpty()) {
                    item { PlaceholderResultCard(label = "Nails") }
                } else {
                    items(nailItems) { item ->
                        ResultCard(cosmeticItem = item, onEvent = {}, navTo = navTo)
                    }
                }
            }
            else -> {
                val nonNailCosmetics = uiState.recommendedCosmetics.filter { it.macroCategory != MacroCategory.NAILS }
                if (nonNailCosmetics.isEmpty()) {
                    items(3) { i ->
                        PlaceholderResultCard(label = when (i) {
                            0 -> "Eyes"
                            1 -> "Cheeks"
                            else -> "Lips"
                        })
                    }
                } else {
                    items(nonNailCosmetics) { item ->
                        ResultCard(cosmeticItem = item, onEvent = {}, navTo = navTo)
                    }
                }
            }
        }

        item {
            Button(
                onClick = { onEvent(SimulatorEvent.SaveToPalette) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    stringResource(R.string.applications_kocolor_features_analyzer_simulator_lock_palette),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
