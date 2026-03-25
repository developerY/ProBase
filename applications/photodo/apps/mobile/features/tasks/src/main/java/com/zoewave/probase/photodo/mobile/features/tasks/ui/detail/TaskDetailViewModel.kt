package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PhotoDoDetailViewModel"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo
) : ViewModel() {

    private val _projectId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<TaskDetailUiState> = _projectId
        .filterNotNull()
        .flatMapLatest { id ->
            photoDoRepo.getProjectDetails(id)
                .map { projectDetails ->
                    if (projectDetails != null) {
                        TaskDetailUiState(loadState = DetailLoadState.Success(projectDetails))
                    } else {
                        TaskDetailUiState(loadState = DetailLoadState.Error("Project has been deleted."))
                    }
                }
                .catch { e ->
                    Log.e(TAG, "Error loading project details", e)
                    emit(TaskDetailUiState(loadState = DetailLoadState.Error(e.message ?: "Unknown error")))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskDetailUiState(loadState = DetailLoadState.Loading)
        )

    /**
     * Called by the UI Route immediately upon creation to kick off the flow.
     */
    fun loadTaskDetails(id: Long) {
        if (id == 0L) return
        if (_projectId.value == id) return

        Log.d(TAG, "Loading details for projectId: $id")
        _projectId.value = id
    }

    fun onEvent(event: TaskDetailEvent) {
        val currentId = _projectId.value ?: return

        when (event) {
            // --- PHOTOS ---
            is TaskDetailEvent.OnPhotoSaved -> {
                viewModelScope.launch {
                    try {
                        photoDoRepo.insertPhoto(
                            PhotoEntity(
                                photoUri = event.uri.toString(),
                                projectId = currentId
                            )
                        )
                        Log.d(TAG, "Photo saved: ${event.uri}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving photo", e)
                    }
                }
            }
            is TaskDetailEvent.OnDeletePhoto -> {
                viewModelScope.launch {
                    val photoToDelete = (uiState.value.loadState as? DetailLoadState.Success)
                        ?.projectDetails?.photos?.find { it.photoId == event.photoId }

                    if (photoToDelete != null) {
                        photoDoRepo.deletePhoto(photoToDelete)
                    }
                }
            }

            // --- TASK ITEMS ---
            is TaskDetailEvent.OnAddItemClicked -> {
                viewModelScope.launch {
                    photoDoRepo.insertTask(
                        TaskEntity(projectId = currentId, text = event.text, isChecked = false)
                    )
                }
            }
            is TaskDetailEvent.OnItemCheckedChange -> {
                viewModelScope.launch {
                    photoDoRepo.updateTask(event.item.copy(isChecked = event.isChecked))
                }
            }
            is TaskDetailEvent.OnDeleteItem -> {
                viewModelScope.launch {
                    photoDoRepo.deleteTask(event.item)
                }
            }

            // --- PROJECT LIST ---
            is TaskDetailEvent.OnDeleteTaskListClicked -> {
                viewModelScope.launch {
                    Log.d(TAG, "Deleting project ID: $currentId")
                    photoDoRepo.deleteProjectById(currentId)
                }
            }
            is TaskDetailEvent.OnEditList -> {
                // TODO: Update project title/description
            }

            // --- UI TOGGLES ---
            TaskDetailEvent.OnCameraClick -> {
                // To keep this pure, you might want a separate StateFlow for UI toggles,
                // but if it's in your main state, you'd need to combine flows.
                // For Nav3, it's often better to just open a new NavEntry for the camera!
            }
            TaskDetailEvent.OnBackFromCamera -> {
                // See note above.
            }
        }
    }
}
