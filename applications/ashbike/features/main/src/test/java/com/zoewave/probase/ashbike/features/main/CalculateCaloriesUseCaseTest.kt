package com.zoewave.probase.ashbike.features.main

import com.zoewave.probase.ashbike.features.main.usecase.CalculateCaloriesUseCase
import com.zoewave.probase.ashbike.features.main.usecase.UserStats
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateCaloriesUseCaseTest {

    private val useCase = CalculateCaloriesUseCase()
    private val defaultUser = UserStats(heightCm = 175f, weightKg = 70f)

    @Test
    fun `emits zero for the very first tick (initialization)`() = runBlocking {
        var currentTime = 1000L
        val results = useCase(
            distanceKmFlow = flowOf(0f),
            currentSpeedKmhFlow = flowOf(0f),
            userStatsFlow = flowOf(defaultUser),
            clockMs = { currentTime }
        ).toList()

        assertEquals(1, results.size)
        assertEquals(0f, results[0], 0.001f) // First tick should always be 0
    }

    @Test
    fun `emits per-tick delta calories based on MET curve`() = runBlocking {
        var currentTime = 1000L
        // Sequence:
        // 1. Initial emission (Time 1000, Dist 0, Speed 20) -> emits 0
        // 2. Second emission (Time 2000, Dist 10, Speed 20) -> emits delta
        val distanceFlow = flowOf(0f, 10f)
        val speedFlow = flowOf(20f, 20f) // Speed < 22kmh corresponds to MET 8.0

        val results = useCase(
            distanceKmFlow = distanceFlow,
            currentSpeedKmhFlow = speedFlow,
            userStatsFlow = flowOf(defaultUser),
            clockMs = {
                val t = currentTime
                currentTime += 3600000L // 1 hour jumps to make math easy (delta = 1 hour)
                t
            }
        ).toList()

        // Expectation for delta:
        // MET (8.0) * weight (70kg) * deltaHours (1.0) = 560 kcal
        assertEquals(2, results.size)
        assertEquals(0f, results[0], 0.001f) // Init
        assertEquals(560f, results[1], 0.001f) // Delta for 1 hour at MET 8.0
    }

    @Test
    fun `stoplight guard emits zero delta if speed is zero despite time passing`() = runBlocking {
        var currentTime = 1000L
        // Sequence:
        // 1. Initial emission
        // 2. Second emission with 0 speed but 1 hour time delta
        val distanceFlow = flowOf(0f, 10f)
        val speedFlow = flowOf(0f, 0f)

        val results = useCase(
            distanceKmFlow = distanceFlow,
            currentSpeedKmhFlow = speedFlow,
            userStatsFlow = flowOf(defaultUser),
            clockMs = {
                val t = currentTime
                currentTime += 3600000L // 1 hour jump
                t
            }
        ).toList()

        assertEquals(2, results.size)
        assertEquals(0f, results[1], 0.001f) // Delta should be 0 because speed is 0
    }

    @Test
    fun `phantom speed guard defers calories if distance has not increased`() = runBlocking {
        var currentTime = 1000L
        // Sequence:
        // 1. Initial emission
        // 2. Second emission: speed 20, but distance is STILL 0 (GPS bounce) -> emits 0
        // 3. Third emission: speed 20, distance jumps to 10 -> emits full 2 hours worth
        val distanceFlow = flowOf(0f, 0f, 10f)
        val speedFlow = flowOf(20f, 20f, 20f)

        val results = useCase(
            distanceKmFlow = distanceFlow,
            currentSpeedKmhFlow = speedFlow,
            userStatsFlow = flowOf(defaultUser),
            clockMs = {
                val t = currentTime
                currentTime += 3600000L // 1 hour jump each time
                t
            }
        ).toList()

        assertEquals(3, results.size)
        assertEquals(0f, results[0], 0.001f) // Init
        assertEquals(0f, results[1], 0.001f) // Phantom speed guard blocks emission
        // Third tick: MET (8.0) * 70kg * 2 hours (since initial tick!) = 1120 kcal
        assertEquals(1120f, results[2], 0.001f)
    }
}
