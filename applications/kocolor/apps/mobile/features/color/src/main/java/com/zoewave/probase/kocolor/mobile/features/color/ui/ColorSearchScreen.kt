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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSearchScreen(
    uiState: ColorSearchUiState,
    onEvent: (ColorSearchEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = parseColor(uiState.selectedColorHex),
            onColorSelected = { 
                onEvent(ColorSearchEvent.SelectColor(it.toHex()))
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false },
            title = "Choose Custom Color"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Color Search", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Profile */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
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
            // Recent Colors Grid
            item {
                Text(
                    "Recent Colors & Seasonal Palettes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f).height(120.dp)) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.recentColors) { hex ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(parseColor(hex))
                                        .clickable { onEvent(ColorSearchEvent.SelectColor(hex)) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { navTo(KoColorRoute.Camera("color_scan")) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF0E4D7).copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Scan Color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Selected Color Detail
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(parseColor(uiState.selectedColorHex))
                            .clickable { showColorPicker = true }
                            .shadow(2.dp, RoundedCornerShape(20.dp))
                    )
                    Spacer(Modifier.width(20.dp))
                    Column {
                        val colorName = if (uiState.selectedColorHex.equals("#C25C4A", ignoreCase = true)) "Terracotta" else "Custom Tone"
                        Text(
                            text = colorName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        Text("HEX: ${uiState.selectedColorHex.uppercase()}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        
                        val rgb = com.zoewave.probase.kocolor.mobile.features.color.util.ColorScienceUtils.hexToRgb(uiState.selectedColorHex)
                        if (rgb != null) {
                            Text("RGB: ${rgb.first}, ${rgb.second}, ${rgb.third}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
            }

            // Filters
            item {
                Text("Filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFE5E5E5)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (uiState.searchMode == SearchMode.EXACT) Color(0xFFEBD3C5) else Color.Transparent)
                            .clickable { onEvent(ColorSearchEvent.SetMode(SearchMode.EXACT)) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Exact Match", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (uiState.searchMode == SearchMode.COMPLEMENTARY) Color(0xFFEBD3C5) else Color.Transparent)
                            .clickable { onEvent(ColorSearchEvent.SetMode(SearchMode.COMPLEMENTARY)) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Complementary Colors", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Advanced Harmony Icons (Image 2)
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { HarmonyIcon("Monochromatic", Icons.Default.BrightnessLow, uiState.searchMode == SearchMode.MONOCHROMATIC) { onEvent(ColorSearchEvent.SetMode(SearchMode.MONOCHROMATIC)) } }
                    item { HarmonyIcon("Analogous", Icons.Default.Compare, uiState.searchMode == SearchMode.ANALOGOUS) { onEvent(ColorSearchEvent.SetMode(SearchMode.ANALOGOUS)) } }
                    item { HarmonyIcon("Complementary", Icons.Default.SwapHoriz, uiState.searchMode == SearchMode.COMPLEMENTARY) { onEvent(ColorSearchEvent.SetMode(SearchMode.COMPLEMENTARY)) } }
                    item { HarmonyIcon("Triadic", Icons.Default.FilterVintage, uiState.searchMode == SearchMode.TRIADIC) { onEvent(ColorSearchEvent.SetMode(SearchMode.TRIADIC)) } }
                }
            }

            // Results: Cosmetics
            item {
                Text("Cosmetics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Spacer(Modifier.height(16.dp))
                if (uiState.matchedCosmetics.isEmpty()) {
                    Text("No matching cosmetics found.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.matchedCosmetics) { item ->
                            ResultCard(item.name, item.brand, item.imageUrl) {
                                navTo(KoColorRoute.CosmeticDetail(item.id))
                            }
                        }
                    }
                }
            }

            // Results: Wardrobe
            item {
                Text("Wardrobe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Spacer(Modifier.height(16.dp))
                if (uiState.matchedWardrobe.isEmpty()) {
                    Text("No matching wardrobe items found.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.matchedWardrobe) { item ->
                            ResultCard(item.name, item.brand ?: "", item.imageUrl) {
                                navTo(KoColorRoute.WardrobeDetail(item.id))
                            }
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

@Composable
private fun HarmonyIcon(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) Color(0xFFEBD3C5) else Color(0xFFF7F7F7))
                .border(1.dp, if (isSelected) Color.Black else Color.Transparent, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ResultCard(name: String, brand: String, imageUrl: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFF7F7F7))
            ) {
                if (imageUrl != null) {
                    AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Image, null, modifier = Modifier.align(Alignment.Center), tint = Color.LightGray)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(brand, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

sealed class ColorSearchEvent {
    data class SelectColor(val hex: String) : ColorSearchEvent()
    data class SetMode(val mode: SearchMode) : ColorSearchEvent()
}
