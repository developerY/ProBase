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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

class CalculateCaloriesUseCase @Inject constructor() {

    // Internal state kept alive by your Foreground Service
    private data class CalorieState(
        val lastDistanceKm: Float = 0f,
        val totalCalories: Float = 0f
    )

    operator fun invoke(
        distanceKmFlow: Flow<Float>,
        currentSpeedKmhFlow: Flow<Float>, // Make sure to pass CURRENT speed here
        userStatsFlow: Flow<UserStats>
    ): Flow<Float> = combine(
        distanceKmFlow,
        currentSpeedKmhFlow,
        userStatsFlow
    ) { distanceKm, currentSpeedKmh, userStats ->
        Triple(distanceKm, currentSpeedKmh, userStats)
    }.scan(CalorieState()) { state, (distanceKm, currentSpeedKmh, userStats) ->

        // 1. How far did the bike move since the exact last GPS tick?
        val deltaDistanceKm = distanceKm - state.lastDistanceKm

        // 2. Stoplight Guard: If we haven't moved forward, add 0 calories and hold state.
        if (deltaDistanceKm <= 0f) {
            return@scan state.copy(
                // Protect against weird negative GPS jumps
                lastDistanceKm = maxOf(distanceKm, state.lastDistanceKm)
            )
        }

        // 3. Safe Speed Guard (Prevents Divide-by-Zero)
        val safeSpeed = if (currentSpeedKmh > 1f) currentSpeedKmh else 1f

        // 4. Exact moving time for this tiny slice of distance
        val deltaHours = deltaDistanceKm / safeSpeed

        // 5. Human-Powered MET rules (Caps at 12 for E-Bike physics)
        /* val met = when {
            safeSpeed < 16f -> 4f
            safeSpeed < 19f -> 6f
            safeSpeed < 22f -> 8f
            safeSpeed < 25f -> 10f
            else -> 12f
        }*/

        val met = when {
            safeSpeed < 16f -> 4f   // Leisurely / Eco mode
            safeSpeed < 19f -> 6f   // Moderate
            safeSpeed < 22f -> 8f   // Brisk
            safeSpeed < 26f -> 10f  // Fast 
            safeSpeed < 32f -> 12f  // Class 1/2 E-Bike Max Assist
            safeSpeed < 45f -> 14f  // Class 3 E-Bike Max Assist
            else -> 16f               // Extreme Downhill / Sprinting
        }

        // 6. Calculate calories burned JUST for this tiny segment
        val tickCalories = met * userStats.weightKg * deltaHours

        // 7. Accumulate and save to RAM for the next tick
        CalorieState(
            lastDistanceKm = distanceKm,
            totalCalories = state.totalCalories + tickCalories
        )
    }.map { state ->
        // Expose only the accumulated Float to the UI
        state.totalCalories
    }
}