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

    data class OnAddExpenseClicked(val description: String, val amount: Double) : TaskDetailEvent
    data class OnDeleteExpense(val expenseId: Long) : TaskDetailEvent

    // --- Checklist Actions ---
    data class OnAddItemClicked(val text: String) : TaskDetailEvent
    data class OnItemCheckedChange(val item: TaskEntity, val isChecked: Boolean) : TaskDetailEvent
    data class OnDeleteItem(val item: TaskEntity) : TaskDetailEvent

    // --- UI Toggles ---
    data object OnCameraClick : TaskDetailEvent
    data object OnBackFromCamera : TaskDetailEvent
    data object OnHelpClicked : TaskDetailEvent

    // UI State Toggles
    data class OnFabMenuToggle(val expanded: Boolean) : TaskDetailEvent
    data class OnShowAddTaskDialog(val show: Boolean) : TaskDetailEvent
    data class OnNewTaskTextChanged(val text: String) : TaskDetailEvent
    data class OnShowAddExpenseDialog(val show: Boolean) : TaskDetailEvent
    data class OnNewExpenseAmountChanged(val amount: String) : TaskDetailEvent
    data class OnNewExpenseDescChanged(val desc: String) : TaskDetailEvent
    data class OnShowDeleteProjectConfirmation(val show: Boolean) : TaskDetailEvent
}
