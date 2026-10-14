package com.zoewave.probase.kocolor.features.seasonal_trends.ui

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorIntelligenceRepository
import com.zoewave.probase.kocolor.features.seasonal_trends.domain.GeminiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeasonalTrendsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val colorRepository: ColorIntelligenceRepository = mockk(relaxed = true)
    private val geminiService: GeminiService = mockk(relaxed = true)

    private lateinit var viewModel: SeasonalTrendsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SeasonalTrendsViewModel(colorRepository, geminiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateIsIdle() {
        assertThat(viewModel.uiState.value).isInstanceOf(SeasonalTrendsUiState.Idle::class.java)
    }

    @Test
    fun testFetchSeasonalTrendsSuccess() = runTest {
        coEvery { geminiService.generateContent(any()) } returns "Deep Plum velvet is your ideal shade."

        viewModel.fetchSeasonalTrends("AUTUMN", "WARM")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(SeasonalTrendsUiState.Success::class.java)
        assertThat((state as SeasonalTrendsUiState.Success).editorial).contains("Deep Plum velvet")
    }

    @Test
    fun testFetchSeasonalTrendsError() = runTest {
        coEvery { geminiService.generateContent(any()) } throws IllegalStateException("API Key missing")

        viewModel.fetchSeasonalTrends("AUTUMN", "WARM")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(SeasonalTrendsUiState.Error::class.java)
        assertThat((state as SeasonalTrendsUiState.Error).message).contains("API Key missing")
    }
}
