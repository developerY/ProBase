package com.zoewave.probase.ashbike.mobile

import com.zoewave.probase.ashbike.features.main.usecase.CalculateCaloriesUseCase
import com.zoewave.probase.ashbike.features.main.usecase.RideStatsUseCase
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationTests {

    @Test
    fun `averageSpeedFlow correctly calculates speed`() = runBlocking {
        val useCase = RideStatsUseCase(CalculateCaloriesUseCase())

        // 10km in 30 minutes (0.5 hours) = 20 km/h
        var calls = 0
        val avgSpeed = useCase.averageSpeedFlow(
            resetSignal = emptyFlow(),
            distanceKmFlow = flow { emit(10f) },
            clockMs = { 
                if (calls++ == 0) 0L else 1800000L // startMs is 0, elapsed is 30 mins
            }
        ).toList().last()

        assertEquals(20.0, avgSpeed, 0.1)
    }
}
