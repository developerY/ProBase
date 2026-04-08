package com.zoewave.probase.goswift.mobile.shots.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.zoewave.probase.goswift.model.CaffeineShot
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun CaffeineClock(
    shots: List<CaffeineShot>,
    modifier: Modifier = Modifier
) {
    val halfLifeHours = 5.0
    val now = System.currentTimeMillis()
    
    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            val strokeWidth = 8.dp.toPx()
            val clockRadius = radius - strokeWidth

            // Draw clock face circle
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = clockRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw hour marks
            for (i in 0 until 12) {
                val angle = (i * 30).toDouble() * Math.PI / 180.0
                val start = Offset(
                    (center.x + (clockRadius - 10.dp.toPx()) * sin(angle)).toFloat(),
                    (center.y - (clockRadius - 10.dp.toPx()) * cos(angle)).toFloat()
                )
                val end = Offset(
                    (center.x + clockRadius * sin(angle)).toFloat(),
                    (center.y - clockRadius * cos(angle)).toFloat()
                )
                drawLine(
                    color = Color.Gray,
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Draw caffeine arcs
            shots.forEach { shot ->
                val shotTime = Instant.ofEpochMilli(shot.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
                
                // Convert shot time to clock angle (0-360)
                // 12 o'clock is 0 degrees, but drawArc starts from 3 o'clock (90 degrees)
                // Angle = (hour * 30 + min * 0.5) - 90
                val startAngle = ((shotTime.hour % 12) * 30f + shotTime.minute * 0.5f) - 90f
                
                // We show the decay for the next 12 hours on the face
                // Concentration at time t: C = C0 * 0.5^(t/halfLife)
                // We'll draw 12 segments of 1 hour each
                for (h in 0 until 12) {
                    val concentration = (0.5).pow(h.toDouble() / halfLifeHours)
                    val sweepAngle = 30f // 1 hour segment
                    
                    if (concentration > 0.05) { // Only draw if significant amount remains
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF2196F3).copy(alpha = (concentration * 0.6f).toFloat()),
                                    Color(0xFF2196F3).copy(alpha = (concentration * 0.3f).toFloat())
                                )
                            ),
                            startAngle = startAngle + (h * 30f),
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - clockRadius, center.y - clockRadius),
                            size = Size(clockRadius * 2, clockRadius * 2),
                            style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            
            // Draw current time hand
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR)
            val minute = calendar.get(Calendar.MINUTE)
            val currentAngle = (hour * 30f + minute * 0.5f) * Math.PI.toFloat() / 180f
            
            drawLine(
                color = Color.Red,
                start = center,
                end = Offset(
                    center.x + (clockRadius * 0.8f) * sin(currentAngle),
                    center.y - (clockRadius * 0.8f) * cos(currentAngle)
                ),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
