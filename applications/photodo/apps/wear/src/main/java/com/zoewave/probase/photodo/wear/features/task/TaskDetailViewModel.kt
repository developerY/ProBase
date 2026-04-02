package com.zoewave.probase.photodo.wear.features.task

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo
) : ViewModel() {

    private val TAG = "PhotoDoTaskDetailViewModel"
    private val _projectId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<TaskDetailUiState> = _projectId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest MutableStateFlow(TaskDetailUiState.Empty)

            photoDoRepo.getProjectDetails(id)
                .map { details ->
                    if (details != null) {
                        TaskDetailUiState.Success(
                            projectTitle = details.project.name,
                            tasks = details.tasks,
                            photos = details.photos
                        )
                    } else {
                        TaskDetailUiState.Empty
                    }
                }
        }
        .catch { e ->
            Log.e(TAG, "Error loading task details", e)
            emit(TaskDetailUiState.Empty)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskDetailUiState.Loading
        )

    fun setProjectId(id: Long?) {
        _projectId.value = id
    }

    fun onToggleTask(task: TaskEntity, isChecked: Boolean) {
        viewModelScope.launch {
            photoDoRepo.updateTask(task.copy(isChecked = isChecked))
        }
    }
}
