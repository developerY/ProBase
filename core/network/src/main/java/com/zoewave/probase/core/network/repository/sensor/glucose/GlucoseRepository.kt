package com.zoewave.probase.core.network.repository.sensor.glucose

import com.zoewave.probase.core.model.health.GlucoseReading
import kotlinx.coroutines.flow.Flow

interface GlucoseRepository {
    /**
     * A continuous stream of glucose readings.
     */
    val glucoseReadings: Flow<GlucoseReading>

    /**
     * Trigger a manual scan (e.g., for NFC).
     */
    suspend fun scanSensor()
}