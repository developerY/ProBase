package com.zoewave.probase.kocolor.mobile.features.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.SavedAnalysis
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Preview(showBackground = true)
@Composable
private fun ColorUiRoutePreview() {
    MaterialTheme {
        ColorUiRoute(
            uiState = ColorUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun ColorUiRoute(
    uiState: ColorUiState,
    onEvent: (ColorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    ColorScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorScreen(
    uiState: ColorUiState,
    onEvent: (ColorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Color Analysis History", 
                        fontFamily = FontFamily.Serif, 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F6F0) // Cream background
    ) { padding ->
        if (uiState.savedSuggestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No history found. Try analyzing a look!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(uiState.savedSuggestions) { analysis ->
                    ColorHistoryCard(
                        analysis = analysis,
                        onClick = { navTo(KoColorRoute.CollectionDetail(analysis.id)) },
                        onEditClick = { navTo(KoColorRoute.Stitch(analysis.id, isCopy = false)) },
                        onCopyClick = { navTo(KoColorRoute.Stitch(analysis.id, isCopy = true)) },
                        onDeleteClick = { onEvent(ColorEvent.DeleteCollection(analysis.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorHistoryCard(
    analysis: SavedAnalysis,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()) }
    val dateStr = dateFormat.format(Date(analysis.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 1. Top Bar: Date, Season and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Surface(
                        color = Color(0xFFEADDFF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = analysis.advice.seasonalType.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onCopyClick) {
                        Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(18.dp), tint = Color.Gray)
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp), tint = Color.Gray)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.DeleteOutline, "Delete", modifier = Modifier.size(18.dp), tint = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. Middle Content: Images + Summary
            Row(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.weight(0.45f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    analysis.advice.faceUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    analysis.advice.clothesUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(0.55f)) {
                    Text(
                        text = analysis.advice.title ?: "Curated Look",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = analysis.advice.summary,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF2C2420)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. Palette Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                analysis.advice.recommendedPalette.take(5).forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                            .border(0.5.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                    )
                }
            }
        }
    }
}

data class ColorDetailUiState(
    val analysis: SavedAnalysis? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorDetailScreen(
    uiState: ColorDetailUiState,
    onEvent: (ColorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val analysis = uiState.analysis ?: return
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analysis Details") },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                analysis.advice.faceUri?.let {
                    Card(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        AsyncImage(
                            model = it,
                            contentDescription = "Face",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                analysis.advice.clothesUri?.let {
                    Card(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        AsyncImage(
                            model = it,
                            contentDescription = "Clothes",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Seasonal Type", style = MaterialTheme.typography.labelMedium)
                        Text(analysis.advice.seasonalType.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Undertone", style = MaterialTheme.typography.labelMedium)
                        Text(analysis.advice.undertone.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Perfect Makeup Palette",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MakeupPaletteGraphic(
                uiState = analysis.advice.recommendedPalette,
                onEvent = onEvent,
                navTo = navTo
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Makeup & Nail Suggestions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            analysis.advice.makeupSuggestions.forEach { suggestion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(suggestion.category, fontWeight = FontWeight.Bold)
                            Text(suggestion.advice)
                        }
                        if (suggestion.category.contains("Nail", ignoreCase = true)) {
                            Button(
                                onClick = {
                                    val color = suggestion.recommendedColors.firstOrNull() ?: "#FF0000"
                                    val finish = if (suggestion.advice.contains("Matte", ignoreCase = true)) "MATTE"
                                                 else if (suggestion.advice.contains("Metallic", ignoreCase = true)) "METALLIC"
                                                 else "GLOSSY"
                                    navTo(KoColorRoute.NailLab(color, finish))
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Experience", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "AI Coordination Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    text = analysis.advice.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun MakeupPaletteGraphic(
    uiState: List<String>,
    onEvent: (ColorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val paletteBackground = Brush.linearGradient(
        colors = listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.background(paletteBackground).padding(16.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 600.dp)
            ) {
                items(uiState) { hex ->
                    MakeupPan(
                        uiState = hex,
                        onEvent = onEvent,
                        navTo = navTo
                    )
                }
            }
        }
    }
}

@Composable
fun MakeupPan(
    uiState: String,
    onEvent: (ColorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(uiState))
    } catch (e: Exception) {
        Color.Gray
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
        )
        Text(
            text = uiState.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun MakeupPanPreview() {
    MaterialTheme {
        MakeupPan(uiState = "#FF0000", onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MakeupPaletteGraphicPreview() {
    MaterialTheme {
        MakeupPaletteGraphic(uiState = listOf("#FF0000", "#00FF00", "#0000FF"), onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorHistoryCardPreview() {
    MaterialTheme {
        ColorHistoryCard(
            analysis = SavedAnalysis(
                id = 1,
                timestamp = System.currentTimeMillis(),
                advice = FashionAdvice(
                    summary = "Your features—cool-toned skin, dark hair, and clear eyes—align perfectly with a Deep Winter palette.",
                    seasonalType = SeasonalType.WINTER,
                    undertone = Undertone.COOL,
                    makeupSuggestions = emptyList(),
                    outfitSuggestions = emptyList(),
                    recommendedPalette = listOf("#0047AB", "#FFFFFF", "#708090", "#C77398", "#3D2B1F")
                )
            ),
            onClick = {},
            onEditClick = {},
            onCopyClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorScreenPreview_Populated() {
    MaterialTheme {
        ColorScreen(
            uiState = ColorUiState(
                savedSuggestions = listOf(
                    SavedAnalysis(
                        id = 1,
                        timestamp = System.currentTimeMillis(),
                        advice = FashionAdvice(
                            summary = "Soft summer look with pastels.",
                            seasonalType = SeasonalType.SUMMER,
                            undertone = Undertone.NEUTRAL,
                            makeupSuggestions = emptyList(),
                            outfitSuggestions = emptyList(),
                            recommendedPalette = listOf("#F8D7DA", "#D4EDDA", "#CCE5FF")
                        )
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorDetailScreenPreview() {
    MaterialTheme {
        ColorDetailScreen(
            uiState = ColorDetailUiState(
                analysis = SavedAnalysis(
                    id = 1,
                    timestamp = System.currentTimeMillis(),
                    advice = FashionAdvice(
                        summary = "Perfect coordination for a night out.",
                        seasonalType = SeasonalType.WINTER,
                        undertone = Undertone.COOL,
                        makeupSuggestions = emptyList(),
                        outfitSuggestions = emptyList(),
                        recommendedPalette = listOf("#1A1A1A", "#FFFFFF", "#C0C0C0", "#FF007F", "#4B0082", "#000080")
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
