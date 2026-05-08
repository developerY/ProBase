package com.zoewave.probase.kocolor.features.color.ui

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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@Composable
fun ColorUiRoute(
    windowSizeClass: WindowSizeClass,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ColorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ColorScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navTo = navTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorScreen(
    uiState: ColorUiState,
    onEvent: (ColorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fashion History") }
            )
        },
        modifier = modifier
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.savedSuggestions) { analysis ->
                    FashionAnalysisCard(
                        analysis = analysis,
                        onClick = { navTo(KoColorRoute.ColorDetail(analysis.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun FashionAnalysisCard(
    analysis: SavedAnalysis,
    onClick: () -> Unit
) {
    val date = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()).format(Date(analysis.timestamp))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = date, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text(analysis.advice.seasonalType.name)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Image previews if available
                if (analysis.advice.faceUri != null || analysis.advice.clothesUri != null) {
                    Row(
                        modifier = Modifier.height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        analysis.advice.faceUri?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = "Face",
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        analysis.advice.clothesUri?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = "Clothes",
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = analysis.advice.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mini palette preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                analysis.advice.recommendedPalette.take(6).forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                            .border(0.5.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorDetailScreen(
    analysis: SavedAnalysis,
    onBack: () -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analysis Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
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
            
            MakeupPaletteGraphic(analysis.advice.recommendedPalette)
            
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
fun MakeupPaletteGraphic(colors: List<String>) {
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
                items(colors) { hex ->
                    MakeupPan(hex)
                }
            }
        }
    }
}

@Composable
fun MakeupPan(hex: String) {
    val color = try {
        Color(android.graphics.Color.parseColor(hex))
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
            text = hex.uppercase(),
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
            ),
            onBack = {},
            navTo = {}
        )
    }
}
