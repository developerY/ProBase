package com.zoewave.probase.kocolor.mobile.features.color.ui

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorIntelligenceRepository
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
class ColorSearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val cosmeticRepository: CosmeticInventoryRepository = mockk(relaxed = true)
    private val wardrobeRepository: WardrobeRepository = mockk(relaxed = true)
    private val sessionRepository: FashionSessionRepository = mockk(relaxed = true)
    private val colorIntelligenceRepository: ColorIntelligenceRepository = mockk(relaxed = true)

    private lateinit var viewModel: ColorSearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { cosmeticRepository.getAllCosmetics() } returns flowOf(emptyList())
        coEvery { wardrobeRepository.getAllClothing() } returns flowOf(emptyList())

        viewModel = ColorSearchViewModel(
            cosmeticRepository, wardrobeRepository, sessionRepository, colorIntelligenceRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialUiState() = runTest {
        val states = mutableListOf<ColorSearchUiState>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }
        testScheduler.advanceUntilIdle()

        val state = states.last()
        assertThat(state.selectedColorHex).isEqualTo("#C25C4A")
        assertThat(state.searchMode).isEqualTo(SearchMode.EXACT)
        assertThat(state.matchedCosmetics).isEmpty()

        job.cancel()
    }

    @Test
    fun testUpdateColorEvent() = runTest {
        val states = mutableListOf<ColorSearchUiState>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }

        viewModel.onEvent(ColorSearchEvent.SelectColor("#000000"))
        testScheduler.advanceUntilIdle()

        val state = states.last()
        assertThat(state.selectedColorHex).isEqualTo("#000000")

        job.cancel()
    }
}
