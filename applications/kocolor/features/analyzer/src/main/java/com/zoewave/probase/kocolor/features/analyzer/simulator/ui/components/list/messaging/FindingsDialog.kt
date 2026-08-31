package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.ui.util.toHex
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulatorEvent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.FaceTelemetryData

@Composable
fun FindingsDialog(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit = {},
    onDismiss: () -> Unit
) {
    if (uiState.userPortraitUri == null) return

    var telemetryExpanded by remember { mutableStateOf(false) }
    var outputExpanded by remember { mutableStateOf(false) }
    var activePickerTarget by remember { mutableStateOf<String?>(null) } // "skin", "eye", "hair"

    activePickerTarget?.let { target ->
        val telemetry = uiState.faceTelemetry
        val currentHex = when (target) {
            "skin" -> telemetry?.skinColorHex ?: "#E8C8B8"
            "eye" -> telemetry?.eyeColorHex ?: "#7A8F9E"
            else -> telemetry?.hairColorHex ?: "#D8D2C5"
        }
        val initialColor = try { Color(android.graphics.Color.parseColor(currentHex)) } catch (e: Exception) { Color.Gray }

        ColorPickerDialog(
            initialColor = initialColor,
            onColorSelected = { selectedColor ->
                val hex = selectedColor.toHex()
                when (target) {
                    "skin" -> onEvent(SimulatorEvent.OnManualSkinColorSelected(hex))
                    "eye" -> onEvent(SimulatorEvent.OnManualEyeColorSelected(hex))
                    "hair" -> onEvent(SimulatorEvent.OnManualHairColorSelected(hex))
                }
                activePickerTarget = null
            },
            onDismissRequest = { activePickerTarget = null }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.applications_kocolor_features_analyzer_findings_title), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
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
                    
                    // Interactive Telemetry Visualizer (Renders portrait image whenever available)
                    uiState.userPortraitUri?.let { portraitUri ->
                        val telemetry = uiState.faceTelemetry ?: FaceTelemetryData(
                            imageWidth = 720,
                            imageHeight = 1280,
                            cheekPoint = null,
                            eyePoint = null,
                            hairBoundingBox = null,
                            faceBoundingBox = null
                        )
                        FaceTelemetryVisualizer(
                            imageUri = portraitUri,
                            telemetry = telemetry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(stringResource(R.string.applications_kocolor_features_analyzer_established_season_format, uiState.fashionProfileLabel ?: ""), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.applications_kocolor_features_analyzer_findings_desc), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    
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
                        val telemetry = uiState.faceTelemetry ?: FaceTelemetryData(
                            imageWidth = 720,
                            imageHeight = 1280,
                            cheekPoint = null,
                            eyePoint = null,
                            hairBoundingBox = null,
                            faceBoundingBox = null,
                            skinLuminance = 0.5f,
                            eyeLuminance = 0.2f,
                            hairLuminance = 0.2f,
                            contrastDelta = 0.3f,
                            undertoneScore = 0.0f
                        )
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
                                season = uiState.fashionProfileLabel,
                                undertoneScore = telemetry.undertoneScore,
                                hairLuminance = telemetry.hairLuminance,
                                eyeLuminance = telemetry.eyeLuminance
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // ML Sampled Color Swatches (Tap Swatch to Calibrate & Re-analyze Category)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SAMPLED FEATURE COLORS", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Bold, 
                                    color = Color.DarkGray,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "TAP TO CALIBRATE", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 9.sp,
                                    color = Color(0xFF7C3AED)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ColorSwatchItem(label = "Skin / Cheek", hex = telemetry.skinColorHex, onClick = { activePickerTarget = "skin" })
                                ColorSwatchItem(label = "Eye / Iris", hex = telemetry.eyeColorHex, onClick = { activePickerTarget = "eye" })
                                ColorSwatchItem(label = "Hair / Root", hex = telemetry.hairColorHex, onClick = { activePickerTarget = "hair" })
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
                                TechnicalLine("Skin Luminance: ${"%.4f".format(telemetry.skinLuminance)}")
                                TechnicalLine("Eye Luminance: ${"%.4f".format(telemetry.eyeLuminance)}")
                                TechnicalLine("Hair Luminance: ${"%.4f".format(telemetry.hairLuminance)}")
                                TechnicalLine("Contrast Delta: ${"%.4f".format(telemetry.contrastDelta)}")
                                TechnicalLine("Undertone Score: ${"%.4f".format(telemetry.undertoneScore)}")
                            }
                        }
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                    Text(stringResource(R.string.applications_kocolor_features_analyzer_analyzing_dna), modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(R.string.applications_kocolor_features_analyzer_close), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

@Preview(showBackground = true)
@Composable
private fun FindingsDialogPreview() {
    MaterialTheme {
        FindingsDialog(
            uiState = MessagingPreviewData.sampleUiState,
            onDismiss = {}
        )
    }
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

@Composable
private fun ColorSwatchItem(label: String, hex: String, onClick: () -> Unit = {}) {
    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (color.luminance() > 0.5f) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.Gray)
        Text(text = hex.uppercase(), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}
