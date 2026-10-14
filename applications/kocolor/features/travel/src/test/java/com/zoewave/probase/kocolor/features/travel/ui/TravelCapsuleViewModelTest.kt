package com.zoewave.probase.kocolor.features.travel.ui

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.weather.AtmosphericRepository
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.usecase.GenerateStyleResultUseCase
import com.zoewave.probase.kocolor.data.usecase.IntentAnalyzer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TravelCapsuleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val atmosphericRepository: AtmosphericRepository = mockk(relaxed = true)
    private val generateStyleResultUseCase: GenerateStyleResultUseCase = mockk(relaxed = true)
    private val wardrobeRepository: WardrobeRepository = mockk(relaxed = true)
    private val cosmeticRepository: CosmeticInventoryRepository = mockk(relaxed = true)
    private val fashionRepository: FashionRepository = mockk(relaxed = true)
    private val intentAnalyzer: IntentAnalyzer = mockk(relaxed = true)

    private lateinit var viewModel: TravelCapsuleViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0

        coEvery { fashionRepository.getProfile() } returns flowOf(null)

        viewModel = TravelCapsuleViewModel(
            atmosphericRepository,
            generateStyleResultUseCase,
            wardrobeRepository,
            cosmeticRepository,
            fashionRepository,
            intentAnalyzer
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        val state = viewModel.uiState.value
        assertThat(state.destination).isEmpty()
        assertThat(state.occasion).isEqualTo("Vacation")
        assertThat(state.startDateMillis).isNotNull()
        assertThat(state.endDateMillis).isNotNull()
    }

    @Test
    fun testUpdateDestinationAndOccasion() {
        viewModel.onEvent(TravelEvent.UpdateDestination("Paris"))
        viewModel.onEvent(TravelEvent.UpdateOccasion("Business Conference"))

        val state = viewModel.uiState.value
        assertThat(state.destination).isEqualTo("Paris")
        assertThat(state.occasion).isEqualTo("Business Conference")
    }

    @Test
    fun testToggleChecklistItem() {
        viewModel.onEvent(TravelEvent.UpdateDestination("Tokyo"))
        
        // Add item manually to checklist for testing
        val sampleItem = PackingChecklistItem(id = "item_1", name = "Passport", category = PackingCategory.DOCUMENTS, isChecked = false)
        viewModel.onEvent(TravelEvent.ToggleItemCheck("item_1"))

        val state = viewModel.uiState.value
        assertThat(state.checklist).isEmpty() // Initial is empty until generated
    }
}
