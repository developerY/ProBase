package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list

import android.graphics.PointF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.FaceTelemetryData
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulatorEvent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingStep(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showFindings by remember { mutableStateOf(false) }
    var telemetryExpanded by remember { mutableStateOf(false) }
    var outputExpanded by remember { mutableStateOf(false) }

    if (showFindings && uiState.userPortraitUri != null) {
        AlertDialog(
            onDismissRequest = { 
                showFindings = false
                telemetryExpanded = false
                outputExpanded = false
            },
            title = { Text("ML Face Detection Findings", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.faceAnalysisError != null) {
                        Text(
                            text = uiState.faceAnalysisError, 
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (uiState.fashionProfileLabel != null) {
                        
                        // Interactive Telemetry Visualizer
                        uiState.faceTelemetry?.let { telemetry ->
                            FaceTelemetryVisualizer(
                                imageUri = uiState.userPortraitUri,
                                telemetry = telemetry,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black)
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        Text("Established Season: ${uiState.fashionProfileLabel}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Your aesthetic identity is being used to ground the AI's stylistic decisions and palette generation.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp), 
                            color = Color.LightGray.copy(alpha = 0.5f), 
                            thickness = 1.dp
                        )
                        
                        // Collapsible Telemetry
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { telemetryExpanded = !telemetryExpanded }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ANALYSIS TELEMETRY", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold, 
                                color = Color.DarkGray,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = if (telemetryExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = telemetryExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "• Format: RGBA_8888 (Native Bitmap mapping, bypassing YUV-to-RGB conversion)", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color.Gray
                                )
                                Text(
                                    text = "• Engine: com.google.mlkit:face-detection (LANDMARK_MODE_ALL)", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color.Gray
                                )
                                Text(
                                    text = "• Vectors: Skin (Cheek sampling), Iris (Eye bounding coords), Hair (Forehead bounding projection)", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color.Gray
                                )
                            }
                        }

                        // Collapsible Output
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { outputExpanded = !outputExpanded }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "OUTPUT ANALYSIS", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold, 
                                color = Color.DarkGray,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = if (outputExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = outputExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            uiState.faceTelemetry?.let { telemetry ->
                                Column(
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // 1. Aesthetic Profile (The Meaning)
                                    Text(
                                        text = "AESTHETIC PROFILE", 
                                        style = MaterialTheme.typography.labelSmall, 
                                        fontWeight = FontWeight.Bold, 
                                        color = Color.DarkGray,
                                        letterSpacing = 1.sp
                                    )
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "• Temperature: ${getTemperatureProfile(telemetry.undertoneScore)}", 
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "• Contrast: ${getContrastProfile(telemetry.contrastDelta)}", 
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "• Depth: ${getDepthProfile(telemetry.hairLuminance, telemetry.eyeLuminance)}", 
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = Color.DarkGray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 2. Raw Telemetry (The Math)
                                    Text(
                                        text = "RAW TELEMETRY", 
                                        style = MaterialTheme.typography.labelSmall, 
                                        fontWeight = FontWeight.Bold, 
                                        color = Color.Gray,
                                        letterSpacing = 1.sp
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "• Skin Luminance: ${"%.4f".format(telemetry.skinLuminance)}", 
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "• Eye Luminance: ${"%.4f".format(telemetry.eyeLuminance)}", 
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "• Hair Luminance: ${"%.4f".format(telemetry.hairLuminance)}", 
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "• Contrast Delta: ${"%.4f".format(telemetry.contrastDelta)}", 
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "• Undertone Score: ${"%.4f".format(telemetry.undertoneScore)}", 
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                        Text("Analyzing aesthetic DNA...", modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showFindings = false
                    telemetryExpanded = false
                    outputExpanded = false
                }) {
                    Text("CLOSE", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // click on this
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.applications_kocolor_features_analyzer_simulator_intent_title),
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                lineHeight = 48.sp,
                color = Color.Black
            )
        }

        // User Portrait Slot (Unified Calibration & Identity)
        item {
            var plotExpanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                onClick = { if (uiState.userPortraitUri != null) showFindings = true }
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (uiState.userPortraitUri != null) Color(0xFFF0E6FF) else Color(0xFFF5F5F5)),
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
                                text = uiState.fashionProfileLabel ?: "Provide a photo to ground the AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (uiState.fashionProfileLabel != null) {
                                IconButton(onClick = { plotExpanded = !plotExpanded }) {
                                    Icon(
                                        imageVector = if (plotExpanded) Icons.Default.ExpandLess else Icons.Default.AutoAwesome, 
                                        null, 
                                        tint = Color(0xFF6750A4), 
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { onEvent(SimulatorEvent.CapturePortrait) }) {
                                Icon(Icons.Default.PhotoCamera, null, tint = Color.DarkGray.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { onEvent(SimulatorEvent.PickPortrait) }) {
                                Icon(Icons.Default.Image, null, tint = Color.Black.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = plotExpanded && uiState.faceTelemetry != null,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        uiState.faceTelemetry?.let { telemetry ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = 24.dp)
                            ) {
                                SeasonalQuadrantMap(
                                    undertoneScore = telemetry.undertoneScore,
                                    hairLuminance = telemetry.hairLuminance,
                                    eyeLuminance = telemetry.eyeLuminance,
                                    modifier = Modifier.height(200.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Anchor Constraints Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ANCHOR CONSTRAINTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // TODO: Map currently locked garments here
                    uiState.anchoredClothingFamilies.forEach { (category, family) ->
                        val item = uiState.fullClothingInventory.find { it.category == category && it.colorFamily == family }
                        if (item != null) {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        onClick = { navTo(KoColorRoute.Wardrobe) },
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, null, tint = Color.Black.copy(alpha = 0.6f))
                        }
                    }
                    
                    Text(
                        text = "Add anchor garment",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        // Clothing Anchors (Color Family Based)
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
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.userMessage,
                        onValueChange = { onEvent(SimulatorEvent.UpdateMessage(it)) },
                        placeholder = { Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_intent_placeholder), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.alpha(0.5f)) },
                        modifier = Modifier.weight(1f).height(140.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    
                    IconButton(
                        onClick = { /* Placeholder for calendar read */ },
                        modifier = Modifier.padding(end = 12.dp).align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Read Calendar",
                            tint = Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onEvent(SimulatorEvent.StartSimulation) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("Get Fashion Advice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = { onEvent(SimulatorEvent.GeneratePlaylist) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Generate 7-Day Playlist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun FaceTelemetryVisualizer(
    imageUri: String?,
    telemetry: FaceTelemetryData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRequest = remember(imageUri) {
        ImageRequest.Builder(context)
            .data(imageUri)
            .crossfade(true)
            .build()
    }

    Box(modifier = modifier.fillMaxWidth().aspectRatio(4f/3f)) { 
        AsyncImage(
            model = imageRequest,
            contentDescription = "Analyzed Portrait",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Canvas(modifier = Modifier.matchParentSize()) {
            val imageW = telemetry.imageWidth.toFloat()
            val imageH = telemetry.imageHeight.toFloat()

            // 1. Calculate the exact scale factor used by ContentScale.Crop
            val scale = maxOf(size.width / imageW, size.height / imageH)

            // 2. Calculate the offset to center the cropped image
            val offsetX = (size.width - (imageW * scale)) / 2f
            val offsetY = (size.height - (imageH * scale)) / 2f

            // 3. Apply both scale and translation to the ML Kit coordinates
            fun scalePoint(p: PointF): Offset {
                return Offset(
                    x = (p.x * scale) + offsetX,
                    y = (p.y * scale) + offsetY
                )
            }

            // Hair Bounding Box
            telemetry.hairBoundingBox?.let { rect ->
                drawRect(
                    color = Color.Yellow.copy(alpha = 0.6f),
                    topLeft = Offset((rect.left * scale) + offsetX, (rect.top * scale) + offsetY),
                    size = Size(rect.width() * scale, rect.height() * scale),
                    style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
            }

            // Face Bounding Box
            telemetry.faceBoundingBox?.let { rect ->
                drawRoundRect(
                    color = Color.Cyan.copy(alpha = 0.4f),
                    topLeft = Offset((rect.left * scale) + offsetX, (rect.top * scale) + offsetY),
                    size = Size(rect.width() * scale, rect.height() * scale),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
                )
            }

            // Cheek Node
            telemetry.cheekPoint?.let { point ->
                drawCircle(
                    color = Color.Cyan,
                    radius = 12f,
                    center = scalePoint(point),
                    style = Stroke(width = 6f)
                )
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.3f),
                    radius = 24f,
                    center = scalePoint(point)
                )
            }

            // Eye Node
            telemetry.eyePoint?.let { point ->
                drawCircle(
                    color = Color.Magenta,
                    radius = 12f,
                    center = scalePoint(point),
                    style = Stroke(width = 6f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessagingStepPreview() {
    MessagingStep(
        uiState = StyleSimulatorUiState(),
        onEvent = {},
        navTo = {}
    )
}

@Composable
fun SeasonalQuadrantMap(
    undertoneScore: Float, // X-Axis: Cool (-1.0) to Warm (1.0)
    hairLuminance: Float, 
    eyeLuminance: Float,   
    modifier: Modifier = Modifier
) {
    // Calculate Depth for Y-Axis: 0.0 (Dark) to 1.0 (Light)
    val depthScore = ((hairLuminance + eyeLuminance) / 2f).coerceIn(0f, 1f)
    
    // Normalize undertone to 0.0 - 1.0 for the Canvas (assuming -1.0 to 1.0 range)
    val normalizedUndertone = ((undertoneScore + 1f) / 2f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .padding(vertical = 16.dp)
    ) {
        // 1. The Labels
        val labelStyle = MaterialTheme.typography.labelSmall.copy(
            color = Color.Gray.copy(alpha = 0.7f),
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text("SUMMER\n(Cool/Light)", style = labelStyle, modifier = Modifier.align(Alignment.TopStart))
        Text("SPRING\n(Warm/Light)", style = labelStyle, textAlign = TextAlign.End, modifier = Modifier.align(Alignment.TopEnd))
        Text("WINTER\n(Cool/Deep)", style = labelStyle, modifier = Modifier.align(Alignment.BottomStart))
        Text("AUTUMN\n(Warm/Deep)", style = labelStyle, textAlign = TextAlign.End, modifier = Modifier.align(Alignment.BottomEnd))

        // 2. The Grid & Data Point
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, bottom = 32.dp, start = 16.dp, end = 16.dp) // Inset the grid from labels
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f

            // Draw Quadrant Crosshairs
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(centerX, 0f),
                end = Offset(centerX, canvasH),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, centerY),
                end = Offset(canvasW, centerY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Plot the User's Coordinate
            // X: Left (0.0/Cool) to Right (1.0/Warm)
            // Y: Bottom (0.0/Dark) to Top (1.0/Light) -> Invert Y so Light is Top
            val plotX = normalizedUndertone * canvasW
            val plotY = (1f - depthScore) * canvasH 
            val userPoint = Offset(plotX, plotY)

            // Glowing Indicator
            drawCircle(
                color = Color(0xFF6750A4).copy(alpha = 0.2f), // Brand purple glow
                radius = 24f,
                center = userPoint
            )
            drawCircle(
                color = Color(0xFF6750A4), // Solid center
                radius = 8f,
                center = userPoint
            )
        }
    }
}

// --- Translation Logic Helpers ---

private fun getContrastProfile(delta: Float): String = when {
    delta > 0.5f -> "High (Striking / Clear)"
    delta > 0.3f -> "Medium (Balanced)"
    else -> "Low (Blended / Muted)"
}

private fun getTemperatureProfile(score: Float): String = when {
    score > 0.05f -> "Warm (Golden / Peach base)"
    score < -0.05f -> "Cool (Pink / Blue base)"
    else -> "Neutral (Balanced / Olive base)"
}

private fun getDepthProfile(hairLuminance: Float, eyeLuminance: Float): String {
    val avgDarkness = (hairLuminance + eyeLuminance) / 2f
    return when {
        avgDarkness < 0.2f -> "Deep (Anchors dark colors well)"
        avgDarkness < 0.5f -> "Moderate (Versatile depth)"
        else -> "Light (Favors airy, pastel palettes)"
    }
}
