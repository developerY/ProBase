package com.zoewave.probase.ashbike.features.main

import com.zoewave.probase.ashbike.features.main.usecase.CalculateCaloriesUseCase
import com.zoewave.probase.ashbike.features.main.usecase.RideStatsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RideStatsUseCaseTest {

    private val useCase = RideStatsUseCase(CalculateCaloriesUseCase())

    @Test
    fun `maxSpeedFlow retains highest speed recorded`() = runBlocking {
        val maxSpeed = useCase.maxSpeedFlow(
            resetSignal = emptyFlow(),
            speedFlow = flowOf(10f, 25f, 15f, 32f, 20f)
        ).first()

        assertEquals(32f, maxSpeed, 0.001f)
    }

    @Test
    fun `averageSpeedFlow calculates correct speed for distance and time`() = runBlocking {
        // 15 km in 45 minutes (0.75 hrs) -> 20 km/h
        var calls = 0
        val avgSpeed = useCase.averageSpeedFlow(
            resetSignal = emptyFlow(),
            distanceKmFlow = flowOf(15f),
            clockMs = { 
                if (calls++ == 0) 0L else 2700000L // startMs is 0, elapsed is 45 mins
            }
        ).first()

        assertEquals(20.0, avgSpeed, 0.01)
    }

    @Test
    fun `averageSpeedFlow returns 0 when time elapsed is 0`() = runBlocking {
        val avgSpeed = useCase.averageSpeedFlow(
            resetSignal = emptyFlow(),
            distanceKmFlow = flowOf(10f),
            clockMs = { 0L }
        ).first()

        assertEquals(0.0, avgSpeed, 0.001)
    }
}
