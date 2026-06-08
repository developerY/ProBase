package com.zoewave.probase.features.health.hydration.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HydrationGlassCard(
    current: Double,
    goal: Double,
    onAdd: (Double) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (current / goal).coerceIn(0.0, 1.0).toFloat()
    var showCustomSlider by remember { mutableStateOf(false) }
    var customAmount by remember { mutableStateOf(250f) }
    
    val glassShape = remember {
        GenericShape { size, _ ->
            val taper = size.width * 0.12f
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - taper, size.height)
            lineTo(taper, size.height)
            close()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (showCustomSlider) 620.dp else 520.dp),
        shape = glassShape, 
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp, 
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.2f)))
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            WavyLiquidEngine(progress = progress)
            
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Text(
                    text = "Hydration", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2C2420).copy(alpha = 0.7f)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1fL".format(current),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    Surface(
                        onClick = onNavigateToSettings,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "of %.1fL goal".format(goal),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                letterSpacing = 1.sp,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickHydrationButton(
                            label = "+250ml",
                            subLabel = "Glass",
                            icon = Icons.Rounded.LocalCafe,
                            modifier = Modifier.weight(1f),
                            onClick = { onAdd(0.25) }
                        )
                        QuickHydrationButton(
                            label = "+500ml",
                            subLabel = "Bottle",
                            icon = Icons.Rounded.WineBar,
                            modifier = Modifier.weight(1f),
                            onClick = { onAdd(0.5) }
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .clickable { showCustomSlider = !showCustomSlider },
                            color = Color.White.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (showCustomSlider) Icons.Default.KeyboardArrowUp else Icons.Rounded.EditNote, 
                                    null, 
                                    modifier = Modifier.size(20.dp), 
                                    tint = Color.Black.copy(alpha = 0.6f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (showCustomSlider) "Cancel" else "+ Custom Amount", 
                                    fontWeight = FontWeight.Bold, 
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showCustomSlider,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${customAmount.toInt()} ml",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Serif
                                )
                                Slider(
                                    value = customAmount,
                                    onValueChange = { customAmount = it },
                                    valueRange = 50f..1000f,
                                    steps = 19,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { 
                                        onAdd(customAmount.toDouble() / 1000.0)
                                        showCustomSlider = false 
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                ) {
                                    Text("Log Amount", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickHydrationButton(
    label: String,
    subLabel: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(40.dp))
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.Black.copy(alpha = 0.6f))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text(subLabel.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.Gray, letterSpacing = 1.sp)
            }
        }
    }
}
