package com.zoewave.probase.kocolor.features.cosmetics.ui

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.MacroCategory
import com.zoewave.probase.kocolor.model.MicroCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VanityLandingScreen(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showTaxonomyInfo by remember { mutableStateOf(false) }

    if (showTaxonomyInfo) {
        ProfessionalTaxonomyDialog(onDismiss = { showTaxonomyInfo = false })
    }

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
                    IconButton(onClick = { navTo(KoColorRoute.InventoryManagement) }) { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") }
                    IconButton(onClick = { navTo(KoColorRoute.ColorSearch) }) { Icon(Icons.Default.Search, contentDescription = "Search") }
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
                        text = "Here is a glance at the collection today.",
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
                        modifier = Modifier.weight(1f),
                        onClick = { navTo(KoColorRoute.CosmeticAnalytics) }
                    )
                    SummaryStatCard(
                        label = "EXPIRING SOON",
                        value = uiState.expiringCosmeticsCount.toString(),
                        icon = Icons.Default.ErrorOutline,
                        modifier = Modifier.weight(1f),
                        onClick = { navTo(KoColorRoute.ExpiringSoon) }
                    )
                }
            }

            // 3. Category Hero Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORIES",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            onClick = { showTaxonomyInfo = true },
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFD4AF37)), // Golden Border
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
                            "Skincare & Prep" to (Color(0xFFF7F2EB) to "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400&q=80"),
                            "Complexion" to (Color(0xFFF9F6F0) to "https://images.unsplash.com/photo-1596704017254-9b121068fb31?w=400&q=80"),
                            "Color & Dimension" to (Color(0xFFFDEEF4) to "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=400&q=80"),
                            "Eyes & Brows" to (Color(0xFFE8F1FD) to "https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=400&q=80"),
                            "Lips" to (Color(0xFFFEECEB) to "https://images.unsplash.com/photo-1586776977607-310e9c725c37?w=400&q=80")
                        )
                        
                        sections.forEach { (name, props) ->
                            val (bgColor, placeholderUrl) = props
                            val metadata = uiState.categoriesMetadata.entries.find { it.key.contains(name, ignoreCase = true) }?.value
                            VanityCategoryCard(
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
private fun VanityCategoryCard(
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
    val description = metadata?.description ?: when {
        name.contains("Skincare", ignoreCase = true) -> "Everything applied before pigment."
        name.contains("Complexion", ignoreCase = true) -> "Products that unify the skin tone."
        name.contains("Dimension", ignoreCase = true) -> "Products that bring life, shadow, and light."
        name.contains("Eyes", ignoreCase = true) -> "All definition for the upper face."
        name.contains("Lips", ignoreCase = true) -> "All color and care for the mouth."
        else -> "Professional curated category."
    }
    
    val currencyFormatter = remember { 
        java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US)
    }

    Card(
        onClick = { navTo(KoColorRoute.CosmeticCategoryCover(name)) },
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Imagery
            AsyncImage(
                model = metadata?.representativeImageUrl ?: placeholderUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.3f),
                contentScale = ContentScale.Crop
            )
            
            // 2. Readability Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(baseColor, baseColor.copy(alpha = 0.9f), Color.Transparent),
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
                    Column(modifier = Modifier.weight(1f)) {
                        val displayName = if (name.contains("&")) name.substringBefore("&").trim() else name
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp, lineHeight = 38.sp),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val itemsText = if (count == 1) "1 Item" else "$count Items"
                            Text(
                                text = itemsText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.alpha(0.9f)
                            )
                            Text(
                                text = "  |  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.3f)
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.alpha(0.6f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = currencyFormatter.format(totalValue),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black
                        )
                        Text(
                            text = "TOTAL VALUE",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.alpha(0.4f)
                        )
                    }
                }

                // Stock Status Bar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "STOCK STATUS", 
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp), 
                            fontWeight = FontWeight.Black, 
                            modifier = Modifier.alpha(0.6f)
                        )
                        Text(
                            text = "${(averageFill * 100).toInt()}%", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Black, 
                            color = Color(0xFF6B705C)
                        )
                    }
                    
                    val statusColor = Color(0xFF6B705C) 

                    LinearProgressIndicator(
                        progress = { averageFill.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = statusColor,
                        trackColor = Color.Black.copy(alpha = 0.05f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    if (leadingBrand != null) {
                        Text(
                            text = "LEADING BRAND: ${leadingBrand.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(0.6f)
                        )
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
    val isExpiring = label.contains("EXPIRING")
    
    val serifFont = FontFamily.Serif
    val charcoal = Color(0xFF2C2420)
    
    // Metallic/Bronze gradient for the expiring number
    val valueBrush = if (isExpiring) {
        Brush.linearGradient(listOf(Color(0xFF8E5431), Color(0xFFD4AF37), Color(0xFF8E5431)))
    } else {
        null
    }

    val actionBg = if (isExpiring) {
        Brush.linearGradient(listOf(Color(0xFF4A0000), Color(0xFF8B0000), Color(0xFF4A0000)))
    } else {
        Brush.linearGradient(listOf(
            Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFADAD), 
            Color(0xFFFFD6A5), Color(0xFFFDFFB6), Color(0xFFCAFFBF)
        ))
    }
    
    val actionText = if (isExpiring) "VIEW ITEMS" else "VIEW BLUEPRINT"
    val actionContentColor = if (isExpiring) Color.White else charcoal.copy(alpha = 0.8f)

    Card(
        modifier = modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFB)),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main Content Area
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
                            fontSize = 72.sp,
                            fontFamily = serifFont,
                            brush = valueBrush
                        ),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 72.sp,
                            fontFamily = serifFont
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

            // Luxury Action Footer
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

@Composable
fun ProfessionalTaxonomyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Professional Taxonomy", 
                style = MaterialTheme.typography.headlineMedium, 
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    TaxonomySection(
                        level = "Level 1",
                        title = "Macro Categories (The UI Layer)",
                        description = "Top-level intuitive 'buckets' for body-zone mapping.",
                        items = listOf(
                            "Skincare & Prep" to "Applied before pigment.",
                            "Complexion (Base)" to "Unifies skin tone.",
                            "Color & Dimension" to "Life, shadow, and light.",
                            "Eyes & Brows" to "Upper face definition.",
                            "Lips" to "Color and care.",
                            "Tools & Hygiene" to "Application and sanitization."
                        )
                    )
                }
                
                item {
                    TaxonomySection(
                        level = "Level 2",
                        title = "Micro Categories (Product Type)",
                        description = "Specific product types ensuring a clean, technical database.",
                        items = listOf(
                            "Skincare" to "Cleanser, Toner, Serum, SPF, Primer.",
                            "Complexion" to "Foundation, Concealer, Setting Powder.",
                            "Dimension" to "Blush, Bronzer, Contour, Highlighter.",
                            "Eyes" to "Eyeshadow, Eyeliner, Mascara, Brow Gel.",
                            "Lips" to "Lipstick, Gloss, Liner, Stain, Balm."
                        )
                    )
                }

                item {
                    TaxonomySection(
                        level = "Level 3",
                        title = "Professional Facets (The Engine Layer)",
                        description = "Expert-status attributes for algorithmic synergy and filtering.",
                        items = listOf(
                            "Formulation" to "Liquid, Cream, Powder, Gel, Balm.",
                            "Chemistry" to "Water, Silicone, or Oil bases (Critical for layering).",
                            "Finish" to "Matte, Satin, Radiant, Metallic, Glitter.",
                            "Coverage" to "Sheer, Light, Medium, Full, Buildable.",
                            "Temperature" to "Warm, Cool, Neutral, Olive (Engine alignment)."
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Understand", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFF9F6F0)
    )
}

@Composable
private fun TaxonomySection(
    level: String,
    title: String,
    description: String,
    items: List<Pair<String, String>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                text = level.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.7f)
            )
        }

        Surface(
            color = Color.White.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { (label, detail) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.alpha(0.7f)
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
