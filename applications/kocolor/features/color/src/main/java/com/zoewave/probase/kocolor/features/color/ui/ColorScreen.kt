package com.zoewave.probase.kocolor.features.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.kocolor.model.FashionProfile
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone

@Composable
fun ColorUiRoute(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: ColorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ColorScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorScreen(
    uiState: ColorUiState,
    onEvent: (ColorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Palette") }
            )
        },
        modifier = modifier
    ) { padding ->
        val profile = uiState.fashionProfile
        if (profile == null || profile.recommendedPalette.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Analyze your face and outfit to see your palette!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Look Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                            Text(profile.seasonalType.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Undertone", style = MaterialTheme.typography.labelMedium)
                            Text(profile.undertone.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                
                MakeupPaletteGraphic(profile.recommendedPalette)
                
                if (!profile.notes.isNullOrBlank()) {
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
                            text = profile.notes!!,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MakeupPaletteGraphic(colors: List<String>) {
    // A stylized representation of an eyeshadow palette
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
                columns = GridCells.Fixed(3), // Fixed 3 columns to look more like a pro palette
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

@Preview(showBackground = true)
@Composable
private fun ColorScreenPreview_Populated() {
    MaterialTheme {
        ColorScreen(
            uiState = ColorUiState(
                fashionProfile = FashionProfile(
                    seasonalType = SeasonalType.SUMMER,
                    undertone = Undertone.NEUTRAL,
                    recommendedPalette = listOf("#F8D7DA", "#D4EDDA", "#CCE5FF", "#FFF3CD", "#D1ECF1", "#F5C6CB", "#C3E6CB", "#B8DAFF", "#FFEEBA"),
                    notes = "Your summer look benefits from soft pastels and muted tones that bridge your neutral undertone with the current outfit."
                )
            ),
            onEvent = {}
        )
    }
}
