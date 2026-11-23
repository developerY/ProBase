package com.zoewave.ashbike.mobile.rides.ui

import android.app.Application
import com.zoewave.ashbike.mobile.rides.domain.MarkRideAsSyncedUseCase
import com.zoewave.ashbike.mobile.rides.domain.SyncRideUseCase
import com.zoewave.probase.ashbike.database.BikeRideRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class RidesViewModelTest {

    private lateinit var viewModel: RidesViewModel
    private val application: Application = mockk()
    private val bikeRideRepo: BikeRideRepo = mockk()
    private val syncRideUseCase: SyncRideUseCase = mockk()
    private val markRideAsSyncedUseCase: MarkRideAsSyncedUseCase = mockk()
    private val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { bikeRideRepo.getAllRidesWithLocations() } returns flowOf(emptyList())

        viewModel = RidesViewModel(
            application = application,
            bikeRideRepo = bikeRideRepo,
            syncRideUseCase = syncRideUseCase,
            markRideAsSyncedUseCase = markRideAsSyncedUseCase
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent DeleteItem calls deleteById`() = runTest {
        coEvery { bikeRideRepo.deleteById("ride_1") } returns Unit

        viewModel.onEvent(RidesEvent.DeleteItem("ride_1"))

        coVerify { bikeRideRepo.deleteById("ride_1") }
    }

    @Test
    fun `onEvent UpdateRideNotes calls updateRideNotes`() = runTest {
        coEvery { bikeRideRepo.updateRideNotes("ride_1", "Great ride!") } returns Unit

        viewModel.onEvent(RidesEvent.UpdateRideNotes("ride_1", "Great ride!"))

        coVerify { bikeRideRepo.updateRideNotes("ride_1", "Great ride!") }
    }
}
