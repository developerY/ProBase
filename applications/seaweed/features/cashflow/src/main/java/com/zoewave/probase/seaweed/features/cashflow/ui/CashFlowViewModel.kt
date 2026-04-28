package com.zoewave.probase.seaweed.features.cashflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowAwareness
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowEngine
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowRepository
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CashFlowUiState(
    val summary: CashFlowSummary? = null,
    val awareness: CashFlowAwareness? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class CashFlowViewModel @Inject constructor(
    private val repository: CashFlowRepository,
    private val engine: CashFlowEngine
) : ViewModel() {

    val uiState: StateFlow<CashFlowUiState> = repository.getCurrentMonthSummary()
        .map { summary ->
            CashFlowUiState(
                summary = summary,
                awareness = engine.generateAwareness(summary)
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CashFlowUiState(isLoading = true)
        )
}
