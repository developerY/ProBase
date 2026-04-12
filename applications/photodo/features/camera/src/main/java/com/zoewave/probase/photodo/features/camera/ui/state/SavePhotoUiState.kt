package com.zoewave.probase.photodo.features.camera.ui.state

import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity

data class SavePhotoUiState(
    val photoUri: String,
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val projects: List<ProjectEntity> = emptyList(),
    val selectedProjectId: Long? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val newCategoryName: String = "",
    val newProjectName: String = "",
    val savedProjectId: Long? = null,
    val savedProjectTitle: String? = null
)
