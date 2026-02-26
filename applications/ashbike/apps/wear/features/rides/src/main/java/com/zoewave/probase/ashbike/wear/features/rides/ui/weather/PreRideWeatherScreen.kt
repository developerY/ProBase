package com.zoewave.probase.ashbike.wear.features.rides.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices

// ==========================================
// 1. UI State Model
// ==========================================
data class WeatherState(
    val condition: String,       // e.g., "Sunny", "Cloudy", "Rain"
    val temperature: Int,
    val windSpeed: Float,
    val humidity: Int,
    val isMetric: Boolean = true // To toggle between C/kmh and F/mph
)

// ==========================================
// 2. The Main Screen
// ==========================================
@Composable
fun PreRideWeatherScreen(
    weather: WeatherState?,
    onStartRideClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (weather == null) {
            item { Text("Fetching weather...", color = Color.Gray) }
            return@ScalingLazyColumn
        }

        // --- 1. Main Condition Icon ---
        item {
            val (icon, tint) = getWeatherIconAndColor(weather.condition)
            Icon(
                imageVector = icon,
                contentDescription = weather.condition,
                tint = tint,
                modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
            )
        }

        // --- 2. Huge Temperature Display ---
        item {
            val tempUnit = if (weather.isMetric) "°C" else "°F"
            Text(
                text = "${weather.temperature}$tempUnit",
                style = MaterialTheme.typography.display2, // Largest font available
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Text(
                text = weather.condition,
                style = MaterialTheme.typography.title3,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // --- 3. Cycling Specifics (Wind & Humidity) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Wind Widget
                val speedUnit = if (weather.isMetric) "km/h" else "mph"
                WeatherSubMetric(
                    icon = Icons.Default.Air,
                    value = "${weather.windSpeed.toInt()} $speedUnit",
                    label = "Wind",
                    iconTint = Color(0xFF90CAF9) // Light Blue
                )

                // Humidity Widget
                WeatherSubMetric(
                    icon = Icons.Default.WaterDrop,
                    value = "${weather.humidity}%",
                    label = "Humid",
                    iconTint = Color(0xFF81D4FA) // Cyan
                )
            }
        }

        // --- 4. The Action Button ---
        item {
            Button(
                onClick = onStartRideClick,
                colors = ButtonDefaults.primaryButtonColors(backgroundColor = Color(0xFF4CAF50)), // Green for GO
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DirectionsBike, contentDescription = "Start")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Ride", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Helper Composable for the small bottom metrics
@Composable
private fun WeatherSubMetric(icon: ImageVector, value: String, label: String, iconTint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.body2, color = Color.White)
        Text(text = label, style = MaterialTheme.typography.caption3, color = Color.Gray)
    }
}

// Helper to map a string condition to a Material Icon
private fun getWeatherIconAndColor(condition: String): Pair<ImageVector, Color> {
    return when (condition.lowercase()) {
        "sunny", "clear" -> Pair(Icons.Default.WbSunny, Color(0xFFFFEB3B)) // Yellow
        "rain", "showers" -> Pair(Icons.Default.WaterDrop, Color(0xFF2196F3)) // Blue
        else -> Pair(Icons.Default.Cloud, Color(0xFFB0BEC5)) // Gray Cloud
    }
}

// ==========================================
// 3. Previews
// ==========================================
@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Weather - Clear"
)
@Composable
fun PreRideWeatherScreenPreviewSunny() {
    MaterialTheme {
        PreRideWeatherScreen(
            weather = WeatherState("Sunny", 22, 14f, 45, true),
            onStartRideClick = {}
        )
    }
}