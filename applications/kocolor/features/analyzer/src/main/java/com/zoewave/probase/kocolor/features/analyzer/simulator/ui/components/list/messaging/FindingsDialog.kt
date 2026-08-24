package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState

@Composable
fun FindingsDialog(
    uiState: StyleSimulatorUiState,
    onDismiss: () -> Unit
) {
    if (uiState.userPortraitUri == null) return

    var telemetryExpanded by remember { mutableStateOf(false) }
    var outputExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    CollapsibleSection(
                        title = "ANALYSIS TELEMETRY",
                        isExpanded = telemetryExpanded,
                        onToggle = { telemetryExpanded = !telemetryExpanded }
                    ) {
                        Column(
                            modifier = Modifier.padding(bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TechnicalLine("Format: RGBA_8888 (Native Bitmap mapping, bypassing YUV-to-RGB conversion)")
                            TechnicalLine("Engine: com.google.mlkit:face-detection (LANDMARK_MODE_ALL)")
                            TechnicalLine("Vectors: Skin (Cheek sampling), Iris (Eye bounding coords), Hair (Forehead bounding projection)")
                        }
                    }

                    // Collapsible Output
                    CollapsibleSection(
                        title = "OUTPUT ANALYSIS",
                        isExpanded = outputExpanded,
                        onToggle = { outputExpanded = !outputExpanded }
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
                                    AestheticLine("Temperature: ${getTemperatureProfile(telemetry.undertoneScore)}")
                                    AestheticLine("Contrast: ${getContrastProfile(telemetry.contrastDelta)}")
                                    AestheticLine("Depth: ${getDepthProfile(telemetry.hairLuminance, telemetry.eyeLuminance)}")
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // --- THE VISUALIZATION (The "Why") ---
                                SeasonalQuadrantMap(
                                    undertoneScore = telemetry.undertoneScore,
                                    hairLuminance = telemetry.hairLuminance,
                                    eyeLuminance = telemetry.eyeLuminance
                                )

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
                                    TechnicalLine("Skin Luminance: ${"%.4f".format(telemetry.skinLuminance)}")
                                    TechnicalLine("Eye Luminance: ${"%.4f".format(telemetry.eyeLuminance)}")
                                    TechnicalLine("Hair Luminance: ${"%.4f".format(telemetry.hairLuminance)}")
                                    TechnicalLine("Contrast Delta: ${"%.4f".format(telemetry.contrastDelta)}")
                                    TechnicalLine("Undertone Score: ${"%.4f".format(telemetry.undertoneScore)}")
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
            TextButton(onClick = onDismiss) {
                Text("CLOSE", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

@Composable
private fun CollapsibleSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title, 
            style = MaterialTheme.typography.labelSmall, 
            fontWeight = FontWeight.Bold, 
            color = Color.DarkGray,
            letterSpacing = 1.sp
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
    
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        content()
    }
}

@Composable
private fun TechnicalLine(text: String) {
    Text(
        text = "• $text", 
        style = MaterialTheme.typography.bodySmall, 
        color = Color.Gray
    )
}

@Composable
private fun AestheticLine(text: String) {
    Text(
        text = "• $text", 
        style = MaterialTheme.typography.bodySmall, 
        color = Color.DarkGray
    )
}
