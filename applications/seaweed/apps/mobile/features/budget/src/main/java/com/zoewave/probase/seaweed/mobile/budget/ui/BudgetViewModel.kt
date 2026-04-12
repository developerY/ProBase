package com.zoewave.probase.seaweed.mobile.budget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.data.BudgetTargetRepository
import com.zoewave.probase.seaweed.model.BudgetTarget
import com.zoewave.probase.seaweed.model.CategoryOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetTargetRepository,
    private val financialRepository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Loading)
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        financialRepository.getCategoryOverviews()
            .onEach { overviews ->
                _uiState.value = BudgetUiState.Success(overviews)
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: BudgetUiEvent) {
        viewModelScope.launch {
            when (event) {
                is BudgetUiEvent.UpdateBudget -> {
                    repository.saveBudget(BudgetTarget(event.categoryName, event.limitAmount))
                }
                is BudgetUiEvent.DeleteBudget -> {
                    repository.deleteBudget(event.categoryName)
                }
                BudgetUiEvent.OnBackClicked -> { /* Handled in Route */ }
            }
        }
    }
}

sealed interface BudgetUiState {
    object Loading : BudgetUiState
    data class Success(val categories: List<CategoryOverview>) : BudgetUiState
}

sealed interface BudgetUiEvent {
    data class UpdateBudget(val categoryName: String, val limitAmount: Double) : BudgetUiEvent
    data class DeleteBudget(val categoryName: String) : BudgetUiEvent
    object OnBackClicked : BudgetUiEvent
}
