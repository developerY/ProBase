package com.zoewave.probase.photodo.features.camera.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.core.data.repository.AiComplianceRepository
import com.zoewave.probase.core.model.tasks.SmartTaskDraft
import com.zoewave.probase.photodo.features.camera.domain.AddPhotoToTaskUseCase
import com.zoewave.probase.photodo.features.camera.ui.state.SavePhotoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val addPhotoToTask: AddPhotoToTaskUseCase,
    private val aiComplianceRepo: AiComplianceRepository
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
    private val _aiGeneratedFields = MutableStateFlow<Set<String>>(emptySet())

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
        _aiGeneratedFields,
        repo.getAllCategories(),
        repo.getAllProjects(), // 🚀 NEW: Fetch existing projects
        _isSaving,
        _isSaved,
        _savedProjectId,
        _savedProjectTitle
    ) { args: Array<Any?> ->
        val categories = args[10] as List<CategoryEntity>
        val allProjects = args[11] as List<ProjectEntity>
        val currentCategoryName = args[2] as String

        // 🚀 NEW: Filter projects by the currently selected category name
        val filteredProjects = if (currentCategoryName.isNotBlank()) {
            val matchedCategoryId = categories.find { it.name.equals(currentCategoryName, ignoreCase = true) }?.categoryId
            if (matchedCategoryId != null) {
                allProjects.filter { it.categoryId == matchedCategoryId }
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }

        SavePhotoUiState(
            photoUri = args[0] as String,
            isFromAi = args[1] as Boolean,
            categoryName = currentCategoryName,
            projectName = args[3] as String,
            taskName = args[4] as String,
            duration = args[5] as String,
            budgetInput = args[6] as String,
            dueDateMillis = args[7] as Long?,
            subTasks = args[8] as List<String>,
            aiGeneratedFields = args[9] as Set<String>,
            categories = categories,
            projects = filteredProjects, // 🚀 NEW: Use filtered list
            isSaving = args[12] as Boolean,
            isSaved = args[13] as Boolean,
            savedProjectId = args[14] as Long?,
            savedProjectTitle = args[15] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SavePhotoUiState("")
    )

    fun setInitialData(uri: String?, draft: SmartTaskDraft? = null) {
        Log.d("SavePhotoDebug", "ViewModel: setInitialData called. Current isSaved: ${_isSaved.value}")
        if (_photoUri.value == uri && draft == null) {
            Log.d("SavePhotoDebug", "ViewModel: setInitialData ignored (same URI and no draft)")
            return
        }
        
        // 🚀 CRITICAL: Reset the status flags so we don't skip the form if the ViewModel is reused.
        Log.d("SavePhotoDebug", "ViewModel: Resetting status flags to false")
        _isSaving.value = false
        _isSaved.value = false
        _savedProjectId.value = null
        _savedProjectTitle.value = null

        _photoUri.value = uri ?: ""
        if (draft != null) {
            _isFromAi.value = true
            _categoryName.value = draft.category ?: ""
            _projectName.value = draft.projectName ?: ""
            _taskName.value = draft.taskName ?: ""
            _duration.value = draft.duration ?: ""
            _budgetInput.value = if (draft.budget != null && draft.budget!! > 0) draft.budget.toString() else ""
            _subTasks.value = draft.subTasks
            
            // Mark fields as AI generated if they were populated
            val generated = mutableSetOf<String>()
            if (draft.category != null) generated.add("category")
            if (draft.projectName != null) generated.add("project")
            if (draft.taskName != null) generated.add("task")
            if (draft.duration != null) generated.add("duration")
            if (draft.budget != null) generated.add("budget")
            _aiGeneratedFields.value = generated
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
        _aiGeneratedFields.value = emptySet()
        _isSaving.value = false
        _isSaved.value = false
        _savedProjectId.value = null
        _savedProjectTitle.value = null
    }

    fun onEvent(event: SavePhotoEvent) {
        when (event) {
            is SavePhotoEvent.OnCategoryNameChanged -> setCategoryName(event.name)
            is SavePhotoEvent.OnProjectNameChanged -> setProjectName(event.name)
            is SavePhotoEvent.OnTaskNameChanged -> setTaskName(event.name)
            is SavePhotoEvent.OnDurationChanged -> setDuration(event.duration)
            is SavePhotoEvent.OnBudgetInputChanged -> setBudgetInput(event.budget)
            is SavePhotoEvent.OnAdjustBudget -> adjustBudget(event.adjustment)
            is SavePhotoEvent.OnDueDateChanged -> setDueDate(event.timestamp)
            SavePhotoEvent.OnReportIssue -> {
                // Reporting is handled via Intent in the UI layer (NavEntry)
                // but we can expose the intent here if needed, or just let UI handle it.
            }
            SavePhotoEvent.OnClearAiData -> clearAiData()
            SavePhotoEvent.OnSaveClicked -> saveTask()
            SavePhotoEvent.OnDismiss -> { /* Handled by navigation */ }
        }
    }

    private fun setCategoryName(name: String) { _categoryName.value = name }
    private fun setProjectName(name: String) { _projectName.value = name }
    private fun setTaskName(name: String) { _taskName.value = name }
    private fun setDuration(duration: String) { _duration.value = duration }
    private fun setBudgetInput(budget: String) { _budgetInput.value = budget }
    private fun setDueDate(timestamp: Long?) { _dueDateMillis.value = timestamp }
    
    private fun adjustBudget(adjustment: Double) {
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

    fun getReportIntent(): android.content.Intent {
        // Disabled for PhotoDo mobile app as per user request
        // return aiComplianceRepo.getReportIntent()
        return android.content.Intent()
    }

    fun clearAiData() {
        _aiGeneratedFields.value.forEach { field ->
            when (field) {
                "category" -> _categoryName.value = ""
                "project" -> _projectName.value = ""
                "task" -> _taskName.value = ""
                "duration" -> _duration.value = ""
                "budget" -> _budgetInput.value = ""
            }
        }
        _aiGeneratedFields.value = emptySet()
    }

    fun saveTask() {
        val uri = _photoUri.value

        viewModelScope.launch {
            _isSaving.value = true
            
            // 1. Get or Create Category
            val finalCategory = _categoryName.value.ifBlank { "Uncategorized" }
            val categoryId = repo.getOrCreateCategoryByName(finalCategory)

            // 2. Check for Existing Project in this Category
            val finalProjectName = _projectName.value.ifBlank { _taskName.value.ifBlank { "New Project" } }
            val existingProject = repo.getProjectByNameAndCategory(categoryId, finalProjectName)

            val inputBudget = _budgetInput.value.toDoubleOrNull() ?: 0.0
            val projectId = if (existingProject != null) {
                Log.d("ProjectDebug", "Merging into existing project: ${existingProject.name} (ID: ${existingProject.projectId})")
                
                // 🚀 MERGE LOGIC:
                // 1. Add budgets together
                // 2. Use new due date if provided
                val updatedProject = existingProject.copy(
                    projectBudget = existingProject.projectBudget + inputBudget,
                    dueDate = _dueDateMillis.value ?: existingProject.dueDate,
                    lastModified = System.currentTimeMillis()
                )
                repo.updateProject(updatedProject)
                existingProject.projectId
            } else {
                Log.d("ProjectDebug", "Creating new project: $finalProjectName in Category ID: $categoryId")
                val newProject = ProjectEntity(
                    categoryId = categoryId,
                    name = finalProjectName,
                    projectBudget = inputBudget,
                    dueDate = _dueDateMillis.value,
                    notes = if (_duration.value.isNotBlank()) "Duration: ${_duration.value}" else null
                )
                repo.upsertProject(newProject)
            }

            Log.d("ProjectDebug", "Final Project ID for navigation: $projectId")

            // 3. Create Main Task (if name provided)
            if (_taskName.value.isNotBlank()) {
                repo.upsertTask(TaskEntity(projectId = projectId, text = _taskName.value))
            }

            // 4. Create Sub-tasks
            _subTasks.value.forEach { subTaskText ->
                repo.upsertTask(TaskEntity(projectId = projectId, text = subTaskText))
            }

            // 5. Attach Photo (if exists)
            if (uri.isNotBlank()) {
                addPhotoToTask(projectId, uri)
            }

            _savedProjectId.value = projectId
            _savedProjectTitle.value = finalProjectName
            _isSaving.value = false
            _isSaved.value = true
            Log.d("SavePhotoDebug", "ViewModel: saveTask complete. isSaved set to true for ID: $projectId")
        }
    }
}
