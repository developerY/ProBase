package com.zoewave.probase.ashbike.wear.features.settings

import com.zoewave.probase.ashbike.database.repository.AppSettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private val appRepo: AppSettingsRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { appRepo.unitsFlow } returns flowOf("Metric (SI)")

        viewModel = SettingsViewModel(appRepo)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent ToggleMetricUnits updates repository`() = runTest {
        coEvery { appRepo.setUnits("Metric (SI)") } returns Unit

        viewModel.onEvent(SettingsEvent.ToggleMetricUnits(true))

        coVerify { appRepo.setUnits("Metric (SI)") }
    }

    @Test
    fun `onEvent ToggleMetricUnits to imperial updates repository`() = runTest {
        coEvery { appRepo.setUnits("Imperial") } returns Unit

        viewModel.onEvent(SettingsEvent.ToggleMetricUnits(false))

        coVerify { appRepo.setUnits("Imperial") }
    }
}
