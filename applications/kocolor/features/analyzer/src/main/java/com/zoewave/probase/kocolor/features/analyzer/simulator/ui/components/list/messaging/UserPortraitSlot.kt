package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulatorEvent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState

@Composable
fun UserPortraitSlot(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    onPortraitClick: () -> Unit
) {
    var plotExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
        onClick = onPortraitClick
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
                        text = if (uiState.fashionProfileLabel != null) "Visual Identity Active" else "No Portrait Detected",
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
