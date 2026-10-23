package com.zoewave.probase.goswift.mobile.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HealthSyncUiRoute() {
    HealthSyncUiScreen()
}

@Composable
fun HealthSyncUiScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GOSWIFT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFB4AA),
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Health Sync",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFE1E2E9),
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF41474D))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFFF6F61), CircleShape)
                    )
                    Text(
                        text = "AI Active",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE1E2E9)
                    )
                }
            }
        }

        // Health Connect Active Banner Card
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(24.dp))
                    Column {
                        Text("Health Connect Active", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF8B9198), modifier = Modifier.size(12.dp))
                            Text("Encrypted On-Device Sync", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        }
                    }
                }
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242C34), contentColor = Color(0xFFE1E2E9)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Sync All", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // TFLite Security Card
        Surface(
            color = Color(0xFF161B20),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = Color(0xFFFDBA74), modifier = Modifier.size(24.dp))
                Column {
                    Text("TensorFlow Lite Edge Engine:", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFDBA74), fontWeight = FontWeight.Bold)
                    Text("Zero unencrypted biometric records egress from hardware storage.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC1C7CE))
                }
            }
        }

        // Synchronized Peripherals
        Text(
            text = "SYNCHRONIZED PERIPHERALS (3 ONLINE)",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B9198),
            fontFamily = FontFamily.Monospace
        )

        PeripheralCard(Icons.Default.Watch, "Pixel Watch 2", "Live Telemetry", true)
        PeripheralCard(Icons.Default.FitnessCenter, "Whoop 4.0", "Polled 2m ago", true)
        PeripheralCard(Icons.Default.Sensors, "Dexcom G7 CGM", "Continuous Stream", true)

        // Live Ingestion
        Text(
            text = "LIVE METABOLIC INGESTION",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B9198),
            fontFamily = FontFamily.Monospace
        )

        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF6F61), modifier = Modifier.size(24.dp))
                    Column {
                        Text("Heart Rate & HRV", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        Text("1 Hz Sampling • Autonomic Balance", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                    }
                }
                Text("68 BPM", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFFFB4AA), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun PeripheralCard(icon: ImageVector, title: String, subtitle: String, online: Boolean) {
    Surface(
        color = Color(0xFF1C2228),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242C34)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFFE1E2E9), modifier = Modifier.size(24.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                }
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (online) Color(0xFFFEF08A) else Color(0xFF8B9198), CircleShape)
            )
        }
    }
}
