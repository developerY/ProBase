package com.zoewave.probase.features.weather.ui.sun

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunIntelligenceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sun Intelligence", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F7F2),
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Large Circular UV Gauge with Glow
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                    // Soft Sun Glow
                    Canvas(modifier = Modifier.size(240.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFB74D).copy(alpha = 0.15f), Color.Transparent),
                                center = center,
                                radius = size.width / 1.5f
                            )
                        )
                    }

                    Canvas(modifier = Modifier.size(240.dp)) {
                        val strokeWidth = 16.dp.toPx()
                        val arcSize = size.minDimension - strokeWidth
                        
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(Color(0xFFFFD54F), Color(0xFFFF8A65), Color(0xFFD32F2F), Color(0xFFFFD54F)),
                                center = center
                            ),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            size = Size(arcSize, arcSize),
                            topLeft = Offset((size.width - arcSize) / 2, (size.height - arcSize) / 2)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("UV Index", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        Text("Level 7 - High", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Peak exposure expected until 2 PM.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }

            // 2. Personal Protection Card with Shadow
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), clip = false),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.WbSunny, null, tint = Color(0xFFFFB74D), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Personal Protection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("SPF 50+", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
                            Text("Broad Spectrum", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Reapply every", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
                            Text("2 hours", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            // 3. Daily UV Exposure Graph Enhanced
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Daily UV Forecast", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Black, 
                    letterSpacing = 1.sp, 
                    color = Color.Gray
                )
                
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    UVExposureGraph(modifier = Modifier.fillMaxSize())
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("6 AM", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("11 AM", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("3 PM", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("8 PM", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            // 4. Sunscreen Reminders with Shadow
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), clip = false),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, null, tint = Color.Gray)
                            Spacer(Modifier.width(12.dp))
                            Text("Sunscreen Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Switch(checked = true, onCheckedChange = {})
                    }
                    
                    HorizontalDivider(modifier = Modifier.alpha(0.1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Next reapplication in:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("1h 30m", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset Timer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun UVExposureGraph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // 1. Draw Grid Lines (Bars)
        val levels = listOf(3f, 6f, 9f, 12f, 15f)
        levels.forEach { level ->
            val y = height - (height * (level / 15f))
            drawLine(
                color = Color.Black.copy(alpha = 0.05f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
            // Optional: Draw level numbers if needed for accuracy
        }

        // 2. Draw Peak Exposure Shading (Middle section)
        val peakStart = width * 0.35f
        val peakEnd = width * 0.65f
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFF8A65).copy(alpha = 0.15f), Color.Transparent)
            ),
            topLeft = Offset(peakStart, 0f),
            size = Size(peakEnd - peakStart, height)
        )

        val path = Path().apply {
            moveTo(0f, height)
            cubicTo(
                width * 0.2f, height,
                width * 0.4f, height * 0.1f,
                width * 0.5f, height * 0.05f
            )
            cubicTo(
                width * 0.6f, height * 0.1f,
                width * 0.8f, height,
                width, height
            )
        }
        
        // 3. Draw Area Fill
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFF8A65).copy(alpha = 0.4f), Color(0xFFFF8A65).copy(alpha = 0.05f))
            )
        )
        
        // 4. Draw Path Stroke
        drawPath(
            path = path,
            color = Color(0xFFFF8A65),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // 5. Data points (Refined Open Circles)
        val points = listOf(0.05f, 0.15f, 0.4f, 0.85f, 1.0f, 0.85f, 0.35f, 0.1f, 0.05f)
        points.forEachIndexed { index, p ->
            val x = width * (index / (points.size - 1).toFloat())
            val y = height - (height * p * 0.95f)
            
            // design: open circle
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color(0xFFFF8A65),
                radius = 5.dp.toPx(),
                center = Offset(x, y),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
