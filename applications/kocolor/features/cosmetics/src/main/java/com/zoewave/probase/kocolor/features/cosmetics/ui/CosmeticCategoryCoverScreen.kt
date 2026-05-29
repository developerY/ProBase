package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.*
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Editorial Header & Category Intelligence Dashboard
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "The $categoryName Edit.",
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "A professional overview of your $categoryName essentials.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // --- CATEGORY INTELLIGENCE GRID ---
                    Text("CATEGORY INTELLIGENCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 16.dp))
                    
                    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryStatCard(uiState = "TOTAL VALUE" to currencyFormatter.format(totalValue), modifier = Modifier.weight(1f))
                            CategoryStatCard(uiState = "AVG COST/USE" to (avgCostPerUse?.let { currencyFormatter.format(it) } ?: "N/A"), modifier = Modifier.weight(1f))
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RankingStatCard(
                                title = "MOST USED",
                                item = mostUsed,
                                icon = Icons.Default.Star,
                                modifier = Modifier.weight(1f)
                            )
                            RankingStatCard(
                                title = "BEST VALUE",
                                item = bestValueItem,
                                icon = Icons.Default.Savings,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RankingStatCard(
                                title = "PREMIUM CHOICE",
                                item = premiumItem,
                                icon = Icons.Default.Diamond,
                                modifier = Modifier.weight(1f)
                            )
                            RankingStatCard(
                                title = "LEAST USED",
                                item = leastUsed,
                                icon = Icons.Default.History,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // NEW: Educational Insight
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
                    Text("VAULT SELECTIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }

            items(items) { item ->
                CosmeticProductGridCard(
                    uiState = item,
                    navTo = navTo
                )
            }
            
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun RankingStatCard(
    title: String,
    item: CosmeticItem?,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.alpha(0.5f)
                )
            }
            Spacer(Modifier.height(8.dp))
            if (item != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(item.colorHex?.let { parseColor(it) } ?: Color.Gray)
                            .border(0.5.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(text = "None yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun CategoryStatCard(
    uiState: Pair<String, String>, 
    modifier: Modifier = Modifier,
    isAlert: Boolean = false
) {
    val title = uiState.first
    val value = uiState.second
    val backgroundColor = if (isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) 
                          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.alpha(0.5f),
                color = contentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold, 
                maxLines = 1,
                color = contentColor
            )
        }
    }
}

@Composable
private fun CosmeticProductGridCard(
    uiState: CosmeticItem,
    navTo: (KoColorRoute) -> Unit
) {
    val item = uiState
    val onClick = { navTo(KoColorRoute.CosmeticDetail(item.id)) }
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.75f).clickable { onClick() },
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(item.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant))
            }

            // Scrim
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)), startY = 200f)))
            
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(text = item.brand, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Black)
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}
