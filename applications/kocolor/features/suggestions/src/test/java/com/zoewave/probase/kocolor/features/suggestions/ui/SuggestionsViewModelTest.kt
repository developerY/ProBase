package com.zoewave.probase.kocolor.features.suggestions.ui

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.features.suggestions.data.SuggestionEngine
import io.mockk.coEvery
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
class SuggestionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val suggestionEngine: SuggestionEngine = mockk(relaxed = true)
    private val fashionRepository: FashionRepository = mockk(relaxed = true)
    private val aiSettings: AiConfigurationSettings = mockk(relaxed = true)

    private lateinit var viewModel: SuggestionsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val sampleProfile = FashionProfile(seasonalType = SeasonalType.SUMMER, undertone = Undertone.COOL)
        coEvery { fashionRepository.getProfile() } returns flowOf(sampleProfile)
        coEvery { aiSettings.getGeminiApiKey() } returns "valid-api-key"
        coEvery { aiSettings.aiModelFlow } returns flowOf("gemini-3.8-flash")

        viewModel = SuggestionsViewModel(suggestionEngine, fashionRepository, aiSettings)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testUiStateCollectsProfile() = runTest {
        val states = mutableListOf<SuggestionsScreenUiState>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }
        testScheduler.advanceUntilIdle()

        val state = states.last()
        assertThat(state.fashionProfile).isNotNull()
        assertThat(state.fashionProfile?.seasonalType).isEqualTo(SeasonalType.SUMMER)

        job.cancel()
    }

    @Test
    fun testGetSuggestionsSuccess() = runTest {
        val states = mutableListOf<SuggestionsScreenUiState>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }
        testScheduler.advanceUntilIdle()

        val sampleAdvice = FashionAdvice(
            summary = "Summer Look",
            seasonalType = SeasonalType.SUMMER,
            undertone = Undertone.COOL,
            makeupSuggestions = emptyList(),
            outfitSuggestions = emptyList(),
            recommendedPalette = emptyList()
        )

        coEvery { suggestionEngine.getPersonalizedAdvice(any(), any(), any(), any()) } returns sampleAdvice

        viewModel.getSuggestions("Evening Style")
        testScheduler.advanceUntilIdle()

        val state = states.last()
        assertThat(state.loadingState).isInstanceOf(SuggestionsLoadingState.Success::class.java)

        job.cancel()
    }
}
