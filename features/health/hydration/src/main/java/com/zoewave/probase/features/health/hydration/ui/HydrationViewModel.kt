package com.zoewave.probase.features.health.hydration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HydrationViewModel @Inject constructor(
    private val healthSessionManager: HealthSessionManager
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HydrationUiState> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            getDailyHydrationFlow()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HydrationUiState.Loading
        )

    private fun getDailyHydrationFlow() = flow {
        val now = Instant.now()
        val startOfDay = now.truncatedTo(ChronoUnit.DAYS)
        val records = healthSessionManager.readHydration(startOfDay, now)
        
        val logs = records.map { 
            HydrationLog(
                id = it.metadata.id,
                amountLiters = it.volume.inLiters,
                timestamp = it.startTime.toEpochMilli()
            )
        }.sortedByDescending { it.timestamp }
        
        // Default target 2.7L as requested in previous refinements
        emit(HydrationUiState.Success(logs.sumOf { it.amountLiters }, 2.7, logs))
    }

    fun onEvent(event: HydrationUiEvent) {
        when (event) {
            is HydrationUiEvent.AddWater -> {
                viewModelScope.launch {
                    healthSessionManager.insertHydration(event.liters)
                    refreshTrigger.emit(Unit)
                }
            }
            HydrationUiEvent.Refresh -> {
                viewModelScope.launch {
                    refreshTrigger.emit(Unit)
                }
            }
        }
    }
}
