package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageMetricsScreen(
    uiState: WardrobeUiState,
    modifier: Modifier = Modifier,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "BEHAVIOR",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF9F9F9)
                )
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        val restingItems = remember(uiState.items) {
            val fortyEightHoursAgo = System.currentTimeMillis() - (48 * 60 * 60 * 1000L)
            uiState.items.filter { (it.lastUsedTimestamp ?: 0L) > fortyEightHoursAgo }
        }

        val heroes = remember(uiState.items) {
            uiState.items.sortedByDescending { it.usageCount }.take(3)
        }

        val buckets = remember(uiState.items) {
            listOf(
                "Never Worn" to uiState.items.filter { it.usageCount == 0 },
                "1–5 Wears" to uiState.items.filter { it.usageCount in 1..5 },
                "6–10 Wears" to uiState.items.filter { it.usageCount in 6..10 },
                "11–20 Wears" to uiState.items.filter { it.usageCount in 11..20 },
                "20+ Wears" to uiState.items.filter { it.usageCount > 20 }
            )
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Recently Resting Section
            if (restingItems.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "RESTING NOW",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF5A3854).copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, Color(0xFF5A3854).copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "${restingItems.size} pieces are temporarily deprioritized by your Style Architect.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF5A3854),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(12.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(restingItems) { item ->
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(item.dominantHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.LightGray)
                                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Wardrobe Heroes
            if (heroes.any { it.usageCount > 0 }) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "WARDROBE HEROES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        heroes.forEach { hero ->
                            HeroItemCard(hero)
                        }
                    }
                }
            }

            // 3. Frequency Buckets
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "USAGE DISTRIBUTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    
                    buckets.forEach { (label, items) ->
                        if (items.isNotEmpty()) {
                            BucketRow(label, items.size, uiState.totalItems)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroItemCard(item: ClothingItem) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.dominantHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.LightGray)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("${item.category.name}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                text = "${item.usageCount}",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                color = Color.Black
            )
        }
    }
}

@Composable
private fun BucketRow(label: String, count: Int, total: Int) {
    val percentage = if (total > 0) count.toFloat() / total else 0f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("${count} pieces", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = Color.Black,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

@Preview
@Composable
fun UsageMetricsScreenPreview() {
    MaterialTheme {
        UsageMetricsScreen(
            uiState = WardrobeUiState(
                totalItems = 10,
                items = listOf(
                    ClothingItem(name = "Silk Blazer", category = ClothingCategory.TOPS, usageCount = 25, colorHex = "#000000", dominantHex = "#000000"),
                    ClothingItem(name = "Linen Pants", category = ClothingCategory.BOTTOMS, usageCount = 4, colorHex = "#F5F5DC", dominantHex = "#F5F5DC"),
                    ClothingItem(name = "Unworn Hat", category = ClothingCategory.ACCESSORIES, usageCount = 0, colorHex = "#FF0000", dominantHex = "#FF0000")
                )
            ),
            navTo = {}
        )
    }
}
