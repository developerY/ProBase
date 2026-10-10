package com.zoewave.probase.kocolor.features.starterpack.ui

import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.CosmeticItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.repository.PackSyncRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PackPreviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<StarterPackRepository>(relaxed = true)
    private val syncRepository = mockk<PackSyncRepository>(relaxed = true)

    private lateinit var viewModel: PackPreviewViewModel
    private val packId = "test_pack"

    private val item1 = CosmeticItemDto(
        id = "item1", name = "Item 1", brand = "Brand 1", macroCategory = "LIPS", microCategory = "LIPSTICK",
        colorHex = "#FFFFFF", shadeName = "Shade 1", imageUrl = "http://example.com/1.png", thumbnailUrl = "http://example.com/1_thumb.png",
        price = 10.0, notes = "Notes", formulation = null, chemistryBase = null, finish = null, coverage = null, temperature = null, volume = null,
        paoMonths = null, expiryDate = null, instructions = null, ingredients = emptyList(), allergens = emptyList(), isVegan = true, isCrueltyFree = true, fdaDataVerified = true
    )

    private val item2 = CosmeticItemDto(
        id = "item2", name = "Item 2", brand = "Brand 2", macroCategory = "EYES", microCategory = "EYESHADOW",
        colorHex = "#000000", shadeName = "Shade 2", imageUrl = "http://example.com/2.png", thumbnailUrl = "http://example.com/2_thumb.png",
        price = 20.0, notes = "Notes 2", formulation = null, chemistryBase = null, finish = null, coverage = null, temperature = null, volume = null,
        paoMonths = null, expiryDate = null, instructions = null, ingredients = emptyList(), allergens = emptyList(), isVegan = true, isCrueltyFree = true, fdaDataVerified = true
    )

    private val mockItems: List<PackItemDto> = listOf(item1, item2)

    @Before
    fun setup() {
        every { syncRepository.getInstalledPacks() } returns flowOf(emptyList())
        every { syncRepository.cartProductIds } returns flowOf(emptySet())
        every { syncRepository.ownedProductIds } returns flowOf(emptySet())
        every { syncRepository.observeAllUsages() } returns flowOf(emptyList())
        coEvery { repository.getPackItems(any()) } returns mockItems
    }

    private fun TestScope.createViewModel(targetItemId: String? = null) {
        viewModel = PackPreviewViewModel(repository, syncRepository)
        backgroundScope.launch { viewModel.uiState.collect() }
        viewModel.initialize(packId = packId, targetItemId = targetItemId, sha256 = null, publisher = null)
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `initial load logic fetches items and handles targetItemId`() = runTest {
        createViewModel(targetItemId = "item1")
        
        assertEquals(mockItems, viewModel.uiState.value.items)
        assertEquals(setOf("item1"), viewModel.uiState.value.selectedIds)
        assertEquals("item1", viewModel.uiState.value.targetItemId)
    }

    @Test
    fun `onToggleSelection updates selectedIds`() = runTest {
        createViewModel()

        viewModel.onToggleSelection("item1")
        testScheduler.advanceUntilIdle()
        assertEquals(setOf("item1"), viewModel.uiState.value.selectedIds)

        viewModel.onToggleSelection("item1")
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.selectedIds.isEmpty())
    }

    @Test
    fun `onSelectAll selects all items`() = runTest {
        createViewModel()

        viewModel.onSelectAll()
        testScheduler.advanceUntilIdle()
        assertEquals(setOf("item1", "item2"), viewModel.uiState.value.selectedIds)
    }

    @Test
    fun `onDeselectAll clears selection`() = runTest {
        createViewModel()

        viewModel.onSelectAll()
        testScheduler.advanceUntilIdle()
        viewModel.onDeselectAll()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.selectedIds.isEmpty())
    }
}
