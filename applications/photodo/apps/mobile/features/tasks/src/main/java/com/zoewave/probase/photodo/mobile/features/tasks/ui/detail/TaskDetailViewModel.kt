package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.ExpenseEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.mobile.features.tasks.ui.TasksSideEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PhotoDoDetailViewModel"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo,
    private val appSettingsRepository: AppSettingsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _effects = Channel<TasksSideEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    // ✅ GOLD STANDARD: Get projectId directly from SavedStateHandle.
    // This makes the ViewModel reactive to navigation arguments and survives process death.
    private val _projectId = MutableStateFlow<Long?>(savedStateHandle.get<Long?>("projectId"))

    private data class UiFlags(
        val fabMenuExpanded: Boolean = false,
        val showAddTaskDialog: Boolean = false,
        val newTaskText: String = "",
        val showAddExpenseDialog: Boolean = false,
        val newExpenseAmount: String = "",
        val newExpenseDesc: String = "",
        val showDeleteProjectConfirmation: Boolean = false
    )

    private val _uiFlags = MutableStateFlow(UiFlags())

    fun loadTaskDetails(projectId: Long) {
        savedStateHandle["projectId"] = projectId
        _projectId.value = projectId
    }

    val uiState: StateFlow<TaskDetailUiState> = combine(
        _projectId.asStateFlow().filterNotNull().flatMapLatest { id ->
            Log.d("ProjectDebug", "TaskDetailViewModel: Fetching details for Project ID: $id")
            photoDoRepo.getProjectDetails(id)
        },
        appSettingsRepository.isAiEnabledFlow,
        appSettingsRepository.animationsEnabledFlow,
        _uiFlags
    ) { projectDetails, isAiEnabled, animationsEnabled, flags ->
        Log.d("ProjectDebug", "TaskDetailViewModel: Received details? ${projectDetails != null}")
        if (projectDetails != null) {
            TaskDetailUiState(
                loadState = DetailLoadState.Success(projectDetails),
                isAiEnabled = isAiEnabled,
                animationsEnabled = animationsEnabled,
                fabMenuExpanded = flags.fabMenuExpanded,
                showAddTaskDialog = flags.showAddTaskDialog,
                newTaskText = flags.newTaskText,
                showAddExpenseDialog = flags.showAddExpenseDialog,
                newExpenseAmount = flags.newExpenseAmount,
                newExpenseDesc = flags.newExpenseDesc,
                showDeleteProjectConfirmation = flags.showDeleteProjectConfirmation
            )
        } else {
            Log.w("ProjectDebug", "TaskDetailViewModel: Project NOT FOUND in DB!")
            TaskDetailUiState(loadState = DetailLoadState.Error("Project has been deleted."))
        }
    }
    .catch { e ->
        Log.e(TAG, "Error loading project details", e)
        emit(TaskDetailUiState(loadState = DetailLoadState.Error(e.message ?: "Unknown error")))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskDetailUiState(loadState = DetailLoadState.Loading)
    )

    fun onEvent(event: TaskDetailEvent) {
        val currentId = _projectId.value ?: return

        when (event) {
            // --- PHOTOS ---
            is TaskDetailEvent.OnPhotoSaved -> {
                viewModelScope.launch {
                    try {
                        photoDoRepo.upsertPhoto(
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
                    photoDoRepo.upsertTask(
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
                    _effects.send(TasksSideEffect.NavigateBack)
                }
            }
            is TaskDetailEvent.OnEditList -> {
                // TODO: Update project title/description
                /*viewModelScope.launch {
                    val currentState = uiState.value.loadState
                    if (currentState is DetailLoadState.Success) {
                        photoDoRepo.updateProject(
                            currentState.projectDetails.project.copy(
                                title = event.title,
                                description = event.description
                            )
                        )
                    }
                }*/
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

            is TaskDetailEvent.OnAddExpenseClicked -> {
                viewModelScope.launch {
                    // 1. Save the receipt/expense record
                    photoDoRepo.upsertExpense(
                        ExpenseEntity(
                            projectId = currentId,
                            description = event.description,
                            amount = event.amount
                        )
                    )

                    // 2. 🚀 CRITICAL: Update the Project's total spent amount so the progress bar moves!
                    // We can safely grab the current project state since it's already loaded in the UI
                    val currentState = uiState.value.loadState
                    if (currentState is DetailLoadState.Success) {
                        val project = currentState.projectDetails.project
                        val currentSpent = project.currentSpend
                        val newTotalSpent = currentSpent + event.amount

                        // Tell Room to update the project.
                        // This will instantly trigger your StateFlow to emit, and the UI progress bar will animate!
                        photoDoRepo.updateProject(
                            project.copy(currentSpend = newTotalSpent)
                        )
                    }
                }
            }

            is TaskDetailEvent.OnDeleteExpense -> {
                viewModelScope.launch {
                    val currentState = uiState.value.loadState
                    if (currentState is DetailLoadState.Success) {
                        val expenseToDelete = currentState.projectDetails.expenses.find { it.expenseId == event.expenseId }
                        if (expenseToDelete != null) {
                            // 1. Delete the expense
                            photoDoRepo.deleteExpense(expenseToDelete)

                            // 2. Update project spent amount
                            val project = currentState.projectDetails.project
                            val currentSpent = project.currentSpend
                            val newTotalSpent = (currentSpent - expenseToDelete.amount).coerceAtLeast(0.0)

                            photoDoRepo.updateProject(
                                project.copy(currentSpend = newTotalSpent)
                            )
                        }
                    }
                }
            }

            TaskDetailEvent.OnHelpClicked -> {
                // Navigation handled in UI
            }

            is TaskDetailEvent.OnFabMenuToggle -> {
                _uiFlags.update { it.copy(fabMenuExpanded = event.expanded) }
            }
            is TaskDetailEvent.OnShowAddTaskDialog -> {
                _uiFlags.update { it.copy(showAddTaskDialog = event.show) }
            }
            is TaskDetailEvent.OnNewTaskTextChanged -> {
                _uiFlags.update { it.copy(newTaskText = event.text) }
            }
            is TaskDetailEvent.OnShowAddExpenseDialog -> {
                _uiFlags.update { it.copy(showAddExpenseDialog = event.show) }
            }
            is TaskDetailEvent.OnNewExpenseAmountChanged -> {
                _uiFlags.update { it.copy(newExpenseAmount = event.amount) }
            }
            is TaskDetailEvent.OnNewExpenseDescChanged -> {
                _uiFlags.update { it.copy(newExpenseDesc = event.desc) }
            }
            is TaskDetailEvent.OnShowDeleteProjectConfirmation -> {
                _uiFlags.update { it.copy(showDeleteProjectConfirmation = event.show) }
            }
        }
    }
}
