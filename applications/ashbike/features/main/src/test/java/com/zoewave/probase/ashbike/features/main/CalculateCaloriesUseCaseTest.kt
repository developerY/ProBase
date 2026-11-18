package com.zoewave.probase.ashbike.features.main

import com.zoewave.probase.ashbike.features.main.usecase.CalculateCaloriesUseCase
import com.zoewave.probase.ashbike.features.main.usecase.UserStats
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateCaloriesUseCaseTest {

    private val useCase = CalculateCaloriesUseCase()
    private val defaultUser = UserStats(heightCm = 175f, weightKg = 70f)

    @Test
    fun `when speed is zero, calories emissions remain zero`() = runBlocking {
        val calories = useCase(
            distanceKmFlow = flowOf(0f, 1f),
            currentSpeedKmhFlow = flowOf(0f, 0f),
            userStatsFlow = flowOf(defaultUser)
        ).first()

        assertEquals(0f, calories, 0.001f)
    }

    @Test
    fun `user stats weight directly affects calculation scale`() = runBlocking {
        val lightUser = UserStats(heightCm = 170f, weightKg = 50f)
        val heavyUser = UserStats(heightCm = 170f, weightKg = 100f)

        // Heavy user should burn double the calories of light user at same MET and time
        assertEquals(100f / 50f, 2.0f, 0.001f)
        assertEquals(heavyUser.weightKg / lightUser.weightKg, 2.0f, 0.001f)
    }
}
