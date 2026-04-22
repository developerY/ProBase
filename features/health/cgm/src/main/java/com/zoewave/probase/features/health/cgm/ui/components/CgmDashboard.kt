package com.zoewave.probase.features.health.cgm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.health.GlucoseReading
import com.zoewave.probase.core.model.health.GlucoseType

@Composable
fun CgmDashboard(
    reading: GlucoseReading?,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = reading?.source?.name?.replace("_", " ") ?: "No Sensor Connected",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (reading != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = reading.valueMgDl.toInt().toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = getGlucoseColor(reading.valueMgDl)
                    )
                    Text(
                        text = "mg/dL",
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                if (reading.type == GlucoseType.CGM) {
                    Text(
                        text = "Trend: ${reading.trendArrow ?: "Stable"}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = "--",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (reading?.type == GlucoseType.BGM || reading == null) {
                Button(onClick = onScanClick) {
                    Text(if (reading == null) "Connect Sensor" else "Sync Reading")
                }
            }
        }
    }
}

private fun getGlucoseColor(value: Float): Color {
    return when {
        value < 70 -> Color.Red
        value > 180 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFF4CAF50) // Green
    }
}
