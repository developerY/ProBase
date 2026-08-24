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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp), ambientColor = Color.Black.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.03f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Clustered to the Left
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Text Component (Smaller)
                Column {
                    Text(
                        text = "Visual ID",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                        fontFamily = FontFamily.Serif,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "ACTIVE:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color.Gray
                    )
                    Text(
                        text = uiState.fashionProfileLabel?.uppercase() ?: "ANALYZING",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.ExtraBold),
                        color = Color.Black
                    )
                }
                
                Spacer(Modifier.width(24.dp))
                
                // 2. Portrait Hub (Larger)
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Vibrant Glowing Halo
                    Surface(
                        modifier = Modifier.size(126.dp),
                        shape = CircleShape,
                        color = Color(0xFFB9A0FF).copy(alpha = 0.4f),
                    ) {}
                    
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.5.dp, Color(0xFFC5B0FF), CircleShape)
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
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        }
                    }
                }

                Spacer(Modifier.width(20.dp))

                // 3. Action Icons (Larger)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    NeumorphicIconButton(
                        icon = Icons.Default.PhotoCamera,
                        size = 62.dp,
                        innerSize = 52.dp,
                        iconSize = 26.dp,
                        onClick = { onEvent(SimulatorEvent.CapturePortrait) }
                    )
                    NeumorphicIconButton(
                        icon = Icons.Default.Image,
                        size = 62.dp,
                        innerSize = 52.dp,
                        iconSize = 26.dp,
                        onClick = { onEvent(SimulatorEvent.PickPortrait) }
                    )
                }
                
                // 4. Spacer to push everything to the left
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(28.dp))

            // Gold Gradient Expansion Bar (Smaller Text)
            val goldBrush = Brush.linearGradient(
                listOf(Color(0xFFD4C097), Color(0xFFF2E7D5), Color(0xFFD4C097))
            )

            Card(
                onClick = { if (uiState.faceTelemetry != null) plotExpanded = !plotExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(goldBrush),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SEASONAL MAPPING",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.Black
                        )
                        Icon(
                            imageVector = if (plotExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Color.Black
                        )
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
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
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

@Composable
private fun NeumorphicIconButton(
    icon: ImageVector,
    size: Dp = 52.dp,
    innerSize: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(innerSize)
                    .border(1.dp, Color.Black.copy(alpha = 0.02f), CircleShape)
                    .background(Color(0xFFFDFDFD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = Color.Black.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserPortraitSlotPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp).background(Color(0xFFE5E5E5))) {
            UserPortraitSlot(
                uiState = MessagingPreviewData.sampleUiState,
                onEvent = {},
                onPortraitClick = {}
            )
        }
    }
}
