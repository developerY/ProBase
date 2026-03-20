package com.zoewave.probase.photodo.mobile.features.tasks.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CategoryTasksViewModel"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CategoryTasksViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo
) : ViewModel() {

    // Internal state tracking which category we are currently viewing
    private val _categoryId = MutableStateFlow<Long?>(null)
    private var currentCategoryName: String = ""

    // 1. The Reactive State (Unchanged - still perfectly maps your DB to the UI)
    val uiState: StateFlow<TasksUiState> = _categoryId
        .filterNotNull()
        .flatMapLatest { id ->
            // Use the exact query you wrote earlier!
            photoDoRepo.getTaskListsForCategory(id)
                .map { taskLists ->
                    val mappedProjects = taskLists.map { entity ->
                        ProjectListUiModel(
                            id = entity.listId,
                            title = entity.name,
                            categoryName = currentCategoryName
                        )
                    }
                    // Reuse the existing state model
                    TasksUiState(projectLists = mappedProjects)
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TasksUiState()
        )

    fun loadCategory(id: Long, name: String) {
        if (_categoryId.value == id) return
        currentCategoryName = name
        _categoryId.value = id
    }

    // 2. The Event Handler (Where the magic happens!)
    fun onEvent(event: TasksEvent) {
        val currentCategoryId = _categoryId.value ?: return

        when (event) {
            // --- ADDING A NEW ITEM LIST (PROJECT) ---
            // Assuming your TasksEvent has something like: data class OnAddList(val title: String)
            is TasksEvent.OnAddList -> {
                viewModelScope.launch {
                    try {
                        // We automatically attach the current category ID!
                        /*val newList = TaskListEntity(
                            categoryId = currentCategoryId,
                            title = event.title,
                            description = "Added from $currentCategoryName Dashboard",
                            status = "Incomplete"
                        )
                        photoDoRepo.insertTaskList(newList)*/
                        //Log.d(TAG, "Successfully created new list: ${event.title}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding new task list", e)
                    }
                }
            }

            // --- DELETING AN ITEM LIST (PROJECT) ---
            is TasksEvent.OnDeleteListClicked -> {
                viewModelScope.launch {
                    /*try {
                        Log.d(TAG, "Deleting TaskList with ID: ${event.listId}")
                        photoDoRepo.deleteTaskListById(event.listId)
                        // Room's flow will automatically emit the updated list,
                        // and the UI will animate the card away instantly!
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deleting task list", e)
                    }*/
                }
            }

            else -> {
                Log.d(TAG, "Unhandled event in Category view: $event")
            }
        }
    }
}