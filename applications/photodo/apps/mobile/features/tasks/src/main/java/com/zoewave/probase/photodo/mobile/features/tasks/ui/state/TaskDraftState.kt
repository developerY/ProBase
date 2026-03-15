package com.zoewave.probase.photodo.mobile.features.tasks.ui.state

// Add this below your main TasksUiState
data class TaskDraftState(
    val selectedCategoryId: Long? = null, // Null means they haven't picked or created one yet
    val newCategoryName: String = "",     // If they are creating a new one on the fly
    val listTitle: String = "",
    val listDescription: String = "",
    val pendingTaskItems: List<String> = emptyList(), // Just strings until saved
    val pendingPhotoUris: List<String> = emptyList()
)