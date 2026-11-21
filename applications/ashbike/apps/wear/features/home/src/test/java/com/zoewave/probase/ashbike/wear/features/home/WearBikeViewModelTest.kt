package com.zoewave.probase.ashbike.wear.features.home

import com.zoewave.ashbike.model.bike.BikeRideInfo
import com.zoewave.ashbike.model.bike.RideState
import com.zoewave.probase.ashbike.features.main.service.BikeServiceManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WearBikeViewModelTest {

    private lateinit var viewModel: WearBikeViewModel
    private val serviceManager: BikeServiceManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { serviceManager.rideInfo } returns MutableStateFlow(
            BikeRideInfo.initial().copy(rideState = RideState.Riding, currentSpeed = 10.0)
        )

        viewModel = WearBikeViewModel(serviceManager)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleTracking when currently tracking stops ride`() {
        viewModel.toggleTracking(isCurrentlyTracking = true)

        verify {
            serviceManager.sendCommand("com.zoewave.probase.ashbike.features.main.service.action.STOP_RIDE")
        }
    }

    @Test
    fun `toggleTracking when not tracking starts ride`() {
        viewModel.toggleTracking(isCurrentlyTracking = false)

        verify {
            serviceManager.sendCommand("com.zoewave.probase.ashbike.features.main.service.action.START_RIDE")
        }
    }

    @Test
    fun `toggleDemoMode toggles on service manager`() {
        viewModel.toggleDemoMode()

        verify { serviceManager.toggleDemoMode() }
    }
}
