package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
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
                    IconButton(onClick = { navTo(KoColorRoute.Wardrobe) }) { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") }
                    IconButton(onClick = { navTo(KoColorRoute.ColorSearch) }) { Icon(Icons.Default.Search, contentDescription = "Search") }
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
                        text = "Curated Closet.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "A professional look at style investments.",
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
                        modifier = Modifier.weight(1f),
                        onClick = { navTo(KoColorRoute.WardrobeAnalytics) }
                    )
                    
                    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                    SummaryStatCard(
                        label = "TOTAL VALUE",
                        value = currencyFormatter.format(uiState.totalInvestment),
                        icon = Icons.Default.MonetizationOn,
                        modifier = Modifier.weight(1f),
                        onClick = { navTo(KoColorRoute.Wardrobe) }
                    )
                }
            }

            // 3. Verticals Hero Cards
            item {
                var showTaxonomyInfo by remember { mutableStateOf(false) }
                if (showTaxonomyInfo) {
                    WardrobeTaxonomyDialog(onDismiss = { showTaxonomyInfo = false })
                }

                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VERTICALS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        
                        Surface(
                            onClick = { showTaxonomyInfo = true },
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "i",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF2C2420)
                                )
                            }
                        }
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val sections = listOf(
                            "Tops" to (Color(0xFFF7F2EB) to "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=400&q=80"),
                            "Bottoms" to (Color(0xFFF9F6F0) to "https://images.unsplash.com/photo-1542272454315-4c01d7abdf4a?w=400&q=80"),
                            "Shoes" to (Color(0xFFE8F1FD) to "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400&q=80"),
                            "Accessories" to (Color(0xFFF3EBFD) to "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&q=80")
                        )
                        
                        sections.forEach { (name, props) ->
                            val (bgColor, placeholderUrl) = props
                            val metadata = uiState.categoriesMetadata.entries.find { it.key.equals(name, ignoreCase = true) }?.value
                            AtelierWardrobeCard(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isValue = label.contains("VALUE")
    val charcoal = Color(0xFF2C2420)
    
    val valueBrush = if (isValue) {
        Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF4CAF50), Color(0xFF1B5E20)))
    } else {
        null
    }

    val actionBg = if (isValue) {
        Brush.linearGradient(listOf(Color(0xFF003300), Color(0xFF006600), Color(0xFF003300)))
    } else {
        Brush.linearGradient(listOf(
            Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFADAD), 
            Color(0xFFFFD6A5), Color(0xFFFDFFB6), Color(0xFFCAFFBF)
        ))
    }
    
    val actionText = if (isValue) "VIEW INVENTORY" else "VIEW INTELLIGENCE"
    val actionContentColor = if (isValue) Color.White else charcoal.copy(alpha = 0.8f)

    Card(
        modifier = modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFB)),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = charcoal.copy(alpha = 0.4f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (valueBrush != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = isValue.let { if (value.length > 6) 38.sp else 64.sp }, // Handle large currency values
                            fontFamily = FontFamily.Serif,
                            brush = valueBrush
                        ),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        fontWeight = FontWeight.Bold,
                        color = charcoal
                    )
                }
                
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    ),
                    color = charcoal.copy(alpha = 0.5f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(actionBg)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, actionContentColor.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Black,
                            color = actionContentColor,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = actionContentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WardrobeTaxonomyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wardrobe Architecture", style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                item {
                    TaxonomySection(
                        level = "Level 1",
                        title = "Archive Verticals",
                        description = "Body-zone mapping for intuitive archival retrieval.",
                        items = listOf("Tops" to "Blazers, Shirts, Knitwear.", "Bottoms" to "Trousers, Skirts, Denim.", "Shoes" to "Heels, Flats, Sneakers.", "Accessories" to "Bags, Belts, Jewelry.")
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Understand", fontWeight = FontWeight.Bold) } },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFF9F6F0)
    )
}

@Composable
private fun TaxonomySection(level: String, title: String, description: String, items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(text = level.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.7f))
        }
        Surface(color = Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { (label, detail) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "•", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(text = detail, style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.7f))
                        }
                    }
                }
            }
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
        AtelierWardrobeCard(
            name = "Tops",
            metadata = null,
            baseColor = Color.Blue,
            placeholderUrl = "",
            navTo = {}
        )
    }
}

@Composable
private fun AtelierWardrobeCard(
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
    val averageUsage = metadata?.averageUsage ?: 0.0
    val description = metadata?.description ?: "Strategic curated wardrobe collection."
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        onClick = { navTo(KoColorRoute.WardrobeCategoryCover(categoryName = name)) },
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Imagery
            AsyncImage(
                model = metadata?.representativeImageUrl ?: placeholderUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.4f),
                contentScale = ContentScale.Crop
            )

            // 2. Readability Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(baseColor, baseColor.copy(alpha = 0.6f), Color.Transparent),
                            startX = 0f,
                            endX = 1000f
                        )
                    )
            )

            // 3. Data Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$count Pieces",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.alpha(0.8f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "|  $description",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.alpha(0.6f)
                            )
                        }
                    }

                    Text(
                        text = currencyFormatter.format(totalValue),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        color = Color.Black
                    )
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.6f).padding(top = 2.dp),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                // Utility Status Bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Average Utility", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.alpha(0.8f))
                        Text(text = "${averageUsage.toInt()} Wears", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF6B705C))
                    }
                    
                    val maxWears = 50.0 
                    val progress = (averageUsage / maxWears).coerceIn(0.0, 1.0)
                    val statusColor = Color(0xFF6B705C) 

                    LinearProgressIndicator(
                        progress = { progress.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = statusColor,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    if (leadingBrand != null) {
                        Text(
                            text = "Leading Brand: $leadingBrand",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(0.8f)
                        )
                    }
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
