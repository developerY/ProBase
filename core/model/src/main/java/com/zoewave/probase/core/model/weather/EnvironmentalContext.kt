package com.zoewave.probase.core.model.weather

import kotlinx.serialization.Serializable

@Serializable
data class EnvironmentalContext(
    val temperature: Double,
    val humidity: Double,
    val uvIndex: Double,
    val isDay: Boolean,
    val weatherCode: Int,
    val hourlyUV: List<Double> = emptyList()
)
