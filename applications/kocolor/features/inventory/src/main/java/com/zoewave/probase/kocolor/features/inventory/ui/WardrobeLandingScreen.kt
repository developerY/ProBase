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
                        
                        sections.chunked(2).forEach { rowSections ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowSections.forEach { (name, props) ->
                                    val (icon, color) = props
                                    val metadata = uiState.categoriesMetadata.entries.find { it.key.equals(name, ignoreCase = true) }?.value
                                    CategoryHeroCard(
                                        uiState = Quadruple(name, icon, metadata, color),
                                        onEvent = {},
                                        navTo = navTo,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowSections.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
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
        CategoryHeroCard(uiState = Quadruple("Tops", Icons.Default.Info, null, Color.Blue), onEvent = {}, navTo = {})
    }
}

@Composable
private fun CategoryHeroCard(
    uiState: Quadruple<String, ImageVector, CategoryMetadata?, Color>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val (name, icon, metadata, baseColor) = uiState
    val count = metadata?.itemCount ?: 0
    val totalValue = metadata?.totalValue ?: 0.0
    val imageUrl = metadata?.representativeImageUrl
    val itemColor = metadata?.representativeColorHex?.let { parseColor(it) }

    Card(
        onClick = { navTo(KoColorRoute.WardrobeCategoryCover(categoryName = name)) },
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, baseColor.copy(alpha = 0.6f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image/Color
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.2f),
                    contentScale = ContentScale.Crop
                )
            } else if (itemColor != null) {
                Box(modifier = Modifier.fillMaxSize().background(itemColor).alpha(0.1f))
            }

            // Ambient Icon Watermark
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 24.dp, y = 24.dp)
                    .size(140.dp)
                    .alpha(0.05f),
                tint = Color.Black
            )

            Column(
                modifier = Modifier.padding(24.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.8f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.Black)
                        }
                    }

                    if (totalValue > 0) {
                        Text(
                            text = "$${"%,.2f".format(totalValue)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "$count PIECES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.alpha(0.5f),
                        letterSpacing = 1.sp
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
