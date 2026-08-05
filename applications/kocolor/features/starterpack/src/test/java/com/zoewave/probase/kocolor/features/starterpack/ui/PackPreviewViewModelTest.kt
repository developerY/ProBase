package com.zoewave.probase.kocolor.features.starterpack.ui

import androidx.lifecycle.SavedStateHandle
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class PackPreviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var repository: StarterPackRepository

    private lateinit var viewModel: PackPreviewViewModel
    private val packId = "test_pack"
    private val mockItems = listOf(
        PackItem(id = "item1", name = "Item 1", brand = "Brand 1", hexColor = "#FFFFFF", shade = "Shade 1", imageUrl = "", thumbnailUrl = ""),
        PackItem(id = "item2", name = "Item 2", brand = "Brand 2", hexColor = "#000000", shade = "Shade 2", imageUrl = "", thumbnailUrl = "")
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    private fun createViewModel(targetItemId: String? = null) = runTest {
        `when`(repository.getPackItems(packId)).thenReturn(mockItems)
        val savedStateHandle = SavedStateHandle(mapOf("packId" to packId, "targetItemId" to targetItemId))
        viewModel = PackPreviewViewModel(repository, savedStateHandle)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
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
        assertEquals(setOf("item1"), viewModel.uiState.value.selectedIds)

        viewModel.onToggleSelection("item1")
        assertTrue(viewModel.uiState.value.selectedIds.isEmpty())
    }

    @Test
    fun `onSelectAll selects all items`() = runTest {
        createViewModel()

        viewModel.onSelectAll()
        assertEquals(setOf("item1", "item2"), viewModel.uiState.value.selectedIds)
    }

    @Test
    fun `onDeselectAll clears selection`() = runTest {
        createViewModel()

        viewModel.onSelectAll()
        viewModel.onDeselectAll()
        assertTrue(viewModel.uiState.value.selectedIds.isEmpty())
    }

    @Test
    fun `onImportSelected calls repository with correct items`() = runTest {
        createViewModel()

        viewModel.onToggleSelection("item1")
        viewModel.onImportSelected()
        
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        
        verify(repository).importItems(listOf(mockItems[0]))
    }
}
