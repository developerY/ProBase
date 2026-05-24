package com.zoewave.kocolor.mobile.glass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.model.RoutineTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlassViewModel @Inject constructor(
    private val routineDao: RoutineDao,
    private val cosmeticDao: CosmeticDao
) : ViewModel() {

    private val _currentDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    
    val uiState: StateFlow<GlassUiState> = _currentDate.flatMapLatest { date ->
        val start = date
        val end = start + 86400000L
        routineDao.getRoutinesForDay(start, end).map { routines ->
            val morningRoutine = routines.map { it.toModel() }.find { it.time == RoutineTime.MORNING }
            GlassUiState(
                morningRoutine = morningRoutine,
                isLoading = false
            )
        }
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
            GlassUiEvent.CloseApp -> { /* Handled by Activity */ }
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
            routineDao.updateRoutine(routine.copy(steps = updatedSteps).toEntity())
        }
    }

    private fun incrementProductUsage(productId: Long) {
        viewModelScope.launch {
            cosmeticDao.getCosmeticById(productId).first()?.let { entity ->
                val model = entity.toModel()
                val currentAmount = model.amountRemaining
                val perUse = model.amountPerUse
                
                val updatedAmount = if (currentAmount != null && perUse != null) {
                    (currentAmount - perUse).coerceAtLeast(0.0)
                } else currentAmount
                
                cosmeticDao.updateCosmetic(model.copy(
                    usageCount = model.usageCount + 1,
                    amountRemaining = updatedAmount
                ).toEntity())
            }
        }
    }
}
