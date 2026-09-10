package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeAnalyticsScreen(
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
            // 01 Snapshot
            item { SnapshotSection(analytics) }

            // 02 DNA
            item { DnaSection(analytics.dna) }

            // 03 Color Story
            item { ColorStorySection(analytics.colorDistribution) }

            // 04 The Collection
            item { CollectionSection(analytics) }

            // 05 Wear Distribution Chart
            item { WearDistributionChartSection(uiState.items) }

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
    Column {
        Text(
            text = "${analytics.totalItems} PIECES",
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
fun ColorHistorySection(wearEvents: List<WearEvent>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialHeader("Your Color History")
        
        if (wearEvents.isEmpty()) {
            Text("No recent wear history.", color = Color.Gray)
            return
        }

        // 1. Calculate Time Range (X-Axis)
        val minTime = wearEvents.minOf { it.timestamp }
        val maxTime = wearEvents.maxOf { it.timestamp }
        val timeRange = (maxTime - minTime).coerceAtLeast(1L).toFloat()

        // 2. Draw the Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 16.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Plot each wear event as a colored dot
            wearEvents.forEach { event ->
                // Normalize X based on time
                val normalizedX = (event.timestamp - minTime).toFloat() / timeRange
                val xPos = normalizedX * canvasWidth

                // Center Y vertically
                val yPos = canvasHeight / 2f

                drawCircle(
                    color = try { Color(android.graphics.Color.parseColor(event.colorHex)) } catch (e: Exception) { Color(0xFF2C3241) },
                    radius = 36f, // Size of the dot
                    center = Offset(xPos, yPos),
                    alpha = 0.7f
                )
            }
        }
        
        // 3. Timeline Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Oldest", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text("Today", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
private fun WearDistributionChartSection(items: List<ClothingItem>) {
    Column {
        EditorialHeader("Wear Distribution")
        Text(
            text = "Each dot represents a single item in your wardrobe. The vertical axis indicates total times worn.",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        val maxWears = items.maxOfOrNull { it.usageCount }?.coerceAtLeast(1) ?: 1
        // Sort items by usage count ascending (least worn on the left, most worn on the right)
        val sortedItems = items.sortedBy { it.usageCount }

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
                    .height(260.dp)
                    .background(Color.White)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Draw X-axis
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Draw Y-axis
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, 0f),
                        end = Offset(0f, canvasHeight),
                        strokeWidth = 2.dp.toPx()
                    )

                    val itemCount = sortedItems.size
                    // We flip the mapping:
                    // Y-axis represents total times worn (0 at bottom, maxWears at top)
                    // X-axis represents the individual clothing items sequentially
                    sortedItems.forEachIndexed { index, item ->
                        val xPos = if (itemCount > 1) {
                            (index.toFloat() / (itemCount - 1).toFloat()) * canvasWidth
                        } else {
                            canvasWidth / 2f
                        }
                        // Inverse mapping for Y so 0 is at the bottom (canvasHeight)
                        val wearRatio = if (maxWears > 0) item.usageCount.toFloat() / maxWears.toFloat() else 0f
                        val yPos = canvasHeight - (wearRatio * canvasHeight)

                        val itemColor = try { Color(android.graphics.Color.parseColor(item.colorHex)) } catch (e: Exception) { Color(0xFF2C3241) }

                        drawCircle(
                            color = itemColor.copy(alpha = 0.7f),
                            radius = 4.dp.toPx(),
                            center = Offset(xPos, yPos)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Least worn", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = "Most worn items", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
private fun WardrobeAnalyticsScreenLongPreview() {
    MaterialTheme {
        WardrobeAnalyticsScreen(
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
private fun WardrobeAnalyticsScreenPreview() {
    MaterialTheme {
        WardrobeAnalyticsScreen(
            uiState = WardrobeUiState(
                items = listOf(
                    ClothingItem(internalId = 1, name = "Universal Khaki Button-Down", category = ClothingCategory.TOPS, usageCount = 18, colorHex = "#B8A992"),
                    ClothingItem(internalId = 2, name = "Camel Leather Boots", category = ClothingCategory.SHOES, usageCount = 14, colorHex = "#BDA06A")
                )
            )
        )
    }
}
