package com.zoewave.probase.goswift.mobile.shots.ui

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.goswift.data.ShotRepository
import com.zoewave.probase.goswift.model.CaffeineShot
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class ShotsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ShotRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `todayGroup excludes shots from previous days and formats dateLabel dynamically`() = runTest(testDispatcher) {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val todayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli() + 1000L
        val yesterdayMillis = today.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() + 1000L

        val shots = listOf(
            CaffeineShot(id = "1", mg = 20, timestamp = todayMillis),
            CaffeineShot(id = "2", mg = 50, timestamp = yesterdayMillis)
        )

        coEvery { repository.getAllShots() } returns flowOf(shots)

        val viewModel = ShotsViewModel(repository)
        val states = mutableListOf<ShotsUiState>()
        val job = CoroutineScope(testDispatcher).launch {
            viewModel.uiState.collect { states.add(it) }
        }

        testDispatcher.scheduler.advanceUntilIdle()

        val lastState = states.last() as ShotsUiState.Success
        val monthDayFormatter = DateTimeFormatter.ofPattern("MMM dd")
        val expectedLabel = "Today, ${today.format(monthDayFormatter)}"

        assertThat(lastState.todayGroup.dateLabel).isEqualTo(expectedLabel)
        assertThat(lastState.todayGroup.doseCount).isEqualTo(1)
        assertThat(lastState.todayGroup.items.size).isEqualTo(1)
        assertThat(lastState.todayGroup.items.first().id).isEqualTo("1")

        job.cancel()
    }
}
