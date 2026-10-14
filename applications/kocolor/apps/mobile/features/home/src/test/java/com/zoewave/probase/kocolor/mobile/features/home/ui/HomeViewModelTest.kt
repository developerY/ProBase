package com.zoewave.probase.kocolor.mobile.features.home.ui

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.weather.AtmosphericRepository
import com.zoewave.probase.core.model.weather.AtmosphericState
import com.zoewave.probase.features.health.core.domain.GetActiveRitualUseCase
import com.zoewave.probase.features.health.core.domain.GetHealthSummaryUseCase
import com.zoewave.probase.features.health.core.domain.HealthSummary
import com.zoewave.probase.features.health.core.domain.LogHydrationUseCase
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.db.KoColorSettings
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fashionRepository: FashionRepository = mockk(relaxed = true)
    private val routineDao: RoutineDao = mockk(relaxed = true)
    private val cosmeticDao: CosmeticDao = mockk(relaxed = true)
    private val clothingDao: ClothingDao = mockk(relaxed = true)
    private val getHealthSummaryUseCase: GetHealthSummaryUseCase = mockk(relaxed = true)
    private val getActiveRitualUseCase: GetActiveRitualUseCase = mockk(relaxed = true)
    private val logHydrationUseCase: LogHydrationUseCase = mockk(relaxed = true)
    private val atmosphericRepository: AtmosphericRepository = mockk(relaxed = true)
    private val koColorSettings: KoColorSettings = mockk(relaxed = true)
    private val starterPackRepository: StarterPackRepository = mockk(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0

        coEvery { fashionRepository.getProfile() } returns flowOf(null)
        coEvery { koColorSettings.hydrationGoalFlow } returns flowOf(2.5)
        coEvery { koColorSettings.temperatureUnitFlow } returns flowOf("CELSIUS")
        every { routineDao.getRoutinesForDay(any(), any()) } returns flowOf(emptyList())
        every { cosmeticDao.getAllCosmetics() } returns flowOf(emptyList())
        every { clothingDao.getAllClothing() } returns flowOf(emptyList())
        every { atmosphericRepository.atmosphericState } returns MutableStateFlow(AtmosphericState())
        every { getHealthSummaryUseCase() } returns flowOf(HealthSummary(hasPermissions = true))
        coEvery { fashionRepository.getAllSuggestions() } returns flowOf(emptyList())

        viewModel = HomeViewModel(
            fashionRepository,
            routineDao,
            cosmeticDao,
            clothingDao,
            getHealthSummaryUseCase,
            getActiveRitualUseCase,
            logHydrationUseCase,
            atmosphericRepository,
            koColorSettings,
            starterPackRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testHomeUiStateEmission() = runTest {
        val states = mutableListOf<HomeUiState>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }
        testScheduler.advanceUntilIdle()

        val state = states.last()
        assertThat(state.hydrationGoalLiters).isEqualTo(2.5)
        assertThat(state.popularCosmetics).isEmpty()
        assertThat(state.popularClothing).isEmpty()

        job.cancel()
    }

    @Test
    fun testLogHydrationAction() = runTest {
        viewModel.onEvent(HomeEvent.LogHydration(0.25))
        testScheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value).isNotNull()
    }
}
