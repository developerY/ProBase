package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulatorEvent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulationStep
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.ResultTab
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
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
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(24.dp), tint = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.userPortraitUri != null) "Visual Identity Active" else "No Portrait Detected",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
                            Icon(Icons.Default.PhotoCamera, null, tint = Color.DarkGray.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { onEvent(SimulatorEvent.PickPortrait) }) {
                            Icon(Icons.Default.Image, null, tint = Color.Black.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Clothing Anchors
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
                onToggle = { onEvent(SimulatorEvent.ToggleClothingFamily(uiState.selectedClothingCategory, it)) },
                emptyMessage = "No clothes in this category. Tap + in Collection to add pieces."
            )
        }

        // Makeup Anchors
        item {
            AnchorSection(
                title = stringResource(R.string.applications_kocolor_features_analyzer_simulator_makeup_anchors),
                categories = listOf(
                    Triple("Eyes", Icons.Default.Visibility, MacroCategory.EYES),
                    Triple("Cheeks", Icons.Default.FaceRetouchingNatural, MacroCategory.DIMENSION),
                    Triple("Lips", Icons.Default.Face, MacroCategory.LIPS),
                    Triple("Nails", Icons.Default.PanTool, MacroCategory.NAILS)
                ),
                selectedCategory = uiState.selectedCosmeticCategory,
                onCategorySelect = { onEvent(SimulatorEvent.SelectCosmeticCategory(it as MacroCategory)) },
                families = uiState.cosmeticFamilies,
                anchoredFamily = uiState.anchoredCosmeticFamilies[uiState.selectedCosmeticCategory],
                onToggle = { onEvent(SimulatorEvent.ToggleCosmeticFamily(uiState.selectedCosmeticCategory, it)) },
                emptyMessage = "No makeup in this category. Tap + in Collection to add products."
            )
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
private fun <T> AnchorSection(
    title: String,
    categories: List<Triple<String, ImageVector, Any>>,
    selectedCategory: Any,
    onCategorySelect: (Any) -> Unit,
    families: Map<ColorFamily, List<T>>,
    anchoredFamily: ColorFamily?,
    onToggle: (ColorFamily) -> Unit,
    emptyMessage: String
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

                if (families.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                } else {
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
            .clickable { onClick() }
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(16.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.Black
                )
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
            textAlign = TextAlign.Center
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
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Header & Rationale
        item {
            Column {
                Text(
                    text = stringResource(R.string.applications_kocolor_features_analyzer_simulator_blueprint),
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isLocalResult) Color.Gray else Color.Green)
                    )
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
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // 2. The Visual Blueprint (Side-by-Side)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(32.dp))
            ) {
                // Left Column: KoColor Sidebar
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .fillMaxHeight()
                        .padding(vertical = 20.dp, horizontal = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "KoColor",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            ),
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        uiState.recommendedPalette.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(parseColor(hex))
                                    .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                            )
                        }
                    }

                    ResultTabToggle(
                        selectedTab = uiState.selectedResultTab,
                        onTabSelected = { onEvent(SimulatorEvent.SelectResultTab(it)) }
                    )
                }

                // Right Column: Blueprint View
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (uiState.selectedResultTab) {
                        ResultTab.FACE -> FaceBlueprintView(uiState)
                        ResultTab.CLOTHES -> ClothingBlueprintView(uiState)
                        ResultTab.NAILS -> HandBlueprintView(uiState)
                    }
                }
            }
        }

        // 3. The Atelier List
        item {
            val label = when (uiState.selectedResultTab) {
                ResultTab.FACE -> "COSMETIC ATELIER"
                ResultTab.CLOTHES -> "CLOTHING ATELIER"
                ResultTab.NAILS -> "NAIL ATELIER"
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                fontWeight = FontWeight.Black,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }

        when (uiState.selectedResultTab) {
            ResultTab.CLOTHES -> {
                if (uiState.recommendedClothing.isEmpty()) {
                    items(3) { i ->
                        PlaceholderResultCard(label = when (i) {
                            0 -> "Top"
                            1 -> "Bottom"
                            else -> "Shoes"
                        })
                    }
                } else {
                    items(uiState.recommendedClothing) { item ->
                        ResultCard(clothingItem = item, onEvent = {}, navTo = navTo)
                    }
                }
            }
            ResultTab.NAILS -> {
                val nailItems = uiState.recommendedCosmetics.filter { it.macroCategory == MacroCategory.NAILS }
                if (nailItems.isEmpty()) {
                    item { PlaceholderResultCard(label = "Nails") }
                } else {
                    items(nailItems) { item ->
                        ResultCard(cosmeticItem = item, onEvent = {}, navTo = navTo)
                    }
                }
            }
            else -> {
                val nonNailCosmetics = uiState.recommendedCosmetics.filter { it.macroCategory != MacroCategory.NAILS }
                if (nonNailCosmetics.isEmpty()) {
                    items(3) { i ->
                        PlaceholderResultCard(label = when (i) {
                            0 -> "Eyes"
                            1 -> "Cheeks"
                            else -> "Lips"
                        })
                    }
                } else {
                    items(nonNailCosmetics) { item ->
                        ResultCard(cosmeticItem = item, onEvent = {}, navTo = navTo)
                    }
                }
            }
        }

        item {
            Button(
                onClick = { onEvent(SimulatorEvent.SaveToPalette) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    stringResource(R.string.applications_kocolor_features_analyzer_simulator_lock_palette),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ResultTabToggle(
    selectedTab: ResultTab,
    onTabSelected: (ResultTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(52.dp)
            .height(180.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ResultTabItem(
                icon = Icons.Default.Face,
                isSelected = selectedTab == ResultTab.FACE,
                onClick = { onTabSelected(ResultTab.FACE) }
            )
            ResultTabItem(
                icon = Icons.Default.Checkroom,
                isSelected = selectedTab == ResultTab.CLOTHES,
                onClick = { onTabSelected(ResultTab.CLOTHES) }
            )
            ResultTabItem(
                icon = Icons.Default.PanTool,
                isSelected = selectedTab == ResultTab.NAILS,
                onClick = { onTabSelected(ResultTab.NAILS) }
            )
        }
    }
}

@Composable
private fun ResultTabItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color.Black else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.Black,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ClothingBlueprintView(uiState: StyleSimulatorUiState) {
    val blueprintOffset = (-30).dp
    val horizontalShift = 0.dp // Perfectly centered for maximum reach
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Central Silhouette Anchor (Maximum Scale)
        Box(
            modifier = Modifier
                .width(420.dp)
                .fillMaxHeight()
                .offset(x = horizontalShift, y = blueprintOffset)
                .clip(RoundedCornerShape(32.dp))
                .alpha(0.35f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.applications_kocolor_feartues_analyzer_body),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Callout Lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2 + horizontalShift.toPx(), size.height / 2 + blueprintOffset.toPx())
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            // TOP: Pointing from the Shoulder
            drawLine(
                color = Color.LightGray,
                start = Offset(center.x + 10.dp.toPx(), center.y - 120.dp.toPx()),
                end = Offset(center.x + 120.dp.toPx(), center.y - 140.dp.toPx()),
                pathEffect = dashEffect, strokeWidth = 1.2.dp.toPx()
            )

            // BOTTOM: Pointing from the Waist
            drawLine(
                color = Color.LightGray,
                start = Offset(center.x + 10.dp.toPx(), center.y + 20.dp.toPx()),
                end = Offset(center.x + 120.dp.toPx(), center.y + 60.dp.toPx()),
                pathEffect = dashEffect, strokeWidth = 1.2.dp.toPx()
            )

            // SHOES: Pointing from the Feet
            drawLine(
                color = Color.LightGray,
                start = Offset(center.x + 10.dp.toPx(), center.y + 180.dp.toPx()),
                end = Offset(center.x + 120.dp.toPx(), center.y + 200.dp.toPx()),
                pathEffect = dashEffect, strokeWidth = 1.2.dp.toPx()
            )
        }

        val topItem = uiState.recommendedClothing.find { it.category == ClothingCategory.TOPS }
        val bottomItem = uiState.recommendedClothing.find { it.category == ClothingCategory.BOTTOMS }
        val shoeItem = uiState.recommendedClothing.find { it.category == ClothingCategory.SHOES }

        BlueprintCallout(
            label = "TOP",
            productName = topItem?.name ?: "Pending...",
            colorHex = topItem?.colorHex,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 10.dp).offset(y = blueprintOffset)
        )

        BlueprintCallout(
            label = "BOTTOM",
            productName = bottomItem?.name ?: "Pending...",
            colorHex = bottomItem?.colorHex,
            modifier = Modifier.align(Alignment.CenterEnd).padding(top = 80.dp).offset(y = blueprintOffset)
        )

        BlueprintCallout(
            label = "SHOES",
            productName = shoeItem?.name ?: "Pending...",
            colorHex = shoeItem?.colorHex,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 10.dp).offset(y = blueprintOffset)
        )
    }
}

@Composable
fun FaceBlueprintView(uiState: StyleSimulatorUiState) {
    val blueprintOffset = (-30).dp
    val horizontalShift = 30.dp
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Central Face Anchor (Always use Line-Art for Blueprint feel)
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = horizontalShift, y = blueprintOffset)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.applications_kocolor_features_analyzer_face),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.4f),
                contentScale = ContentScale.Fit
            )
        }

        // Callout Lines & Shades
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2 + horizontalShift.toPx(), size.height / 2 + blueprintOffset.toPx())
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            // 1. Draw "Shades" (Soft Glows on the face)
            val eyesItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.EYES }
            val cheeksItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
            val lipsItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.LIPS }

            // Eyes Shade
            eyesItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(center.x - 40.dp.toPx(), center.y - 55.dp.toPx()),
                        radius = 25.dp.toPx()
                    ),
                    radius = 25.dp.toPx(),
                    center = Offset(center.x - 40.dp.toPx(), center.y - 55.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(center.x + 40.dp.toPx(), center.y - 55.dp.toPx()),
                        radius = 25.dp.toPx()
                    ),
                    radius = 25.dp.toPx(),
                    center = Offset(center.x + 40.dp.toPx(), center.y - 55.dp.toPx())
                )
            }

            // Cheeks Shade
            cheeksItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(center.x - 55.dp.toPx(), center.y + 35.dp.toPx()),
                        radius = 45.dp.toPx()
                    ),
                    radius = 45.dp.toPx(),
                    center = Offset(center.x - 55.dp.toPx(), center.y + 35.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(center.x + 55.dp.toPx(), center.y + 35.dp.toPx()),
                        radius = 45.dp.toPx()
                    ),
                    radius = 45.dp.toPx(),
                    center = Offset(center.x + 55.dp.toPx(), center.y + 35.dp.toPx())
                )
            }

            // Lips Shade
            lipsItem?.colorHex?.let { hex ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(parseColor(hex).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(center.x, center.y + 85.dp.toPx()),
                        radius = 35.dp.toPx()
                    ),
                    radius = 35.dp.toPx(),
                    center = Offset(center.x, center.y + 85.dp.toPx())
                )
            }

            // 2. Draw Callout Lines
            // Eyes (Top Left)
            drawLine(
                color = Color.LightGray,
                start = Offset(center.x - 40.dp.toPx(), center.y - 55.dp.toPx()),
                end = Offset(center.x - 140.dp.toPx(), center.y - 140.dp.toPx()),
                pathEffect = dashEffect, strokeWidth = 1.dp.toPx()
            )

            // Cheeks (Mid Left - Lowered)
            drawLine(
                color = Color.LightGray,
                start = Offset(center.x - 55.dp.toPx(), center.y + 35.dp.toPx()),
                end = Offset(center.x - 160.dp.toPx(), center.y + 120.dp.toPx()),
                pathEffect = dashEffect, strokeWidth = 1.dp.toPx()
            )

            // Lips (Bottom Right)
            drawLine(
                color = Color.LightGray,
                start = Offset(center.x, center.y + 85.dp.toPx()),
                end = Offset(center.x + 120.dp.toPx(), center.y + 180.dp.toPx()),
                pathEffect = dashEffect, strokeWidth = 1.dp.toPx()
            )
        }

        val eyesItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.EYES }
        val cheeksItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.DIMENSION }
        val lipsItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.LIPS }

        BlueprintCallout(
            label = "EYES",
            productName = eyesItem?.name ?: "Pending...",
            colorHex = eyesItem?.colorHex,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 10.dp, start = 5.dp).offset(y = blueprintOffset)
        )

        BlueprintCallout(
            label = "CHEEKS",
            productName = cheeksItem?.name ?: "Pending...",
            colorHex = cheeksItem?.colorHex,
            modifier = Modifier.align(Alignment.CenterStart).padding(top = 280.dp, start = 5.dp).offset(y = blueprintOffset)
        )

        BlueprintCallout(
            label = "LIPS",
            productName = lipsItem?.name ?: "Pending...",
            colorHex = lipsItem?.colorHex,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 0.dp, end = 5.dp).offset(y = blueprintOffset - 10.dp)
        )
    }
}

@Composable
fun HandBlueprintView(uiState: StyleSimulatorUiState) {
    val blueprintOffset = (-30).dp
    val horizontalShift = 30.dp
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Central Hand Anchor (Always use Line-Art for Blueprint feel)
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = horizontalShift, y = blueprintOffset)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.applications_kocolor_features_analyzer_hand),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.4f),
                contentScale = ContentScale.Fit
            )
        }

        // Callout Lines & Shades
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2 + horizontalShift.toPx(), size.height / 2 + blueprintOffset.toPx())
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            // Look in both lists for the "Nail" anchor (Allowing AI creativity)
            val nailsCosmetic = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.NAILS }
            val nailsClothing = uiState.recommendedClothing.find { it.category == ClothingCategory.ACCESSORIES && it.name.contains("nail", ignoreCase = true) }
            
            val nailsHex = nailsCosmetic?.colorHex ?: nailsClothing?.colorHex

            // Nails Shade
            nailsHex?.let { hex ->
                val pigment = parseColor(hex).copy(alpha = 0.5f)
                drawCircle(pigment, radius = 6.dp.toPx(), center = Offset(center.x - 85.dp.toPx(), center.y - 48.dp.toPx())) 
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x - 48.dp.toPx(), center.y - 88.dp.toPx())) 
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x + 5.dp.toPx(), center.y - 105.dp.toPx()))  
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x + 60.dp.toPx(), center.y - 80.dp.toPx())) 
                drawCircle(pigment, radius = 7.dp.toPx(), center = Offset(center.x + 95.dp.toPx(), center.y + 12.dp.toPx())) 
            }

            // Elegant Curved Callout Line
            val start = Offset(center.x + 60.dp.toPx(), center.y - 80.dp.toPx())
            val end = Offset(center.x + 140.dp.toPx(), center.y - 160.dp.toPx())
            
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(start.x, start.y)
                quadraticTo(
                    center.x + 120.dp.toPx(), center.y - 80.dp.toPx(),
                    end.x, end.y
                )
            }
            
            drawPath(
                path = path,
                color = Color.LightGray,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            )
        }

        val nailsItem = uiState.recommendedCosmetics.find { it.macroCategory == MacroCategory.NAILS }

        BlueprintCallout(
            label = "NAILS",
            productName = nailsItem?.name ?: "Pending...",
            colorHex = nailsItem?.colorHex,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 10.dp).offset(y = (-60).dp)
        )
    }
}

@Composable
fun BlueprintCallout(
    label: String,
    productName: String,
    colorHex: String?,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .width(if (isExpanded) 140.dp else 100.dp)
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    fontWeight = FontWeight.Light,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )

                if (isExpanded) {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                        fontFamily = FontFamily.Serif,
                        maxLines = 2,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                } else {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.Gray,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Color Indicator (The "Pin")
        if (colorHex != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(parseColor(colorHex))
                    .border(1.5.dp, Color.White, CircleShape)
                    .shadow(3.dp, CircleShape)
            )
        }
    }
}

@Composable
fun ResultCard(
    clothingItem: ClothingItem? = null,
    cosmeticItem: CosmeticItem? = null,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val name = clothingItem?.name ?: cosmeticItem?.name ?: ""
    val brand = clothingItem?.brand ?: cosmeticItem?.brand ?: stringResource(R.string.applications_kocolor_features_analyzer_simulator_bespoke)
    val category = clothingItem?.category?.name ?: cosmeticItem?.microCategory?.displayName ?: ""
    val imageUrl = clothingItem?.imageUrl ?: cosmeticItem?.imageUrl
    val colorHex = clothingItem?.colorHex ?: cosmeticItem?.colorHex

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.fillMaxHeight().width(120.dp).background(colorHex?.let { parseColor(it) } ?: Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = Color.Black.copy(alpha = 0.1f), modifier = Modifier.size(32.dp))
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = category, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
                Text(text = brand, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun PlaceholderResultCard(label: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp).alpha(0.5f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.fillMaxHeight().width(120.dp).background(Color(0xFFF9F9F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory2, null, tint = Color.Black.copy(alpha = 0.05f), modifier = Modifier.size(32.dp))
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontWeight = FontWeight.Bold)
                Text(text = "Pending Selection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif, color = Color.LightGray)
                Text(text = "AI Curation in Progress", style = MaterialTheme.typography.bodySmall, color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultCardPreview() {
    MaterialTheme {
        ResultCard(
            clothingItem = ClothingItem(name = "Shirt", category = ClothingCategory.TOPS),
            onEvent = {},
            navTo = {}
        )
    }
}
