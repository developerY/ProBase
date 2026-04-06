package com.zoewave.probase.goswift.mobile.home.ui

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.goswift.data.HealthRepository
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
class HomeViewModelTest {

    private val repository = mockk<ShotRepository>()
    private val healthRepository = mockk<HealthRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllShots() } returns flowOf(emptyList())
        coEvery { healthRepository.getSleepSessions(any(), any()) } returns emptyList()
        coEvery { healthRepository.getExerciseSessions(any(), any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(repository, healthRepository)
        assertThat(viewModel.uiState.value).isEqualTo(HomeUiState.Loading)
    }

    @Test
    fun `state updates with health data`() = runTest(testDispatcher) {
        val mockSleep = mockk<SleepSessionRecord>()
        every { mockSleep.startTime } returns Instant.now().minusSeconds(3600 * 8)
        every { mockSleep.endTime } returns Instant.now()
        
        coEvery { healthRepository.getSleepSessions(any(), any()) } returns listOf(mockSleep)
        
        val viewModel = HomeViewModel(repository, healthRepository)
        
        val states = mutableListOf<HomeUiState>()
        val job = launch {
            viewModel.uiState.collect { states.add(it) }
        }
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val lastState = states.last()
        assertThat(lastState).isInstanceOf(HomeUiState.Success::class.java)
        val successState = lastState as HomeUiState.Success
        assertThat(successState.sleepDuration).isEqualTo("8h 0m")
        
        job.cancel()
    }
}
