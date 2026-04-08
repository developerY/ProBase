package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.ProjectListUiModel
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TaskDraftState
import com.zoewave.probase.photodo.mobile.features.tasks.ui.state.TasksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel @Inject constructor(
    private val repo: PhotoDoRepo,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _draftState = MutableStateFlow(TaskDraftState())
    val draftState: StateFlow<TaskDraftState> = _draftState.asStateFlow()

    private val _requestedCategoryId = savedStateHandle.getStateFlow<Long?>("categoryId", null)
    private val _uiFlags = MutableStateFlow(UiFlags())

    fun setCategoryId(categoryId: Long) {
        savedStateHandle["categoryId"] = categoryId
    }

    private data class UiFlags(
        val isAddCategorySheetOpen: Boolean = false,
        val isAddListSheetOpen: Boolean = false,
        val isAddTaskItemSheetOpen: Boolean = false,
        val isAddPhotoSheetOpen: Boolean = false
    )

    val uiState: StateFlow<TasksUiState> = combine(
        _requestedCategoryId.flatMapLatest { requestedId ->
            if (requestedId != null) {
                repo.getCategoriesWithProjectsAndTasks().map { allData ->
                    val targetData = allData.find { it.category.categoryId == requestedId }
                    if (targetData != null) {
                        val mappedProjects = targetData.projects.map { projectWithTasks ->
                            val project = projectWithTasks.project
                            val tasks = projectWithTasks.tasks
                            ProjectListUiModel(
                                projectId = project.projectId,
                                title = project.name,
                                categoryName = targetData.category.name,
                                isFavorite = project.isFavorite,
                                isUrgent = project.isUrgent,
                                currentSpend = project.currentSpend,
                                projectBudget = project.projectBudget,
                                dueDateMillis = project.dueDate,
                                doneTasksCount = tasks.count { it.isChecked },
                                totalTasksCount = tasks.size,
                                thumbnailUri = projectWithTasks.photos.firstOrNull()?.photoUri
                            )
                        }
                        SmartDbResult(targetData.category.categoryId, targetData.category.name, mappedProjects, false)
                    } else {
                        SmartDbResult(null, "Category Not Found", emptyList(), true)
                    }
                }
            } else {
                repo.getCategoriesWithProjectsAndTasks().map { allData ->
                    if (allData.isEmpty()) {
                        SmartDbResult(null, "No Categories Yet", emptyList(), true)
                    } else {
                        val firstData = allData.first()
                        val mappedProjects = firstData.projects.map { projectWithTasks ->
                            val project = projectWithTasks.project
                            val tasks = projectWithTasks.tasks
                            ProjectListUiModel(
                                projectId = project.projectId,
                                title = project.name,
                                categoryName = firstData.category.name,
                                isFavorite = project.isFavorite,
                                isUrgent = project.isUrgent,
                                currentSpend = project.currentSpend,
                                projectBudget = project.projectBudget,
                                dueDateMillis = project.dueDate,
                                doneTasksCount = tasks.count { it.isChecked },
                                totalTasksCount = tasks.size,
                                thumbnailUri = projectWithTasks.photos.firstOrNull()?.photoUri
                            )
                        }
                        SmartDbResult(firstData.category.categoryId, firstData.category.name, mappedProjects, false)
                    }
                }
            }
        },
        _draftState,
        _uiFlags
    ) { dbResult, draft, flags ->
        TasksUiState(
            isLoading = false,
            categoryId = dbResult.categoryId,
            categoryName = dbResult.categoryName,
            projectLists = dbResult.projectLists,
            isNoCategoriesYet = dbResult.isNoCategoriesYet,
            draftState = draft,
            isAddCategorySheetOpen = flags.isAddCategorySheetOpen,
            isAddListSheetOpen = flags.isAddListSheetOpen,
            isAddTaskItemSheetOpen = flags.isAddTaskItemSheetOpen,
            isAddPhotoSheetOpen = flags.isAddPhotoSheetOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState(isLoading = true)
    )

    fun onEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.OnAddRandomTaskClicked -> insertRandomTask()
            is TasksEvent.OnTaskToggled -> updateTask(event.taskId, event.isCompleted)
            is TasksEvent.OnClearDatabaseClicked -> viewModelScope.launch { repo.clearAllData() }
            is TasksEvent.OnDeleteCategoryClicked -> {
                viewModelScope.launch {
                    val category = repo.getCategoryById(event.categoryId).first()
                    if (category != null) {
                        repo.deleteCategory(category)
                    }
                }
            }

            is TasksEvent.OnDraftTitleChanged -> _draftState.update { it.copy(listTitle = event.title) }
            is TasksEvent.OnDraftCategoryNameChanged -> _draftState.update { it.copy(newCategoryName = event.name) }
            is TasksEvent.OnDraftChecklistItemAdded -> _draftState.update { it.copy(pendingTaskItems = it.pendingTaskItems + event.itemText) }
            is TasksEvent.OnDraftCategorySelected -> _draftState.update { it.copy(selectedCategoryId = event.categoryId) }
            is TasksEvent.OnDraftPhotoAttached -> _draftState.update { it.copy(pendingPhotoUris = it.pendingPhotoUris + event.uri) }
            is TasksEvent.OnSaveDraftClicked -> saveDraftToDatabase()

            is TasksEvent.OnAddCategoryClicked -> _uiFlags.update { it.copy(isAddCategorySheetOpen = true) }
            is TasksEvent.OnAddListClicked -> _uiFlags.update { it.copy(isAddListSheetOpen = true) }
            is TasksEvent.OnAddTaskItemClicked -> _uiFlags.update { it.copy(isAddTaskItemSheetOpen = true) }
            is TasksEvent.OnAddPhotoClicked -> _uiFlags.update { it.copy(isAddPhotoSheetOpen = true) }

            is TasksEvent.OnDismissBottomSheet -> {
                _uiFlags.update {
                    it.copy(
                        isAddCategorySheetOpen = false,
                        isAddListSheetOpen = false,
                        isAddTaskItemSheetOpen = false,
                        isAddPhotoSheetOpen = false
                    )
                }
            }

            is TasksEvent.OnProjectClicked -> {}

            is TasksEvent.OnToggleProjectFavorite -> {
                viewModelScope.launch {
                    repo.updateProjectFavorite(event.projectId, event.isFavorite)
                }
            }

            is TasksEvent.OnToggleProjectUrgent -> {
                viewModelScope.launch {
                    repo.updateProjectUrgency(event.projectId, event.isUrgent)
                }
            }

            is TasksEvent.OnAddList -> {
                val currentCategoryId = uiState.value.categoryId ?: return
                viewModelScope.launch {
                    val newProject = ProjectEntity(
                        categoryId = currentCategoryId,
                        name = event.toString()
                    )
                    repo.upsertProject(newProject)
                    onEvent(TasksEvent.OnDismissBottomSheet)
                }
            }

            is TasksEvent.OnDeleteProject -> {
                viewModelScope.launch {
                    repo.deleteProjectById(event.projectId)
                }
            }

            is TasksEvent.OnCreateFromTemplate -> {
                viewModelScope.launch {
                    // 1. We need the Category ID to save the project.
                    // Assuming your Repo has a way to find or create a category by name:
                    val categoryId = repo.getOrCreateCategoryByName(event.template.categoryName)

                    // 2. Create the Project Entity
                    val newProject = ProjectEntity(
                        categoryId = categoryId,
                        name = event.template.title,
                        projectBudget = event.template.defaultBudget,
                        currentSpend = 0.0,
                        isUrgent = false,
                        isFavorite = false
                    )

                    // 3. Insert into Room (this returns the new unique ID!)
                    val newProjectId = repo.upsertProject(newProject)

                    // 4. (Optional but recommended) Trigger a UI Effect to navigate
                    // straight to the new TaskDetailScreen using this newProjectId!
                }
            }

            is TasksEvent.OnDraftBudgetChanged -> _draftState.update { it.copy(budgetInput = event.budgetInput) }
            is TasksEvent.OnDraftDueDateChanged -> _draftState.update { it.copy(dueDateMillis = event.timestamp) }
        }
    }

    private fun saveDraftToDatabase() {
        val draft = _draftState.value
        if (draft.listTitle.isBlank()) return

        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()

            // ENHANCEMENT: Use the Smart Default Category ID if they didn't pick one in the draft!
            val categoryId: Long = when {
                draft.selectedCategoryId != null -> draft.selectedCategoryId
                uiState.value.categoryId != null -> uiState.value.categoryId!!
                else -> {
                    val newCat = CategoryEntity(name = draft.newCategoryName.ifBlank { "Uncategorized" })
                repo.upsertCategory(newCat) // This returns the new ID  
                }
            }

            val newProject = ProjectEntity(
                categoryId = categoryId,
                name = draft.listTitle,
                projectBudget = draft.budgetInput.toDoubleOrNull() ?: 0.0,
                dueDate = draft.dueDateMillis
            )


            val generatedProjectId: Long = repo.upsertProject(newProject)

            // Insert Task Items
            draft.pendingTaskItems.forEach { itemText ->
                repo.upsertTask(TaskEntity(projectId = generatedProjectId, text = itemText, isChecked = false))
            }

            // Insert Photos
            draft.pendingPhotoUris.forEach { uri ->
                repo.upsertPhoto(PhotoEntity(projectId = generatedProjectId, photoUri = uri, timestamp = timestamp))
            }

            // Clear the draft so the UI closes the Bottom Sheet cleanly
            _draftState.update { TaskDraftState() }
            onEvent(TasksEvent.OnDismissBottomSheet)
        }
    }

    private fun insertRandomTask() {
        // Implementation omitted for brevity
    }

    private fun updateTask(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            val task = repo.getTaskById(taskId)
            if (task != null) {
                repo.updateTask(task.copy(isChecked = isCompleted))
            }
        }
    }
}

private data class SmartDbResult(
    val categoryId: Long?,
    val categoryName: String,
    val projectLists: List<ProjectListUiModel>,
    val isNoCategoriesYet: Boolean
)