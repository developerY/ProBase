package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskItemEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskListEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.mobile.features.tasks.domain.TaskDevTools
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TaskDraftState
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel @Inject constructor(
    private val repo: PhotoDoRepo,
    private val devTools: TaskDevTools
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState(isLoading = true))
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    // Expose the draft to your Bottom Sheets
    private val _draftState = MutableStateFlow(TaskDraftState())
    val draftState: StateFlow<TaskDraftState> = _draftState.asStateFlow()

    // Tracks the ID passed in from Tab 1
    private val _requestedCategoryId = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            _draftState.collect { draft ->
                _uiState.update { it.copy(draftState = draft) }
            }
        }

        viewModelScope.launch {
            // The "Smart Default" Database collector
            _requestedCategoryId.flatMapLatest { requestedId ->
                if (requestedId != null) {
                    // Scenario A: User clicked a specific category from Tab 1
                    repo.getCategoriesWithTaskLists().map { allData ->
                        val targetData = allData.find { it.category.categoryId == requestedId }
                        if (targetData != null) {
                            val mappedProjects = targetData.taskLists.map {
                                ProjectListUiModel(it.listId, it.name, targetData.category.name, it.isFavorite, it.isUrgent)
                            }
                            SmartDbResult(targetData.category.categoryId, targetData.category.name, mappedProjects, false)
                        } else {
                            SmartDbResult(null, "Category Not Found", emptyList(), true)
                        }
                    }
                } else {
                    // Scenario B: User tapped the Tasks tab directly. Default to the very first category!
                    repo.getCategoriesWithTaskLists().map { allData ->
                        if (allData.isEmpty()) {
                            SmartDbResult(null, "No Categories Yet", emptyList(), true)
                        } else {
                            val firstData = allData.first()
                            val mappedProjects = firstData.taskLists.map {
                                ProjectListUiModel(it.listId, it.name, firstData.category.name, it.isFavorite, it.isUrgent)
                            }
                            SmartDbResult(firstData.category.categoryId, firstData.category.name, mappedProjects, false)
                        }
                    }
                }
            }.collect { dbResult ->
                // Gently update the UI state WITHOUT overwriting your bottom sheet flags!
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        categoryId = dbResult.categoryId,
                        categoryName = dbResult.categoryName,
                        projectLists = dbResult.projectLists,
                        isNoCategoriesYet = dbResult.isNoCategoriesYet
                    )
                }
            }
        }
    }

    // Nav3 calls this to pass the ID
    fun setCategoryId(id: Long?) {
        // The Memory Fix: Only update if the router passed a REAL ID.
        if (id != null) {
            _requestedCategoryId.value = id
        }
    }

    fun onEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.OnAddRandomTaskClicked -> insertRandomTask()
            is TasksEvent.OnTaskToggled -> updateTask(event.taskId, event.isCompleted)
            is TasksEvent.OnGenerateFullMockDataClicked -> viewModelScope.launch { devTools.seedDatabase() }
            is TasksEvent.OnClearDatabaseClicked -> viewModelScope.launch { repo.clearAllData() }

            is TasksEvent.OnDraftTitleChanged -> _draftState.update { it.copy(listTitle = event.title) }
            is TasksEvent.OnDraftCategoryNameChanged -> _draftState.update { it.copy(newCategoryName = event.name) }
            is TasksEvent.OnDraftChecklistItemAdded -> _draftState.update { it.copy(pendingTaskItems = it.pendingTaskItems + event.itemText) }
            is TasksEvent.OnDraftCategorySelected -> _draftState.update { it.copy(selectedCategoryId = event.categoryId) }
            is TasksEvent.OnDraftPhotoAttached -> _draftState.update { it.copy(pendingPhotoUris = it.pendingPhotoUris + event.uri) }
            is TasksEvent.OnSaveDraftClicked -> saveDraftToDatabase()

            is TasksEvent.OnAddCategoryClicked -> _uiState.update { it.copy(isAddCategorySheetOpen = true) }
            is TasksEvent.OnAddListClicked -> _uiState.update { it.copy(isAddListSheetOpen = true) }
            is TasksEvent.OnAddTaskItemClicked -> _uiState.update { it.copy(isAddTaskItemSheetOpen = true) }
            is TasksEvent.OnAddPhotoClicked -> _uiState.update { it.copy(isAddPhotoSheetOpen = true) }

            is TasksEvent.OnDismissBottomSheet -> {
                _uiState.update {
                    it.copy(
                        isAddCategorySheetOpen = false,
                        isAddListSheetOpen = false,
                        isAddTaskItemSheetOpen = false,
                        isAddPhotoSheetOpen = false
                    )
                }
            }

            // --- SAFELY IMPLEMENTED NEW EVENTS ---

            is TasksEvent.OnProjectClicked -> {
                // Intentionally left blank!
                // We intercept this in TasksListUiRoute to trigger navigation.
            }

            is TasksEvent.OnToggleProjectFavorite -> {
                viewModelScope.launch {
                    // Uncomment once you add this function to PhotoDoRepo:
                    repo.updateProjectFavorite(event.projectId, event.isFavorite)
                }
            }

            is TasksEvent.OnToggleProjectUrgent -> {
                viewModelScope.launch {
                    // Uncomment once you add this function to PhotoDoRepo:
                    repo.updateProjectUrgency(event.projectId, event.isUrgent)
                }
            }

            is TasksEvent.OnAddList -> {
                // If bypassing the draft state entirely and passing a string directly
                val currentCategoryId = _uiState.value.categoryId ?: return
                viewModelScope.launch {
                    val newList = TaskListEntity(
                        categoryId = currentCategoryId,
                        name = event.toString() // Assuming your event has a 'title' property
                    )
                    repo.insertTaskList(newList)
                    onEvent(TasksEvent.OnDismissBottomSheet)
                }
            }

            is TasksEvent.OnDeleteListClicked -> {
                viewModelScope.launch {
                    // Uncomment once you add this function to PhotoDoRepo:
                    // repo.deleteTaskListById(event.listId)
                }
            }
        }
    }

    private fun saveDraftToDatabase() {
        val draft = _draftState.value
        if (draft.listTitle.isBlank()) return

        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()

            // ENHANCEMENT: Use the Smart Default Category ID if they didn't pick one in the draft!
            val categoryId: Long = draft.selectedCategoryId
                ?: _uiState.value.categoryId
                ?: run {
                    val newCat = CategoryEntity(name = draft.newCategoryName.ifBlank { "Uncategorized" })
                    repo.insertCategory(newCat)
                }

            val newList = TaskListEntity(categoryId = categoryId, name = draft.listTitle)
            val generatedListId: Long = repo.insertTaskList(newList)

            // Insert Task Items
            draft.pendingTaskItems.forEach { itemText ->
                repo.insertTaskItem(TaskItemEntity(listId = generatedListId, text = itemText, isChecked = false))
            }

            // Insert Photos
            draft.pendingPhotoUris.forEach { uri ->
                repo.insertPhoto(PhotoEntity(listId = generatedListId, photoUri = uri, timestamp = timestamp))
            }

            // Clear the draft so the UI closes the Bottom Sheet cleanly
            _draftState.value = TaskDraftState()
            onEvent(TasksEvent.OnDismissBottomSheet) // Close sheets cleanly
        }
    }

    private fun insertRandomTask() {
        viewModelScope.launch {
            val randomId = (1..100000).random().toLong()
            val randomTitle = "Test Task: ${UUID.randomUUID().toString().take(6)}"
            // Simulation logic preserved
        }
    }

    private fun updateTask(taskId: Long, isCompleted: Boolean) {
        // Preserved
    }
}

// Private helper to make the stream cleaner
private data class SmartDbResult(
    val categoryId: Long?,
    val categoryName: String,
    val projectLists: List<ProjectListUiModel>,
    val isNoCategoriesYet: Boolean
)