package com.zoewave.probase.photodo.features.timebudgeting.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import com.zoewave.probase.photodo.features.timebudgeting.data.repo.TimeBudgetRepository
import com.zoewave.probase.photodo.features.timebudgeting.ui.state.TimeBudgetUiModel
import com.zoewave.probase.photodo.features.timebudgeting.ui.state.TimeBudgetUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class TimeBudgetViewModel @Inject constructor(
    private val photoDoRepo: PhotoDoRepo,
    private val timeBudgetRepo: TimeBudgetRepository
) : ViewModel() {

    val uiState: StateFlow<TimeBudgetUiState> = combine(
        photoDoRepo.getAllCategories(),
        timeBudgetRepo.getAllTimeBudgets()
    ) { categories, budgets ->
        val budgetModels = budgets.mapNotNull { budget ->
            val category = categories.find { it.categoryId == budget.categoryId }
            if (category != null) {
                TimeBudgetUiModel(
                    categoryId = category.categoryId,
                    categoryName = category.name,
                    targetTimeMillis = budget.targetTimeMillis,
                    loggedTimeMillis = 0L, // Logic to aggregate logs will go here
                    period = budget.period
                )
            } else null
        }
        TimeBudgetUiState(budgets = budgetModels)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimeBudgetUiState(isLoading = true)
    )
}
