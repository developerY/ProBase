package com.zoewave.probase.kocolor.mobile.features.color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.kocolor.mobile.features.color.R
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
            title = stringResource(R.string.applications_kocolor_apps_mobile_features_color_choose_custom)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_search), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_apps_mobile_features_color_back))
                    }
                },
                actions = {
                    IconButton(onClick = { /* Profile */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = stringResource(R.string.applications_kocolor_apps_mobile_features_color_profile))
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
                    stringResource(R.string.applications_kocolor_apps_mobile_features_color_recent_palettes),
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
                            Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_scan_color), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
                        val colorName = if (uiState.selectedColorHex.equals("#C25C4A", ignoreCase = true)) "Terracotta" else stringResource(R.string.applications_kocolor_apps_mobile_features_color_custom_tone)
                        Text(
                            text = colorName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_hex_format, uiState.selectedColorHex.uppercase()), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        
                        val rgb = com.zoewave.probase.kocolor.mobile.features.color.util.ColorScienceUtils.hexToRgb(uiState.selectedColorHex)
                        if (rgb != null) {
                            Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_rgb_format, rgb.first, rgb.second, rgb.third), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
            }

            // Filters
            item {
                Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_filters), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
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
                        Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_exact_match), fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_complementary), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Advanced Harmony Icons (Image 2)
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { 
                        HarmonyIcon(
                            uiState = HarmonyIconUiState(stringResource(R.string.applications_kocolor_apps_mobile_features_color_monochromatic), Icons.Default.BrightnessLow, uiState.searchMode == SearchMode.MONOCHROMATIC), 
                            onEvent = { onEvent(ColorSearchEvent.SetMode(SearchMode.MONOCHROMATIC)) }, 
                            navTo = {}
                        ) 
                    }
                    item { 
                        HarmonyIcon(
                            uiState = HarmonyIconUiState(stringResource(R.string.applications_kocolor_apps_mobile_features_color_analogous), Icons.Default.Compare, uiState.searchMode == SearchMode.ANALOGOUS), 
                            onEvent = { onEvent(ColorSearchEvent.SetMode(SearchMode.ANALOGOUS)) }, 
                            navTo = {}
                        ) 
                    }
                    item { 
                        HarmonyIcon(
                            uiState = HarmonyIconUiState(stringResource(R.string.applications_kocolor_apps_mobile_features_color_complementary), Icons.Default.SwapHoriz, uiState.searchMode == SearchMode.COMPLEMENTARY), 
                            onEvent = { onEvent(ColorSearchEvent.SetMode(SearchMode.COMPLEMENTARY)) }, 
                            navTo = {}
                        ) 
                    }
                    item { 
                        HarmonyIcon(
                            uiState = HarmonyIconUiState(stringResource(R.string.applications_kocolor_apps_mobile_features_color_triadic), Icons.Default.FilterVintage, uiState.searchMode == SearchMode.TRIADIC), 
                            onEvent = { onEvent(ColorSearchEvent.SetMode(SearchMode.TRIADIC)) }, 
                            navTo = {}
                        ) 
                    }
                }
            }

            // Results: Cosmetics
            item {
                Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_cosmetics), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Spacer(Modifier.height(16.dp))
                if (uiState.matchedCosmetics.isEmpty()) {
                    Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_no_matching_cosmetics), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.matchedCosmetics) { item ->
                            ResultCard(
                                uiState = ResultCardUiState(item.name, item.brand, item.imageUrl),
                                onEvent = { navTo(KoColorRoute.CosmeticDetail(item.id)) },
                                navTo = navTo
                            )
                        }
                    }
                }
            }

            // Results: Wardrobe
            item {
                Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_wardrobe), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Spacer(Modifier.height(16.dp))
                if (uiState.matchedWardrobe.isEmpty()) {
                    Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_no_matching_wardrobe), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.matchedWardrobe) { item ->
                            ResultCard(
                                uiState = ResultCardUiState(item.name, item.brand ?: "", item.imageUrl),
                                onEvent = { navTo(KoColorRoute.WardrobeDetail(item.id)) },
                                navTo = navTo
                            )
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

data class HarmonyIconUiState(val label: String, val icon: ImageVector, val isSelected: Boolean)

@Composable
private fun HarmonyIcon(
    uiState: HarmonyIconUiState, 
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onEvent(Unit) }) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (uiState.isSelected) Color(0xFFEBD3C5) else Color(0xFFF7F7F7))
                .border(1.dp, if (uiState.isSelected) Color.Black else Color.Transparent, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(uiState.icon, contentDescription = uiState.label, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(uiState.label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

data class ResultCardUiState(val name: String, val brand: String, val imageUrl: String?)

@Composable
private fun ResultCard(
    uiState: ResultCardUiState, 
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clickable { onEvent() },
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
                if (uiState.imageUrl != null) {
                    AsyncImage(model = uiState.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Image, null, modifier = Modifier.align(Alignment.Center), tint = Color.LightGray)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(uiState.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(uiState.brand, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

sealed class ColorSearchEvent {
    data class SelectColor(val hex: String) : ColorSearchEvent()
    data class SetMode(val mode: SearchMode) : ColorSearchEvent()
}
