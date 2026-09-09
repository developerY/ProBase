package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.core.model.ritual.Temperature
import com.zoewave.probase.kocolor.data.usecase.IntentFulfillment
import com.zoewave.probase.kocolor.data.usecase.ObservedEnsembleMetrics
import com.zoewave.probase.kocolor.data.usecase.StyleIntentProfile
import com.zoewave.probase.kocolor.data.usecase.StyleIntentState
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScore
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.CollapsibleFashionistaScoreCard
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
    intentFulfillment: IntentFulfillment? = null,
    fashionistaScore: FashionistaScore? = null,
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

        blueprintAnalysisSection(
            isLocalResult = isLocalResult,
            data = data,
            intentFulfillment = intentFulfillment,
            rationale = rationale,
            navTo = navTo
        )

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

@Preview(showBackground = true, name = "Blueprint Detail Content Preview")
@Composable
private fun BlueprintDetailContentPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BlueprintDetailContent(
                title = "STYLE BLUEPRINT",
                rationale = "Selected an energetic Electric Coral Cropped Hoodie anchored with warm neutral pleated trousers and camel boots for an elevated, vibrant daily look.",
                isLocalResult = false,
                recommendedClothing = listOf(
                    ClothingItem(internalId = 3, name = "Electric Coral Cropped Hoodie", category = ClothingCategory.ACTIVEWEAR, material = "100% Organic Cotton", colorHex = "#FF5F1F"),
                    ClothingItem(internalId = 35, name = "Warm Ivory Pleated Trousers", category = ClothingCategory.BOTTOMS, material = "Cotton Blend", colorHex = "#EDD5B1"),
                    ClothingItem(internalId = 48, name = "Camel Leather Boots", category = ClothingCategory.SHOES, material = "Full Grain Leather", colorHex = "#BDA06A")
                ),
                recommendedCosmetics = listOf(
                    CosmeticItem(internalId = 123, name = "Golden Hour Shimmer", brand = "KoColor", macroCategory = MacroCategory.EYES, microCategory = MicroCategory.EYESHADOW, temperature = Temperature.NEUTRAL, colorHex = "#FFD700"),
                    CosmeticItem(internalId = 78, name = "Natural Peach Blush", brand = "KoColor", macroCategory = MacroCategory.DIMENSION, microCategory = MicroCategory.BLUSH, temperature = Temperature.WARM, colorHex = "#FFA07A"),
                    CosmeticItem(internalId = 114, name = "Warm Terracotta Lipstick", brand = "KoColor", macroCategory = MacroCategory.LIPS, microCategory = MicroCategory.LIPSTICK, temperature = Temperature.WARM, colorHex = "#C75B39"),
                    CosmeticItem(internalId = 133, name = "Cobalt Core Polish", brand = "KoColor", macroCategory = MacroCategory.NAILS, microCategory = MicroCategory.NAIL_POLISH, temperature = Temperature.COOL, colorHex = "#0047AB")
                ),
                recommendedPalette = listOf("#FF5F1F", "#EDD5B1", "#BDA06A", "#0047AB"),
                selectedResultTab = ResultTab.CLOTHES,
                onTabSelected = {},
                intentFulfillment = IntentFulfillment(
                    state = StyleIntentState.Specified(StyleIntentProfile()),
                    score = 91.2f,
                    observedMetrics = ObservedEnsembleMetrics(
                        colorfulness = 0.88f,
                        colorContrast = 0.82f,
                        novelty = 0.75f,
                        formality = 0.50f
                    ),
                    unmetIntent = emptyList()
                ),
                fashionistaScore = FashionistaScore(
                    colorHarmonyScore = 95.5f,
                    silhouetteScore = 85.0f,
                    contrastScore = 95.0f,
                    totalScore = 92.7f,
                    isApproved = true,
                    coverage = 1.0,
                    standardId = "FASHIONISTA_STD",
                    standardVersion = "v1.1"
                ),
                actionButtonText = "Save Advice to Collection",
                onActionClick = {},
                navTo = {}
            )
        }
    }
}
