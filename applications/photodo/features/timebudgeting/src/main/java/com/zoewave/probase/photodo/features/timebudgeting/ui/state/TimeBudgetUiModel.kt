package com.zoewave.probase.photodo.features.timebudgeting.ui.state

import androidx.compose.runtime.Immutable

@Immutable
data class TimeBudgetUiModel(
    val categoryId: Long,
    val categoryName: String,
    val targetTimeMillis: Long,
    val loggedTimeMillis: Long,
    val period: String
) {
    val progress: Float = if (targetTimeMillis > 0) loggedTimeMillis.toFloat() / targetTimeMillis.toFloat() else 0f
    val isOverBudget: Boolean = loggedTimeMillis > targetTimeMillis
}
