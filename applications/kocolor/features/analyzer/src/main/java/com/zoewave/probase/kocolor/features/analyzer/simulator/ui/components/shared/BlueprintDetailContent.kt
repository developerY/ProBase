package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.CollapsibleFashionistaScoreCard
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.FashionistaScoreGauge
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.ResultTab
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.VisualBlueprintData
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.VisualBlueprintSection
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.mapToVisualBlueprintData
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.PlaceholderResultCard
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.ResultCard
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun BlueprintDetailContent(
    title: String,
    rationale: String?,
    isLocalResult: Boolean,
    recommendedClothing: List<ClothingItem>,
    recommendedCosmetics: List<CosmeticItem>,
    recommendedPalette: List<String>,
    selectedResultTab: ResultTab,
    onTabSelected: (ResultTab) -> Unit,
    visualBlueprintData: VisualBlueprintData? = null,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    navTo: (KoColorRoute) -> Unit
) {
    val data = visualBlueprintData ?: mapToVisualBlueprintData(
        cosmetics = recommendedCosmetics,
        clothing = recommendedClothing,
        palette = recommendedPalette
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Header & Rationale
        item {
            Column {
                Text(
                    text = title,
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
                            .background(if (isLocalResult) Color.Gray else Color.Green)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isLocalResult) "LOCAL ARCHITECT" else "AI OPTIMIZED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                rationale?.let {
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
                data = data,
                initialTab = selectedResultTab,
                onTabSelected = onTabSelected
            )
        }

        // 3. The Atelier List
        item {
            val label = when (selectedResultTab) {
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

        when (selectedResultTab) {
            ResultTab.CLOTHES -> {
                if (recommendedClothing.isEmpty()) {
                    items(3) { i ->
                        PlaceholderResultCard(label = when (i) {
                            0 -> "Top"
                            1 -> "Bottom"
                            else -> "Shoes"
                        })
                    }
                } else {
                    items(recommendedClothing) { item ->
                        ResultCard(clothingItem = item, onEvent = {}, navTo = navTo)
                    }
                }
            }
            ResultTab.NAILS -> {
                val nailItems = recommendedCosmetics.filter { it.macroCategory == MacroCategory.NAILS }
                if (nailItems.isEmpty()) {
                    item { PlaceholderResultCard(label = "Nails") }
                } else {
                    items(nailItems) { item ->
                        ResultCard(cosmeticItem = item, onEvent = {}, navTo = navTo)
                    }
                }
            }
            else -> {
                val nonNailCosmetics = recommendedCosmetics.filter { it.macroCategory != MacroCategory.NAILS }
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
            CollapsibleFashionistaScoreCard(score = data.koColorScore)
        }

        if (actionButtonText != null && onActionClick != null) {
            item {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(
                        actionButtonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
