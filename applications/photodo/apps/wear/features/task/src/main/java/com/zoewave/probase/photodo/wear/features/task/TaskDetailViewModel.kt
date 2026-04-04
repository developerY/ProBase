package com.zoewave.probase.photodo.wear.features.task

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.photodo.data.SyncDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val syncDataStore: SyncDataStore,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "PhotoDoTaskDetailViewModel"
    private val _projectId = savedStateHandle.getStateFlow<Long?>("projectId", null)

    fun setProjectId(projectId: Long) {
        savedStateHandle["projectId"] = projectId
    }

    val uiState: StateFlow<TaskDetailUiState> = combine(_projectId, syncDataStore.latestSyncDataFlow) { id, categories ->
        if (id == null || categories.isEmpty()) return@combine TaskDetailUiState.Empty

        // Deep search for the project
        var foundProject: com.zoewave.probase.photodo.model.sync.SyncProject? = null
        for (category in categories) {
            foundProject = category.projects.find { it.id == id }
            if (foundProject != null) break
        }

        if (foundProject != null) {
            TaskDetailUiState.Success(
                projectId = id,
                projectTitle = foundProject.name,
                tasks = foundProject.tasks.map { syncTask ->
                    TaskEntity(
                        taskId = syncTask.id,
                        projectId = id,
                        text = syncTask.title,
                        isChecked = syncTask.isCompleted
                    )
                },
                photos = emptyList(), // Not syncing photos directly
                photoCount = foundProject.photoCount,
                hasPhoto = foundProject.hasPhoto
            )
        } else {
            TaskDetailUiState.Empty
        }
    }
    .catch { e ->
        Log.e(TAG, "Error loading task details from DataStore", e)
        emit(TaskDetailUiState.Empty)
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskDetailUiState.Loading
    )

    fun onEvent(event: TaskDetailEvent) {
        // Handle events here
    }
}
