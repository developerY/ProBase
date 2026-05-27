package com.zoewave.probase.kocolor.features.inventory.ui

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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
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
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
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
                        com.zoewave.probase.kocolor.model.ClothingItem(id = 1, name = "Silk Shirt", brand = "Luxury", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS, price = 85.0, usageCount = 12),
                        com.zoewave.probase.kocolor.model.ClothingItem(id = 2, name = "Cashmere Sweater", brand = "Premium", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS, price = 250.0, usageCount = 3)
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
    navTo: (KoColorRoute) -> Unit
) {
    val categoryName = uiState.categoryName
    val state = uiState.wardrobeUiState
    val items = remember(state.items, categoryName) {
        state.items.filter { it.category.name.contains(categoryName, ignoreCase = true) }
    }
    
    val totalInvestment = items.sumOf { it.price ?: 0.0 }
    val mostWorn = items.maxByOrNull { it.usageCount }
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
        }
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
                    
                    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryStatCard(uiState = "PORTFOLIO VALUE" to currencyFormatter.format(totalInvestment), modifier = Modifier.weight(1f))
                            CategoryStatCard(uiState = "AVG CPW" to (avgCostPerWear?.let { currencyFormatter.format(it) } ?: "N/A"), modifier = Modifier.weight(1f))
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RankingStatCard(
                                title = "MOST WORN",
                                item = mostWorn,
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
                                title = "PREMIUM PIECE",
                                item = premiumPiece,
                                icon = Icons.Default.Diamond,
                                modifier = Modifier.weight(1f)
                            )
                            CategoryStatCard(uiState = "PIECE COUNT" to items.size.toString(), modifier = Modifier.weight(1f))
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

@Composable
private fun RankingStatCard(
    title: String,
    item: ClothingItem?,
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
                            .background(item.colorHex?.let { parseColor(it) } ?: item.dominantHex?.let { parseColor(it) } ?: Color.Gray)
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
private fun ClothingProductGridCard(uiState: ClothingItem, navTo: (KoColorRoute) -> Unit) {
    val item = uiState
    val onClick = { navTo(KoColorRoute.WardrobeDetail(item.id)) }
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

                // Representative Color Badge
                val itemColor = item.dominantHex?.let { parseColor(it) } 
                    ?: item.colorHex?.let { parseColor(it) } 
                    ?: Color.White
                
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(20.dp)
                        .align(Alignment.TopEnd),
                    color = itemColor,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {}
            } else {
                val itemColor = item.dominantHex?.let { parseColor(it) } 
                    ?: item.colorHex?.let { parseColor(it) } 
                    ?: MaterialTheme.colorScheme.surfaceVariant
                Box(modifier = Modifier.fillMaxSize().background(itemColor))
            }

            // Scrim
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)), startY = 200f)))
            
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(text = item.brand ?: "", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Black)
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}
