package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.components.CategoryStatCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.ClothingProductGridCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.RankingStatCard
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale

data class WardrobeCategoryCoverUiState(
    val categoryName: String,
    val wardrobeUiState: WardrobeUiState
)

@Preview(showBackground = true)
@Composable
private fun WardrobeCategoryCoverScreenPreview() {
    MaterialTheme {
        WardrobeCategoryCoverScreen(
            uiState = WardrobeCategoryCoverUiState(
                categoryName = "Tops",
                wardrobeUiState = WardrobeUiState(
                    items = listOf(
                        ClothingItem(id = 1, name = "Silk Shirt", brand = "Luxury", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS, price = 85.0, usageCount = 12),
                        ClothingItem(id = 2, name = "Cashmere Sweater", brand = "Premium", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS, price = 250.0, usageCount = 3)
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeCategoryCoverScreen(
    uiState: WardrobeCategoryCoverUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryName = uiState.categoryName
    val state = uiState.wardrobeUiState
    val items = remember(state.items, categoryName) {
        state.items.filter { it.category.name.contains(categoryName, ignoreCase = true) }
    }
    
    val totalInvestment = items.sumOf { it.price ?: 0.0 }
    val mostWorn = items.maxByOrNull { it.usageCount }
    val leastWorn = items.minByOrNull { it.usageCount }
    
    val bestValueItem = items.filter { it.costPerUse != null }.minByOrNull { it.costPerUse!! }
    val premiumPiece = items.maxByOrNull { it.price ?: 0.0 }
    val avgCostPerWear = items.mapNotNull { it.costPerUse }.let { if (it.isEmpty()) null else it.average() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName.uppercase(), style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_inventory_back))
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.WardrobeAnalytics) }) { Icon(Icons.Default.Insights, null) }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Editorial Header & Intelligence Dashboard
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_inventory_category_collection_format, categoryName),
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_inventory_category_performance_format, categoryName),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Text(stringResource(R.string.applications_kocolor_features_inventory_style_intelligence), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 16.dp))
                    
                    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryStatCard(uiState = stringResource(R.string.applications_kocolor_features_inventory_portfolio_value_label) to currencyFormatter.format(totalInvestment), modifier = Modifier.weight(1f))
                            CategoryStatCard(uiState = stringResource(R.string.applications_kocolor_features_inventory_avg_cpw_label) to (avgCostPerWear?.let { currencyFormatter.format(it) } ?: stringResource(R.string.applications_kocolor_features_inventory_not_available)), modifier = Modifier.weight(1f))
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RankingStatCard(
                                title = stringResource(R.string.applications_kocolor_features_inventory_most_worn_label),
                                item = mostWorn,
                                icon = Icons.Default.Star,
                                modifier = Modifier.weight(1f)
                            )
                            RankingStatCard(
                                title = stringResource(R.string.applications_kocolor_features_inventory_best_value_label),
                                item = bestValueItem,
                                icon = Icons.Default.Savings,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RankingStatCard(
                                title = stringResource(R.string.applications_kocolor_features_inventory_premium_piece_label),
                                item = premiumPiece,
                                icon = Icons.Default.Diamond,
                                modifier = Modifier.weight(1f)
                            )
                            RankingStatCard(
                                title = stringResource(R.string.applications_kocolor_features_inventory_least_worn_label),
                                item = leastWorn,
                                icon = Icons.Default.History,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Educational Insight
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                val insight = when(categoryName.lowercase()) {
                                    "tops" -> stringResource(R.string.applications_kocolor_features_inventory_pro_tip_tops)
                                    "bottoms" -> stringResource(R.string.applications_kocolor_features_inventory_pro_tip_bottoms)
                                    "shoes" -> stringResource(R.string.applications_kocolor_features_inventory_pro_tip_shoes)
                                    "accessories" -> stringResource(R.string.applications_kocolor_features_inventory_pro_tip_accessories)
                                    else -> stringResource(R.string.applications_kocolor_features_inventory_data_insight_default)
                                }
                                Text(text = insight, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    Text(stringResource(R.string.applications_kocolor_features_inventory_archive_entries_label), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }

            items(items) { item ->
                ClothingProductGridCard(uiState = item, navTo = navTo)
            }
            
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}
