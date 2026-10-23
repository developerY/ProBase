package com.zoewave.probase.goswift.data.caffeine

import java.time.Duration
import java.time.Instant
import java.time.ZoneId

data class CaffeineIntakeRecord(
    val id: String,
    val timestamp: Instant,
    val amountMg: Double,
    val sourceName: String,
    val isPlanned: Boolean = false
) {
    init {
        require(amountMg >= 0.0) { "Caffeine amount cannot be negative." }
    }
}

data class PharmacokineticConfig(
    val halfLife: Duration = Duration.ofHours(5),
    val dailyLimitMg: Double = 400.0,
    val sleepThresholdMg: Double = 50.0,
    val negligibleCutoffMg: Double = 1.0
) {
    init {
        require(!halfLife.isZero && !halfLife.isNegative) { "Half-life must be positive." }
        require(dailyLimitMg > 0.0) { "Daily limit must be positive." }
        require(sleepThresholdMg > 0.0) { "Sleep threshold must be positive." }
    }
}

data class TelemetryPoint(
    val timestamp: Instant,
    val estimatedRemainingMg: Double,
    val includesPlanned: Boolean = false
)

data class CaffeineTelemetrySnapshot(
    val evaluatedAt: Instant,
    val estimatedRemainingMg: Double,
    val dailyConsumedMg: Double,
    val dailyLimitMg: Double,
    val dailyLimitRatio: Float,
    val estimatedSleepReadyInstant: Instant?,
    val estimatedZeroClearanceInstant: Instant?,
    val timelineSeries: List<TelemetryPoint>
)
