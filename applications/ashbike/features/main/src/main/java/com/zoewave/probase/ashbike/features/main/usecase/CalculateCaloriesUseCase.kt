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

    // Added lastDistanceKm to track micro-movements tick-by-tick
    private data class CalorieState(
        val lastTickMs: Long = 0L,
        val lastDistanceKm: Float = 0f,
        val totalCalories: Float = 0f
    )

    operator fun invoke(
        distanceKmFlow: Flow<Float>,
        currentSpeedKmhFlow: Flow<Float>,
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
            return@scan CalorieState(
                lastTickMs = nowMs,
                lastDistanceKm = distanceKm,
                totalCalories = 0f
            )
        }

        val deltaMs = nowMs - state.lastTickMs
        val deltaDistance = distanceKm - state.lastDistanceKm

        // 2. Pause Guard (If Flow is paused for > 10 seconds, ignore the gap)
        if (deltaMs > 10_000L) {
            return@scan state.copy(lastTickMs = nowMs, lastDistanceKm = distanceKm)
        }

        // 3. THE GATEKEEPER: Ignore all math until they ride 10 meters.
        // This filters out GPS bounce while they are putting on their helmet.
        /* if (distanceKm < 0.01f) {
            return@scan state.copy(lastTickMs = nowMs)
        }*/

        // 4. Phantom Speed Guard (YOUR NEW CHECK):
        // If the distance hasn't increased, do not add calories, even if speed > 0.
        // We use maxOf to protect against weird negative GPS jumps.
        if (deltaDistance <= 0f) {
            return@scan state.copy(
                lastTickMs = nowMs,
                lastDistanceKm = maxOf(distanceKm, state.lastDistanceKm)
            )
        }

        // 5. Stoplight Guard (If speed is 0, hold state)
        if (currentSpeedKmh <= 0f) {
            return@scan state.copy(lastTickMs = nowMs, lastDistanceKm = distanceKm)
        }

        // 6. Convert elapsed MS to Hours for this specific tick
        val deltaHours = deltaMs / 3600000f

        // 7. Expanded Aerodynamic MET Curve (Caps at 80 km/h)
        val met = when {
            currentSpeedKmh < 16f -> 4.0f   // Leisurely / Warmup (~10 mph)
            currentSpeedKmh < 19f -> 6.0f   // Light effort (~12 mph)
            currentSpeedKmh < 22f -> 8.0f   // Moderate (~14 mph)
            currentSpeedKmh < 26f -> 10.0f  // Vigorous (~16 mph)
            currentSpeedKmh < 32f -> 12.0f  // Very fast / Fast Club Ride (~20 mph)
            currentSpeedKmh < 38f -> 15.8f  // Racing pace / Cat 3/4 (~24 mph)
            currentSpeedKmh < 45f -> 19.0f  // Elite Time Trial / Breakaway (~28 mph)
            currentSpeedKmh < 55f -> 24.0f  // Pro Lead-out / Fast Descent (~34 mph)
            currentSpeedKmh < 65f -> 30.0f  // Pro Sprint (~40 mph)
            currentSpeedKmh < 75f -> 38.0f  // Motor-paced / Steep Descent (~46 mph)
            currentSpeedKmh < 80f -> 45.0f  // Extreme Mountain Descent (~50 mph)
            else -> 55.0f                   // 80+ km/h (Absolute free-fall)
        }

        // 8. Calculate calories burned JUST during this tick
        val tickCalories = met * userStats.weightKg * deltaHours

        // 9. Accumulate
        CalorieState(
            lastTickMs = nowMs,
            lastDistanceKm = distanceKm,
            totalCalories = state.totalCalories + tickCalories
        )
    }.map { state ->
        // Expose only the accumulated Float to the UI
        state.totalCalories
    }
}