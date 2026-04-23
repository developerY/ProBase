package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.applications.photodo.db.entity.ProjectDetails
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class TaskDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var photoDoRepo: PhotoDoRepo
    private lateinit var appSettingsRepository: AppSettingsRepository
    private lateinit var viewModel: TaskDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        photoDoRepo = mockk()
        appSettingsRepository = mockk()

        every { photoDoRepo.getProjectDetails(any()) } returns flowOf(null)
        every { appSettingsRepository.isAiEnabledFlow } returns flowOf(false)
        every { appSettingsRepository.animationsEnabledFlow } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when expense is added, project spent amount is updated`() = runTest {
        val projectId = 1L
        val initialProject = ProjectEntity(projectId = projectId, categoryId = 1, name = "Test", currentSpend = 100.0)
        val projectDetails = ProjectDetails(project = initialProject, tasks = emptyList(), photos = emptyList(), expenses = emptyList())
        
        every { photoDoRepo.getProjectDetails(projectId) } returns flowOf(projectDetails)
        coEvery { photoDoRepo.upsertExpense(any()) } returns 1L
        coEvery { photoDoRepo.updateProject(any()) } returns Unit
        
        viewModel = TaskDetailViewModel(photoDoRepo, appSettingsRepository, SavedStateHandle(mapOf("projectId" to projectId)))
        advanceUntilIdle()
        
        viewModel.onEvent(TaskDetailEvent.OnAddExpenseClicked("New Expense", 50.0))
        advanceUntilIdle()
        
        coVerify {
            photoDoRepo.updateProject(match { it.currentSpend == 150.0 }) 
        }
    }
}
