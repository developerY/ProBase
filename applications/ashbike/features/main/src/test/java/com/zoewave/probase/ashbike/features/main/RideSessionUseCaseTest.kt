package com.zoewave.probase.ashbike.features.main

import com.zoewave.probase.ashbike.database.repository.UserProfileRepository
import com.zoewave.probase.ashbike.features.main.usecase.RideSession
import com.zoewave.probase.ashbike.features.main.usecase.RideSessionUseCase
import com.zoewave.probase.ashbike.features.main.usecase.RideStatsUseCase
import com.zoewave.probase.core.data.repository.travel.UnifiedLocationRepository
import com.zoewave.probase.core.data.repository.travel.compass.CompassRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RideSessionUseCaseTest {

    private lateinit var useCase: RideSessionUseCase
    private val locationRepo: UnifiedLocationRepository = mockk()
    private val compassRepo: CompassRepository = mockk()
    private val statsUseCase: RideStatsUseCase = mockk()
    private val userProfileRepo: UserProfileRepository = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { locationRepo.locationFlow } returns flowOf()
        every { locationRepo.speedFlow } returns flowOf()
        every { compassRepo.headingFlow } returns flowOf()

        every { userProfileRepo.heightFlow } returns flowOf("175")
        every { userProfileRepo.weightFlow } returns flowOf("70")

        val emptySession = RideSession(
            startTimeMs = 0L,
            path = emptyList(),
            elapsedMs = 0L,
            totalDistanceKm = 0f,
            averageSpeedKmh = 0.0,
            maxSpeedKmh = 0f,
            elevationGainM = 0f,
            elevationLossM = 0f,
            caloriesBurned = 0,
            heading = 0f
        )
        val sessionFlow = MutableStateFlow(emptySession)

        every { statsUseCase.sessionFlow(any(), any(), any(), any(), any()) } returns sessionFlow

        useCase = RideSessionUseCase(locationRepo, compassRepo, statsUseCase, userProfileRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `stopAndGetSession returns current session`() = runTest {
        val session = useCase.stopAndGetSession()
        assertEquals(0f, session.totalDistanceKm)
    }
}
