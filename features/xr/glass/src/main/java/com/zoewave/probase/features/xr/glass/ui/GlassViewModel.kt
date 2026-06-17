package com.zoewave.probase.features.xr.glass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.LiveAiRepository
import com.zoewave.probase.core.data.repository.RitualRepository
import com.zoewave.probase.core.model.ritual.RoutineTime
import com.zoewave.probase.features.xr.glass.data.GlassSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlassViewModel @Inject constructor(
    private val ritualRepository: RitualRepository,
    private val liveAiRepository: LiveAiRepository,
    private val glassSessionRepository: GlassSessionRepository
) : ViewModel() {

    private val _currentDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    
    val uiState: StateFlow<GlassUiState> = combine(
        _currentDate.flatMapLatest { date ->
            val start = date
            val end = start + 86400000L
            combine(
                ritualRepository.getRoutinesForDay(start, end),
                glassSessionRepository.requestedRoutineTime
            ) { models, requestedTimeName ->
                val requestedTime = requestedTimeName?.let { 
                    try { RoutineTime.valueOf(it) } catch (e: Exception) { null } 
                }
                
                if (requestedTime != null) {
                    models.find { it.time == requestedTime } ?: models.firstOrNull()
                } else {
                    val currentTime = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    val targetTime = if (currentTime >= 15) RoutineTime.EVENING else RoutineTime.MORNING
                    models.find { it.time == targetTime } ?: models.firstOrNull()
                }
            }
        },
        liveAiRepository.isSessionActive,
        liveAiRepository.audioLevel
    ) { activeRoutine, isAiActive, aiLevel ->
        GlassUiState(
            morningRoutine = activeRoutine,
            isLoading = false,
            isAiActive = isAiActive,
            aiAudioLevel = aiLevel
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlassUiState())

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun onEvent(event: GlassUiEvent) {
        when (event) {
            is GlassUiEvent.ToggleStep -> toggleStep(event.stepId)
            GlassUiEvent.ToggleAi -> toggleAi()
            is GlassUiEvent.CloseApp -> { 
                liveAiRepository.stopSession()
            }
        }
    }

    private fun toggleAi() {
        if (liveAiRepository.isSessionActive.value) {
            liveAiRepository.stopSession()
        } else {
            liveAiRepository.startSession()
        }
    }

    private fun toggleStep(stepId: String) {
        viewModelScope.launch {
            val routine = uiState.value.morningRoutine ?: return@launch
            val updatedSteps = routine.steps.map { step ->
                if (step.id == stepId) {
                    val newCompleted = !step.isCompleted
                    if (newCompleted) {
                        step.productIds.forEach { pid -> incrementProductUsage(pid) }
                    }
                    step.copy(isCompleted = newCompleted)
                } else step
            }
            ritualRepository.updateRoutine(routine.copy(steps = updatedSteps))
        }
    }

    private fun incrementProductUsage(productId: Long) {
        viewModelScope.launch {
            ritualRepository.getCosmeticById(productId).first()?.let { model ->
                val currentAmount = model.amountRemaining
                val perUse = model.amountPerUse
                
                val updatedAmount = if (currentAmount != null && perUse != null) {
                    (currentAmount - perUse).coerceAtLeast(0.0)
                } else currentAmount
                
                ritualRepository.updateCosmetic(model.copy(
                    usageCount = model.usageCount + 1,
                    amountRemaining = updatedAmount
                ))
            }
        }
    }
}
