package com.zoewave.probase.features.weather.ui.sun

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SunIntelligenceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SunIntelligenceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SunIntelligenceScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SunIntelligenceScreen(
    uiState: SunIntelligenceUiState,
    onEvent: (SunIntelligenceEvent) -> Unit,
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
        when (uiState) {
            SunIntelligenceUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SunIntelligenceUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = Color.Red)
                }
            }
            is SunIntelligenceUiState.Success -> {
                val context = uiState.context
                val uvIndex = context?.uvIndex ?: 0.0
                val hourlyUV = context?.hourlyUV ?: emptyList()
                
                val level = uvIndex.toInt()
                val levelText = when {
                    level < 3 -> "Low"
                    level < 6 -> "Moderate"
                    level < 8 -> "High"
                    level < 11 -> "Very High"
                    else -> "Extreme"
                }
                
                val displayLocation = if (uiState.isLocationFallback) "Location could not be found" else "Peak exposure expected until 2 PM."

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                        .alpha(if (uiState.isLocationFallback) 0.6f else 1.0f),
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
                                Text(
                                    text = if (context != null) "Level $level - $levelText" else "---", 
                                    style = MaterialTheme.typography.headlineMedium, 
                                    fontWeight = FontWeight.Bold, 
                                    fontFamily = FontFamily.Serif
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                Text(
                    text = displayLocation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f)
                )
                    }

                    // 2. Personal Protection Card with Shadow
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (context != null) 1f else 0.6f)
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
                                    Text(if (level >= 8) "SPF 50+" else "SPF 30+", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
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
                    Column(
                        modifier = Modifier.alpha(if (hourlyUV.isNotEmpty()) 1f else 0.6f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Daily UV Forecast", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Black, 
                            letterSpacing = 1.sp, 
                            color = Color.Gray
                        )
                        
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            UVExposureGraph(
                                hourlyData = hourlyUV,
                                currentUV = uvIndex,
                                modifier = Modifier.fillMaxSize()
                            )
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
                                Switch(
                                    checked = uiState.isTimerActive, 
                                    onCheckedChange = { onEvent(SunIntelligenceEvent.ToggleTimer(it)) }
                                )
                            }
                            
                            HorizontalDivider(modifier = Modifier.alpha(0.1f))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Next reapplication in:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(
                                        text = formatTime(uiState.reapplicationTimeRemaining), 
                                        style = MaterialTheme.typography.headlineSmall, 
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Button(
                                    onClick = { onEvent(SunIntelligenceEvent.ResetTimer) },
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
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

data class UVPoint(val hour: Int, val value: Double, val isReal: Boolean)

@Composable
fun UVExposureGraph(
    hourlyData: List<Double>,
    currentUV: Double,
    modifier: Modifier = Modifier
) {
    val points = remember(hourlyData, currentUV) {
        val peakHour = 13.0 // 1 PM
        val sigma = 3.5 // Controls the width of the bell curve
        val maxUV = if (hourlyData.isNotEmpty()) hourlyData.maxOrNull() ?: 1.0 else (currentUV * 1.2).coerceAtLeast(5.0)
        
        // Window 6 AM to 8 PM
        (6..20).map { hour ->
            // Use real data if available, otherwise extrapolate using Gaussian bell curve
            val realValue = hourlyData.getOrNull(hour)
            if (realValue != null && realValue > 0.1) {
                UVPoint(hour, realValue, true)
            } else {
                // Bell curve: f(x) = a * exp(-(x-b)^2 / (2c^2))
                val hourDiff = hour - peakHour
                val exponent = -0.5 * (hourDiff * hourDiff) / (sigma * sigma)
                val extrapolated = maxUV * kotlin.math.exp(exponent)
                UVPoint(hour, extrapolated, false)
            }
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxVal = points.maxOf { it.value }.coerceAtLeast(1.0)
        
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

        // 3. Draw Area Fill & Path
        val coral = Color(0xFFFF8A65)
        val gray = Color.LightGray.copy(alpha = 0.5f)

        val fullPath = Path().apply {
            moveTo(0f, height)
            points.forEachIndexed { index, p ->
                val x = width * (index / (points.size - 1).toFloat())
                val y = height - (height * (p.value / maxVal).toFloat() * 0.95f)
                lineTo(x, y)
            }
            lineTo(width, height)
            close()
        }
        
        drawPath(
            path = fullPath,
            brush = Brush.verticalGradient(
                listOf(coral.copy(alpha = 0.3f), Color.Transparent)
            )
        )
        
        // 4. Draw Path Segments with color distinction
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val x1 = width * (i / (points.size - 1).toFloat())
            val y1 = height - (height * (p1.value / maxVal).toFloat() * 0.95f)
            val x2 = width * ((i + 1) / (points.size - 1).toFloat())
            val y2 = height - (height * (p2.value / maxVal).toFloat() * 0.95f)
            
            drawLine(
                color = if (p1.isReal && p2.isReal) coral else gray,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        
        // 5. Data points (Refined Open Circles)
        points.forEachIndexed { index, p ->
            val x = width * (index / (points.size - 1).toFloat())
            val y = height - (height * (p.value / maxVal).toFloat() * 0.95f)
            
            val pointColor = if (p.isReal) coral else gray

            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = pointColor,
                radius = 5.dp.toPx(),
                center = Offset(x, y),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
