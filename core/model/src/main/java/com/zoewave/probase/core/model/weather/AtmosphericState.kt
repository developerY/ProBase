package com.zoewave.probase.core.model.weather

data class AtmosphericState(
    val weather: OpenWeatherResponse? = null,
    val environmentalContext: EnvironmentalContext? = null,
    val lastUpdated: Long = 0L,
    val isFallback: Boolean = false
)
