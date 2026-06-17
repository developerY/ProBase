package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.components.AnalyticsStatCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.WardrobeEfficiencyRow
import com.zoewave.probase.kocolor.features.inventory.ui.components.WardrobeTaxonomyDialog
import com.zoewave.probase.kocolor.features.inventory.ui.components.WearRankingRow
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeAnalyticsScreen(
    uiState: WardrobeUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showTaxonomyInfo by remember { mutableStateOf(false) }

    if (showTaxonomyInfo) {
        WardrobeTaxonomyDialog(
            uiState = Unit,
            onEvent = { showTaxonomyInfo = false },
            navTo = {}
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_inventory_style_intelligence), style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_inventory_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showTaxonomyInfo = true }) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_inventory_info_icon),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_inventory_style_dna),
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_inventory_style_dna_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_inventory_portfolio_performance), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                        AnalyticsStatCard(
                            label = stringResource(R.string.applications_kocolor_features_inventory_total_value), 
                            value = currencyFormatter.format(uiState.totalInvestment), 
                            icon = Icons.Default.MonetizationOn, 
                            modifier = Modifier.weight(1f),
                            onClick = {}
                        )
                        AnalyticsStatCard(
                            label = stringResource(R.string.applications_kocolor_features_inventory_avg_cpw), 
                            value = uiState.items.mapNotNull { it.costPerUse }.let { if (it.isEmpty()) stringResource(R.string.applications_kocolor_features_inventory_not_available) else currencyFormatter.format(it.average()) }, 
                            icon = Icons.AutoMirrored.Filled.TrendingDown, 
                            modifier = Modifier.weight(1f),
                            onClick = {}
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_inventory_most_worn), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val topUsed = uiState.items.filter { it.usageCount > 0 }.sortedByDescending { it.usageCount }.take(5)
                    
                    if (topUsed.isEmpty()) {
                        Text(stringResource(R.string.applications_kocolor_features_inventory_no_wear_history), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            topUsed.forEachIndexed { index, item ->
                                WearRankingRow(item = item, rank = index + 1, maxUsage = topUsed.first().usageCount)
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_inventory_wardrobe_palette), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val colorDistribution = uiState.items.mapNotNull { it.colorHex ?: it.dominantHex }.groupBy { it }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
                    
                    if (colorDistribution.isEmpty()) {
                        Text(stringResource(R.string.applications_kocolor_features_inventory_palette_prompt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(16.dp)),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            colorDistribution.forEach { (hex, count) ->
                                Box(
                                    modifier = Modifier
                                        .weight(count.toFloat())
                                        .fillMaxHeight()
                                        .background(parseColor(hex))
                                        .border(0.5.dp, Color.Black.copy(alpha = 0.1f))
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_inventory_style_efficiency), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val bestValue = uiState.items.filter { it.costPerUse != null }.sortedBy { it.costPerUse }.take(5)
                    
                    if (bestValue.isEmpty()) {
                        Text(stringResource(R.string.applications_kocolor_features_inventory_efficiency_prompt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            bestValue.forEach { item ->
                                WardrobeEfficiencyRow(item = item, label = stringResource(R.string.applications_kocolor_features_inventory_per_wear))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WardrobeAnalyticsScreenPreview() {
    MaterialTheme {
        WardrobeAnalyticsScreen(
            uiState = WardrobeUiState(
                totalInvestment = 2450.0,
                items = listOf(
                    ClothingItem(id = 1, name = "Silk Blazer", category = ClothingCategory.TOPS, usageCount = 12, colorHex = "#F5F5DC", price = 350.0),
                    ClothingItem(id = 2, name = "Denim Jeans", category = ClothingCategory.BOTTOMS, usageCount = 45, colorHex = "#000080", price = 120.0)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
