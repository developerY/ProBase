package com.zoewave.probase.ashbike.features.main

import com.zoewave.probase.ashbike.features.main.usecase.CalculateCaloriesUseCase
import com.zoewave.probase.ashbike.features.main.usecase.UserStats
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.coroutines.delay

class CalculateCaloriesUseCaseTest {

    private val useCase = CalculateCaloriesUseCase()
    private val defaultUser = UserStats(heightCm = 175f, weightKg = 70f)

    @Test
    fun `emits zero for the very first tick (initialization)`() = runBlocking {
        val results = useCase(
            distanceKmFlow = flow { emit(0f) },
            currentSpeedKmhFlow = flow { emit(0f) },
            userStatsFlow = flow { emit(defaultUser) },
            clockMs = { 1000L }
        ).toList()

        assertEquals(2, results.size) // scan initial + first tick
        assertEquals(0f, results[1], 0.001f)
    }

    @Test
    fun `emits per-tick delta calories based on MET curve`() = runBlocking {
        var time = 1000L
        val results = useCase(
            distanceKmFlow = flow { emit(0f); delay(10); emit(10f) },
            currentSpeedKmhFlow = flow { emit(20f); delay(10); emit(20f) },
            userStatsFlow = flow { emit(defaultUser) },
            clockMs = { val t = time; time += 10000L; t } // 10 seconds elapsed on second tick
        ).toList()

        // MET (8.0) * weight (70kg) * deltaHours (10s / 3600s) = 1.5555 kcal
        assertEquals(0f, results[1], 0.001f) // Init tick
        assertEquals(1.555f, results[2], 0.001f) 
    }

    @Test
    fun `stoplight guard emits zero delta if speed is zero despite time passing`() = runBlocking {
        var time = 1000L
        val results = useCase(
            distanceKmFlow = flow { emit(0f); delay(10); emit(10f) },
            currentSpeedKmhFlow = flow { emit(0f); delay(10); emit(0f) },
            userStatsFlow = flow { emit(defaultUser) },
            clockMs = { val t = time; time += 10000L; t }
        ).toList()

        assertEquals(0f, results[2], 0.001f)
    }

    @Test
    fun `phantom speed guard defers calories if distance has not increased`() = runBlocking {
        var time = 1000L
        val results = useCase(
            distanceKmFlow = flow { emit(0f); delay(10); emit(0f); delay(10); emit(10f) },
            currentSpeedKmhFlow = flow { emit(20f); delay(10); emit(20f); delay(10); emit(20f) },
            userStatsFlow = flow { emit(defaultUser) },
            clockMs = { val t = time; time += 10000L; t }
        ).toList()

        assertEquals(0f, results[1], 0.001f) // Init
        assertEquals(0f, results[2], 0.001f) // Phantom speed guard blocks emission
        assertEquals(1.555f, results[3], 0.001f) // 10 seconds worth of calories (MET 8.0 * 70kg * 10s/3600s)
    }
}
