package com.zoewave.probase.photodo.wear.features.project

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo
) : ViewModel() {

    private val TAG = "PhotoDoProjectListViewModel"
    private val _categoryId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<ProjectListUiState> = _categoryId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest MutableStateFlow(ProjectListUiState.Empty)

            photoDoRepo.getCategoriesWithProjectsAndTasks()
                .map { allData ->
                    val targetData = allData.find { it.category.categoryId == id }
                    if (targetData != null) {
                        val mappedProjects = targetData.projects.map { projectWithTasks ->
                            val project = projectWithTasks.project
                            val tasks = projectWithTasks.tasks
                            val totalTasks = tasks.size
                            val completedTasks = tasks.count { it.isChecked }

                            ProjectWearUiModel(
                                id = project.projectId,
                                name = project.name,
                                budget = project.projectBudget,
                                currentSpend = project.currentSpend,
                                dueDate = project.dueDate,
                                isUrgent = project.isUrgent,
                                progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
                            )
                        }
                        ProjectListUiState.Success(
                            categoryName = targetData.category.name,
                            projects = mappedProjects
                        )
                    } else {
                        ProjectListUiState.Empty
                    }
                }
        }
        .catch { e ->
            Log.e(TAG, "Error loading projects", e)
            emit(ProjectListUiState.Empty)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProjectListUiState.Loading
        )

    fun setCategoryId(id: Long?) {
        _categoryId.value = id
    }
}
