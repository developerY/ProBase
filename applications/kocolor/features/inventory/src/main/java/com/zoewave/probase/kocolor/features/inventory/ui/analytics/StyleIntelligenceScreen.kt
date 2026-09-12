package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.features.inventory.ui.components.ProInsightCard
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlinx.coroutines.launch
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
    var selectedCategoryFilter by remember { mutableStateOf<ClothingCategory?>(null) }
    val filteredItems = remember(uiState.items, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) uiState.items
        else uiState.items.filter { it.category == selectedCategoryFilter }
    }
    
    val engine = remember { com.zoewave.probase.kocolor.features.inventory.domain.WardrobeAnalyticsEngine() }
    val analytics = remember(uiState.items) { engine.computeAnalytics(uiState.items) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "THE FOOTPRINT",
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
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 0. Wardrobe DNA
            item {
                Column {
                    Text(
                        text = "YOUR WARDROBE DNA",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    Text(
                        text = "Based on your wardrobe, not your personal color profile.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = analytics.dna.primaryIdentity,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "• ${analytics.dna.temperatureBias}", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                    Text(text = "• ${analytics.dna.depth}", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                    Text(text = "• ${analytics.dna.contrast}", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                    Text(text = "• ${analytics.dna.chroma}", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                }
            }

            // 1. Chromatic Core & Spectrum System (Placed on top!)
            item {
                var selectedGroup by remember { mutableStateOf<Pair<String, List<ClothingItem>>?>(null) }
                val lazyListState = rememberLazyListState()
                val scope = rememberCoroutineScope()

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PROFILE ANALYSIS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Your Color Spectrum",
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2420)
                            )
                        }

                        if (selectedGroup != null || selectedCategoryFilter != null) {
                            Text(
                                text = "Reset",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    selectedGroup = null
                                    selectedCategoryFilter = null
                                }
                            )
                        }
                    }

                    // Category Filter Chips Row placed directly under "Your Color Spectrum" title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All Items", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(50.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Black,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = Color.DarkGray
                            )
                        )

                        ClothingCategory.entries.forEach { category ->
                            FilterChip(
                                selected = selectedCategoryFilter == category,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == category) null else category
                                },
                                label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(50.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.Black,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    labelColor = Color.DarkGray
                                )
                            )
                        }
                    }
                    
                    val colorGroups = remember(filteredItems) {
                        filteredItems
                            .filter { it.colorHex.isNotBlank() }
                            .groupBy { it.colorHex }
                            .toList()
                            .sortedWith(compareBy(
                                { (hex, _) ->
                                    val hsv = FloatArray(3)
                                    try {
                                        AndroidColor.colorToHSV(AndroidColor.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
                                        if (hsv[1] < 0.1f) 1 else 0 
                                    } catch (e: Exception) { 1 }
                                },
                                { (hex, _) ->
                                    val hsv = FloatArray(3)
                                    try {
                                        AndroidColor.colorToHSV(AndroidColor.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
                                        val hue = hsv[0]
                                        if (hue > 330) hue - 360 else hue
                                    } catch (e: Exception) { 0f }
                                },
                                { (hex, _) ->
                                    val hsv = FloatArray(3)
                                    try {
                                        AndroidColor.colorToHSV(AndroidColor.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
                                        hsv[2] 
                                    } catch (e: Exception) { 0f }
                                }
                            ))
                    }

                    if (colorGroups.isNotEmpty()) {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val totalGroups = colorGroups.size
                            val containerWidth = maxWidth
                            val baseItemWidth = if (totalGroups > 0) (containerWidth / totalGroups).coerceAtLeast(4.dp) else 0.dp

                            LazyRow(
                                state = lazyListState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                items(colorGroups.size) { index ->
                                    val group = colorGroups[index]
                                    val (hex, _) = group
                                    val isSelected = selectedGroup?.first == hex

                                    val animatedWidth by animateDpAsState(
                                        targetValue = when {
                                            selectedGroup == null -> baseItemWidth
                                            isSelected -> 100.dp
                                            else -> 12.dp
                                        },
                                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                                        label = "width"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .width(animatedWidth)
                                            .fillMaxHeight()
                                            .background(Color(AndroidColor.parseColor(if (hex.startsWith("#")) hex else "#$hex")))
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = if (isSelected) Color.White else Color.Transparent
                                            )
                                            .clickable {
                                                if (isSelected) {
                                                    selectedGroup = null
                                                } else {
                                                    selectedGroup = group
                                                    scope.launch {
                                                        lazyListState.animateScrollToItem(index)
                                                    }
                                                }
                                            }
                                    )
                                }
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
                                            .background(Color(AndroidColor.parseColor(if (hex.startsWith("#")) hex else "#$hex")))
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
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.size(48.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ) {
                                            if (!item.imageUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = item.imageUrl,
                                                    contentDescription = item.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color(AndroidColor.parseColor(if (item.colorHex.startsWith("#")) item.colorHex else "#${item.colorHex}"))),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Checkroom,
                                                        contentDescription = null,
                                                        tint = Color.White.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.width(16.dp))

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

            // 2. Performance Row
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

            // 2.5 Portfolio Composition (Collapsible)
            item {
                var isPortfolioExpanded by remember { mutableStateOf(false) }
                val rotationState by animateFloatAsState(
                    targetValue = if (isPortfolioExpanded) 180f else 0f,
                    label = "PortfolioChevronRotation"
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPortfolioExpanded = !isPortfolioExpanded }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PORTFOLIO COMPOSITION",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isPortfolioExpanded) "Hide Portfolio Composition" else "Show Portfolio Composition",
                            modifier = Modifier.rotate(rotationState),
                            tint = Color.Gray
                        )
                    }

                    AnimatedVisibility(visible = isPortfolioExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val sortedCategories = uiState.categoriesMetadata.toList().sortedByDescending { it.second.itemCount }
                            sortedCategories.forEach { (name, metadata) ->
                                PortfolioCategoryRow(
                                    name = name,
                                    metadata = metadata,
                                    totalItems = uiState.totalItems,
                                    totalInvestment = uiState.totalInvestment
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            ProInsightCard(
                                text = if (uiState.totalItems > 0) {
                                    "Your wardrobe shows ${uiState.diversityIndex} diversity. " +
                                            "Balanced distribution across ${uiState.itemsByCategory.size} verticals."
                                } else {
                                    "Start adding items to analyze your strategic diversity."
                                }
                            )
                        }
                    }
                }
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
