package com.zoewave.probase.photodo.features.camera.ui.state

import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity

data class SavePhotoUiState(
    val photoUri: String,
    val isFromAi: Boolean = false,
    
    // Form Fields
    val categoryName: String = "",
    val projectName: String = "",
    val taskName: String = "",
    val duration: String = "",
    val budgetInput: String = "",
    val dueDateMillis: Long? = null,
    val subTasks: List<String> = emptyList(),

    // Selections/Data
    val categories: List<CategoryEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    
    // Status
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val savedProjectId: Long? = null,
    val savedProjectTitle: String? = null
)
