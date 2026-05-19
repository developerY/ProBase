package com.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.ClothingItem
import com.probase.kocolor.features.inventory.util.toComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarmentDetailScreen(
    clothingItem: ClothingItem,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Garment Analysis") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = clothingItem.imageUrl, contentDescription = "Garment Image",
                modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Extracted Palette", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(clothingItem.paletteHexes) { hex ->
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(hex.toComposeColor())
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Color Signatures", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    ColorSwatchCard(label = "Dominant", hex = clothingItem.dominantHex)
                    clothingItem.vibrantHex?.let { ColorSwatchCard(label = "Vibrant", hex = it) }
                    clothingItem.mutedHex?.let { ColorSwatchCard(label = "Muted", hex = it) }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text("Styling Intelligence", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                clothingItem.seasonalPalette?.let { IntelligenceChip(title = "Seasonal Palette", value = it) ; Spacer(modifier = Modifier.height(8.dp)) }
                clothingItem.colorTemperature?.let { IntelligenceChip(title = "Temperature", value = it) ; Spacer(modifier = Modifier.height(8.dp)) }
                clothingItem.contrastLevel?.let { IntelligenceChip(title = "Contrast Level", value = it) }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
