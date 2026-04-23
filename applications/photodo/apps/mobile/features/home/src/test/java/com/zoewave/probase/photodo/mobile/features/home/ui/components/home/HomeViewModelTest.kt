package com.zoewave.probase.photodo.mobile.features.home.ui.components.home

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.CategoryWithProjectsAndTasks
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectWithTasks
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import io.mockk.coEvery
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var photoDoRepo: PhotoDoRepo
    private lateinit var appSettingsRepository: AppSettingsRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        photoDoRepo = mockk()
        appSettingsRepository = mockk()

        every { photoDoRepo.getCategoriesWithProjectsAndTasks() } returns flowOf(emptyList())
        every { appSettingsRepository.isAiEnabledFlow } returns flowOf(false)
        every { appSettingsRepository.animationsEnabledFlow } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        viewModel = HomeViewModel(photoDoRepo, appSettingsRepository)
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun `when data is loaded, state is updated`() = runTest {
        val category = CategoryEntity(categoryId = 1, name = "Work")
        val project = ProjectEntity(projectId = 1, categoryId = 1, name = "Project 1", isUrgent = true)
        val task = TaskEntity(taskId = 1, projectId = 1, text = "Task 1", isChecked = true)
        
        val projectWithTasks = ProjectWithTasks(project, listOf(task))
        val categoryData = CategoryWithProjectsAndTasks(category, listOf(projectWithTasks))
        
        every { photoDoRepo.getCategoriesWithProjectsAndTasks() } returns flowOf(listOf(categoryData))
        
        viewModel = HomeViewModel(photoDoRepo, appSettingsRepository)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.categories).hasSize(1)
        assertThat(state.categories[0].name).isEqualTo("Work")
        assertThat(state.categories[0].progressPercentage).isEqualTo(1.0f)
        assertThat(state.urgentProjects).hasSize(1)
        assertThat(state.urgentProjects[0].title).isEqualTo("Project 1")
    }

    @Test
    fun `when category search query changes, state is updated`() = runTest {
        viewModel = HomeViewModel(photoDoRepo, appSettingsRepository)
        
        viewModel.onEvent(HomeEvent.OnCategorySearchQueryChanged("test"))
        
        assertThat(viewModel.uiState.value.categorySearchQuery).isEqualTo("test")
    }
}
