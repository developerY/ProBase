package com.zoewave.probase.kocolor.mobile.features.health

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.health.core.ui.HealthEvent
import com.zoewave.probase.features.health.core.ui.HealthUiState
import java.time.LocalDate

@Composable
fun StyleHealthDashboard(
    state: HealthUiState.Success,
    onEvent: (HealthEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now().toString()
    val hydration = state.weeklyHydration[today] ?: 0.0
    val hydrationGoal = 2.0
    val lastSleep = state.sleepSessions.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Text(
            text = "Style Foundation",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )

        // 1. Premium Hydration Visualization
        HydrationVisual(
            current = hydration,
            goal = hydrationGoal,
            onAdd = { onEvent(HealthEvent.LogHydration(it)) }
        )

        // 2. Beautiful Sleep Section
        SleepVisual(sleepData = lastSleep)
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun HydrationVisual(
    current: Double,
    goal: Double,
    onAdd: (Double) -> Unit
) {
    val progress = (current / goal).coerceIn(0.0, 1.0).toFloat()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                // Background Circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF2196F3).copy(alpha = 0.1f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Color(0xFF64B5F6), Color(0xFF2196F3), Color(0xFF64B5F6))),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WaterDrop, null, tint = Color(0xFF2196F3), modifier = Modifier.size(32.dp))
                    Text(
                        text = "%.1fL".format(current),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        text = "of %.1fL goal".format(goal),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(0.25 to "Glass", 0.5 to "Bottle").forEach { (vol, label) ->
                    OutlinedButton(
                        onClick = { onAdd(vol) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+${(vol*1000).toInt()}ml $label")
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepVisual(sleepData: com.zoewave.probase.core.model.health.SleepSessionData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0).copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFF9C27B0).copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Bedtime, null, tint = Color(0xFF9C27B0))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Restorative Rest", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Last night's foundation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (sleepData != null) {
                val hours = sleepData.duration?.toHours() ?: 0
                val minutes = sleepData.duration?.toMinutesPart() ?: 0
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = hours.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                    Text(text = "h", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = minutes.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                    Text(text = "m", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { (hours + minutes/60f) / 8f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = Color(0xFF9C27B0),
                    trackColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
                )
            } else {
                Text("No data synced for last night.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
