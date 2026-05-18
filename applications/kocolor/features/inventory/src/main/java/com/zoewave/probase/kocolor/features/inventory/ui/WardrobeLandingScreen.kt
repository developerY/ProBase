package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeLandingScreen(
    uiState: WardrobeUiState,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Style Archive", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) { Icon(Icons.Default.Search, null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Welcome Header
            item {
                Column {
                    Text(
                        text = "Your Curated Closet.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "A professional look at your style investments.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // 2. Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryStatCard(
                        label = "TOTAL PIECES",
                        value = uiState.totalItems.toString(),
                        icon = Icons.Default.Checkroom,
                        modifier = Modifier.weight(1f)
                    )
                    
                    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                    SummaryStatCard(
                        label = "TOTAL VALUE",
                        value = currencyFormatter.format(uiState.totalInvestment),
                        icon = Icons.Default.MonetizationOn,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Categories Circular
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text("VERTICALS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                    // Professional Category Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = 3
                    ) {
                        val sections = listOf(
                            "Tops" to Icons.Default.DryCleaning,
                            "Bottoms" to Icons.AutoMirrored.Filled.Label,
                            "Shoes" to Icons.Default.IceSkating,
                            "Accessories" to Icons.Default.Watch
                        )

                        sections.forEach { (name, icon) ->
                            val count = uiState.itemsByCategory[name.uppercase()] ?: 0
                            FilterChip(
                                selected = false,
                                onClick = { navTo(KoColorRoute.WardrobeCategoryCover(categoryName = name)) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                },
                                leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    iconColor = MaterialTheme.colorScheme.primary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    borderWidth = 1.dp
                                )
                            )
                        }
                    }
                }
            }

            // 4. Recently Added
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RECENTLY ADDED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(
                            "See All",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { navTo(KoColorRoute.Wardrobe) }
                        )
                    }
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(uiState.items.take(5)) { item ->
                            RecentClothingCard(item) { navTo(KoColorRoute.WardrobeDetail(item.id)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CategoryCircle(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            modifier = Modifier.size(64.dp).clickable { onClick() },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RecentClothingCard(item: ClothingItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(220.dp).aspectRatio(0.8f).clickable { onClick() },
        shape = RoundedCornerShape(28.dp)
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
            
            // Badge Overlay
            Surface(
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = item.category.name.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.Black
                )
            }

            // Scrim
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)), startY = 300f)))
            
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = item.brand ?: "", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}
