package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import java.text.NumberFormat
import java.util.Locale

@Preview(showBackground = true)
@Composable
private fun WardrobeCategoryCoverScreenPreview() {
    MaterialTheme {
        WardrobeCategoryCoverScreen(
            uiState = "Tops" to WardrobeUiState(
                items = listOf(
                    com.zoewave.probase.kocolor.model.ClothingItem(id = 1, name = "Silk Shirt", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS, price = 85.0)
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
    uiState: Pair<String, WardrobeUiState>,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val categoryName = uiState.first
    val state = uiState.second
    val items = remember(state.items, categoryName) {
        state.items.filter { it.category.name.contains(categoryName, ignoreCase = true) }
    }
    
    val totalInvestment = items.sumOf { it.price ?: 0.0 }
    val recentItem = items.maxByOrNull { it.timestamp }

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
                        text = "The $categoryName Collection.",
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                        CategoryStatCard(uiState = "TOTAL INVESTMENT" to currencyFormatter.format(totalInvestment), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                        CategoryStatCard(uiState = "RECENT ADDITION" to (recentItem?.name ?: "None"), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                    }
                }
            }

            items(items) { item ->
                ClothingProductGridCard(uiState = item, onEvent = {}, navTo = navTo)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryStatCardPreview() {
    MaterialTheme {
        CategoryStatCard(uiState = "Label" to "Value", onEvent = {}, navTo = {})
    }
}

@Composable
private fun CategoryStatCard(uiState: Pair<String, String>, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit, modifier: Modifier = Modifier) {
    val title = uiState.first
    val value = uiState.second
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.Black, 
                modifier = Modifier.alpha(0.5f)
            )
            Spacer(Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClothingProductGridCardPreview() {
    MaterialTheme {
        ClothingProductGridCard(
            uiState = ClothingItem(id = 1, name = "Item", brand = "Brand", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
private fun ClothingProductGridCard(uiState: ClothingItem, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
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
            } else {
                Box(modifier = Modifier.fillMaxSize().background(item.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant))
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
