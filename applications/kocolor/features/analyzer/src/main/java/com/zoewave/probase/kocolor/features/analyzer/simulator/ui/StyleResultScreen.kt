package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleIntentProfile
import com.zoewave.probase.kocolor.data.usecase.StyleIntentState
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScore
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleResultScreen(
    uiState: StyleResultUiState,
    modifier: Modifier = Modifier,
    onEvent: (SimulatorEvent) -> Unit = {},
    navTo: (KoColorRoute) -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "FASHIONISTA ANALYSIS",
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 4.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { padding ->
        StyleResultContent(
            uiState = uiState,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleResultContent(
    uiState: StyleResultUiState,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyzing Style Telemetry & Generating Blueprint...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            val columnModifier = if (isScrollable) {
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            }

            Column(
                modifier = columnModifier,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. FASHIONISTA Badge Component
                uiState.fashionistaScore?.let { score ->
                    FashionistaScoreBadge(score = score)
                }

                // 2. Intent Fulfillment Component
                uiState.intentFulfillment?.let { intentFulfillment ->
                    IntentFulfillmentCard(fulfillment = intentFulfillment)
                }

                // 3. AI Rationale Container
                uiState.blueprint?.rationale?.let { rationale ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "STYLE ARCHITECT RATIONALE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = rationale,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 3. Outfit Card Component
                if (uiState.selectedClothing.isNotEmpty()) {
                    Text(
                        text = "SELECTED OUTFIT ENSEMBLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    uiState.selectedClothing.forEach { item ->
                        ClothingItemCard(item = item)
                    }
                }

                // Recommended Palette Swatches
                uiState.blueprint?.recommendedPalette?.let { palette ->
                    if (palette.isNotEmpty()) {
                        Column {
                            Text(
                                text = "RECOMMENDED PALETTE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                palette.forEach { hex ->
                                    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Cosmetic Grid Component
                if (uiState.selectedCosmetics.isNotEmpty()) {
                    Text(
                        text = "COSMETIC SELECTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.selectedCosmetics.forEach { item ->
                            CosmeticRoleCard(item = item)
                        }
                    }
                }

                // Error Message
                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun FashionistaScoreBadge(score: FashionistaScore) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "ScoreChevronRotation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FASHIONISTA SCORE (${score.standardId} ${score.standardVersion})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${score.totalScore.roundToInt()}/100",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Coverage: ${(score.coverage * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (score.isApproved) Color(0xFF10B981) else Color(0xFFEF4444)
                    ) {
                        Text(
                            text = if (score.isApproved) "APPROVED" else "REJECTED",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "Collapse Score Breakdown" else "Expand Score Breakdown",
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            // Expandable Sub-Score Breakdown Card under FASHIONISTA Score
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AESTHETIC CALIBRATION BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    ScoreMetricRow(label = "Color Harmony", score = score.colorHarmonyScore)
                    ScoreMetricRow(label = "Silhouette Proportion", score = score.silhouetteScore)
                    ScoreMetricRow(label = "Contrast & Depth", score = score.contrastScore)
                }
            }
        }
    }
}

@Composable
private fun ScoreMetricRow(label: String, score: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${score.roundToInt()}/100",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun IntentFulfillmentCard(fulfillment: IntentFulfillment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val scoreVal = fulfillment.score
            if (fulfillment.isSpecified && scoreVal != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "YOUR REQUEST FULFILLMENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${scoreVal.roundToInt()}/100",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (scoreVal >= 70f) Color(0xFF3B82F6) else Color(0xFFF59E0B)
                    ) {
                        Text(
                            text = if (scoreVal >= 70f) "MATCHED" else "PARTIAL",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                if (fulfillment.unmetIntent.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unmet Intent: ${fulfillment.unmetIntent.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Column {
                    Text(
                        text = "STYLE CHARACTER (NO INTENT SPECIFIED)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ensemble Colorfulness",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%.2f".format(fulfillment.observedMetrics.colorfulness),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClothingItemCard(item: ClothingItem) {
    val color = try { Color(android.graphics.Color.parseColor(item.colorHex)) } catch (e: Exception) { Color.Gray }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = item.category.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                item.material?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun CosmeticRoleCard(item: CosmeticItem) {
    val color = try { Color(android.graphics.Color.parseColor(item.colorHex)) } catch (e: Exception) { Color.Gray }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = item.macroCategory.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 9.sp
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3F4F6),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = item.temperature.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Style Result Preview")
@Composable
private fun StyleResultScreenPreview() {
    MaterialTheme {
        StyleResultContent(
            uiState = StyleResultUiState(
                blueprint = StyleBlueprint(
                    rationale = "Harmonic vibrant look designed for a warm daily occasion.",
                    selectedClothingIds = listOf("w_3", "w_35", "w_48"),
                    selectedCosmeticIds = listOf("c_123", "c_78", "c_114", "c_133"),
                    recommendedPalette = listOf("#FF5F1F", "#EDD5B1", "#BDA06A", "#0047AB")
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
                selectedClothing = listOf(
                    ClothingItem(internalId = 3, name = "Electric Coral Cropped Hoodie", category = ClothingCategory.ACTIVEWEAR, material = "100% Organic Cotton", colorHex = "#FF5F1F"),
                    ClothingItem(internalId = 35, name = "Warm Ivory Pleated Trousers", category = ClothingCategory.BOTTOMS, material = "Cotton Blend", colorHex = "#EDD5B1"),
                    ClothingItem(internalId = 48, name = "Camel Leather Boots", category = ClothingCategory.SHOES, material = "Full Grain Leather", colorHex = "#BDA06A")
                ),
                selectedCosmetics = listOf(
                    CosmeticItem(internalId = 123, name = "Golden Hour Shimmer", brand = "KoColor", macroCategory = MacroCategory.EYES, microCategory = MicroCategory.EYESHADOW, temperature = Temperature.NEUTRAL, colorHex = "#FFD700"),
                    CosmeticItem(internalId = 78, name = "Natural Peach Blush", brand = "KoColor", macroCategory = MacroCategory.DIMENSION, microCategory = MicroCategory.BLUSH, temperature = Temperature.WARM, colorHex = "#FFA07A"),
                    CosmeticItem(internalId = 114, name = "Warm Terracotta Lipstick", brand = "KoColor", macroCategory = MacroCategory.LIPS, microCategory = MicroCategory.LIPSTICK, temperature = Temperature.WARM, colorHex = "#C75B39"),
                    CosmeticItem(internalId = 133, name = "Cobalt Core Polish", brand = "KoColor", macroCategory = MacroCategory.NAILS, microCategory = MicroCategory.NAIL_POLISH, temperature = Temperature.COOL, colorHex = "#0047AB")
                ),
                isLoading = false
            )
        )
    }
}
