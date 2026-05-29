package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VanityLandingScreen(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Glow Archive", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.CosmeticAnalytics) }) { Icon(Icons.Default.Insights, contentDescription = "Analytics") }
                    IconButton(onClick = { /* Search */ }) { Icon(Icons.Default.Search, null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navTo(KoColorRoute.CosmeticAdd()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
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
                        text = "Good morning, Beautiful.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Here is a glance at your collection today.",
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
                        label = "TOTAL PRODUCTS",
                        value = uiState.totalCosmetics.toString(),
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        label = "EXPIRING SOON",
                        value = uiState.expiringCosmeticsCount.toString(),
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Category Hero Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text("CATEGORIES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val sections = listOf(
                            "Skincare & Prep" to (Color(0xFFF7F2EB) to "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400&q=80"),
                            "Complexion" to (Color(0xFFF9F6F0) to "https://images.unsplash.com/photo-1596704017254-9b121068fb31?w=400&q=80"),
                            "Color & Dimension" to (Color(0xFFFDEEF4) to "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=400&q=80"),
                            "Eyes & Brows" to (Color(0xFFE8F1FD) to "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=400&q=80"),
                            "Lips" to (Color(0xFFFEECEB) to "https://images.unsplash.com/photo-1586776977607-310e9c725c37?w=400&q=80")
                        )
                        
                        sections.forEach { (name, props) ->
                            val (bgColor, placeholderUrl) = props
                            val metadata = uiState.categoriesMetadata.entries.find { it.key.contains(name, ignoreCase = true) }?.value
                            AtelierCategoryCard(
                                name = name,
                                metadata = metadata,
                                baseColor = bgColor,
                                placeholderUrl = placeholderUrl,
                                navTo = navTo,
                                modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.clickable { navTo(KoColorRoute.Cosmetics()) }
                        )
                    }
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(uiState.items.take(5)) { item ->
                            RecentProductCard(
                                uiState = item,
                                navTo = navTo
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AtelierCategoryCard(
    name: String,
    metadata: CategoryMetadata?,
    baseColor: Color,
    placeholderUrl: String,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val count = metadata?.itemCount ?: 0
    val totalValue = metadata?.totalValue ?: 0.0
    val leadingBrand = metadata?.leadingBrand
    val averageFill = metadata?.averageFillLevel ?: 1.0
    
    val currencyFormatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US)

    Card(
        onClick = { navTo(KoColorRoute.CosmeticCategoryCover(name)) },
        modifier = modifier.height(IntrinsicSize.Min),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Professional Imagery
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.5f))
            ) {
                AsyncImage(
                    model = metadata?.representativeImageUrl ?: placeholderUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(24.dp))

            // 2. Data Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        val displayName = if (name.contains("&")) name.substringBefore("&").trim() else name
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black
                        )
                        Text(
                            text = "$count Items",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.alpha(0.6f)
                        )
                    }

                    Text(
                        text = currencyFormatter.format(totalValue),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Stock Status Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Stock Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.alpha(0.6f))
                        Text(text = "${(averageFill * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF6B705C))
                    }
                    
                    val statusColor = Color(0xFF6B705C) // Using the muted olive from the image for consistency

                    LinearProgressIndicator(
                        progress = { averageFill.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = statusColor,
                        trackColor = statusColor.copy(alpha = 0.1f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    if (leadingBrand != null) {
                        Text(
                            text = "Leading Brand: $leadingBrand",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp).alpha(0.6f)
                        )
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
    modifier: Modifier = Modifier
) {
    val color = if (label.contains("EXPIRING")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(20.dp), tint = color) }
            }
            Spacer(Modifier.height(16.dp))
            Text(text = value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecentProductCard(
    uiState: CosmeticItem,
    navTo: (KoColorRoute) -> Unit
) {
    val item = uiState
    val onClick = { navTo(KoColorRoute.CosmeticDetail(item.id)) }
    Card(
        modifier = Modifier.width(200.dp).height(260.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) {
                AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            
            Surface(modifier = Modifier.padding(16.dp), color = Color.White.copy(alpha = 0.9f), shape = CircleShape) {
                Text(text = item.microCategory.name.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, fontSize = 9.sp)
            }
            
            Column(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))).padding(16.dp)) {
                Text(text = item.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = item.brand, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    return try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
}

@Preview(showBackground = true)
@Composable
private fun VanityLandingScreenPreview() {
    MaterialTheme {
        VanityLandingScreen(
            uiState = CosmeticsUiState(
                totalCosmetics = 34,
                items = listOf(CosmeticItem(name = "Sample", brand = "Brand", macroCategory = MacroCategory.COMPLEXION, microCategory = MicroCategory.FOUNDATION))
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
