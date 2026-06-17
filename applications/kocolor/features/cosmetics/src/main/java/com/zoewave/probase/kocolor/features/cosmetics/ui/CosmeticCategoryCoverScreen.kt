package com.zoewave.probase.kocolor.features.cosmetics.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.*
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale

data class CosmeticCategoryCoverUiState(
    val categoryName: String,
    val cosmeticsUiState: CosmeticsUiState
)

@Preview(showBackground = true)
@Composable
private fun CosmeticCategoryCoverScreenPreview() {
    MaterialTheme {
        CosmeticCategoryCoverScreen(
            uiState = CosmeticCategoryCoverUiState(
                categoryName = "Face",
                cosmeticsUiState = CosmeticsUiState(
                    items = listOf(
                        CosmeticItem(id = 1, name = "Silk Primer", brand = "KoColor", macroCategory = MacroCategory.PREP, microCategory = MicroCategory.PRIMER, usageCount = 45, price = 28.0, colorHex = "#F8F0E3"),
                        CosmeticItem(id = 2, name = "Cool Ivory", brand = "KoColor", macroCategory = MacroCategory.COMPLEXION, microCategory = MicroCategory.FOUNDATION, usageCount = 120, price = 42.0, colorHex = "#FAD4D4"),
                        CosmeticItem(id = 3, name = "Neutral Beige", brand = "KoColor", macroCategory = MacroCategory.COMPLEXION, microCategory = MicroCategory.FOUNDATION, usageCount = 5, price = 38.0, colorHex = "#EAD4B4")
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
fun CosmeticCategoryCoverScreen(
    uiState: CosmeticCategoryCoverUiState,
    modifier: Modifier = Modifier,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val categoryName = uiState.categoryName
    val state = uiState.cosmeticsUiState
    val items = remember(state.items, categoryName) {
        state.items.filter { it.macroCategory.displayName.contains(categoryName, ignoreCase = true) }
    }
    
    val totalValue = items.sumOf { it.price ?: 0.0 }
    val mostUsed = items.maxByOrNull { it.usageCount }
    val leastUsed = items.minByOrNull { it.usageCount }
    
    val bestValueItem = items.filter { it.costPerUse != null }.minByOrNull { it.costPerUse!! }
    val premiumItem = items.maxByOrNull { it.price ?: 0.0 }

    val avgCostPerUse = items.mapNotNull { it.costPerUse }.let { if (it.isEmpty()) null else it.average() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName.uppercase(), style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_back))
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.CosmeticAnalytics) }) { Icon(Icons.Default.Insights, null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navTo(KoColorRoute.CosmeticAdd(categoryFilter = categoryName)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_add_product))
            }
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
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_cosmetics_category_edit_format, categoryName),
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_cosmetics_category_desc_format, categoryName),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_category_intelligence), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 16.dp))
                    
                    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryStatCard(
                                uiState = CategoryStatUiState(
                                    title = stringResource(R.string.applications_kocolor_features_cosmetics_total_value), 
                                    value = currencyFormatter.format(totalValue)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            CategoryStatCard(
                                uiState = CategoryStatUiState(
                                    title = stringResource(R.string.applications_kocolor_features_cosmetics_avg_cost_use), 
                                    value = (avgCostPerUse?.let { currencyFormatter.format(it) } ?: stringResource(R.string.applications_kocolor_features_cosmetics_not_available))
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RankingStatCard(
                                uiState = RankingStatUiState(
                                    title = stringResource(R.string.applications_kocolor_features_cosmetics_most_used),
                                    item = mostUsed,
                                    icon = Icons.Default.Star
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            RankingStatCard(
                                uiState = RankingStatUiState(
                                    title = stringResource(R.string.applications_kocolor_features_cosmetics_best_value),
                                    item = bestValueItem,
                                    icon = Icons.Default.Savings
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RankingStatCard(
                                uiState = RankingStatUiState(
                                    title = stringResource(R.string.applications_kocolor_features_cosmetics_premium_choice),
                                    item = premiumItem,
                                    icon = Icons.Default.Diamond
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            RankingStatCard(
                                uiState = RankingStatUiState(
                                    title = stringResource(R.string.applications_kocolor_features_cosmetics_least_used),
                                    item = leastUsed,
                                    icon = Icons.Default.History
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                val insight = when(categoryName.lowercase()) {
                                    "face" -> "Pro Tip: Layer liquids before powders to prevent pilling."
                                    "eyes" -> "Expert Tip: Use a primer to increase pigment longevity."
                                    "lips" -> "Artist Note: Exfoliate before applying matte stains."
                                    "cheeks" -> "Styling: Apply blush slightly higher for a lifted look."
                                    else -> "Data Insight: You use these items in 40% of your rituals."
                                }
                                Text(text = insight, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_vault_selections), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }

            items(items) { item ->
                CosmeticProductGridCard(
                    uiState = item,
                    onEvent = {},
                    navTo = navTo
                )
            }
            
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}
