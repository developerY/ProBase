package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.features.inventory.ui.components.ProInsightCard
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleIntelligenceScreen(
    uiState: WardrobeUiState,
    modifier: Modifier = Modifier,
    navTo: (KoColorRoute) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
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
            // 1. Performance Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "PORTFOLIO PERFORMANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AnalysisSmallCard(
                            label = "TOTAL VALUE",
                            value = currencyFormatter.format(uiState.totalInvestment),
                            modifier = Modifier.weight(1f)
                        )
                        val avgCpw = uiState.items.mapNotNull { 
                            if (it.usageCount > 0 && it.price != null) it.price!! / it.usageCount else null 
                        }.let { if (it.isEmpty()) null else it.average() }
                        
                        AnalysisSmallCard(
                            label = "AVG CPW",
                            value = avgCpw?.let { currencyFormatter.format(it) } ?: "N/A",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Chromatic Core
            item {
                var selectedGroup by remember { mutableStateOf<Pair<String, List<ClothingItem>>?>(null) }

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
                            .sortedWith(compareBy(
                                { (hex, _) ->
                                    val hsv = FloatArray(3)
                                    try {
                                        AndroidColor.colorToHSV(AndroidColor.parseColor(hex), hsv)
                                        // Neutrals to the end (Saturation < 0.1)
                                        if (hsv[1] < 0.1f) 1 else 0 
                                    } catch (e: Exception) { 1 }
                                },
                                { it.second.size * -1 } // Then by count descending
                            ))
                    }

                    if (colorGroups.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .horizontalScroll(rememberScrollState())
                        ) {
                            colorGroups.forEach { group ->
                                val (hex, items) = group
                                val isSelected = selectedGroup?.first == hex
                                val segmentWidth by animateDpAsState(
                                    targetValue = if (isSelected) (items.size * 22 + 80).dp.coerceIn(100.dp, 200.dp) else (items.size * 14 + 18).dp.coerceIn(24.dp, 90.dp),
                                    label = "SegmentWidthAnimation"
                                )
                                Box(
                                    modifier = Modifier
                                        .width(segmentWidth)
                                        .fillMaxHeight()
                                        .background(Color(AndroidColor.parseColor(hex)))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 0.5.dp,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f)
                                        )
                                        .clickable {
                                            selectedGroup = if (isSelected) null else group
                                        }
                                )
                            }
                        }

                        // 🔍 Selection Details
                        selectedGroup?.let { (hex, items) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(AndroidColor.parseColor(hex)))
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "${items.size} ${if (items.size == 1) "Garment" else "Garments"} in this shade",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { navTo(KoColorRoute.WardrobeDetail(item.internalId)) }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = item.brand ?: item.category.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "View Details",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .rotate(180f),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text("No color data available", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            }

            // 2.5 Portfolio Composition (Full Footprint Table)
            item {
                Text(
                    text = "PORTFOLIO COMPOSITION",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
            }

            val sortedCategories = uiState.categoriesMetadata.toList().sortedByDescending { it.second.itemCount }
            items(sortedCategories) { (name, metadata) ->
                PortfolioCategoryRow(
                    name = name,
                    metadata = metadata,
                    totalItems = uiState.totalItems,
                    totalInvestment = uiState.totalInvestment
                )
            }

            item {
                ProInsightCard(
                    text = if (uiState.totalItems > 0) {
                        "Your wardrobe shows ${uiState.diversityIndex} diversity. " +
                                "Balanced distribution across ${uiState.itemsByCategory.size} verticals."
                    } else {
                        "Start adding items to analyze your strategic diversity."
                    }
                )
            }

            // 3. Style Efficiency List (Collapsible)
            item {
                var isCpwExpanded by remember { mutableStateOf(false) }
                val rotationState by animateFloatAsState(
                    targetValue = if (isCpwExpanded) 180f else 0f,
                    label = "CpwChevronRotation"
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCpwExpanded = !isCpwExpanded }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STYLE EFFICIENCY (CPW)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isCpwExpanded) "Collapse CPW" else "Expand CPW",
                            modifier = Modifier.rotate(rotationState),
                            tint = Color.Gray
                        )
                    }

                    AnimatedVisibility(visible = isCpwExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val efficiencySorted = uiState.items.sortedBy { 
                                it.price?.div(it.usageCount.takeIf { count -> count > 0 } ?: 1) ?: Double.MAX_VALUE 
                            }

                            if (efficiencySorted.isEmpty()) {
                                Text(
                                    text = "No cost per wear data available.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            } else {
                                efficiencySorted.forEach { item ->
                                    CPWItemCard(item = item)
                                }
                            }
                        }
                    }
                }
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
                totalInvestment = 1200.0,
                items = listOf(
                    ClothingItem(name = "Silk Blazer", category = ClothingCategory.TOPS, usageCount = 25, price = 350.0, colorHex = "#000000"),
                    ClothingItem(name = "Linen Pants", category = ClothingCategory.BOTTOMS, usageCount = 0, price = 120.0, colorHex = "#F5F5DC")
                )
            ),
            navTo = {}
        )
    }
}
