package com.zoewave.probase.kocolor.features.inventory.ui.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.features.inventory.domain.ColorDistribution
import com.zoewave.probase.kocolor.features.inventory.domain.RotationAnalytics
import com.zoewave.probase.kocolor.features.inventory.domain.VersatilityAnalytics
import com.zoewave.probase.kocolor.features.inventory.domain.WardrobeAnalytics
import com.zoewave.probase.kocolor.features.inventory.domain.WardrobeAnalyticsEngine
import com.zoewave.probase.kocolor.features.inventory.domain.WardrobeDna
import com.zoewave.probase.kocolor.features.inventory.domain.WardrobeInsight
import com.zoewave.probase.kocolor.features.inventory.domain.WearEvent
import com.zoewave.probase.kocolor.features.inventory.ui.WardrobeUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegacyWardrobeAnalyticsScreen(
    uiState: WardrobeUiState,
    modifier: Modifier = Modifier,
    navTo: (KoColorRoute) -> Unit = {}
) {
    val engine = remember { WardrobeAnalyticsEngine() }
    val analytics = remember(uiState.items) { engine.computeAnalytics(uiState.items) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "WARDROBE ANALYTICS",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 00 Analytics Coverage & Observation Period
            item { CoverageQualitySection(analytics.analyticsCoverage) }

            // 01 Snapshot
            item { SnapshotSection(analytics) }

            // 02 DNA
            item { DnaSection(analytics.dna) }

            // 03 Color Story
            item { ColorStorySection(analytics.colorDistribution) }

            // 04 The Collection
            item { CollectionSection(analytics) }

            // 05 Wear Distribution Chart
            item { WearDistributionChartSection(uiState.items, navTo) }

            // 05 Color History
            item { ColorHistorySection(analytics.wearHistory) }

            // 06 Rotation
            item { RotationSection(analytics.rotation) }

            // 06 Versatility
            item { VersatilitySection(analytics.versatility, navTo) }

            // 07 Wardrobe Gaps
            item { CoverageSection(analytics.insights) }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun CoverageQualitySection(coverage: com.zoewave.probase.kocolor.features.inventory.domain.AnalyticsCoverage) {
    val dateFormat = remember { java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault()) }
    val startStr = dateFormat.format(java.util.Date(coverage.periodStart))
    val endStr = dateFormat.format(java.util.Date(coverage.periodEnd))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DATA COVERAGE & QUALITY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "${coverage.totalWearRecords} wear records analyzed",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Observation Period: $startStr – $endStr",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CoverageMetric("Inventory", coverage.inventoryCoverage)
            CoverageMetric("Color Data", coverage.colorDataCoverage)
            CoverageMetric("Wear History", coverage.wearHistoryCoverage)
        }
    }
}

@Composable
private fun CoverageMetric(label: String, percentage: Float) {
    Column {
        Text(
            text = "${(percentage * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun EditorialHeader(title: String) {
    Column {
        Text(
            text = title.uppercase(),
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun SnapshotSection(analytics: WardrobeAnalytics) {
    val dynamicTotal = analytics.activeItems + analytics.rarelyWornItems + analytics.neverWornItems
    val displayTotal = if (analytics.totalItems == dynamicTotal) analytics.totalItems else dynamicTotal

    Column {
        Text(
            text = "$displayTotal PIECES",
            fontSize = 42.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "Your wardrobe at a glance",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column {
                Text(
                    text = "${analytics.activeItems}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "in rotation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Column {
                Text(
                    text = "${analytics.rarelyWornItems}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "rarely worn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Column {
                Text(
                    text = "${analytics.neverWornItems}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "never worn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun DnaSection(dna: WardrobeDna) {
    Column {
        EditorialHeader("Your Wardrobe DNA")
        Text(
            text = "Based on your wardrobe, not your personal color profile.",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = dna.primaryIdentity,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "• ${dna.temperatureBias}", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
        Text(text = "• ${dna.depth}", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
        Text(text = "• ${dna.contrast}", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
        Text(text = "• ${dna.chroma}", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
    }
}

@Composable
private fun ColorStorySection(colors: ColorDistribution) {
    Column {
        EditorialHeader("The Color Story")
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Neutrals", style = MaterialTheme.typography.bodyLarge)
            Text("${colors.neutralsPct}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Warm Tones", style = MaterialTheme.typography.bodyLarge)
            Text("${colors.warmPct}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Cool Accents", style = MaterialTheme.typography.bodyLarge)
            Text("${colors.coolPct}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "TOP COLORS IN ROTATION",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        colors.topColors.forEach { stat ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(stat.name, modifier = Modifier.weight(0.35f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                ) {
                    val color = try { Color(android.graphics.Color.parseColor(stat.hex)) } catch (e: Exception) { Color.Gray }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stat.percentage)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionSection(analytics: WardrobeAnalytics) {
    val dist = analytics.categoryDistribution
    Column {
        EditorialHeader("The Collection")
        CategoryRow("Tops", dist.tops)
        CategoryRow("Bottoms", dist.bottoms)
        CategoryRow("Dresses", dist.dresses)
        CategoryRow("Shoes", dist.shoes)
        CategoryRow("Outerwear", dist.outerwear)
        CategoryRow("Activewear", dist.activewear)
    }
}

@Composable
private fun CategoryRow(name: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, style = MaterialTheme.typography.bodyLarge)
        Text("$count", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun RotationSection(rotation: RotationAnalytics) {
    Column {
        EditorialHeader("Your Rotation")
        Text(
            text = "Rotation health: ${rotation.healthScore} / 100",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${rotation.rarelyWorn} pieces have had little recent use.",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        val maxWears = rotation.mostWorn.maxOfOrNull { it.wearCount }?.coerceAtLeast(1) ?: 1

        Text(
            text = "MOST WORN",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        rotation.mostWorn.forEach { item ->
            ItemFrequencyBar(name = item.name, wearCount = item.wearCount, maxWears = maxWears)
        }

        if (rotation.leastWorn.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "LEAST WORN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            rotation.leastWorn.forEach { item ->
                ItemFrequencyBar(name = item.name, wearCount = item.wearCount, maxWears = maxWears)
            }
        }
    }
}

@Composable
private fun ItemFrequencyBar(name: String, wearCount: Int, maxWears: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = name,
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            )
            Text(
                text = "$wearCount wears",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.LightGray.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (wearCount.toFloat() / maxWears.toFloat()).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Color(0xFF2C3241))
            )
        }
    }
}

@Composable
private fun ColorHistorySection(
    wearEvents: List<WearEvent>,
    navTo: (KoColorRoute) -> Unit = {}
) {
    var selectedEvent by remember { mutableStateOf<WearEvent?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialHeader("Your Color History")
        Text(
            text = "Tap any color dot on the thread to inspect garment history.",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        
        if (wearEvents.isEmpty()) {
            Text("No recent wear history.", color = Color.Gray)
            return
        }

        val minTime = wearEvents.minOf { it.timestamp }
        val maxTime = wearEvents.maxOf { it.timestamp }
        val timeRange = (maxTime - minTime).coerceAtLeast(1L).toFloat()

        var dotPositions by remember { mutableStateOf<List<Pair<Offset, WearEvent>>>(emptyList()) }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(vertical = 16.dp)
                .pointerInput(wearEvents) {
                    detectTapGestures { tapOffset ->
                        val maxDistPx = 36.dp.toPx()
                        val closest = dotPositions.minByOrNull { (dotPos, _) ->
                            val dx = dotPos.x - tapOffset.x
                            val dy = dotPos.y - tapOffset.y
                            kotlin.math.sqrt(dx * dx + dy * dy)
                        }
                        if (closest != null) {
                            val (dotPos, event) = closest
                            val dx = dotPos.x - tapOffset.x
                            val dy = dotPos.y - tapOffset.y
                            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                            if (dist <= maxDistPx) {
                                selectedEvent = if (selectedEvent == event) null else event
                            }
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val newPositions = mutableListOf<Pair<Offset, WearEvent>>()

            wearEvents.forEach { event ->
                val normalizedX = (event.timestamp - minTime).toFloat() / timeRange
                val xPos = normalizedX * canvasWidth
                val yPos = canvasHeight / 2f
                val pos = Offset(xPos, yPos)
                newPositions.add(pos to event)

                val itemColor = try {
                    val cleanHex = if (event.colorHex.startsWith("#")) event.colorHex else "#${event.colorHex}"
                    Color(android.graphics.Color.parseColor(cleanHex))
                } catch (e: Exception) {
                    Color(0xFF2C3241)
                }

                val isSelected = selectedEvent == event

                if (isSelected) {
                    drawCircle(
                        color = Color.Black,
                        radius = 42f,
                        center = pos,
                        style = Stroke(width = 4f)
                    )
                }

                drawCircle(
                    color = itemColor,
                    radius = if (isSelected) 38f else 36f,
                    center = pos,
                    alpha = if (isSelected) 1f else 0.7f
                )
            }
            dotPositions = newPositions
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Oldest", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text("Today", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        // 🔍 Selected Event Details Card
        AnimatedVisibility(visible = selectedEvent != null) {
            selectedEvent?.let { event ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clickable { navTo(KoColorRoute.WardrobeDetail(event.itemId)) },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp),
                            color = Color.White
                        ) {
                            if (!event.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = event.imageUrl,
                                    contentDescription = event.itemName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(try { Color(android.graphics.Color.parseColor(if (event.colorHex.startsWith("#")) event.colorHex else "#${event.colorHex}")) } catch (e: Exception) { Color.Gray }),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Checkroom,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.itemName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${event.category} • Worn in your rotation",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = "Details →",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WearDistributionChartSection(
    items: List<ClothingItem>,
    navTo: (KoColorRoute) -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf<ClothingItem?>(null) }

    Column {
        EditorialHeader("Wear Distribution")
        Text(
            text = "Each dot represents a single item in your wardrobe. Tap any dot to inspect item details.",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        val maxWears = items.maxOfOrNull { it.usageCount }?.coerceAtLeast(1) ?: 1
        val sortedItems = remember(items) { items.sortedBy { it.usageCount } }

        if (sortedItems.isEmpty()) {
            Text(
                text = "No clothing items found.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                var dotPositions by remember { mutableStateOf<List<Pair<Offset, ClothingItem>>>(emptyList()) }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .pointerInput(sortedItems) {
                            detectTapGestures { tapOffset ->
                                val maxDistPx = 32.dp.toPx()
                                val closest = dotPositions.minByOrNull { (dotPos, _) ->
                                    val dx = dotPos.x - tapOffset.x
                                    val dy = dotPos.y - tapOffset.y
                                    kotlin.math.sqrt(dx * dx + dy * dy)
                                }
                                if (closest != null) {
                                    val (dotPos, item) = closest
                                    val dx = dotPos.x - tapOffset.x
                                    val dy = dotPos.y - tapOffset.y
                                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                                    if (dist <= maxDistPx) {
                                        selectedItem = if (selectedItem == item) null else item
                                    }
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Draw X-axis (bottom)
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw Y-axis (left)
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, 0f),
                        end = Offset(0f, canvasHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    val itemCount = sortedItems.size
                    val newPositions = mutableListOf<Pair<Offset, ClothingItem>>()

                    sortedItems.forEachIndexed { index, item ->
                        val xPos = if (itemCount > 1) {
                            (index.toFloat() / (itemCount - 1).toFloat()) * canvasWidth
                        } else {
                            canvasWidth / 2f
                        }
                        
                        val wearRatio = if (maxWears > 0) item.usageCount.toFloat() / maxWears.toFloat() else 0f
                        val yPos = canvasHeight - (wearRatio * canvasHeight)
                        val pos = Offset(xPos, yPos)
                        newPositions.add(pos to item)

                        val itemColor = try {
                            val cleanHex = if (item.colorHex.startsWith("#")) item.colorHex else "#${item.colorHex}"
                            Color(android.graphics.Color.parseColor(cleanHex))
                        } catch (e: Exception) {
                            Color(0xFF2C3241)
                        }

                        val isSelected = selectedItem == item

                        if (isSelected) {
                            drawCircle(
                                color = Color.Black,
                                radius = 14f,
                                center = pos,
                                style = Stroke(width = 3f)
                            )
                        }

                        drawCircle(
                            color = itemColor.copy(alpha = if (isSelected) 1f else 0.8f),
                            radius = if (isSelected) 10f else 7f,
                            center = pos
                        )
                    }
                    dotPositions = newPositions
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Least worn",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Most worn items",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                // 🔍 Selected Dot Tooltip Card
                AnimatedVisibility(visible = selectedItem != null) {
                    selectedItem?.let { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clickable { navTo(KoColorRoute.WardrobeDetail(item.internalId)) },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(44.dp),
                                    color = Color.White
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
                                                .background(try { Color(android.graphics.Color.parseColor(if (item.colorHex.startsWith("#")) item.colorHex else "#${item.colorHex}")) } catch (e: Exception) { Color.Gray }),
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

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${item.category.name} • ${item.usageCount} wears",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                Text(
                                    text = "Details →",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersatilitySection(
    versatility: VersatilityAnalytics,
    navTo: (KoColorRoute) -> Unit
) {
    val garment = versatility.mostVersatile
    Column {
        EditorialHeader("Versatility & Utility")
        Text(
            text = "${garment.compatibleLooksCount} compatible looks across ${versatility.totalPossibleLooks} possible outfit combinations",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "MOST VERSATILE PIECE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = garment.name,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Compatible with ${garment.compatibleBottoms} bottoms, ${garment.compatibleShoes} shoes, and ${garment.compatibleOuterwear} outerwear pieces.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        navTo(KoColorRoute.FashionJourney(intent = garment.name))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create a look with this piece →",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverageSection(insights: List<WardrobeInsight>) {
    Column {
        EditorialHeader("WARDROBE OPPORTUNITIES")
        insights.forEach { insight ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "+ ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 412,     // Standard phone width
    heightDp = 2700    // Extend this to fit your entire scrollable list
)
@Composable
private fun LegacyWardrobeAnalyticsScreenLongPreview() {
    MaterialTheme {
        LegacyWardrobeAnalyticsScreen(
            uiState = WardrobeUiState(
                items = listOf(
                    ClothingItem(internalId = 1, name = "Universal Khaki Button-Down", category = ClothingCategory.TOPS, usageCount = 18, colorHex = "#B8A992"),
                    ClothingItem(internalId = 2, name = "Camel Leather Boots", category = ClothingCategory.SHOES, usageCount = 14, colorHex = "#BDA06A")
                )
            )
        )
    }
}

@Preview(showBackground = true, name = "Wardrobe Analytics Editorial Preview")
@Composable
private fun LegacyWardrobeAnalyticsScreenPreview() {
    MaterialTheme {
        LegacyWardrobeAnalyticsScreen(
            uiState = WardrobeUiState(
                items = listOf(
                    ClothingItem(internalId = 1, name = "Universal Khaki Button-Down", category = ClothingCategory.TOPS, usageCount = 18, colorHex = "#B8A992"),
                    ClothingItem(internalId = 2, name = "Camel Leather Boots", category = ClothingCategory.SHOES, usageCount = 14, colorHex = "#BDA06A")
                )
            )
        )
    }
}
