package com.zoewave.probase.photodo.features.camera.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.features.camera.domain.AddPhotoToTaskUseCase
import com.zoewave.probase.photodo.features.camera.ui.state.SavePhotoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SavePhotoViewModel @Inject constructor(
    private val repo: PhotoDoRepo,
    private val addPhotoToTask: AddPhotoToTaskUseCase
) : ViewModel() {

    private val _photoUri = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    private val _isSaving = MutableStateFlow(false)
    private val _isSaved = MutableStateFlow(false)
    private val _newCategoryName = MutableStateFlow("")
    private val _newProjectName = MutableStateFlow("")
    private val _savedProjectId = MutableStateFlow<Long?>(null)
    private val _savedProjectTitle = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SavePhotoUiState> = combine(
        _photoUri,
        repo.getAllCategories(),
        _selectedCategoryId.flatMapLatest { categoryId ->
            if (categoryId != null) repo.getProjectsForCategory(categoryId)
            else flowOf(emptyList())
        },
        _selectedCategoryId,
        _selectedProjectId,
        _isSaving,
        _isSaved,
        _newCategoryName,
        _newProjectName,
        _savedProjectId,
        _savedProjectTitle
    ) { args: Array<Any?> ->
        SavePhotoUiState(
            photoUri = args[0] as String,
            categories = args[1] as List<com.zoewave.probase.applications.photodo.db.entity.CategoryEntity>,
            projects = args[2] as List<com.zoewave.probase.applications.photodo.db.entity.ProjectEntity>,
            selectedCategoryId = args[3] as Long?,
            selectedProjectId = args[4] as Long?,
            isSaving = args[5] as Boolean,
            isSaved = args[6] as Boolean,
            newCategoryName = args[7] as String,
            newProjectName = args[8] as String,
            savedProjectId = args[9] as Long?,
            savedProjectTitle = args[10] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SavePhotoUiState("")
    )

    fun setPhotoUri(uri: String) {
        if (_photoUri.value == uri) return // No-op if it's already set
        
        _photoUri.value = uri
        // Reset all selection and success flags for the new photo
        _selectedCategoryId.value = null
        _selectedProjectId.value = null
        _isSaving.value = false
        _isSaved.value = false
        _newCategoryName.value = ""
        _newProjectName.value = ""
        _savedProjectId.value = null
        _savedProjectTitle.value = null
    }

    fun selectCategory(categoryId: Long) {
        if (categoryId == -1L) {
            _selectedCategoryId.value = null
        } else {
            _selectedCategoryId.value = categoryId
        }
        _selectedProjectId.value = null
        _isSaved.value = false // Reset saved status if they change destination
    }

    fun selectProject(projectId: Long) {
        _selectedProjectId.value = projectId
        _isSaved.value = false // Reset saved status if they change destination
    }

    fun setNewCategoryName(name: String) {
        _newCategoryName.value = name
        _isSaved.value = false
    }

    fun setNewProjectName(name: String) {
        _newProjectName.value = name
        _isSaved.value = false
    }

    fun createAndSelectCategory() {
        val name = _newCategoryName.value
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repo.upsertCategory(com.zoewave.probase.applications.photodo.db.entity.CategoryEntity(name = name))
            _selectedCategoryId.value = id
            _newCategoryName.value = ""
        }
    }

    fun createAndSelectProject() {
        val name = _newProjectName.value
        val categoryId = _selectedCategoryId.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repo.upsertProject(com.zoewave.probase.applications.photodo.db.entity.ProjectEntity(categoryId = categoryId, name = name))
            _selectedProjectId.value = id
            _newProjectName.value = ""
        }
    }

    fun savePhoto() {
        val uri = _photoUri.value
        if (uri.isBlank()) return

        viewModelScope.launch {
            _isSaving.value = true
            
            var projectId = _selectedProjectId.value
            var projectTitle = ""
            
            if (projectId == null && _newProjectName.value.isNotBlank()) {
                val categoryId = _selectedCategoryId.value
                if (categoryId != null) {
                    projectTitle = _newProjectName.value
                    projectId = repo.upsertProject(
                        com.zoewave.probase.applications.photodo.db.entity.ProjectEntity(
                            categoryId = categoryId, 
                            name = projectTitle
                        )
                    )
                }
            } else if (projectId != null) {
                // Fetch the existing title!
                projectTitle = repo.getProjectById(projectId).firstOrNull()?.name ?: "Task"
            }
            
            if (projectId != null) {
                addPhotoToTask(projectId, uri)
                _savedProjectId.value = projectId
                _savedProjectTitle.value = projectTitle
                _isSaving.value = false
                _isSaved.value = true
                
                // Clear selections after save to follow user suggestion and prevent prefilled state
                _selectedCategoryId.value = null
                _selectedProjectId.value = null
                _newCategoryName.value = ""
                _newProjectName.value = ""
            } else {
                _isSaving.value = false
            }
        }
    }
}
