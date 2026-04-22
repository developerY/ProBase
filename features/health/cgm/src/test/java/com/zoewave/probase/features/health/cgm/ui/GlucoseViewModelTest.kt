package com.zoewave.probase.features.health.cgm.ui

import com.zoewave.probase.features.health.cgm.data.di.GlucoseRepositoryFactory
import com.zoewave.probase.features.health.cgm.data.repository.GlucoseRepository
import com.zoewave.probase.core.model.health.GlucoseReading
import com.zoewave.probase.core.model.health.GlucoseSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class GlucoseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var factory: GlucoseRepositoryFactory
    private lateinit var viewModel: GlucoseViewModel
    private lateinit var mockRepo: GlucoseRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        factory = mockk()
        mockRepo = mockk()
        
        val reading = GlucoseReading(120f, Instant.now(), GlucoseSource.SIMULATOR)
        every { mockRepo.glucoseReadings } returns flowOf(reading)
        every { factory.create(any()) } returns mockRepo
        
        viewModel = GlucoseViewModel(factory)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is simulator`() = runTest {
        assertEquals(GlucoseSource.SIMULATOR, viewModel.selectedSource.value)
    }

    @Test
    fun `switching source updates selectedSource and latestReading`() = runTest {
        val newReading = GlucoseReading(140f, Instant.now(), GlucoseSource.DEXCOM_SHARE)
        val dexcomRepo = mockk<GlucoseRepository>()
        every { dexcomRepo.glucoseReadings } returns flowOf(newReading)
        every { factory.create(GlucoseSource.DEXCOM_SHARE) } returns dexcomRepo

        viewModel.switchSource(GlucoseSource.DEXCOM_SHARE)
        advanceUntilIdle()

        assertEquals(GlucoseSource.DEXCOM_SHARE, viewModel.selectedSource.value)
        assertEquals(140f, viewModel.latestReading.value?.valueMgDl)
    }
}
