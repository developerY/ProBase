package com.zoewave.probase.ashbike.features.main

import com.zoewave.probase.ashbike.features.main.usecase.CalculateCaloriesUseCase
import com.zoewave.probase.ashbike.features.main.usecase.RideStatsUseCase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.coroutines.delay

class RideStatsUseCaseTest {

    private val useCase = RideStatsUseCase(CalculateCaloriesUseCase())

    @Test
    fun `maxSpeedFlow retains highest speed recorded`() = runBlocking {
        val maxSpeed = useCase.maxSpeedFlow(
            resetSignal = emptyFlow(),
            speedFlow = flow { emit(10f); delay(10); emit(25f); delay(10); emit(15f); delay(10); emit(32f); delay(10); emit(20f) }
        ).toList().last()

        assertEquals(32f, maxSpeed, 0.001f)
    }

    @Test
    fun `averageSpeedFlow calculates correct speed for distance and time`() = runBlocking {
        // 15 km in 45 minutes (0.75 hrs) -> 20 km/h
        var calls = 0
        val avgSpeed = useCase.averageSpeedFlow(
            resetSignal = emptyFlow(),
            distanceKmFlow = flow { emit(15f) },
            clockMs = { 
                if (calls++ == 0) 0L else 2700000L // startMs is 0, elapsed is 45 mins
            }
        ).toList().last()

        assertEquals(20.0, avgSpeed, 0.01)
    }

    @Test
    fun `averageSpeedFlow returns 0 when time elapsed is 0`() = runBlocking {
        val avgSpeed = useCase.averageSpeedFlow(
            resetSignal = emptyFlow(),
            distanceKmFlow = flow { emit(10f) },
            clockMs = { 0L }
        ).toList().last()

        assertEquals(0.0, avgSpeed, 0.001)
    }
}
