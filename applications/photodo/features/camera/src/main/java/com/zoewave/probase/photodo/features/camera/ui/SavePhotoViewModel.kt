package com.zoewave.probase.photodo.features.camera.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.photodo.features.camera.domain.AddPhotoToTaskUseCase
import com.zoewave.probase.photodo.features.camera.ui.state.SavePhotoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavePhotoViewModel @Inject constructor(
    private val repo: PhotoDoRepo,
    private val addPhotoToTask: AddPhotoToTaskUseCase
) : ViewModel() {

    private val _photoUri = MutableStateFlow("")
    private val _isFromAi = MutableStateFlow(false)
    
    // Form Fields
    private val _categoryName = MutableStateFlow("")
    private val _projectName = MutableStateFlow("")
    private val _taskName = MutableStateFlow("")
    private val _duration = MutableStateFlow("")
    private val _budgetInput = MutableStateFlow("")
    private val _dueDateMillis = MutableStateFlow<Long?>(null)
    private val _subTasks = MutableStateFlow<List<String>>(emptyList())

    // Status
    private val _isSaving = MutableStateFlow(false)
    private val _isSaved = MutableStateFlow(false)
    private val _savedProjectId = MutableStateFlow<Long?>(null)
    private val _savedProjectTitle = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SavePhotoUiState> = combine(
        _photoUri,
        _isFromAi,
        _categoryName,
        _projectName,
        _taskName,
        _duration,
        _budgetInput,
        _dueDateMillis,
        _subTasks,
        repo.getAllCategories(),
        _isSaving,
        _isSaved,
        _savedProjectId,
        _savedProjectTitle
    ) { args: Array<Any?> ->
        SavePhotoUiState(
            photoUri = args[0] as String,
            isFromAi = args[1] as Boolean,
            categoryName = args[2] as String,
            projectName = args[3] as String,
            taskName = args[4] as String,
            duration = args[5] as String,
            budgetInput = args[6] as String,
            dueDateMillis = args[7] as Long?,
            subTasks = args[8] as List<String>,
            categories = args[9] as List<CategoryEntity>,
            isSaving = args[10] as Boolean,
            isSaved = args[11] as Boolean,
            savedProjectId = args[12] as Long?,
            savedProjectTitle = args[13] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SavePhotoUiState("")
    )

    fun setInitialData(uri: String, draft: SmartTaskDraft? = null) {
        if (_photoUri.value == uri && draft == null) return
        
        _photoUri.value = uri
        if (draft != null) {
            _isFromAi.value = true
            _categoryName.value = draft.category ?: ""
            _projectName.value = draft.projectName ?: ""
            _taskName.value = draft.taskName ?: ""
            _duration.value = draft.duration ?: ""
            _budgetInput.value = if (draft.budget != null && draft.budget!! > 0) draft.budget.toString() else ""
            _subTasks.value = draft.subTasks
        } else {
            _isFromAi.value = false
            resetForm()
        }
    }

    private fun resetForm() {
        _categoryName.value = ""
        _projectName.value = ""
        _taskName.value = ""
        _duration.value = ""
        _budgetInput.value = ""
        _dueDateMillis.value = null
        _subTasks.value = emptyList()
        _isSaving.value = false
        _isSaved.value = false
        _savedProjectId.value = null
        _savedProjectTitle.value = null
    }

    fun setCategoryName(name: String) { _categoryName.value = name }
    fun setProjectName(name: String) { _projectName.value = name }
    fun setTaskName(name: String) { _taskName.value = name }
    fun setDuration(duration: String) { _duration.value = duration }
    fun setBudgetInput(budget: String) { _budgetInput.value = budget }
    fun setDueDate(timestamp: Long?) { _dueDateMillis.value = timestamp }
    
    fun adjustBudget(adjustment: Double) {
        val current = _budgetInput.value.toDoubleOrNull() ?: 0.0
        val newValue = (current + adjustment).coerceAtLeast(0.0)
        _budgetInput.value = if (newValue % 1 == 0.0) newValue.toLong().toString() else newValue.toString()
    }

    fun addSubTask(text: String) {
        if (text.isNotBlank()) {
            _subTasks.update { it + text }
        }
    }

    fun removeSubTask(index: Int) {
        _subTasks.update { it.toMutableList().apply { removeAt(index) } }
    }

    fun saveTask() {
        val uri = _photoUri.value
        if (uri.isBlank()) return

        viewModelScope.launch {
            _isSaving.value = true
            
            // 1. Get or Create Category
            val finalCategory = _categoryName.value.ifBlank { "Uncategorized" }
            val categoryId = repo.getOrCreateCategoryByName(finalCategory)

            // 2. Create Project
            val finalProjectName = _projectName.value.ifBlank { _taskName.value.ifBlank { "New Project" } }
            val newProject = ProjectEntity(
                categoryId = categoryId,
                name = finalProjectName,
                projectBudget = _budgetInput.value.toDoubleOrNull() ?: 0.0,
                dueDate = _dueDateMillis.value,
                notes = if (_duration.value.isNotBlank()) "Duration: ${_duration.value}" else null
            )
            val projectId = repo.upsertProject(newProject)

            // 3. Create Main Task (if name provided)
            if (_taskName.value.isNotBlank()) {
                repo.upsertTask(TaskEntity(projectId = projectId, text = _taskName.value))
            }

            // 4. Create Sub-tasks
            _subTasks.value.forEach { subTaskText ->
                repo.upsertTask(TaskEntity(projectId = projectId, text = subTaskText))
            }

            // 5. Attach Photo
            addPhotoToTask(projectId, uri)

            _savedProjectId.value = projectId
            _savedProjectTitle.value = finalProjectName
            _isSaving.value = false
            _isSaved.value = true
        }
    }
}
