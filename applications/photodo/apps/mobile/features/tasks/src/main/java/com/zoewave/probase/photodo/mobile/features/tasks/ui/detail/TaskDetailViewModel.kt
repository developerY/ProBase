package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskItemEntity
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

    // We use a StateFlow for the ID so we can trigger the database query reactively
    private val _listId = MutableStateFlow<Long?>(null)

    // 1. The Single Source of Truth!
    // Whenever _listId gets set, this flow automatically fetches the data and maps it to the UI.
    val uiState: StateFlow<TaskDetailUiState> = _listId
        .filterNotNull() // Don't query until we have a real ID
        .flatMapLatest { id ->
            photoDoRepo.getTaskListWithPhotos(id)
                .map { taskListWithPhotos ->
                    if (taskListWithPhotos != null) {
                        TaskDetailUiState(loadState = DetailLoadState.Success(taskListWithPhotos))
                    } else {
                        // If the list is deleted, Room emits null.
                        // We catch it here so the UI knows to close the screen!
                        TaskDetailUiState(loadState = DetailLoadState.Error("List has been deleted."))
                    }
                }
                .catch { e ->
                    Log.e(TAG, "Error loading task details", e)
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
        if (_listId.value == id) return // Prevent duplicate loads

        Log.d(TAG, "Loading details for listId: $id")
        _listId.value = id
    }

    fun onEvent(event: TaskDetailEvent) {
        val currentId = _listId.value ?: return

        when (event) {
            // --- PHOTOS ---
            is TaskDetailEvent.OnPhotoSaved -> {
                viewModelScope.launch {
                    try {
                        photoDoRepo.insertPhoto(
                            PhotoEntity(
                                photoUri = event.uri.toString(),
                                listId = currentId
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
                    // Because your DAO likely just takes the photo ID or the whole entity:
                    val photoToDelete = (uiState.value.loadState as? DetailLoadState.Success)
                        ?.taskListWithPhotos?.photos?.find { it.photoId == event.photoId }

                    if (photoToDelete != null) {
                        photoDoRepo.deletePhoto(photoToDelete)
                    }
                }
            }

            // --- TASK ITEMS ---
            is TaskDetailEvent.OnAddItemClicked -> {
                viewModelScope.launch {
                    photoDoRepo.insertTaskItem(
                        TaskItemEntity(listId = currentId, text = event.text, isChecked = false)
                    )
                }
            }
            is TaskDetailEvent.OnItemCheckedChange -> {
                viewModelScope.launch {
                    photoDoRepo.updateTaskItem(event.item.copy(isChecked = event.isChecked))
                }
            }
            is TaskDetailEvent.OnDeleteItem -> {
                viewModelScope.launch {
                    photoDoRepo.deleteTaskItem(event.item)
                }
            }

            // --- PROJECT LIST ---
            is TaskDetailEvent.OnDeleteTaskListClicked -> {
                viewModelScope.launch {
                    Log.d(TAG, "Deleting task list ID: $currentId")
                    // Instead of passing the whole object, just delete by ID!
                    photoDoRepo.deleteTaskListById(currentId)

                    // Note: Room will automatically emit `null` to our flatMapLatest above,
                    // flipping the UI state to Error("List has been deleted").
                }
            }
            is TaskDetailEvent.OnEditList -> {
                // TODO: Update list title/description
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