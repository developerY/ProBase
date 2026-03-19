package com.zoewave.probase.ashbike.features.main.usecase

/**
 * Calculates calories burned based on:
 *  - distance (km)
 *  - speed (km/h)
 *  - user weight (kg)
 * Uses a simple MET × weight × duration model.
 */

/**
 * Simple MET-based calories calculator.
 */

import android.os.SystemClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

class CalculateCaloriesUseCase @Inject constructor() {

    // Internal state kept alive by your Foreground Service
    private data class CalorieState(
        val lastTickMs: Long = 0L,
        val totalCalories: Float = 0f
    )

    operator fun invoke(
        distanceKmFlow: Flow<Float>,      // 1. Used strictly as a Gatekeeper
        currentSpeedKmhFlow: Flow<Float>, // 2. Used for the Physics (MET)
        userStatsFlow: Flow<UserStats>
    ): Flow<Float> = combine(
        distanceKmFlow,
        currentSpeedKmhFlow,
        userStatsFlow
    ) { distanceKm, currentSpeedKmh, userStats ->
        Triple(distanceKm, currentSpeedKmh, userStats)
    }.scan(CalorieState()) { state, (distanceKm, currentSpeedKmh, userStats) ->

        // Use elapsedRealtime to prevent timezone/network sync bugs!
        val nowMs = SystemClock.elapsedRealtime()

        // 1. First tick initialization
        if (state.lastTickMs == 0L) {
            return@scan CalorieState(lastTickMs = nowMs, totalCalories = 0f)
        }

        val deltaMs = nowMs - state.lastTickMs

        // 2. Pause Guard (If Flow is paused for > 10 seconds, ignore the gap)
        if (deltaMs > 10_000L) {
            return@scan state.copy(lastTickMs = nowMs)
        }

        // 3. THE GATEKEEPER: Ignore all math until they ride 10 meters.
        // This filters out GPS bounce while they are putting on their helmet.
        /* if (distanceKm < 0.01f) {
            return@scan state.copy(lastTickMs = nowMs)
        }*/

        // 4. Stoplight Guard (If stopped, hold state)
        if (currentSpeedKmh <= 0f) {
            return@scan state.copy(lastTickMs = nowMs)
        }

        // 5. Convert MS to Hours
        val deltaHours = deltaMs / 3600000f

        // 6. E-Bike Physics Cap (Maxes at 12 MET for human effort)
        val met = when {
            currentSpeedKmh < 16f -> 4f   // Leisurely / Eco mode
            currentSpeedKmh < 19f -> 6f   // Moderate
            currentSpeedKmh < 22f -> 8f   // Brisk
            currentSpeedKmh < 26f -> 10f  // Fast
            currentSpeedKmh < 32f -> 12f  // Class 1/2 E-Bike Max Assist
            currentSpeedKmh < 45f -> 14f  // Class 3 E-Bike Max Assist
            else -> 16f               // Extreme Downhill / Sprinting
        }

        // 7. Calculate calories burned JUST during this tick
        val tickCalories = met * userStats.weightKg * deltaHours

        // 8. Accumulate
        CalorieState(
            lastTickMs = nowMs,
            totalCalories = state.totalCalories + tickCalories
        )
    }.map { state ->
        // Expose only the accumulated Float to the UI
        state.totalCalories
    }
}