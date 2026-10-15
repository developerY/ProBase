package com.zoewave.probase.kocolor.mobile.features.color.ui.hub

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.kocolor.features.colors.domain.model.StylistEdit
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
class ColorHubViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val colorIntelligenceRepository: ColorIntelligenceRepository = mockk(relaxed = true)

    private lateinit var viewModel: ColorHubViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val sampleEdit = StylistEdit(primaryInsight = "Needs warmth", recommendation = "Add red", anchorColors = listOf("#FF0000"))

        coEvery { colorIntelligenceRepository.getAllInventoryColors() } returns flowOf(emptyList())
        coEvery { colorIntelligenceRepository.getPaletteGaps(any()) } returns flowOf(listOf("#FF0000"))
        coEvery { colorIntelligenceRepository.getStylistEdit(any()) } returns flowOf(sampleEdit)

        viewModel = ColorHubViewModel(colorIntelligenceRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testUiStateCollectsData() = runTest {
        val states = mutableListOf<ColorHubUiState>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }
        testScheduler.advanceUntilIdle()

        val state = states.last()
        assertThat(state.inventoryColors).isEmpty()
        assertThat(state.paletteGaps).containsExactly("#FF0000")
        assertThat(state.stylistEdit?.primaryInsight).isEqualTo("Needs warmth")

        job.cancel()
    }
}
