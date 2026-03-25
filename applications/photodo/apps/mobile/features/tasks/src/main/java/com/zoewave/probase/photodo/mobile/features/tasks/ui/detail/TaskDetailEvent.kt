package com.zoewave.probase.photodo.mobile.features.tasks.ui.detail


import android.net.Uri
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity

sealed interface TaskDetailEvent {

    // --- Project Level Actions ---
    data object OnDeleteTaskListClicked : TaskDetailEvent
    data object OnEditList : TaskDetailEvent // Triggered to edit title/description

    // --- Photo Actions ---
    data class OnPhotoSaved(val uri: Uri) : TaskDetailEvent
    data class OnDeletePhoto(val photoId: Long) : TaskDetailEvent

    // --- Checklist Actions ---
    data class OnAddItemClicked(val text: String) : TaskDetailEvent
    data class OnItemCheckedChange(val item: TaskEntity, val isChecked: Boolean) : TaskDetailEvent
    data class OnDeleteItem(val item: TaskEntity) : TaskDetailEvent

    // --- UI Toggles ---
    data object OnCameraClick : TaskDetailEvent
    data object OnBackFromCamera : TaskDetailEvent
}