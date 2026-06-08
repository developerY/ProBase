package com.zoewave.probase.features.health.hydration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.service.health.HealthSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val isSmartAlertsEnabled = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HydrationUiState> = combine(
        refreshTrigger.onStart { emit(Unit) },
        isSmartAlertsEnabled
    ) { _, enabled ->
        enabled
    }.flatMapLatest { enabled ->
        getDailyHydrationFlow(enabled)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HydrationUiState.Loading
        )

    private fun getDailyHydrationFlow(isSmartAlertsEnabled: Boolean) = flow {
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

        val currentLiters = logs.sumOf { it.amountLiters }
        val targetLiters = 2.7
        val nextReminder = calculateNextReminder(currentLiters, targetLiters)
        
        emit(HydrationUiState.Success(
            dailyTotalLiters = currentLiters, 
            targetLiters = targetLiters, 
            recentLogs = logs,
            isSmartAlertsEnabled = isSmartAlertsEnabled,
            nextReminderTime = if (isSmartAlertsEnabled) nextReminder else null
        ))
    }

    private fun calculateNextReminder(current: Double, target: Double): String? {
        if (current >= target) return null
        
        val now = java.time.LocalTime.now()
        val endOfDay = java.time.LocalTime.of(21, 0) // 9 PM
        if (now.isAfter(endOfDay)) return null

        val remainingLiters = target - current
        val glassesNeeded = remainingLiters / 0.25 // 250ml per glass
        if (glassesNeeded <= 0) return null

        val minutesLeft = java.time.Duration.between(now, endOfDay).toMinutes()
        val intervalMinutes = (minutesLeft / glassesNeeded).toLong()

        val reminderTime = now.plusMinutes(intervalMinutes)
        return reminderTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
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
            is HydrationUiEvent.ToggleSmartAlerts -> {
                isSmartAlertsEnabled.value = event.enabled
            }
        }
    }
}
