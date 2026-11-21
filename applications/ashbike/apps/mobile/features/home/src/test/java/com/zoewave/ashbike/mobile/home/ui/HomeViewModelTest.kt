package com.zoewave.ashbike.mobile.home.ui

import com.zoewave.ashbike.data.repository.bike.BikeRepository
import com.zoewave.ashbike.data.usecases.WeatherUseCase
import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import com.zoewave.probase.ashbike.features.main.service.BikeServiceManager
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val bikeServiceManager: BikeServiceManager = mockk()
    private val weatherUseCase: WeatherUseCase = mockk()
    private val appSettingsRepository: AppSettingsRepository = mockk()
    private val bikeRepository: BikeRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { appSettingsRepository.gpsAccuracyFlow } returns flowOf(com.zoewave.ashbike.model.bike.LocationEnergyLevel.BALANCED)
        every { bikeRepository.rideInfo } returns MutableStateFlow(com.zoewave.ashbike.model.bike.BikeRideInfo.initial())
        every { bikeRepository.isGlassConnected } returns MutableStateFlow(false)
        every { bikeRepository.isGlassSessionActive } returns MutableStateFlow(false)

        viewModel = HomeViewModel(
            bikeServiceManager = bikeServiceManager,
            weatherUseCase = weatherUseCase,
            appSettingsRepository = appSettingsRepository,
            bikeRepository = bikeRepository
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent StartRide calls sendCommand on BikeServiceManager`() = runTest {
        every { bikeServiceManager.sendCommand(any()) } returns Unit

        viewModel.onEvent(HomeEvent.StartRide)

        coVerify { bikeServiceManager.sendCommand("ACTION_START_RIDE") }
    }

    @Test
    fun `onEvent StopRide calls sendCommand on BikeServiceManager`() = runTest {
        every { bikeServiceManager.sendCommand(any()) } returns Unit

        viewModel.onEvent(HomeEvent.StopRide)

        coVerify { bikeServiceManager.sendCommand("ACTION_STOP_RIDE") }
    }
}
