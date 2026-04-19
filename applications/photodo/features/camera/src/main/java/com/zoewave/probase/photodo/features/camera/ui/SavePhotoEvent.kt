package com.zoewave.probase.photodo.features.camera.ui

sealed interface SavePhotoEvent {
    data class OnCategoryNameChanged(val name: String) : SavePhotoEvent
    data class OnProjectNameChanged(val name: String) : SavePhotoEvent
    data class OnTaskNameChanged(val name: String) : SavePhotoEvent
    data class OnDurationChanged(val duration: String) : SavePhotoEvent
    data class OnBudgetInputChanged(val budget: String) : SavePhotoEvent
    data class OnAdjustBudget(val adjustment: Double) : SavePhotoEvent
    data class OnDueDateChanged(val timestamp: Long?) : SavePhotoEvent
    data object OnReportIssue : SavePhotoEvent
    data object OnClearAiData : SavePhotoEvent
    data object OnSaveClicked : SavePhotoEvent
    data object OnDismiss : SavePhotoEvent
}
