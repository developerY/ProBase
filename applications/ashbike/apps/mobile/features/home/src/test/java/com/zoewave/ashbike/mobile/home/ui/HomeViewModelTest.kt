package com.zoewave.ashbike.mobile.home.ui

import android.util.Log
import com.zoewave.ashbike.data.repository.bike.BikeRepository
import com.zoewave.ashbike.data.usecases.WeatherUseCase
import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import com.zoewave.probase.ashbike.features.main.service.BikeServiceManager
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        every { appSettingsRepository.themeFlow } returns flowOf("System")
        every { appSettingsRepository.languageFlow } returns flowOf("English")
        every { appSettingsRepository.notificationsFlow } returns flowOf("Enabled")
        every { appSettingsRepository.unitsFlow } returns flowOf("Metric")
        every { appSettingsRepository.gpsAccuracyFlow } returns flowOf(com.zoewave.ashbike.model.bike.LocationEnergyLevel.BALANCED)
        every { appSettingsRepository.longRideEnabledFlow } returns flowOf(false)

        every { bikeRepository.currentGear } returns MutableStateFlow(1)
        every { bikeRepository.isBikeConnected } returns MutableStateFlow(false)
        every { bikeRepository.isGlassConnected } returns MutableStateFlow(false)
        every { bikeRepository.isGlassSessionActive } returns MutableStateFlow(false)
        every { bikeRepository.rideInfo } returns MutableStateFlow(com.zoewave.ashbike.model.bike.BikeRideInfo.initial())
        every { bikeServiceManager.rideInfo } returns MutableStateFlow(com.zoewave.ashbike.model.bike.BikeRideInfo.initial())

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
        unmockkAll()
    }

    @Test
    fun `onEvent StartRide calls sendCommand on BikeServiceManager`() = runTest {
        every { bikeServiceManager.sendCommand(any()) } returns Unit

        viewModel.onEvent(HomeEvent.StartRide)

        coVerify { bikeServiceManager.sendCommand(com.zoewave.probase.ashbike.features.main.service.BikeForegroundService.ACTION_START_RIDE) }
    }

    @Test
    fun `onEvent StopRide calls sendCommand on BikeServiceManager`() = runTest {
        every { bikeServiceManager.sendCommand(any()) } returns Unit

        viewModel.onEvent(HomeEvent.StopRide)

        coVerify { bikeServiceManager.sendCommand(com.zoewave.probase.ashbike.features.main.service.BikeForegroundService.ACTION_STOP_RIDE) }
    }
}
