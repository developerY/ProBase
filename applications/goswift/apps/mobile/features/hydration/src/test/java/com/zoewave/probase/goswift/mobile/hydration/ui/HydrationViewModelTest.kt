package com.zoewave.probase.goswift.mobile.hydration.ui

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.goswift.data.HealthRepository
import com.zoewave.probase.goswift.data.HydrationRepository
import com.zoewave.probase.goswift.data.ShotRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HydrationViewModelTest {

    private val hydrationRepository = mockk<HydrationRepository>()
    private val shotRepository = mockk<ShotRepository>()
    private val healthRepository = mockk<HealthRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { hydrationRepository.getHydrationRecords(any(), any()) } returns emptyList()
        every { shotRepository.getAllShots() } returns flowOf(emptyList())
        coEvery { healthRepository.getExerciseSessions(any(), any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        val viewModel = HydrationViewModel(hydrationRepository, shotRepository, healthRepository)
        assertThat(viewModel.uiState.value).isEqualTo(HydrationUiState.Loading)
    }

    @Test
    fun `state updates with hydration data`() = runTest(testDispatcher) {
        val viewModel = HydrationViewModel(hydrationRepository, shotRepository, healthRepository)
        
        val states = mutableListOf<HydrationUiState>()
        val job = launch {
            viewModel.uiState.collect { states.add(it) }
        }
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val lastState = states.last()
        assertThat(lastState).isInstanceOf(HydrationUiState.Success::class.java)
        val successState = lastState as HydrationUiState.Success
        assertThat(successState.dailyTotalLiters).isEqualTo(0.0)
        assertThat(successState.targetLiters).isEqualTo(2.0)
        
        job.cancel()
    }
}
