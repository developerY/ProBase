package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.features.inventory.ui.components.CategoryStatCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.CategoryStatUiState
import com.zoewave.probase.kocolor.features.inventory.ui.components.ClothingProductGridCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.RankingStatCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.RankingStatUiState
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
                        ClothingItem(id = 1, name = "Silk Shirt", brand = "Luxury", category = ClothingCategory.TOPS, price = 85.0, usageCount = 12, colorHex = "#FFFFFF"),
                        ClothingItem(id = 2, name = "Cashmere Sweater", brand = "Premium", category = ClothingCategory.TOPS, price = 250.0, usageCount = 3, colorHex = "#FFFFFF")
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
    modifier: Modifier = Modifier,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "The $categoryName Collection.",
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Strategic performance of your $categoryName curated closet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Text("CATEGORY INTELLIGENCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 16.dp))
                    
                    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryStatCard(
                                uiState = CategoryStatUiState(
                                    title = "PORTFOLIO VALUE", 
                                    value = currencyFormatter.format(totalInvestment)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            CategoryStatCard(
                                uiState = CategoryStatUiState(
                                    title = "AVG CPW", 
                                    value = (avgCostPerWear?.let { currencyFormatter.format(it) } ?: "N/A")
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RankingStatCard(
                                uiState = RankingStatUiState(
                                    title = "MOST WORN",
                                    item = mostWorn,
                                    icon = Icons.Default.Star
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            RankingStatCard(
                                uiState = RankingStatUiState(
                                    title = "BEST VALUE",
                                    item = bestValueItem,
                                    icon = Icons.Default.Savings
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RankingStatCard(
                                uiState = RankingStatUiState(
                                    title = "PREMIUM PIECE",
                                    item = premiumPiece,
                                    icon = Icons.Default.Diamond
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            RankingStatCard(
                                uiState = RankingStatUiState(
                                    title = "LEAST WORN",
                                    item = leastWorn,
                                    icon = Icons.Default.History
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                val insight = when(categoryName.lowercase()) {
                                    "tops" -> "Sustainability: Choose natural fibers like silk for better cost-per-wear longevity."
                                    "bottoms" -> "Style Note: Darker tones in your bottoms provide a solid foundation for varied pairings."
                                    "shoes" -> "Expert Tip: Rotating your footwear extends the life of the materials."
                                    "accessories" -> "Investment: High-quality accessories are the easiest way to elevate a standard look."
                                    else -> "Wardrobe Data: You've maximized your investment in 20% of this collection."
                                }
                                Text(text = insight, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    Text("ARCHIVE ENTRIES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
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
