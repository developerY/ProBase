package com.zoewave.probase.kocolor.features.starterpack.ui

import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.repository.PackSyncRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StarterPackViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<StarterPackRepository>(relaxed = true)
    private val syncRepository = mockk<PackSyncRepository>(relaxed = true)

    private lateinit var viewModel: StarterPackViewModel

    private val mockSearchIndex: Map<String, List<String>> = mapOf(
        "1" to listOf("Foundation", "BrandA", "p1"),
        "2" to listOf("Lipstick", "BrandB", "p2"),
        "3" to listOf("Concealer", "BrandA", "p3")
    )

    @Before
    fun setup() = runTest {
        coEvery { repository.getSearchIndex() } returns mockSearchIndex
        every { syncRepository.getInstalledPacks() } returns flowOf(emptyList())
        coEvery { syncRepository.fetchManifest() } returns Result.success(emptyList())

        viewModel = StarterPackViewModel(repository, syncRepository)
        // Wait for init blocks
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `RefreshManifest event triggers manifest fetch`() = runTest {
        viewModel.onEvent(StarterPackEvent.RefreshManifest)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        coVerify { syncRepository.fetchManifest() }
    }

    @Test
    fun `search filtering logic with debounce works correctly`() = runTest {
        // Start collecting the flow to make it active
        backgroundScope.launch { viewModel.filteredSearchIndex.collect() }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Wait for initial debounce
        advanceTimeBy(301L)
        
        // Change query
        viewModel.onEvent(StarterPackEvent.SearchQueryChanged("BrandA"))
        
        // Wait for debounce
        advanceTimeBy(301L)
        
        val filtered = viewModel.filteredSearchIndex.value
        assertEquals(2, filtered.size)
        assertEquals(listOf("Foundation", "BrandA", "p1"), filtered["1"])
        assertEquals(listOf("Concealer", "BrandA", "p3"), filtered["3"])
    }

    @Test
    fun `search filtering by term works correctly`() = runTest {
        backgroundScope.launch { viewModel.filteredSearchIndex.collect() }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(StarterPackEvent.SearchQueryChanged("Lipstick"))
        advanceTimeBy(301L)

        val filtered = viewModel.filteredSearchIndex.value
        assertEquals(1, filtered.size)
        assertEquals(listOf("Lipstick", "BrandB", "p2"), filtered["2"])
    }
}
