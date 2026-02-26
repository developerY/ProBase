package com.zoewave.probase.ashbike.wear.features.rides.ui.graphs

import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.zoewave.ashbike.model.bike.LocationPoint

@Composable
fun ElevationProfileScreen(
    locations: List<LocationPoint>,
    modifier: Modifier = Modifier
) {
    // Filter out any GPS points that failed to register an altitude
    val validPoints = remember(locations) {
        locations.filter { it.altitude != null }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (validPoints.size < 2) {
            Text("Not enough elevation data", color = Color.Gray)
            return@Box
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Elevation Profile",
                style = MaterialTheme.typography.caption1,
                color = Color(0xFF4CAF50) // Material Green
            )
            Spacer(modifier = Modifier.height(8.dp))

            // The Canvas Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up remaining vertical space
            ) {
                ElevationAreaCanvas(points = validPoints)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Quick Stats
            val maxAlt = validPoints.maxOf { it.altitude!! }
            val minAlt = validPoints.minOf { it.altitude!! }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Min", style = MaterialTheme.typography.caption3, color = Color.Gray)
                    Text("${minAlt.toInt()}m", style = MaterialTheme.typography.caption2, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Max", style = MaterialTheme.typography.caption3, color = Color.Gray)
                    Text("${maxAlt.toInt()}m", style = MaterialTheme.typography.caption2, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ElevationAreaCanvas(points: List<LocationPoint>, modifier: Modifier = Modifier.fillMaxSize()) {
    val lineColor = Color(0xFF4CAF50) // Material Green
    val gradientColors = listOf(
        lineColor.copy(alpha = 0.6f), // Mostly opaque at the peak
        lineColor.copy(alpha = 0.0f)  // Fades to transparent at the bottom
    )

    Canvas(modifier = modifier) {
        val minTime = points.first().timestamp
        val maxTime = points.last().timestamp
        val timeRange = (maxTime - minTime).coerceAtLeast(1L) // Prevent divide by zero

        val minAlt = points.minOf { it.altitude!! }
        val maxAlt = points.maxOf { it.altitude!! }
        // Add a 10% buffer to the top so the highest peak doesn't touch the exact edge of the canvas
        val altRange = ((maxAlt - minAlt) * 1.1f).coerceAtLeast(1f)

        val linePath = Path()
        val fillPath = Path()

        points.forEachIndexed { index, point ->
            // 1. Calculate X based on time progression
            val timeRatio = (point.timestamp - minTime).toFloat() / timeRange
            val x = timeRatio * size.width

            // 2. Calculate Y based on altitude (Remember: 0 is the top of the canvas, size.height is the bottom)
            val altRatio = (point.altitude!! - minAlt) / altRange
            val y = size.height - (altRatio * size.height)

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, size.height) // Start the fill from the very bottom left
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Close the fill path by drawing lines to the bottom right, then back to the start
        fillPath.lineTo(size.width, size.height)
        fillPath.close()

        // 3. Draw the Area Gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = gradientColors,
                startY = 0f,
                endY = size.height
            )
        )

        // 4. Draw the Mountain Line on top
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round // Makes the peaks smooth instead of sharp/jagged
            )
        )
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Elevation Profile"
)
@Composable
fun ElevationProfileScreenPreview() {
    // Generate a dummy ride over a fake hill
    val baseTime = System.currentTimeMillis()
    val dummyPoints = listOf(
        LocationPoint(0.0, 0.0, 100f, baseTime),
        LocationPoint(0.0, 0.0, 110f, baseTime + 60000),
        LocationPoint(0.0, 0.0, 150f, baseTime + 120000),
        LocationPoint(0.0, 0.0, 280f, baseTime + 180000), // The Peak!
        LocationPoint(0.0, 0.0, 260f, baseTime + 240000),
        LocationPoint(0.0, 0.0, 190f, baseTime + 300000),
        LocationPoint(0.0, 0.0, 120f, baseTime + 360000),
        LocationPoint(0.0, 0.0, 105f, baseTime + 420000)
    )

    MaterialTheme {
        ElevationProfileScreen(locations = dummyPoints)
    }
}