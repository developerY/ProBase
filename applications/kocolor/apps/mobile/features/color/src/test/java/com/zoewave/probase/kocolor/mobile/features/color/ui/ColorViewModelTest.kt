package com.zoewave.probase.kocolor.mobile.features.color.ui

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.data.repository.PlaylistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ColorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fashionRepository: FashionRepository = mockk(relaxed = true)
    private val playlistRepository: PlaylistRepository = mockk(relaxed = true)

    private lateinit var viewModel: ColorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val sampleAdvice = FashionAdvice(
            title = "Paris Trip",
            summary = "Autumn Palette",
            seasonalType = SeasonalType.AUTUMN,
            undertone = Undertone.WARM,
            makeupSuggestions = emptyList(),
            outfitSuggestions = emptyList(),
            recommendedPalette = emptyList()
        )
        val sampleSaved = SavedAnalysis(id = 1L, timestamp = System.currentTimeMillis(), advice = sampleAdvice)

        coEvery { fashionRepository.getAllSuggestions() } returns flowOf(listOf(sampleSaved))
        coEvery { playlistRepository.observeLatestPlaylist() } returns flowOf(null)

        viewModel = ColorViewModel(fashionRepository, playlistRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testUiStateCollectsSavedSuggestions() = runTest {
        val states = mutableListOf<ColorUiState>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }
        testScheduler.advanceUntilIdle()

        val state = states.last()
        assertThat(state.savedSuggestions).hasSize(1)
        assertThat(state.savedSuggestions.first().advice.title).isEqualTo("Paris Trip")

        job.cancel()
    }

    @Test
    fun testDeleteCollectionEvent() = runTest {
        viewModel.onEvent(ColorEvent.DeleteCollection(1L))
        testScheduler.advanceUntilIdle()

        coVerify { fashionRepository.deleteSuggestion(1L) }
    }
}
