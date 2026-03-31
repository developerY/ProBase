package com.zoewave.probase.photodo.mobile.features.tasks.ui

import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity

data class SavePhotoUiState(
    val photoUri: String,
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val projects: List<ProjectEntity> = emptyList(),
    val selectedProjectId: Long? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)
