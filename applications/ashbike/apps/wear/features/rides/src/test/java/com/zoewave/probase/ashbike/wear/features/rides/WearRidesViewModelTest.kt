package com.zoewave.probase.ashbike.wear.features.rides

import com.zoewave.ashbike.model.bike.BikeRide
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
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WearRidesViewModelTest {

    private lateinit var viewModel: RidesViewModel
    private val repo: BikeRideRepo = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { repo.getAllRides() } returns flowOf(emptyList())

        viewModel = RidesViewModel(repo)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent OnDeleteClick deletes ride`() = runTest {
        coEvery { repo.deleteById("ride1") } returns Unit

        viewModel.onEvent(RidesEvent.OnDeleteClick("ride1"))

        coVerify { repo.deleteById("ride1") }
    }
}
