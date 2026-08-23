package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.kocolor.features.analyzer.R
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

    if (showFindings && uiState.userPortraitUri != null) {
        AlertDialog(
            onDismissRequest = { showFindings = false },
            title = { Text("ML Face Detection Findings", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.fashionProfileLabel != null) {
                        Text("Established Season: ${uiState.fashionProfileLabel}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Your aesthetic identity is being used to ground the AI's stylistic decisions and palette generation.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                        Text("Analyzing aesthetic DNA...", modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFindings = false }) {
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                onClick = { if (uiState.userPortraitUri != null) showFindings = true }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxSize(),
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

@Preview(showBackground = true)
@Composable
fun MessagingStepPreview() {
    MessagingStep(
        uiState = StyleSimulatorUiState(),
        onEvent = {},
        navTo = {}
    )
}
