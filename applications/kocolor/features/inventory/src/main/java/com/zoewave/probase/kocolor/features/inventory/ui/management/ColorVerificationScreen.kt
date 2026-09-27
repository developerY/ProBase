package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.util.toComposeColor
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlin.math.sqrt

data class ColorVerificationUiState(
    val items: List<ClothingItem> = emptyList()
)

enum class WardrobeHarmonyMode(val displayName: String) {
    EXACT("Exact Match"),
    COMPLEMENTARY("Complementary"),
    ANALOGOUS("Analogous"),
    MONOCHROMATIC("Monochromatic")
}

@Composable
fun ColorVerificationRoute(
    uiState: ColorVerificationUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    ColorVerificationScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
}

@Preview(showBackground = true)
@Composable
private fun ColorVerificationScreenPreview() {
    MaterialTheme {
        ColorVerificationScreen(
            uiState = ColorVerificationUiState(
                items = listOf(ClothingItem(name = "Item", category = ClothingCategory.TOPS, colorHex = "#FFFFFF"))
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorVerificationScreen(
    uiState: ColorVerificationUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var selectedColorHex by remember { mutableStateOf("#5A3854") } // Default Plum
    var searchMode by remember { mutableStateOf(WardrobeHarmonyMode.EXACT) }

    val presetColors = listOf(
        "#5A3854", "#800020", "#C25C4A", "#8D6E63", "#415A77",
        "#3B5249", "#D4AF37", "#000000", "#FFFFFF", "#8B8378"
    )

    // Filter wardrobe clothing items by selected color and search mode
    val matchedItems = remember(uiState.items, selectedColorHex, searchMode) {
        uiState.items.filter { item ->
            val itemHex = item.dominantHex ?: item.colorHex ?: return@filter false
            val dist = calculateColorDistance(selectedColorHex, itemHex)
            when (searchMode) {
                WardrobeHarmonyMode.EXACT -> dist < 110f
                WardrobeHarmonyMode.COMPLEMENTARY -> dist < 180f
                WardrobeHarmonyMode.ANALOGOUS -> dist < 140f
                WardrobeHarmonyMode.MONOCHROMATIC -> dist < 140f
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Wardrobe Color Search",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.applications_kocolor_features_inventory_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFBF8F5)),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Description
            item {
                Column {
                    Text(
                        text = "Match Clothes by Chromatic Swatch",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2420)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select a color swatch to discover matching garments in your wardrobe.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            // Target Color Swatches Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TARGET SWATCH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.Gray
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(presetColors) { hex ->
                            val isSelected = hex.equals(selectedColorHex, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedColorHex = hex },
                                shape = CircleShape,
                                color = hex.toComposeColor(),
                                border = if (isSelected) BorderStroke(3.dp, Color(0xFFD4AF37)) else BorderStroke(1.dp, Color.LightGray)
                            ) {
                                if (isSelected) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (hex == "#FFFFFF") Color.Black else Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Harmony Mode Selection Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "HARMONY MODE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.Gray
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(WardrobeHarmonyMode.entries) { mode ->
                            val isSelected = mode == searchMode
                            FilterChip(
                                selected = isSelected,
                                onClick = { searchMode = mode },
                                label = { Text(mode.displayName, fontSize = 12.sp) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2C2420),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color.Gray
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFFE0E0E0),
                                    selectedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }

            // Matching Items Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MATCHING GARMENTS (${matchedItems.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.Gray
                    )
                }
            }

            // Matching Garments List
            if (matchedItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No wardrobe pieces match this color harmony.\nTry selecting a different color swatch or mode.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            } else {
                items(matchedItems) { item ->
                    WardrobeColorSearchCard(
                        item = item,
                        targetHex = selectedColorHex,
                        onClick = { navTo(KoColorRoute.WardrobeDetail(item.internalId)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WardrobeColorSearchCard(
    item: ClothingItem,
    targetHex: String,
    onClick: () -> Unit
) {
    val itemHex = item.dominantHex ?: item.colorHex ?: "#FFFFFF"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                if (item.imageUrl != null) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Checkroom,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        tint = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.category.displayName.uppercase()} • ${item.brand ?: "ARCHIVE"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2420)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(itemHex.toComposeColor())
                            .border(1.dp, Color.LightGray, CircleShape)
                    )

                    Text(
                        text = "Dominant: $itemHex",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun calculateColorDistance(hex1: String, hex2: String): Float {
    return try {
        val c1 = android.graphics.Color.parseColor(if (hex1.startsWith("#")) hex1 else "#$hex1")
        val c2 = android.graphics.Color.parseColor(if (hex2.startsWith("#")) hex2 else "#$hex2")
        val r = (android.graphics.Color.red(c1) - android.graphics.Color.red(c2)).toFloat()
        val g = (android.graphics.Color.green(c1) - android.graphics.Color.green(c2)).toFloat()
        val b = (android.graphics.Color.blue(c1) - android.graphics.Color.blue(c2)).toFloat()
        sqrt(r * r + g * g + b * b)
    } catch (e: Exception) {
        Float.MAX_VALUE
    }
}
