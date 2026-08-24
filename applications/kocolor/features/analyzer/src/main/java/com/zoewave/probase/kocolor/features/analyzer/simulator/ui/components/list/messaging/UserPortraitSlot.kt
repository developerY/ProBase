package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: [Combined Text] --- [Portrait with Glow] --- [Icons Column]
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Text Component on the Left (Visual ID + Active Label)
                Column(modifier = Modifier.weight(1.1f)) {
                    Text(
                        text = "Visual ID",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "ACTIVE:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${uiState.fashionProfileLabel?.uppercase() ?: "ANALYZING"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        //color = Color.Black.copy(alpha = 0.5f)
                    )
                }
                
                // 2. Portrait in the Center with Multi-Layered Glow
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1.2f)
                ) {
                    // Outer soft glow
                    Surface(
                        modifier = Modifier.size(117.dp),
                        shape = CircleShape,
                        color = Color(0xFF6750A4).copy(alpha = 0.04f)
                    ) {}
                    // Inner soft glow
                    Surface(
                        modifier = Modifier.size(104.dp),
                        shape = CircleShape,
                        color = Color(0xFF6750A4).copy(alpha = 0.08f)
                    ) {}
                    
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFF6750A4).copy(alpha = 0.25f), CircleShape)
                            .background(if (uiState.userPortraitUri != null) Color.Transparent else Color(0xFFF5F5F5))
                            .clickable { onPortraitClick() },
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
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(36.dp), tint = Color.LightGray)
                        }
                    }
                }

                // 3. Icons on the Right in a Column (Now Bigger)
                Column(
                    modifier = Modifier.weight(0.7f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = { onEvent(SimulatorEvent.CapturePortrait) },
                        modifier = Modifier.size(47.dp),
                        shape = CircleShape,
                        color = Color(0xFFFDFDFD),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(22.dp), tint = Color.Black.copy(alpha = 0.6f))
                        }
                    }
                    Surface(
                        onClick = { onEvent(SimulatorEvent.PickPortrait) },
                        modifier = Modifier.size(47.dp),
                        shape = CircleShape,
                        color = Color(0xFFFDFDFD),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, null, modifier = Modifier.size(22.dp), tint = Color.Black.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Seasonal Mapping Collapsible Section (Smaller Text)
            Card(
                onClick = { if (uiState.faceTelemetry != null) plotExpanded = !plotExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SEASONAL MAPPING",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = if (plotExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
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
                            .fillMaxWidth()
                            .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        SeasonalQuadrantMap(
                            season = uiState.fashionProfileLabel ?: "",
                            undertoneScore = telemetry.undertoneScore,
                            hairLuminance = telemetry.hairLuminance,
                            eyeLuminance = telemetry.eyeLuminance,
                            modifier = Modifier.height(240.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserPortraitSlotPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp).background(Color(0xFFF0F0F0))) {
            UserPortraitSlot(
                uiState = MessagingPreviewData.sampleUiState,
                onEvent = {},
                onPortraitClick = {}
            )
        }
    }
}
