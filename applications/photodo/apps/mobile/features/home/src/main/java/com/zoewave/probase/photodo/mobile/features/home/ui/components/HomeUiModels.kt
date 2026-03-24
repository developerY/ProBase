package com.zoewave.probase.photodo.mobile.features.home.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Summary metrics for the top visual graphic card.
 */
data class HomeOverviewSummaryUiModel(
    val totalCategories: Int,
    val completedTasks: Int,
    val totalTasks: Int,
    val overallProgress: Float // From 0.0f to 1.0f
)

/**
 * A small, compact model for the quick-jump category chips.
 */
data class CategoryQuickJumpUiModel(
    val id: Long,
    val name: String,
    val progressText: String, // e.g., "5/50 Tasks"
    val progressPercentage: Float, // For the tiny progress bar
    val containerColor: Color,
    val icon: ImageVector
)