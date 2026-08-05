package com.zoewave.probase.kocolor.features.starterpack.ui

import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.SearchIndexEntry
import com.zoewave.probase.kocolor.features.starterpack.data.repository.PackSyncRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.clearInvocations
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class StarterPackViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var repository: StarterPackRepository

    @Mock
    lateinit var syncRepository: PackSyncRepository

    private lateinit var viewModel: StarterPackViewModel

    private val mockSearchIndex = listOf(
        SearchIndexEntry(id = "1", term = "Foundation", brand = "BrandA", packId = "p1"),
        SearchIndexEntry(id = "2", term = "Lipstick", brand = "BrandB", packId = "p2"),
        SearchIndexEntry(id = "3", term = "Concealer", brand = "BrandA", packId = "p3")
    )

    @Before
    fun setup() = runTest {
        MockitoAnnotations.openMocks(this@StarterPackViewModelTest)
        `when`(repository.getSearchIndex()).thenReturn(mockSearchIndex)
        `when`(syncRepository.getInstalledPacks()).thenReturn(flowOf(emptyList()))
        `when`(syncRepository.fetchManifest()).thenReturn(Result.success(emptyList()))

        viewModel = StarterPackViewModel(repository, syncRepository)
        // Wait for init blocks
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        
        // Clear invocations from init
        clearInvocations(syncRepository)
    }

    @Test
    fun `RefreshManifest event triggers manifest fetch`() = runTest {
        viewModel.onEvent(StarterPackEvent.RefreshManifest)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        verify(syncRepository).fetchManifest()
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
        assertEquals("BrandA", filtered[0].brand)
        assertEquals("BrandA", filtered[1].brand)
    }

    @Test
    fun `search filtering by term works correctly`() = runTest {
        backgroundScope.launch { viewModel.filteredSearchIndex.collect() }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(StarterPackEvent.SearchQueryChanged("Lipstick"))
        advanceTimeBy(301L)

        val filtered = viewModel.filteredSearchIndex.value
        assertEquals(1, filtered.size)
        assertEquals("Lipstick", filtered[0].term)
    }
}
