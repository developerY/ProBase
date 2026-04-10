package com.zoewave.probase.photodo.features.timebudgeting.ui.state

import androidx.compose.runtime.Immutable

@Immutable
data class TimeBudgetUiState(
    val isLoading: Boolean = false,
    val budgets: List<TimeBudgetUiModel> = emptyList(),
    val errorMessage: String? = null
)
