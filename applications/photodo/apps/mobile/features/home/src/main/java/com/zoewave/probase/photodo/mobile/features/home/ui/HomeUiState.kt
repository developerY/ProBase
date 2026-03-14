package com.zoewave.probase.photodo.mobile.features.home.ui

import com.zoewave.photodo.model.PhotoTask

data class HomeUiState(
    val isLoading: Boolean = true,
    val recentPhotoTasks: List<PhotoTask> = emptyList(),
    val errorMessage: String? = null
)