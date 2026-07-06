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
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun MagicBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "magic")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "phase"
    )

    Box(modifier = Modifier.fillMaxSize().alpha(0.1f).blur(100.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6200EE), Color.Transparent),
                    center = center.copy(x = center.x * phase * 2, y = center.y * (1 - phase) * 2)
                ),
                radius = size.maxDimension
            )
        }
    }
}

@Composable
fun MessagingStep(
    userMessage: String,
    userPortraitUri: String?,
    allClothing: List<ClothingItem> = emptyList(),
    allCosmetics: List<CosmeticItem> = emptyList(),
    anchoredClothing: List<ClothingItem> = emptyList(),
    anchoredCosmetics: List<CosmeticItem> = emptyList(),
    selectedClothingCategory: ClothingCategory = ClothingCategory.TOPS,
    selectedCosmeticCategory: MacroCategory = MacroCategory.LIPS,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Spacer(Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.applications_kocolor_features_analyzer_simulator_intent_title),
                style = MaterialTheme.typography.displayMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                lineHeight = 52.sp
            )
        }

        // User Portrait Slot
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userPortraitUri != null) {
                            AsyncImage(
                                model = userPortraitUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp), tint = Color.White.copy(alpha = 0.5f))
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (userPortraitUri != null) "Visual Identity Active" else "No Portrait Detected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (userPortraitUri != null) "Tap to change visual anchor" else "Tap to add visual context",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onEvent(SimulatorEvent.CapturePortrait) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.PhotoCamera, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { onEvent(SimulatorEvent.PickPortrait) },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Collections, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Clothing Anchors
        if (allClothing.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "CLOTHING ANCHORS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val categories = listOf(
                        Triple("Top", Icons.Default.Checkroom, ClothingCategory.TOPS),
                        Triple("Bottom", Icons.Default.Layers, ClothingCategory.BOTTOMS),
                        Triple("Shoes", Icons.AutoMirrored.Filled.DirectionsWalk, ClothingCategory.SHOES)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { (name, icon, cat) ->
                                    val isSelected = selectedClothingCategory == cat
                                    CategoryTab(
                                        label = name,
                                        icon = icon,
                                        isSelected = isSelected,
                                        onClick = { onEvent(SimulatorEvent.SelectClothingCategory(cat)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            val itemsInSelectedCategory = allClothing.filter { it.category == selectedClothingCategory }
                            AnchorItemRow(
                                items = itemsInSelectedCategory,
                                selectedIds = anchoredClothing.map { it.id },
                                onToggle = { onEvent(SimulatorEvent.ToggleAnchoredClothing(it)) }
                            )
                        }
                    }
                }
            }
        }

        // Makeup Anchors
        if (allCosmetics.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "MAKEUP ANCHORS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val categories = listOf(
                        Triple("Eyes", Icons.Default.Visibility, MacroCategory.EYES),
                        Triple("Cheeks", Icons.Default.FaceRetouchingNatural, MacroCategory.DIMENSION),
                        Triple("Lips", Icons.Default.Face, MacroCategory.LIPS)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { (name, icon, cat) ->
                                    val isSelected = selectedCosmeticCategory == cat
                                    CategoryTab(
                                        label = name,
                                        icon = icon,
                                        isSelected = isSelected,
                                        onClick = { onEvent(SimulatorEvent.SelectCosmeticCategory(cat)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            val itemsInSelectedCategory = allCosmetics.filter { it.macroCategory == selectedCosmeticCategory }
                            AnchorItemRow(
                                items = itemsInSelectedCategory,
                                selectedIds = anchoredCosmetics.map { it.id },
                                onToggle = { onEvent(SimulatorEvent.ToggleAnchoredCosmetic(it)) }
                            )
                        }
                    }
                }
            }
        }
        
        item {
            OutlinedTextField(
                value = userMessage,
                onValueChange = { onEvent(SimulatorEvent.UpdateMessage(it)) },
                placeholder = { Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_intent_placeholder), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.alpha(0.4f)) },
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(32.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        item {
            Button(
                onClick = { onEvent(SimulatorEvent.StartSimulation) },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_begin_action), style = MaterialTheme.typography.titleMedium, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
private fun CategoryTab(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFE8E8FF) else Color(0xFFF3F2F8),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBCBFF)) else null
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else Color.Gray
            )
        }
    }
}

@Composable
private fun <T> AnchorItemRow(
    items: List<T>,
    selectedIds: List<Long>,
    onToggle: (T) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(items) { item ->
            val id = when (item) {
                is ClothingItem -> item.id
                is CosmeticItem -> item.id
                else -> 0L
            }
            val colorHex = when (item) {
                is ClothingItem -> item.colorHex
                is CosmeticItem -> item.colorHex
                else -> null
            }
            val imageUrl = when (item) {
                is ClothingItem -> item.imageUrl
                is CosmeticItem -> item.imageUrl
                else -> null
            }
            val isSelected = selectedIds.contains(id)
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorHex?.let { parseColor(it) } ?: Color.LightGray)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onToggle(item) },
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                if (isSelected) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp), tint = Color.White)
                    }
                }
            }
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
            initialValue = 0.9f, targetValue = 1.1f,
            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
            label = "scale"
        )

        Box(
            modifier = Modifier.size(200.dp).graphicsLayer(scaleX = scale, scaleY = scale),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Icon(
                imageVector = when (uiState.simulationStep) {
                    SimulationStep.BIO_MARKERS -> Icons.Default.Favorite
                    SimulationStep.ROUTINE -> Icons.Default.AutoAwesome
                    else -> Icons.Default.Grain
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        verticalArrangement = Arrangement.spacedBy(40.dp)
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_chromatic_core), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val palette = uiState.recommendedPalette
                    palette.forEach { hex ->
                        Box(
                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(parseColor(hex))
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
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_lock_palette), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(40.dp))
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
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.fillMaxHeight().width(140.dp).background(item.colorHex?.let { parseColor(it) } ?: Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl != null) {
                    AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                }
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = item.category.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(text = item.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Text(text = item.brand ?: stringResource(R.string.applications_kocolor_features_analyzer_simulator_bespoke), style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.6f))
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
