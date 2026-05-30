package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.model.*
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticAnalyticsScreen(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showTaxonomyInfo by remember { mutableStateOf(false) }

    if (showTaxonomyInfo) {
        ProfessionalTaxonomyDialog(onDismiss = { showTaxonomyInfo = false })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("VANITY INTELLIGENCE", style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
                        text = "Beauty Blueprint.",
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Data-driven insights from the curated collection.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // 2. High-Level Performance Metrics
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("COLLECTION PERFORMANCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                        AnalyticsStatCard(
                            label = "TOTAL USES", 
                            value = uiState.items.sumOf { it.usageCount }.toString(), 
                            icon = Icons.Default.History, 
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsStatCard(
                            label = "AVG CPU", 
                            value = uiState.items.mapNotNull { it.costPerUse }.let { if (it.isEmpty()) "---" else currencyFormatter.format(it.average()) }, 
                            icon = Icons.AutoMirrored.Filled.TrendingDown, 
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Usage Leaderboard
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("USAGE LEADERBOARD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val topUsed = uiState.items.filter { it.usageCount > 0 }.sortedByDescending { it.usageCount }.take(5)
                    
                    if (topUsed.isEmpty()) {
                        Text("No usage data available yet. Start logging your rituals!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            topUsed.forEachIndexed { index, item ->
                                UsageRankingRow(item = item, rank = index + 1, maxUsage = topUsed.first().usageCount)
                            }
                        }
                    }
                }
            }

            // 4. Chromatic Core
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("CHROMATIC CORE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val colorDistribution = uiState.items.mapNotNull { it.colorHex }.groupBy { it }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
                    
                    if (colorDistribution.isEmpty()) {
                        Text("Capture more product colors to see your palette.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            // 5. Efficiency Analysis
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("STYLE EFFICIENCY (BEST VALUE)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val bestValue = uiState.items.filter { it.costPerUse != null }.sortedBy { it.costPerUse }.take(3)
                    
                    if (bestValue.isEmpty()) {
                        Text("Complete more usage cycles to see performance data.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            bestValue.forEach { item ->
                                ValueEfficiencyRow(item = item, label = "PER USE")
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
private fun UsageRankingRow(item: CosmeticItem, rank: Int, maxUsage: Int) {
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
                Text(text = "${item.brand} · ${item.usageCount} uses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(item.colorHex?.let { parseColor(it) } ?: Color.Gray)
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
private fun ValueEfficiencyRow(item: CosmeticItem, label: String, usePrice: Boolean = false) {
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
                .background(item.colorHex?.let { parseColor(it) } ?: Color.Gray)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = item.brand, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun CosmeticAnalyticsScreenPreview() {
    MaterialTheme {
        CosmeticAnalyticsScreen(
            uiState = CosmeticsUiState(
                items = listOf(
                    CosmeticItem(
                        id = 1, 
                        name = "Silk Primer", 
                        brand = "KoColor", 
                        macroCategory = MacroCategory.PREP,
                        microCategory = MicroCategory.PRIMER,
                        usageCount = 45, 
                        colorHex = "#F8F0E3", 
                        price = 28.0
                    ),
                    CosmeticItem(
                        id = 2, 
                        name = "Cool Ivory", 
                        brand = "KoColor", 
                        macroCategory = MacroCategory.COMPLEXION,
                        microCategory = MicroCategory.FOUNDATION,
                        usageCount = 120, 
                        colorHex = "#FAD4D4", 
                        price = 42.0
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
