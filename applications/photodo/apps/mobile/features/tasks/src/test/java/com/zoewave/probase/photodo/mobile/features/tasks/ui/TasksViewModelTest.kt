package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.CategoryWithProjectsAndTasks
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectWithTasks
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: PhotoDoRepo
    private lateinit var viewModel: TasksViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        every { repo.getCategoriesWithProjectsAndTasks() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        viewModel = TasksViewModel(repo, SavedStateHandle())
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun `when categories exist, first category is selected by default`() = runTest {
        val category1 = CategoryEntity(categoryId = 1, name = "Cat 1")
        val category2 = CategoryEntity(categoryId = 2, name = "Cat 2")
        val data = listOf(
            CategoryWithProjectsAndTasks(category1, emptyList()),
            CategoryWithProjectsAndTasks(category2, emptyList())
        )
        
        every { repo.getCategoriesWithProjectsAndTasks() } returns flowOf(data)
        
        viewModel = TasksViewModel(repo, SavedStateHandle())
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.categoryName).isEqualTo("Cat 1")
        assertThat(state.categoryId).isEqualTo(1L)
    }

    @Test
    fun `when categoryId is requested, that category is selected`() = runTest {
        val category1 = CategoryEntity(categoryId = 1, name = "Cat 1")
        val category2 = CategoryEntity(categoryId = 2, name = "Cat 2")
        val data = listOf(
            CategoryWithProjectsAndTasks(category1, emptyList()),
            CategoryWithProjectsAndTasks(category2, emptyList())
        )
        
        every { repo.getCategoriesWithProjectsAndTasks() } returns flowOf(data)
        
        viewModel = TasksViewModel(repo, SavedStateHandle(mapOf("categoryId" to 2L)))
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertThat(state.categoryName).isEqualTo("Cat 2")
        assertThat(state.categoryId).isEqualTo(2L)
    }
}
