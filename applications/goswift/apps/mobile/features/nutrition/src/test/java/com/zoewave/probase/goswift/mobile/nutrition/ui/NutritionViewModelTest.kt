package com.zoewave.probase.goswift.mobile.nutrition.ui

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.goswift.data.NutritionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionViewModelTest {

    private val nutritionRepository = mockk<NutritionRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { nutritionRepository.getNutritionRecords(any(), any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        val viewModel = NutritionViewModel(nutritionRepository)
        assertThat(viewModel.uiState.value).isEqualTo(NutritionUiState.Loading)
    }

    @Test
    fun `state updates with nutrition data`() = runTest(testDispatcher) {
        val viewModel = NutritionViewModel(nutritionRepository)
        
        val states = mutableListOf<NutritionUiState>()
        val job = launch {
            viewModel.uiState.collect { states.add(it) }
        }
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val lastState = states.last()
        assertThat(lastState).isInstanceOf(NutritionUiState.Success::class.java)
        val successState = lastState as NutritionUiState.Success
        assertThat(successState.dailyCalories).isEqualTo(0.0)
        
        job.cancel()
    }
}
