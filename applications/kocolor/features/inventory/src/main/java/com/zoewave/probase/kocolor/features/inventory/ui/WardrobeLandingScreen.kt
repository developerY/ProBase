package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.BorderStroke
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
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import java.text.NumberFormat
import java.util.Locale

@Preview(showBackground = true)
@Composable
private fun WardrobeLandingScreenPreview() {
    MaterialTheme {
        WardrobeLandingScreen(
            uiState = WardrobeUiState(
                totalItems = 9,
                totalInvestment = 1615.0,
                items = listOf(
                    com.zoewave.probase.kocolor.model.ClothingItem(id = 1, name = "Blouse", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeLandingScreen(
    uiState: WardrobeUiState,
    onEvent: (WardrobeEvent) -> Unit,
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
                    IconButton(onClick = { navTo(KoColorRoute.WardrobeAnalytics) }) { Icon(Icons.Default.Insights, null) }
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
                        uiState = Triple("TOTAL PIECES", uiState.totalItems.toString(), Icons.Default.Checkroom),
                        onEvent = {},
                        navTo = {},
                        modifier = Modifier.weight(1f)
                    )
                    
                    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                    SummaryStatCard(
                        uiState = Triple("TOTAL VALUE", currencyFormatter.format(uiState.totalInvestment), Icons.Default.MonetizationOn),
                        onEvent = {},
                        navTo = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Verticals Hero Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text("VERTICALS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val sections = listOf(
                            "Tops" to (Icons.Default.DryCleaning to Color(0xFFFDEEF4)),
                            "Bottoms" to (Icons.AutoMirrored.Filled.Label to Color(0xFFE8F1FD)),
                            "Shoes" to (Icons.Default.IceSkating to Color(0xFFFEECEB)),
                            "Accessories" to (Icons.Default.Watch to Color(0xFFF3EBFD))
                        )
                        
                        sections.forEach { (name, props) ->
                            val (icon, color) = props
                            val metadata = uiState.categoriesMetadata.entries.find { it.key.equals(name, ignoreCase = true) }?.value
                            CategoryHeroCard(
                                name = name,
                                icon = icon,
                                metadata = metadata,
                                baseColor = color,
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
                            modifier = Modifier.clickable { navTo(KoColorRoute.Wardrobe) }
                        )
                    }
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(uiState.items.take(5)) { item ->
                            RecentClothingCard(
                                uiState = item,
                                onEvent = {},
                                navTo = navTo
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryStatCardPreview() {
    MaterialTheme {
        SummaryStatCard(uiState = Triple("Label", "Value", Icons.Default.Info), onEvent = {}, navTo = {})
    }
}

@Composable
private fun SummaryStatCard(
    uiState: Triple<String, String, ImageVector>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val (label, value, icon) = uiState
    val color = if (label.contains("VALUE")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Preview(showBackground = true)
@Composable
private fun CategoryHeroCardPreview() {
    MaterialTheme {
        CategoryHeroCard(
            name = "Tops",
            icon = Icons.Default.Info,
            metadata = null,
            baseColor = Color.Blue,
            navTo = {}
        )
    }
}

@Composable
private fun CategoryHeroCard(
    name: String,
    icon: ImageVector,
    metadata: CategoryMetadata?,
    baseColor: Color,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val count = metadata?.itemCount ?: 0
    val totalValue = metadata?.totalValue ?: 0.0
    val leadingBrand = metadata?.leadingBrand
    val averageUsage = metadata?.averageUsage ?: 0.0
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        onClick = { navTo(KoColorRoute.WardrobeCategoryCover(categoryName = name)) },
        modifier = modifier.height(IntrinsicSize.Min),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, baseColor.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.White.copy(alpha = 0.5f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color.Black)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        Text(text = "$count Pieces", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.alpha(0.7f))
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = currencyFormatter.format(totalValue), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(text = "Total Investment", style = MaterialTheme.typography.labelSmall, modifier = Modifier.alpha(0.5f), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(text = "Average Utility", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(text = "${averageUsage.toInt()} Wears", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                
                val maxWears = 50.0 // Normalizing against a high utility item
                val progress = (averageUsage / maxWears).coerceIn(0.0, 1.0)
                
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                if (leadingBrand != null) {
                    Text(
                        text = "Leading Brand: $leadingBrand",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp).alpha(0.6f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecentClothingCardPreview() {
    MaterialTheme {
        RecentClothingCard(
            uiState = ClothingItem(id = 1, name = "Item", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
private fun RecentClothingCard(uiState: ClothingItem, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    val item = uiState
    val onClick = { navTo(KoColorRoute.WardrobeDetail(item.id)) }
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

                // Representative Color Badge
                val itemColor = item.dominantHex?.let { parseColor(it) } 
                    ?: item.colorHex?.let { parseColor(it) } 
                    ?: Color.White
                
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(24.dp)
                        .align(Alignment.TopEnd),
                    color = itemColor,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {}
            } else {
                val itemColor = item.dominantHex?.let { parseColor(it) } 
                    ?: item.colorHex?.let { parseColor(it) } 
                    ?: MaterialTheme.colorScheme.surfaceVariant
                Box(modifier = Modifier.fillMaxSize().background(itemColor))
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
