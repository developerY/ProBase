package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulatorEvent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.AnchorSection
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun MessagingStep(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showFindings by remember { mutableStateOf(false) }

    if (showFindings) {
        FindingsDialog(
            uiState = uiState,
            onEvent = onEvent,
            onDismiss = { showFindings = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "KoColor",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "Boutique",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }
                

            }
        }

        item {
            Text(
                text = stringResource(R.string.applications_kocolor_features_analyzer_simulator_intent_title),
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                lineHeight = 48.sp,
                color = Color.Black
            )
        }

        item {
            UserPortraitSlot(
                uiState = uiState,
                onEvent = onEvent,
                onPortraitClick = { if (uiState.userPortraitUri != null) showFindings = true }
            )
        }

        // Anchor Constraints Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.applications_kocolor_features_analyzer_anchor_constraints),
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
                        text = stringResource(R.string.applications_kocolor_features_analyzer_add_anchor_garment),
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
                    Triple(stringResource(R.string.applications_kocolor_features_analyzer_top), Icons.Default.Checkroom, ClothingCategory.TOPS),
                    Triple(stringResource(R.string.applications_kocolor_features_analyzer_bottom), Icons.Default.Layers, ClothingCategory.BOTTOMS),
                    Triple(stringResource(R.string.applications_kocolor_features_analyzer_shoes), Icons.AutoMirrored.Filled.DirectionsWalk, ClothingCategory.SHOES)
                ),
                selectedCategory = uiState.selectedClothingCategory,
                onCategorySelect = { onEvent(SimulatorEvent.SelectClothingCategory(it as ClothingCategory)) },
                families = uiState.clothingFamilies,
                anchoredFamily = uiState.anchoredClothingFamilies[uiState.selectedClothingCategory],
                onToggle = { onEvent(SimulatorEvent.ToggleClothingFamily(uiState.selectedClothingCategory, it)) },
                emptyMessage = stringResource(R.string.applications_kocolor_features_analyzer_no_clothes_in_category)
            )
        }

        // Makeup Anchors
        item {
            AnchorSection(
                title = stringResource(R.string.applications_kocolor_features_analyzer_simulator_makeup_anchors),
                categories = listOf(
                    Triple(stringResource(R.string.applications_kocolor_features_analyzer_eyes), Icons.Default.Visibility, MacroCategory.EYES),
                    Triple(stringResource(R.string.applications_kocolor_features_analyzer_cheeks), Icons.Default.FaceRetouchingNatural, MacroCategory.DIMENSION),
                    Triple(stringResource(R.string.applications_kocolor_features_analyzer_lips), Icons.Default.Face, MacroCategory.LIPS),
                    Triple(stringResource(R.string.applications_kocolor_features_analyzer_nails), Icons.Default.PanTool, MacroCategory.NAILS)
                ),
                selectedCategory = uiState.selectedCosmeticCategory,
                onCategorySelect = { onEvent(SimulatorEvent.SelectCosmeticCategory(it as MacroCategory)) },
                families = uiState.cosmeticFamilies,
                anchoredFamily = uiState.anchoredCosmeticFamilies[uiState.selectedCosmeticCategory],
                onToggle = { onEvent(SimulatorEvent.ToggleCosmeticFamily(uiState.selectedCosmeticCategory, it)) },
                emptyMessage = stringResource(R.string.applications_kocolor_features_analyzer_no_makeup_in_category)
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
                        placeholder = { Text(stringResource(R.string.applications_kocolor_features_analyzer_simulator_intent_placeholder), style = MaterialTheme.typography.labelLarge, modifier = Modifier.alpha(0.5f)) },
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
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Polished 4-Color AI Execution Context Legend (Clickable to Settings)
                    val providerName = uiState.activeAiProvider
                    // Blues, Purples, and Shades of Greens (Strictly No Red, No Yellow/Gold)
                    val (dotColor, titleString, subtitleString) = when {
                        providerName.contains("Math", ignoreCase = true) -> Triple(Color(0xFF00897B), "Offline Math", "Algorithm Mode (No AI)") // Teal/Mint Green
                        providerName.contains("Nano", ignoreCase = true) -> Triple(Color(0xFF2E7D32), "Local AI", "Gemini Nano On-Device") // Forest Green
                        providerName.contains("Firebase", ignoreCase = true) -> Triple(Color(0xFF1976D2), "Secure Cloud", "Firebase Vault Encrypted") // Ocean Blue
                        providerName.contains("BYOK", ignoreCase = true) -> Triple(Color(0xFF7E57C2), "Custom Key", "BYOK Cloud Execution") // Deep Purple
                        else -> Triple(Color(0xFF5C6BC0), "Determining...", "Initializing Engine") // Soft Indigo
                    }
                    
                    Surface(
                        onClick = { navTo(KoColorRoute.Settings(null)) },
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Nested box for a glowing dot effect
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(color = dotColor.copy(alpha = 0.2f), shape = CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(color = dotColor, shape = CircleShape)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = titleString,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = dotColor
                                        )
                                    }
                                    Text(
                                        text = subtitleString,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Circular navigation icon container
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.04f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = "Settings",
                                        modifier = Modifier.size(12.dp),
                                        tint = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { onEvent(SimulatorEvent.StartSimulation) },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E1E1E) // Slightly lighter than pure black for 3D effect depth
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(32.dp))
                            Text(
                                text = stringResource(R.string.applications_kocolor_features_analyzer_simulator_begin_action), 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold, 
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37), // Gold spark
                                modifier = Modifier.size(32.dp).padding(end = 12.dp)
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { onEvent(SimulatorEvent.GeneratePlaylist) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                    enabled = false
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(androidx.compose.ui.res.stringResource(R.string.applications_kocolor_features_analyzer_generate_7day_playlist), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessagingStepPreview() {
    MaterialTheme {
        MessagingStep(
            uiState = MessagingPreviewData.sampleUiState,
            onEvent = {},
            navTo = {}
        )
    }
}

