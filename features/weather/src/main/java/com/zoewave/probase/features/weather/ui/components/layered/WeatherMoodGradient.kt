package com.zoewave.probase.features.weather.ui.components.layered

import androidx.compose.ui.graphics.Color

object WeatherMoodGradient {
    fun getColors(condition: LayeredWeatherCondition, isDaytime: Boolean): List<Color> {
        return when (condition) {
            LayeredWeatherCondition.SUNNY -> {
                if (isDaytime) {
                    listOf(Color(0xFFFFF9C4), Color(0xFFFFECB3), Color(0xFFFFE082)) // Sunny Morning/Day
                } else {
                    listOf(Color(0xFF2C3E50), Color(0xFF4B79A1), Color(0xFF283E51)) // Clear Night
                }
            }
            LayeredWeatherCondition.CLOUDY -> listOf(Color(0xFFECE9E6), Color(0xFFFFFFFF), Color(0xFFD7D2CC)) // Overcast Soft
            LayeredWeatherCondition.RAINY -> listOf(Color(0xFF4B79A1), Color(0xFF283E51), Color(0xFF1F1C2C)) // Rain Mood
            LayeredWeatherCondition.THUNDER -> listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)) // Stormy
            LayeredWeatherCondition.WINDY -> listOf(Color(0xFFE0EAFC), Color(0xFFCFDEF3), Color(0xFFBBD2C5)) // Fresh/Airy
        }
    }
}
