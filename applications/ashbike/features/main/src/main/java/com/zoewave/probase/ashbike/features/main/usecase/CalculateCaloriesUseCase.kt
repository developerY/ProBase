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

    // Internal state to accumulate calories tick-by-tick
    private data class CalorieState(
        val lastTickMs: Long = 0L,
        val totalCalories: Float = 0f
    )

    operator fun invoke(
        distanceKmFlow: Flow<Float>, // Kept in signature so you don't have to change your Service
        speedKmhFlow: Flow<Float>,
        userStatsFlow: Flow<UserStats>
    ): Flow<Float> = combine(
        distanceKmFlow,
        speedKmhFlow,
        userStatsFlow
    ) { _, speedKmh, userStats ->
        // Pass instantaneous speed and stats into the accumulator
        Pair(speedKmh, userStats)
    }.scan(CalorieState()) { state, (speedKmh, userStats) ->
        val nowMs = System.currentTimeMillis()

        // 1. Initialize on the very first flow tick
        if (state.lastTickMs == 0L) {
            return@scan CalorieState(lastTickMs = nowMs, totalCalories = 0f)
        }

        val deltaMs = nowMs - state.lastTickMs

        // 2. Time-Leap Guard (Prevents massive spikes if the app is paused/dozing in the background)
        if (deltaMs > 10_000L) {
            return@scan state.copy(lastTickMs = nowMs)
        }

        // 3. Convert ms to hours for this specific tiny slice of time
        val deltaHours = deltaMs / 3600000f

        // 4. Calculate MET based on current effort
        val tickCalories = if (speedKmh >= 1f) {
            val met = when {
                speedKmh < 16f -> 4f
                speedKmh < 19f -> 6f
                speedKmh < 22f -> 8f
                speedKmh < 25f -> 10f
                else -> 12f
            }
            // Burn active calories
            met * userStats.weightKg * deltaHours
        } else {
            // Speed is 0: Do not add active calories (Prevents standing-still accumulation)
            0f
        }

        // 5. Add to the grand total
        CalorieState(
            lastTickMs = nowMs,
            totalCalories = state.totalCalories + tickCalories
        )
    }.map { state ->
        // Expose only the Float value to your Service
        state.totalCalories
    }
}