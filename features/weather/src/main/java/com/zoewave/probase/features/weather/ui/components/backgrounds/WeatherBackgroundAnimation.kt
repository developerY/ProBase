package com.zoewave.probase.features.weather.ui.components.backgrounds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.zoewave.probase.features.weather.ui.components.combine.WeatherConditionUnif

@Preview(showBackground = true, widthDp = 200, heightDp = 400)
@Composable
fun WeatherBackgroundAnimationSunnyPreview() {
    WeatherBackgroundAnimation(weatherCondition = WeatherConditionUnif.SUNNY)
}

@Preview(showBackground = true, widthDp = 200, heightDp = 400)
@Composable
fun WeatherBackgroundAnimationRainyPreview() {
    WeatherBackgroundAnimation(weatherCondition = WeatherConditionUnif.RAINY)
}

@Composable
fun WeatherBackgroundAnimation(
    weatherCondition: WeatherConditionUnif,
    modifier: Modifier = Modifier
) {
    when (weatherCondition) {
        WeatherConditionUnif.SUNNY -> {
            // Example: a bright, animated gradient for sunny conditions
            Box(
                modifier = modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Yellow,
                            Color(0xFFFFA726)
                        )
                    )
                )
            )
        }

        WeatherConditionUnif.CLOUDY -> {
            // Cloudy: use a subtle animated cloud background
            CloudyBackgroundAnimation(modifier)
        }

        WeatherConditionUnif.RAINY -> {
            // Rainy: show falling raindrops animation
            RainBackgroundAnimation(modifier)
        }

        WeatherConditionUnif.SNOWY -> {
            // Snowy: show falling snowflakes animation
            SnowBackgroundAnimation(modifier)
        }

        else -> {
            // Default background
            Box(modifier = modifier.background(Color.LightGray))
        }
    }
}


// Similar implementations can be created for RainBackgroundAnimation and SnowBackgroundAnimation.
