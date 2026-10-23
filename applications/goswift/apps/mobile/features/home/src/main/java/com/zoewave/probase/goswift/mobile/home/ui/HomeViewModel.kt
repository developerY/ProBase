package com.zoewave.probase.goswift.mobile.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.goswift.data.caffeine.CaffeineDecayEngine
import com.zoewave.probase.goswift.data.caffeine.CaffeineIntakeRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val decayEngine: CaffeineDecayEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val records = mutableListOf<CaffeineIntakeRecord>()

    init {
        updateState()
    }

    fun logIntake(amountMg: Double) {
        records.add(
            CaffeineIntakeRecord(
                id = UUID.randomUUID().toString(),
                timestamp = Instant.now(),
                amountMg = amountMg,
                sourceName = "Quick Log"
            )
        )
        updateState()
    }

    private fun updateState() {
        viewModelScope.launch {
            val remaining = decayEngine.calculateRemainingMg(
                records = records,
                evaluationTime = Instant.now()
            )
            _uiState.update { it.copy(remainingMg = remaining) }
        }
    }
}
