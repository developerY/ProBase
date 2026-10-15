package com.zoewave.probase.kocolor.mobile.features.home.ui

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone
import com.zoewave.probase.kocolor.data.FashionRepository
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
class CollectionDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fashionRepository: FashionRepository = mockk(relaxed = true)

    private lateinit var viewModel: CollectionDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CollectionDetailViewModel(fashionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadAnalysis() = runTest {
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

        coEvery { fashionRepository.getSuggestionById(1L) } returns sampleSaved

        viewModel.loadAnalysis(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.analysis.value
        assertThat(state).isNotNull()
        assertThat(state?.id).isEqualTo(1L)
        assertThat(state?.advice?.title).isEqualTo("Paris Trip")
    }
}
