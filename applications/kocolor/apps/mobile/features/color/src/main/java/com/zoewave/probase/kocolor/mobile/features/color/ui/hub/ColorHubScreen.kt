package com.zoewave.probase.kocolor.mobile.features.color.ui.hub

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.colors.domain.model.ColorSignature
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorHubScreen(
    uiState: ColorHubUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Color Hub", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Chromatic DNA (The Inventory Visualization)
            item {
                Text("Chromatic DNA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                ChromaticDnaBar(colors = uiState.inventoryColors)
            }

            // 2. Palette Gap Analysis
            item {
                Text("Palette Gaps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Detected based on your ${uiState.userSeason.name} profile", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                
                if (uiState.paletteGaps.isEmpty()) {
                    Text("Your inventory is perfectly balanced!", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.paletteGaps) { hex ->
                            GapIndicator(hex = hex)
                        }
                    }
                }
            }
            
            // 3. Recommended Additions (Simulated)
            item {
                Text("Suggested Harmonies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Text(
                        "Try adding more deep jewel tones to your wardrobe to complement your current collection of cool neutrals.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

@Composable
fun ChromaticDnaBar(colors: List<ColorSignature>) {
    val sortedColors = remember(colors) {
        colors.sortedBy { it.hex } // Simplified sorting for now
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        if (sortedColors.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No color data found", style = MaterialTheme.typography.labelSmall)
            }
        } else {
            sortedColors.forEach { sig ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(parseColor(sig.hex))
                        .clickable { /* Show Professional Spec Sheet */ }
                )
            }
        }
    }
}

@Composable
fun GapIndicator(hex: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(parseColor(hex))
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(Modifier.height(8.dp))
        Text("Missing", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: Exception) {
        Color.Gray
    }
}
