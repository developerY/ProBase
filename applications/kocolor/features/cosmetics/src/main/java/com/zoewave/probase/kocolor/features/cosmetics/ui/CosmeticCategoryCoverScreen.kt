package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
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
                categoryName = "Lips",
                cosmeticsUiState = CosmeticsUiState(
                    items = listOf(
                        CosmeticItem(id = 1, name = "Lipstick", brand = "Luxury", category = com.zoewave.probase.kocolor.model.CosmeticCategory.LIPSTICK, price = 35.0)
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
        state.items.filter { it.category.groupName.contains(categoryName, ignoreCase = true) }
    }
    
    val totalValue = items.sumOf { it.price ?: 0.0 }
    val mostUsed = items.maxByOrNull { it.usageCount }
    val leastUsed = items.minByOrNull { it.usageCount }
    
    val mostExpensive = items.maxByOrNull { it.price ?: 0.0 }
    val leastExpensive = items.filter { (it.price ?: 0.0) > 0 }.minByOrNull { it.price ?: 0.0 }
    
    val bestValueItem = items.filter { it.costPerUse != null }.minByOrNull { it.costPerUse!! }
    val lowestRoiItem = items.filter { it.costPerUse != null }.maxByOrNull { it.costPerUse!! }

    val totalUses = items.sumOf { it.usageCount }
    val avgCostPerUse = items.mapNotNull { it.costPerUse }.let { if (it.isEmpty()) null else it.average() }
    
    val now = System.currentTimeMillis()
    val thirtyDays = 30L * 24 * 60 * 60 * 1000
    val expiringCount = items.count { it.estimatedExpiry?.let { exp -> (exp - now) in 0..thirtyDays } ?: false }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName.uppercase(), style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
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
            // Editorial Header
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "The $categoryName Edit.",
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryStatCard(uiState = "TOTAL VALUE" to currencyFormatter.format(totalValue), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                            CategoryStatCard(uiState = "ITEMS" to items.size.toString(), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryStatCard(uiState = "MOST USED" to (mostUsed?.name ?: "None"), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                            CategoryStatCard(uiState = "LEAST USED" to (leastUsed?.name ?: "None"), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryStatCard(uiState = "MOST EXPENSIVE" to (mostExpensive?.let { currencyFormatter.format(it.price ?: 0.0) } ?: "N/A"), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                            CategoryStatCard(uiState = "LEAST EXPENSIVE" to (leastExpensive?.let { currencyFormatter.format(it.price ?: 0.0) } ?: "N/A"), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryStatCard(uiState = "BEST VALUE (CPU)" to (bestValueItem?.let { currencyFormatter.format(it.costPerUse ?: 0.0) } ?: "N/A"), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                            CategoryStatCard(uiState = "LOWEST ROI (CPU)" to (lowestRoiItem?.let { currencyFormatter.format(it.costPerUse ?: 0.0) } ?: "N/A"), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoryStatCard(uiState = "AVG COST/USE" to (avgCostPerUse?.let { currencyFormatter.format(it) } ?: "N/A"), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                            CategoryStatCard(uiState = "TOTAL USES" to totalUses.toString(), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                        }
                        if (expiringCount > 0) {
                            CategoryStatCard(
                                uiState = "EXPIRING SOON" to "$expiringCount ITEMS", 
                                onEvent = {}, 
                                navTo = {}, 
                                modifier = Modifier.fillMaxWidth(),
                                isAlert = true
                            )
                        }
                    }
                }
            }

            items(items) { item ->
                CosmeticProductGridCard(
                    uiState = item,
                    onEvent = {},
                    navTo = navTo
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryStatCardPreview() {
    MaterialTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryStatCard(uiState = "NORMAL" to "Value", onEvent = {}, navTo = {})
            CategoryStatCard(uiState = "ALERT" to "Alert Value", onEvent = {}, navTo = {}, isAlert = true)
        }
    }
}

@Composable
private fun CategoryStatCard(
    uiState: Pair<String, String>, 
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit, 
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

@Preview(showBackground = true)
@Composable
private fun CosmeticProductGridCardPreview() {
    MaterialTheme {
        CosmeticProductGridCard(
            uiState = CosmeticItem(id = 1, name = "Item", brand = "Brand", category = com.zoewave.probase.kocolor.model.CosmeticCategory.LIPSTICK),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
private fun CosmeticProductGridCard(
    uiState: CosmeticItem,
    onEvent: (Unit) -> Unit,
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

