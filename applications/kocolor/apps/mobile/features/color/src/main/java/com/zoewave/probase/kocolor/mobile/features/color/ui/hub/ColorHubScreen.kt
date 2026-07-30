package com.zoewave.probase.kocolor.mobile.features.color.ui.hub

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.colors.domain.model.ColorSignature
import com.zoewave.probase.kocolor.features.colors.domain.model.SourceType
import com.zoewave.probase.kocolor.features.colors.util.ColorScienceUtils
import com.zoewave.probase.kocolor.features.seasonal_trends.ui.SeasonalTrendsContainer
import com.zoewave.probase.kocolor.features.seasonal_trends.ui.SeasonalTrendsViewModel
import com.zoewave.probase.kocolor.mobile.features.color.R
import com.zoewave.probase.kocolor.model.KoColorRoute
import android.graphics.Color as AndroidColor
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorHubScreen(
    uiState: ColorHubUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var selectedGroup by remember { mutableStateOf<Pair<String, List<ColorSignature>>?>(null) }
    var showShopWipDialog by remember { mutableStateOf(false) }
    val serifFont = FontFamily.Serif

    if (showShopWipDialog) {
        AlertDialog(
            onDismissRequest = { showShopWipDialog = false },
            title = { 
                Text(
                    "Future of Fashion: AI Curation", 
                    fontFamily = serifFont, 
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "We are building a sophisticated AI-driven curator to transform your color analysis into action.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Coming soon to the Boutique:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    BulletListItem("AI shortlists of items that perfectly fill your detected seasonal gaps.")
                    // BulletListItem("Instant AR Try-On deep-links into NailLab and FaceLab for every item.")
                    BulletListItem("Professional-grade matches verified by the Glow Archive engine.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showShopWipDialog = false }) {
                    Text("CLOSE", fontWeight = FontWeight.Black)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Chromatic DNA", 
                        fontFamily = serifFont, 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF2C2420)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F6F0) // Matching the dashboard background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            // --- SECTION 1: YOUR COLOR SPECTRUM ---
            item {
                Column {
                    Text(
                        "PROFILE ANALYSIS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Color Spectrum",
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = serifFont,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C2420)
                        )
                        Text(
                            "Reset",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { selectedGroup = null }
                        )
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    ChromaticDnaBar(
                        colors = uiState.inventoryColors,
                        selectedGroup = selectedGroup,
                        onGroupSelected = { selectedGroup = it },
                        navTo = navTo
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Skin Tone Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE4A493)) // Roseate Sand swatch
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            buildAnnotatedString {
                                append("Signature skin tone detected: ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Roseate Sand")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2C2420)
                        )
                    }
                }
            }

            // --- SECTION 2: CURATED ESSENTIALS ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    val title = if (selectedGroup == null) {
                        "Curated Essentials"
                    } else {
                        val (hex, _) = selectedGroup!!
                        "Shade: ${ColorScienceUtils.findNearestPantone(hex).name}"
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = serifFont,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2420)
                    )
                    
                    if (selectedGroup == null) {
                        // Placeholder View
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ColorLens,
                                        contentDescription = null,
                                        tint = Color.Gray.copy(alpha = 0.3f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Select a color above to see matching items",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
                        // Selected Shade View
                        val (_, items) = selectedGroup!!
                        
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Dynamic list of items in the shade
                            items.forEach { sig ->
                                InventoryProductCard(
                                    name = sig.name ?: "Unnamed Item",
                                    source = when (sig.sourceType) {
                                        SourceType.WARDROBE -> "Wardrobe"
                                        SourceType.VANITY -> "Vanity"
                                    },
                                    hex = sig.hex,
                                    onClick = {
                                        val route = when (sig.sourceType) {
                                            SourceType.WARDROBE -> KoColorRoute.WardrobeDetail(sig.sourceId)
                                            SourceType.VANITY -> KoColorRoute.CosmeticDetail(sig.sourceId)
                                        }
                                        navTo(route)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 3: PALETTE INSIGHTS ---
            item {
                Column {
                    Text(
                        "Palette Insights",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = serifFont,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Detected gaps based on your ")
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)) {
                                append(uiState.userSeason.name)
                            }
                            append(" season profile.")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InsightGapCard(
                            label = "MISSING",
                            color = Color(0xFF000080), // Midnight Navy
                            name = "Midnight Navy",
                            isEssential = true,
                            modifier = Modifier.weight(1f)
                        )
                        InsightGapCard(
                            label = "RECOMMENDED",
                            color = Color(0xFF800080), // Royal Plum
                            name = "Royal Plum",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- SECTION 4: THE STYLIST'S EDIT ---
            item {
                uiState.stylistEdit?.let { edit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF745E7A))
                    ) {
                        Column(modifier = Modifier.padding(32.dp)) {
                            Text(
                                edit.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = serifFont,
                                fontStyle = FontStyle.Italic,
                                color = Color.White
                            )
                            
                            Spacer(Modifier.height(24.dp))
                            
                            BulletPoint(
                                text = buildAnnotatedString {
                                    append(edit.primaryInsight)
                                }
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            BulletPoint(
                                text = buildAnnotatedString {
                                    append(edit.recommendation)
                                }
                            )
                            
                            Spacer(Modifier.height(32.dp))
                            
                            Button(
                                onClick = { showShopWipDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(
                                    edit.buttonText,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 5: SEASONAL INSPIRATION ---
            item {
                val trendsViewModel: SeasonalTrendsViewModel = hiltViewModel()
                val trendsState by trendsViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(uiState.userSeason) {
                    trendsViewModel.fetchSeasonalTrends(
                        uiState.userSeason.name,
                        "Roseate Sand" // Placeholder for actual skin tone
                    )
                }

                SeasonalTrendsContainer(
                    uiState = trendsState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

@Composable
private fun InventoryProductCard(
    name: String,
    source: String,
    hex: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(parseColor(hex))
                    .border(0.5.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2420)
                )
                Text(
                    text = source,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun InsightGapCard(
    label: String,
    color: Color,
    name: String,
    isEssential: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(color)
                        .shadow(4.dp, CircleShape)
                )
                
                if (isEssential) {
                    Surface(
                        color = Color(0xFF8B7330),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.offset(x = 8.dp, y = (-4).dp)
                    ) {
                        Text(
                            "ESSENTIAL",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2420)
            )
        }
    }
}

@Composable
private fun BulletPoint(text: AnnotatedString) {
    Row {
        Text(
            "✦", 
            color = Color(0xFFD4AF37),
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            lineHeight = 22.sp
        )
    }
}

@Composable
fun ChromaticDnaBar(
    colors: List<ColorSignature>,
    selectedGroup: Pair<String, List<ColorSignature>>?,
    onGroupSelected: (Pair<String, List<ColorSignature>>?) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val colorGroups = remember(colors) {
        colors
            .filter { it.hex.isNotBlank() }
            .groupBy { it.hex }
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

    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // The Spectrum Bar (Harmonica Scroll Effect)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val totalGroups = colorGroups.size
            val containerWidth = maxWidth
            
            // Calculate unselected width to fill exactly the container if possible, 
            // but keep a minimum for visibility.
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
                    
                    // harmonica expansion effect
                    val animatedWidth by animateDpAsState(
                        targetValue = when {
                            selectedGroup == null -> baseItemWidth
                            isSelected -> 100.dp
                            else -> 12.dp // Shrink neighbors
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "width"
                    )

                    Box(
                        modifier = Modifier
                            .width(animatedWidth)
                            .fillMaxHeight()
                            .background(parseColor(hex))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent
                            )
                            .clickable { 
                                if (isSelected) {
                                    onGroupSelected(null)
                                } else {
                                    onGroupSelected(group)
                                    scope.launch {
                                        // Center the clicked item
                                        lazyListState.animateScrollToItem(index)
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletListItem(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("•", modifier = Modifier.width(20.dp), fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
        Text(text, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: Exception) {
        Color.Gray
    }
}
