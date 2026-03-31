package com.zoewave.probase.photodo.mobile.features.tasks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.mobile.features.tasks.domain.AddPhotoToTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
        _newProjectName
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
            newProjectName = args[8] as String
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SavePhotoUiState("")
    )

    fun setPhotoUri(uri: String) {
        _photoUri.value = uri
    }

    fun selectCategory(categoryId: Long) {
        if (categoryId == -1L) {
            _selectedCategoryId.value = null
        } else {
            _selectedCategoryId.value = categoryId
        }
        _selectedProjectId.value = null
    }

    fun selectProject(projectId: Long) {
        _selectedProjectId.value = projectId
    }

    fun setNewCategoryName(name: String) {
        _newCategoryName.value = name
    }

    fun setNewProjectName(name: String) {
        _newProjectName.value = name
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
            
            if (projectId == null && _newProjectName.value.isNotBlank()) {
                val categoryId = _selectedCategoryId.value
                if (categoryId != null) {
                    projectId = repo.upsertProject(
                        com.zoewave.probase.applications.photodo.db.entity.ProjectEntity(
                            categoryId = categoryId, 
                            name = _newProjectName.value
                        )
                    )
                }
            }
            
            if (projectId != null) {
                addPhotoToTask(projectId, uri)
                _isSaving.value = false
                _isSaved.value = true
            } else {
                _isSaving.value = false
            }
        }
    }
}
