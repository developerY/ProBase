package com.zoewave.probase.features.weather.ui.components.layered

import com.zoewave.probase.core.model.weather.EnvironmentalContext

object WeatherAdvice {
    fun getBeautyAdvice(envContext: EnvironmentalContext): String? {
        return when {
            envContext.uvIndex > 3.0 -> "☀️ High UV detected. Prioritize SPF in your ritual today."
            envContext.humidity < 30.0 -> "💧 Low humidity. Use a humectant to retain moisture."
            else -> null
        }
    }
}
