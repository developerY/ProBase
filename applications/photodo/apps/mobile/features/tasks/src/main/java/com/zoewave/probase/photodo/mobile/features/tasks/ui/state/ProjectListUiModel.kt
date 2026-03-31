package com.zoewave.probase.photodo.mobile.features.tasks.ui.state

import androidx.compose.runtime.Immutable

@Immutable
data class ProjectListUiModel(
    val projectId: Long,
    val title: String,
    val categoryName: String,
    // ✅ Add the flags to the UI Model
    val isFavorite: Boolean = false,
    val isUrgent: Boolean = false,
    val currentSpend: Double = 0.0,
    val projectBudget: Double = 0.0,
    val dueDateMillis: Long? = null,
    val isCompleted: Boolean = false
) {
    val hasBudget: Boolean = projectBudget > 0
    val hasDueDate: Boolean = dueDateMillis != null
    val isOverdue: Boolean = dueDateMillis != null && System.currentTimeMillis() > dueDateMillis
    val isDueSoon: Boolean = dueDateMillis != null && !isOverdue && (dueDateMillis - System.currentTimeMillis()) < 86400000L // 24 hours
    val remainingBudget: Double = projectBudget - currentSpend
    val absoluteRemainingBudget: Double = kotlin.math.abs(remainingBudget)
    val isOverBudget: Boolean = currentSpend > projectBudget
    val budgetUsagePercent: Float = if (projectBudget > 0) (currentSpend / projectBudget).toFloat() else 0f
    val isNearBudgetLimit: Boolean = budgetUsagePercent >= 0.9f && !isOverBudget
    val isAtRisk: Boolean = !isCompleted && (isOverdue || isOverBudget || (isDueSoon && isNearBudgetLimit))
    val isOnTrack: Boolean = !isCompleted && !isAtRisk && !isOverdue && !isOverBudget
    val isActive: Boolean = !isCompleted
    val isSuccessful: Boolean = isCompleted && !isOverBudget && !isOverdue
    val isOverBudgetCompleted: Boolean = isCompleted && isOverBudget
    val isLateCompleted: Boolean = isCompleted && isOverdue
    val isLateAndOverBudget: Boolean = isCompleted && isOverdue && isOverBudget

    val status: ProjectStatus = when {
        isLateAndOverBudget -> ProjectStatus.CRITICAL
        isOverBudgetCompleted || isLateCompleted -> ProjectStatus.WARNING
        isSuccessful -> ProjectStatus.COMPLETED
        isAtRisk -> ProjectStatus.AT_RISK
        isOnTrack -> ProjectStatus.ON_TRACK
        else -> ProjectStatus.DEFAULT
    }
}

enum class ProjectStatus {
    ON_TRACK, AT_RISK, CRITICAL, WARNING, COMPLETED, DEFAULT
}
