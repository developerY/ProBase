package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulatorEvent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulationStep
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.ColorFamily
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun MagicBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "magic")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "phase"
    )

    Box(modifier = Modifier.fillMaxSize().alpha(0.05f).blur(80.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE8EAF6), Color(0xFFF3E5F5), Color.White),
                    center = center.copy(x = center.x * phase * 1.5f, y = center.y * (1 - phase) * 1.5f)
                )
            )
        }
    }
}

@Composable
fun MessagingStep(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.applications_kocolor_features_analyzer_simulator_intent_title),
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                lineHeight = 52.sp,
                color = Color.Black
            )
        }

        // User Portrait Slot
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.userPortraitUri != null) {
                            AsyncImage(
                                model = uiState.userPortraitUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(28.dp), tint = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.userPortraitUri != null) "Visual Identity Active" else "No Portrait Detected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Tap to change visual anchor",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { onEvent(SimulatorEvent.CapturePortrait) }) {
                            Icon(Icons.Default.PhotoCamera, null, tint = Color.DarkGray.copy(alpha = 0.6f), modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = { onEvent(SimulatorEvent.PickPortrait) }) {
                            Icon(Icons.Default.Collections, null, tint = Color.DarkGray.copy(alpha = 0.6f), modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }

        // Clothing Anchors
        if (uiState.fullClothingInventory.isNotEmpty()) {
            item {
                AnchorSection(
                    title = stringResource(R.string.applications_kocolor_features_analyzer_simulator_clothing_anchors),
                    categories = listOf(
                        Triple("Top", Icons.Default.Checkroom, ClothingCategory.TOPS),
                        Triple("Bottom", Icons.Default.Layers, ClothingCategory.BOTTOMS),
                        Triple("Shoes", Icons.AutoMirrored.Filled.DirectionsWalk, ClothingCategory.SHOES)
                    ),
                    selectedCategory = uiState.selectedClothingCategory,
                    onCategorySelect = { onEvent(SimulatorEvent.SelectClothingCategory(it as ClothingCategory)) },
                    families = uiState.clothingFamilies,
                    anchoredFamily = uiState.anchoredClothingFamilies[uiState.selectedClothingCategory],
                    onToggle = { onEvent(SimulatorEvent.ToggleClothingFamily(uiState.selectedClothingCategory, it)) }
                )
            }
        }

        // Makeup Anchors
        if (uiState.fullCosmeticInventory.isNotEmpty()) {
            item {
                AnchorSection(
                    title = stringResource(R.string.applications_kocolor_features_analyzer_simulator_makeup_anchors),
                    categories = listOf(
                        Triple("Eyes", Icons.Default.Visibility, MacroCategory.EYES),
                        Triple("Cheeks", Icons.Default.FaceRetouchingNatural, MacroCategory.DIMENSION),
                        Triple("Lips", Icons.Default.Face, MacroCategory.LIPS)
                    ),
                    selectedCategory = uiState.selectedCosmeticCategory,
                    onCategorySelect = { onEvent(SimulatorEvent.SelectCosmeticCategory(it as MacroCategory)) },
                    families = uiState.cosmeticFamilies,
                    anchoredFamily = uiState.anchoredCosmeticFamilies[uiState.selectedCosmeticCategory],
                    onToggle = { onEvent(SimulatorEvent.ToggleCosmeticFamily(uiState.selectedCosmeticCategory, it)) }
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                OutlinedTextField(
                    value = uiState.userMessage,
                    onValueChange = { onEvent(SimulatorEvent.UpdateMessage(it)) },
                    placeholder = { Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_intent_placeholder), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.alpha(0.5f)) },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        item {
            Button(
                onClick = { onEvent(SimulatorEvent.StartSimulation) },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_begin_action), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun AnchorSection(
    title: String,
    categories: List<Triple<String, ImageVector, Any>>,
    selectedCategory: Any,
    onCategorySelect: (Any) -> Unit,
    families: Map<ColorFamily, Any>,
    anchoredFamily: ColorFamily?,
    onToggle: (ColorFamily) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color.Black
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { (name, icon, cat) ->
                        val isSelected = selectedCategory == cat
                        CategoryPill(
                            label = name,
                            icon = icon,
                            isSelected = isSelected,
                            onClick = { onCategorySelect(cat) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Perceptual Color Buckets Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Only show families that have items in the current category
                    ColorFamily.entries.filter { families.containsKey(it) }.forEach { family ->
                        val isSelected = anchoredFamily == family

                        item {
                            ColorFamilySwatch(
                                family = family,
                                isSelected = isSelected,
                                onClick = { onToggle(family) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorFamilySwatch(
    family: ColorFamily,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(parseColor(family.hex))
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
            }
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFFE8EAF6) else Color(0xFFF5F5F5),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5CAE9)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color(0xFF3F51B5) else Color.Gray
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.Black else Color.Gray
            )
        }
    }
}

@Composable
fun AnalysisStep(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.95f, targetValue = 1.05f,
            animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse),
            label = "scale"
        )

        Box(
            modifier = Modifier.size(180.dp).graphicsLayer(scaleX = scale, scaleY = scale),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Icon(
                imageVector = when (uiState.simulationStep) {
                    SimulationStep.BIO_MARKERS -> Icons.Default.Favorite
                    SimulationStep.ROUTINE -> Icons.Default.AutoAwesome
                    else -> Icons.Default.Grain
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(Modifier.height(48.dp))
        
        Text(
            text = when (uiState.simulationStep) {
                SimulationStep.BIO_MARKERS -> stringResource(R.string.applications_kocolor_features_analyzer_simulator_reading_bio)
                SimulationStep.ROUTINE -> stringResource(R.string.applications_kocolor_features_analyzer_simulator_syncing_rituals)
                SimulationStep.GENERATING -> stringResource(R.string.applications_kocolor_features_analyzer_simulator_architecting_style)
                else -> stringResource(R.string.applications_kocolor_features_analyzer_simulator_magic)
            },
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.applications_kocolor_features_analyzer_simulator_synthesizing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.width(280.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ResultStep(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column {
                Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_blueprint), style = MaterialTheme.typography.displaySmall, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (uiState.isLocalResult) Color.Gray else Color.Green))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isLocalResult) stringResource(R.string.applications_kocolor_features_analyzer_simulator_local_calc) else stringResource(R.string.applications_kocolor_features_analyzer_simulator_ai_optimized), 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 1.sp
                    )
                }
                
                uiState.rationale?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f),
                        lineHeight = 24.sp
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_chromatic_core), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val palette = uiState.recommendedPalette
                    palette.forEach { hex ->
                        Box(
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(parseColor(hex)).border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        )
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_vault_selections), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        items(uiState.recommendedClothing) { item: ClothingItem ->
            ResultCard(uiState = item, onEvent = {}, navTo = navTo)
        }
        
        item {
            Button(
                onClick = { onEvent(SimulatorEvent.SaveToPalette) },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_lock_palette), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ResultCard(
    uiState: ClothingItem,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val item = uiState
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.fillMaxHeight().width(120.dp).background(item.colorHex?.let { parseColor(it) } ?: Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl != null) {
                    AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = Color.Black.copy(alpha = 0.1f), modifier = Modifier.size(32.dp))
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = item.category.name, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
                Text(text = item.brand ?: stringResource(R.string.applications_kocolor_features_analyzer_simulator_bespoke), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultCardPreview() {
    MaterialTheme {
        ResultCard(
            uiState = ClothingItem(name = "Shirt", category = ClothingCategory.TOPS),
            onEvent = {},
            navTo = {}
        )
    }
}
