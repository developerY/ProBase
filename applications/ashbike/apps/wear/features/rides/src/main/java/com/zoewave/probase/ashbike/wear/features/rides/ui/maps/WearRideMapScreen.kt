package com.zoewave.probase.ashbike.wear.features.rides.ui.maps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.zoewave.ashbike.model.bike.LocationPoint
import kotlin.math.max

@Composable
fun WearRideMapScreen(
    modifier: Modifier = Modifier,
    locations: List<LocationPoint>,
    addressLabel: String = "49 St, New York, NY 10019",
) {
    // Theme Colors based on your image
    val mapBackground = Color(0xFFC5E1A5) // Light Green
    val gridColor = Color.White.copy(alpha = 0.3f)
    val slowColor = Color(0xFFD32F2F) // Red
    val fastColor = Color(0xFFFFD54F) // Yellow

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mapBackground)
    ) {
        // ==========================================
        // 1. The Canvas (Grid and Route)
        // ==========================================
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw Grid Lines
            val gridSize = 40.dp.toPx()
            var xPos = 0f
            while (xPos < size.width) {
                drawLine(gridColor, Offset(xPos, 0f), Offset(xPos, size.height), 1f)
                xPos += gridSize
            }
            var yPos = 0f
            while (yPos < size.height) {
                drawLine(gridColor, Offset(0f, yPos), Offset(size.width, yPos), 1f)
                yPos += gridSize
            }

            if (locations.size < 2) return@Canvas

            // Calculate bounding box to scale points to the screen
            val minLat = locations.minOf { it.latitude }
            val maxLat = locations.maxOf { it.latitude }
            val minLng = locations.minOf { it.longitude }
            val maxLng = locations.maxOf { it.longitude }

            val latRange = max(maxLat - minLat, 0.0001)
            val lngRange = max(maxLng - minLng, 0.0001)

            // Add padding so the route doesn't touch the absolute edge of the watch face
            val padding = 40.dp.toPx()
            val drawableWidth = size.width - (padding * 2)
            val drawableHeight = size.height - (padding * 2)

            // Helper to map lat/lng to canvas X/Y
            fun getScreenCoords(point: LocationPoint): Offset {
                val x = padding + ((point.longitude - minLng) / lngRange) * drawableWidth
                // Y is inverted because Canvas 0,0 is top-left, but North (maxLat) is up
                val y = padding + (1f - ((point.latitude - minLat) / latRange)) * drawableHeight
                return Offset(x.toFloat(), y.toFloat())
            }

            // Draw line segments color-coded by speed
            val strokeWidth = 4.dp.toPx()

            for (i in 0 until locations.size - 1) {
                val p1 = locations[i]
                val p2 = locations[i + 1]

                val startOffset = getScreenCoords(p1)
                val endOffset = getScreenCoords(p2)

                // Simple mock speed calculation (in a real app, use Location distanceTo / time)
                // Here, we fake a ratio between 0f (slow) and 1f (fast) for the visual
                val speedRatio = (i % 5) / 4f
                val segmentColor = lerp(slowColor, fastColor, speedRatio)

                drawLine(
                    color = segmentColor,
                    start = startOffset,
                    end = endOffset,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // Draw Start Marker (Red Square)
            val startCoords = getScreenCoords(locations.first())
            drawRect(
                color = slowColor,
                topLeft = Offset(startCoords.x - 6f, startCoords.y - 6f),
                size = androidx.compose.ui.geometry.Size(12f, 12f)
            )

            // Draw End Marker (Yellow Arrowhead)
            val endCoords = getScreenCoords(locations.last())
            drawCircle(
                color = fastColor,
                radius = 8f,
                center = endCoords
            )
        }

        // ==========================================
        // 2. Overlays (UI Elements)
        // ==========================================

        // Address Label (Top Center)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = addressLabel,
                style = MaterialTheme.typography.caption2,
                color = Color.Black
            )
        }

        // Coffee POI Icon (Top Left)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 32.dp, start = 16.dp)
                .size(36.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalCafe,
                contentDescription = "Coffee Stop",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // North Arrow (Top Right)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 36.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("N", style = MaterialTheme.typography.caption3, color = Color.Black)
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "North",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }

        // Scale Line (Bottom Left)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 32.dp, start = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(40.dp).height(2.dp).background(Color.Black))
            Spacer(modifier = Modifier.width(4.dp))
            Text("100 m", style = MaterialTheme.typography.caption3, color = Color.Black)
        }

        // Speed Legend (Bottom Right)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Slow", style = MaterialTheme.typography.caption3, color = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(listOf(slowColor, fastColor))
                    )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Fast", style = MaterialTheme.typography.caption3, color = Color.Black)
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    name = "Wear Polyline Map"
)
@Composable
fun WearRideMapScreenPreview() {
    // Mock coordinates that form a zig-zag route similar to your image
    val baseTime = System.currentTimeMillis()
    val mockRoute = listOf(
        LocationPoint(40.7600, -73.9800, null, baseTime),
        LocationPoint(40.7620, -73.9780, null, baseTime + 1000),
        LocationPoint(40.7610, -73.9740, null, baseTime + 2000),
        LocationPoint(40.7630, -73.9720, null, baseTime + 3000),
        LocationPoint(40.7620, -73.9680, null, baseTime + 4000),
        LocationPoint(40.7640, -73.9660, null, baseTime + 5000)
    )

    MaterialTheme {
        // A Box to clip the map to a circle just in case it runs on a square emulator
        Box(modifier = Modifier.fillMaxSize().clip(CircleShape)) {
            WearRideMapScreen(locations = mockRoute)
        }
    }
}