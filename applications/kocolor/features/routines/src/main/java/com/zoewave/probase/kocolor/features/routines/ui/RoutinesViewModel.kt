package com.zoewave.probase.kocolor.features.routines.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import com.zoewave.probase.kocolor.features.routines.data.RoutineDefaults
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.RoutineStep
import com.zoewave.probase.kocolor.model.RoutineTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

sealed class RoutinesUiState {
    data object Loading : RoutinesUiState()
    data class Success(
        val morningRoutine: BeautyRoutine?,
        val eveningRoutine: BeautyRoutine?
    ) : RoutinesUiState()
}

sealed class RoutinesEvent {
    data class ToggleStep(val routine: BeautyRoutine, val stepId: String) : RoutinesEvent()
}

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineDao: RoutineDao
) : ViewModel() {

    private val _currentDate = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis)

    val uiState: StateFlow<RoutinesUiState> = _currentDate.flatMapLatest { date ->
        val startOfDay = date
        val endOfDay = date + 24 * 60 * 60 * 1000
        routineDao.getRoutinesForDay(startOfDay, endOfDay).map { entities ->
            if (entities.isEmpty()) {
                initializeDay(date)
                RoutinesUiState.Loading
            } else {
                val morning = entities.find { it.time == RoutineTime.MORNING }?.toModel()
                val evening = entities.find { it.time == RoutineTime.EVENING }?.toModel()
                RoutinesUiState.Success(morning, evening)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoutinesUiState.Loading)

    private fun initializeDay(date: Long) {
        viewModelScope.launch {
            routineDao.insertRoutine(RoutineEntity(
                title = "morning beautiful routine",
                time = RoutineTime.MORNING,
                steps = RoutineDefaults.getMorningRoutine(),
                date = date
            ))
            routineDao.insertRoutine(RoutineEntity(
                title = "Evening Routine",
                time = RoutineTime.EVENING,
                steps = RoutineDefaults.getEveningRoutine(),
                date = date
            ))
        }
    }

    fun onEvent(event: RoutinesEvent) {
        when (event) {
            is RoutinesEvent.ToggleStep -> toggleStep(event.routine, event.stepId)
        }
    }

    private fun toggleStep(routine: BeautyRoutine, stepId: String) {
        viewModelScope.launch {
            val updatedSteps = routine.steps.map {
                if (it.id == stepId) it.copy(isCompleted = !it.isCompleted) else it
            }
            routineDao.updateRoutine(RoutineEntity(
                id = routine.id,
                title = routine.title,
                time = routine.time,
                steps = updatedSteps,
                date = routine.date
            ))
        }
    }

    private fun RoutineEntity.toModel() = BeautyRoutine(
        id = id,
        title = title,
        time = time,
        steps = steps,
        date = date
    )
}
