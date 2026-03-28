package com.zoewave.probase.photodo.mobile.features.tasks.ui.state

// And define the new model:
data class ProjectListUiModel(
    val projectId: Long,
    val title: String,
    val categoryName: String,
    // ✅ Add the flags to the UI Model
    val isFavorite: Boolean = false,
    val isUrgent: Boolean = false,
    val currentSpend: Double = 0.0,
    val projectBudget: Double = 0.0
) {
    val hasBudget: Boolean = projectBudget > 0
    val remainingBudget: Double = projectBudget - currentSpend
    val absoluteRemainingBudget: Double = kotlin.math.abs(remainingBudget)
    val isOverBudget: Boolean = currentSpend > projectBudget
    val budgetUsagePercent: Float = if (projectBudget > 0) (currentSpend / projectBudget).toFloat() else 0f
    val isNearBudgetLimit: Boolean = budgetUsagePercent >= 0.9f && !isOverBudget
}
