package com.zoewave.ashbike.mobile.glass.ui

import com.zoewave.ashbike.data.repository.bike.BikeRepository
import com.zoewave.ashbike.model.bike.BikeRideInfo
import com.zoewave.ashbike.model.bike.SuspensionState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class GlassViewModelTest {

    private lateinit var viewModel: GlassViewModel
    private val bikeRepository: BikeRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { bikeRepository.currentGear } returns MutableStateFlow(1)
        every { bikeRepository.suspensionState } returns MutableStateFlow(SuspensionState.OPEN)
        every { bikeRepository.rideInfo } returns MutableStateFlow(BikeRideInfo.initial())

        viewModel = GlassViewModel(bikeRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent SelectGear calls setGear on repository`() = runTest {
        coEvery { bikeRepository.setGear(5) } returns Unit

        viewModel.onEvent(GlassUiEvent.SelectGear(5))

        coVerify { bikeRepository.setGear(5) }
    }

    @Test
    fun `onEvent ChangeScreen updates internal screen state mapped to uiState`() = runTest {
        viewModel.onEvent(GlassUiEvent.ChangeScreen(ScreenState.GEAR_LIST))

        val state = viewModel.uiState.first()
        assertEquals(ScreenState.GEAR_LIST, state.currentScreen)
    }
}
