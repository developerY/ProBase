package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskItemEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskListEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.mobile.features.tasks.domain.TaskDevTools
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TaskDraftState
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
// import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
// import com.zoewave.probase.applications.photodo.db.entity.TaskItemEntity

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repo: PhotoDoRepo,
    private val devTools: TaskDevTools // Inject the interface
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    // Expose the draft to your Bottom Sheets
    private val _draftState = MutableStateFlow(TaskDraftState())
    val draftState: StateFlow<TaskDraftState> = _draftState.asStateFlow()

    init {
        // TODO: Collect from your DB here
        // repo.getAllTasks().map { entities -> mapToUiModels(entities) }
        //     .onEach { uiState.update { state -> state.copy(tasks = it) } }
        //     .launchIn(viewModelScope)
    }

    fun onEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.OnAddRandomTaskClicked -> insertRandomTask()
            is TasksEvent.OnTaskToggled -> updateTask(event.taskId, event.isCompleted)

            is TasksEvent.OnGenerateFullMockDataClicked -> {
                viewModelScope.launch {
                    devTools.seedDatabase() // Stays safe in debug source set!
                }
            }

            is TasksEvent.OnClearDatabaseClicked -> {
                viewModelScope.launch {
                    repo.clearAllData() // Works perfectly in BOTH debug and release!
                }
            }

            is TasksEvent.OnDraftTitleChanged -> {
                _draftState.update { it.copy(listTitle = event.title) }
            }
            is TasksEvent.OnDraftChecklistItemAdded -> {
                _draftState.update {
                    it.copy(pendingTaskItems = it.pendingTaskItems + event.itemText)
                }
            }
            is TasksEvent.OnDraftCategorySelected -> {
                _draftState.update { it.copy(selectedCategoryId = event.categoryId) }
            }
            is TasksEvent.OnDraftPhotoAttached -> {
                _draftState.update {
                    it.copy(pendingPhotoUris = it.pendingPhotoUris + event.uri)
                }
            }
            is TasksEvent.OnSaveDraftClicked -> saveDraftToDatabase()
            TasksEvent.OnAddCategoryClicked -> TODO()
            TasksEvent.OnAddListClicked -> TODO()
            TasksEvent.OnAddPhotoClicked -> TODO()
            TasksEvent.OnAddTaskItemClicked -> TODO()
            TasksEvent.OnDismissBottomSheet -> TODO()
        }
    }

    private fun saveDraftToDatabase() {
        val draft = _draftState.value
        if (draft.listTitle.isBlank()) return // Basic validation

        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()

            // 1. Resolve Category
            // If they picked an existing one, use it. Otherwise, create a new one.
            val categoryId : Long = draft.selectedCategoryId ?: run {
                val newCat =
                    CategoryEntity(name = draft.newCategoryName.ifBlank { "Uncategorized" })
                repo.insertCategory(newCat) // NOTE: Your DAO needs to return the generated Long here!
            }

            // 2. Create the TaskList
            // We pass the resolved categoryId down. Room generates the listId automatically.
            val newList = TaskListEntity(
                categoryId = categoryId,
                name = draft.listTitle
            )
            val generatedListId : Long = repo.insertTaskList(newList) // DAO must return Long!

            // 3. Insert Task Items
            draft.pendingTaskItems.forEach { itemText ->
                repo.insertTaskItem(
                    TaskItemEntity(
                        listId = generatedListId,
                        text = itemText,
                        isChecked = false
                    )
                )
            }

            // 4. Insert Photos
            draft.pendingPhotoUris.forEach { uri ->
                repo.insertPhoto(
                    PhotoEntity(
                        listId = generatedListId,
                        photoUri = uri,
                        timestamp = timestamp
                    )
                )
            }

            // 5. Clear the draft so the UI closes the Bottom Sheet cleanly
            _draftState.value = TaskDraftState()
        }
    }

    private fun insertRandomTask() {
        viewModelScope.launch {
            val randomId = (1..100000).random().toLong()
            val randomTitle = "Test Task: ${UUID.randomUUID().toString().take(6)}"

            // TODO: Call your Room insert function here
            // repo.insertTask(TaskItemEntity(id = randomId, title = randomTitle, ...))

            // Simulated UI update for testing before DB is fully wired
            _uiState.update { state ->
                val newTask = TaskItemUiModel(randomId, randomTitle, false)
                state.copy(tasks = state.tasks + newTask)
            }
        }
    }

    private fun updateTask(taskId: Long, isCompleted: Boolean) {
        // TODO: Call your Room update function here
        // repo.updateTaskStatus(taskId, isCompleted)
    }
}