package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
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
fun StyleIntelligenceScreen(
    uiState: WardrobeUiState,
    modifier: Modifier = Modifier,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "ANALYSIS",
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Chromatic Core
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "CHROMATIC CORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    
                    val colorGroups = remember(uiState.items) {
                        uiState.items
                            .filter { it.colorHex.isNotBlank() }
                            .groupBy { it.colorHex }
                            .toList()
                            .sortedByDescending { it.second.size }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        colorGroups.forEach { (hex, items) ->
                            Box(
                                modifier = Modifier
                                    .weight(items.size.toFloat())
                                    .fillMaxHeight()
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.2f))
                            )
                        }
                    }
                }
            }

            // 2. Performance Stats
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnalysisSmallCard(
                        label = "DIVERSITY",
                        value = uiState.diversityIndex,
                        modifier = Modifier.weight(1f)
                    )
                    AnalysisSmallCard(
                        label = "UTILIZATION",
                        value = "${(uiState.glowScore * 100).toInt()}%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Cost Per Wear (CPW) Analysis
            item {
                Text(
                    text = "COST PER WEAR ANALYSIS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
            }

            items(uiState.items.sortedBy { it.price?.div(it.usageCount.takeIf { it > 0 } ?: 1) ?: Double.MAX_VALUE }) { item ->
                CPWItemCard(item = item)
            }
        }
    }
}

@Composable
private fun CPWItemCard(item: ClothingItem) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val cpw = if (item.usageCount > 0 && item.price != null) {
        item.price!! / item.usageCount
    } else null

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "INVESTMENT: ${currencyFormatter.format(item.price ?: 0.0)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = cpw?.let { currencyFormatter.format(it) } ?: "NOT DEPLOYED",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (cpw != null) Color(0xFF1B5E20) else Color.Gray
                )
                Text(
                    text = "CPW",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun AnalysisSmallCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Preview
@Composable
fun StyleIntelligenceScreenPreview() {
    MaterialTheme {
        StyleIntelligenceScreen(
            uiState = WardrobeUiState(
                glowScore = 0.84,
                diversityIndex = "Strategic",
                items = listOf(
                    ClothingItem(name = "Silk Blazer", category = ClothingCategory.TOPS, usageCount = 25, price = 350.0, colorHex = "#000000"),
                    ClothingItem(name = "Linen Pants", category = ClothingCategory.BOTTOMS, usageCount = 0, price = 120.0, colorHex = "#F5F5DC")
                )
            ),
            navTo = {}
        )
    }
}
