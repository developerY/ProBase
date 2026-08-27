package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FashionistaScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val strokeWidth = 6.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                val gradientBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFE89BA4), // Soft Pastel Rose / Pink
                        Color(0xFFC7BBA5), // Warm Neutral Khaki
                        Color(0xFF839C78), // Muted Sage Green
                        Color(0xFF2F6364), // Deep Teal
                        Color(0xFFE89BA4)  // Seamless loop
                    )
                )

                // Background subtle track
                drawCircle(
                    color = Color.Black.copy(alpha = 0.05f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Sweep Arc based on score percentage
                val sweepAngle = (score.coerceIn(0, 100) / 100f) * 360f
                drawArc(
                    brush = gradientBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.Black
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "KOCOLOR FASHIONISTA SCORE",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FashionistaScoreGaugePreview() {
    FashionistaScoreGauge(score = 87)
}
