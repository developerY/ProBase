package com.zoewave.probase.goswift.mobile.hydration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.goswift.data.HealthRepository
import com.zoewave.probase.goswift.data.HydrationRepository
import com.zoewave.probase.goswift.data.ShotRepository
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
    private val hydrationRepository: HydrationRepository,
    private val shotRepository: ShotRepository,
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HydrationUiState> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            combine(
                getDailyHydrationFlow(),
                calculateTargetFlow()
            ) { dailyData, target ->
                HydrationUiState.Success(
                    dailyTotalLiters = dailyData.total,
                    targetLiters = target,
                    recentLogs = dailyData.logs
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HydrationUiState.Loading
        )

    private fun getDailyHydrationFlow() = flow {
        val now = Instant.now()
        val startOfDay = now.truncatedTo(ChronoUnit.DAYS)
        val records = hydrationRepository.getHydrationRecords(startOfDay, now)
        
        val logs = records.map { 
            HydrationLog(
                id = it.metadata.id,
                amountLiters = it.volume.inLiters,
                timestamp = it.startTime.toEpochMilli()
            )
        }.sortedByDescending { it.timestamp }
        
        emit(DailyData(logs.sumOf { it.amountLiters }, logs))
    }

    private fun calculateTargetFlow() = flow {
        // Base target 2.0L
        // + 0.5L for every 100mg caffeine
        // + 0.5L for every 30m exercise
        val now = Instant.now()
        val startOfDay = now.truncatedTo(ChronoUnit.DAYS)
        
        val shots = shotRepository.getAllShots() // Note: this is a flow, but we need current snapshot for simple target
        // For simplicity, we'll fetch once in this flow.
        
        val exerciseSessions = healthRepository.getExerciseSessions(startOfDay, now)
        val exerciseMinutes = exerciseSessions.sumOf { 
            java.time.Duration.between(it.startTime, it.endTime).toMinutes() 
        }
        
        var target = 2.0
        target += (exerciseMinutes / 30) * 0.5
        
        // We'll also account for caffeine if we had easy access to current total here.
        // For now, base + exercise.
        
        emit(target)
    }

    private data class DailyData(val total: Double, val logs: List<HydrationLog>)

    fun onEvent(event: HydrationUiEvent) {
        when (event) {
            is HydrationUiEvent.AddWater -> {
                viewModelScope.launch {
                    hydrationRepository.addHydrationRecord(event.liters, Instant.now())
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
