package com.zoewave.probase.features.health.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ble.GattConnectionState

data class BioElementTrackerUiState(
    val connectionState: GattConnectionState,
    val metrics: Map<String, String>
)

@Composable
fun BioElementTrackerCard(
    uiState: BioElementTrackerUiState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ElementPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val isConnected = uiState.connectionState == GattConnectionState.Connected

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(16.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Bio-Element Background Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.6f) // Dimmed to indicate inactive
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF4E50).copy(alpha = 0.3f), // Fire
                                Color(0xFF1F1C2C).copy(alpha = 0.8f), // Midnight
                                Color(0xFF00C9FF).copy(alpha = 0.3f)  // Water
                            )
                        )
                    )
            )

            // Stylized Tracker Band Visualization
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.8f)
                    .height(60.dp)
                    .clip(CircleShape)
                    .alpha(0.5f) // Dimmed
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF232526), Color(0xFF414345))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Central Glowing Core
                val coreColor = if (isConnected) Color(0xFF00E676) else Color(0xFFFF1744)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer(scaleX = pulse, scaleY = pulse)
                        .shadow(12.dp, CircleShape, ambientColor = coreColor, spotColor = coreColor)
                        .background(coreColor, CircleShape)
                )
            }

            // Coming Soon Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "COMING SOON",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Connection Status & Metrics Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "ELEMENT TRACKER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = if (isConnected) "Synced" else "Awaiting Sync",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Icon(
                        imageVector = if (isConnected) Icons.Rounded.BluetoothConnected else Icons.Rounded.Bluetooth,
                        contentDescription = null,
                        tint = if (isConnected) Color(0xFF00C9FF) else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (isConnected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TrackerMetric(
                            label = "Fire",
                            value = uiState.metrics["Temperature"]?.substringBefore("°") ?: "--",
                            unit = "BPM",
                            icon = Icons.Default.Favorite,
                            color = Color(0xFFFF4E50)
                        )
                        TrackerMetric(
                            label = "Light",
                            value = uiState.metrics["Lux"]?.substringAfter(":")?.trim() ?: "--",
                            unit = "LX",
                            icon = Icons.Default.LightMode,
                            color = Color(0xFFFFD600)
                        )
                        TrackerMetric(
                            label = "Air",
                            value = uiState.metrics["Humidity"]?.substringAfter(":")?.substringBefore("%")?.trim() ?: "--",
                            unit = "%",
                            icon = Icons.Default.Air,
                            color = Color(0xFF00C9FF)
                        )
                    }
                } else {
                    Text(
                        text = "Tap to initiate bio-element correlation",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackerMetric(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = color)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = unit, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 2.dp))
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
    }
}

@Preview
@Composable
private fun BioElementTrackerCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp).background(Color(0xFFF9F7F2)), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BioElementTrackerCard(
                uiState = BioElementTrackerUiState(
                    connectionState = GattConnectionState.Connected,
                    metrics = mapOf("Temperature" to "72°C", "Lux" to "Lux: 450.0", "Humidity" to "Humidity: 45.0%")
                )
            )
            BioElementTrackerCard(
                uiState = BioElementTrackerUiState(
                    connectionState = GattConnectionState.Disconnected,
                    metrics = emptyMap()
                )
            )
        }
    }
}
