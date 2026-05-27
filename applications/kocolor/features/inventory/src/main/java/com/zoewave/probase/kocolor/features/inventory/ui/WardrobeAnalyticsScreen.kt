package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeAnalyticsScreen(
    uiState: WardrobeUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("STYLE INTELLIGENCE", style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // 1. Headline
            item {
                Column {
                    Text(
                        text = "Your Style DNA.",
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quantitative analysis of your curated wardrobe.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // 2. High-Level Performance Metrics
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("PORTFOLIO PERFORMANCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                        AnalyticsStatCard(
                            label = "TOTAL VALUE", 
                            value = currencyFormatter.format(uiState.totalInvestment), 
                            icon = Icons.Default.MonetizationOn, 
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsStatCard(
                            label = "AVG CPW", 
                            value = uiState.items.mapNotNull { it.costPerUse }.let { if (it.isEmpty()) "---" else currencyFormatter.format(it.average()) }, 
                            icon = Icons.AutoMirrored.Filled.TrendingDown, 
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Usage Leaderboard (The "Most Worn")
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("MOST WORN PIECES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val topUsed = uiState.items.filter { it.usageCount > 0 }.sortedByDescending { it.usageCount }.take(5)
                    
                    if (topUsed.isEmpty()) {
                        Text("No wear history logged. Start recording your daily looks!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            topUsed.forEachIndexed { index, item ->
                                WearRankingRow(item = item, rank = index + 1, maxUsage = topUsed.first().usageCount)
                            }
                        }
                    }
                }
            }

            // 4. Wardrobe Palette (Chromatic Distribution)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("WARDROBE PALETTE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val colorDistribution = uiState.items.mapNotNull { it.colorHex ?: it.dominantHex }.groupBy { it }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
                    
                    if (colorDistribution.isEmpty()) {
                        Text("Capture garment colors to visualize your palette.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text(
                            text = "Your style is defined by ${colorDistribution.size} distinct tones.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 5. Investment vs Utility (Best CPW)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("STYLE EFFICIENCY (COST PER WEAR)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val bestValue = uiState.items.filter { it.costPerUse != null }.sortedBy { it.costPerUse }.take(5)
                    
                    if (bestValue.isEmpty()) {
                        Text("Log more wear events to calculate wardrobe efficiency.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            bestValue.forEach { item ->
                                WardrobeEfficiencyRow(item = item, label = "PER WEAR")
                            }
                        }
                    }
                }
            }

            // 6. Premium Assets (Highest Price)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("PORTFOLIO ASSETS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val premium = uiState.items.filter { it.price != null }.sortedByDescending { it.price }.take(3)
                    
                    if (premium.isEmpty()) {
                        Text("Add prices to see investment analytics.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            premium.forEach { item ->
                                WardrobeEfficiencyRow(item = item, label = "PRICE", usePrice = true)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

@Composable
private fun AnalyticsStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WearRankingRow(item: ClothingItem, rank: Int, maxUsage: Int) {
    val progress = item.usageCount.toFloat() / maxUsage.coerceAtLeast(1)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = rank.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = "${item.brand ?: "Archive"} · ${item.usageCount} wears", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(item.colorHex?.let { parseColor(it) } ?: item.dominantHex?.let { parseColor(it) } ?: Color.Gray)
                    .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun WardrobeEfficiencyRow(item: ClothingItem, label: String, usePrice: Boolean = false) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    val displayValue = if (usePrice) item.price ?: 0.0 else item.costPerUse ?: 0.0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(item.colorHex?.let { parseColor(it) } ?: item.dominantHex?.let { parseColor(it) } ?: Color.Gray)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = item.brand ?: "Archive", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = currencyFormatter.format(displayValue),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    ClothingItem(id = 1, name = "Silk Blazer", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS, usageCount = 12, colorHex = "#F5F5DC", price = 350.0),
                    ClothingItem(id = 2, name = "Denim Jeans", category = com.zoewave.probase.kocolor.model.ClothingCategory.BOTTOMS, usageCount = 45, colorHex = "#000080", price = 120.0)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
