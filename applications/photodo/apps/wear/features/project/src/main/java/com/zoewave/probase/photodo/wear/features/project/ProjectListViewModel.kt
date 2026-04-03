package com.zoewave.probase.photodo.wear.features.project

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.photodo.data.SyncDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val syncDataStore: SyncDataStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "PhotoDoProjectListViewModel"
    private val _categoryId = savedStateHandle.getStateFlow<Long?>("categoryId", null)

    val uiState: StateFlow<ProjectListUiState> = combine(_categoryId, syncDataStore.latestSyncDataFlow) { id, categories ->
        if (id == null || categories.isEmpty()) return@combine ProjectListUiState.Empty

        val targetCategory = categories.find { it.id == id }
        if (targetCategory != null) {
            val mappedProjects = targetCategory.projects.map { syncProject ->
                val totalTasks = syncProject.tasks.size
                val completedTasks = syncProject.tasks.count { it.isCompleted }

                ProjectWearUiModel(
                    id = syncProject.id,
                    name = syncProject.name,
                    budget = syncProject.totalBudget,
                    currentSpend = syncProject.spentAmount,
                    dueDate = null, 
                    isUrgent = false, 
                    progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f,
                    hasPhoto = syncProject.hasPhoto
                )
            }
            ProjectListUiState.Success(
                categoryName = targetCategory.name,
                projects = mappedProjects
            )
        } else {
            ProjectListUiState.Empty
        }
    }
    .catch { e ->
        Log.e(TAG, "Error loading projects from DataStore", e)
        emit(ProjectListUiState.Empty)
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProjectListUiState.Loading
    )

    fun onEvent(event: ProjectListEvent) {
        when (event) {
            is ProjectListEvent.OnProjectClick -> {
                // Handled in Route for navigation
            }
        }
    }
}
