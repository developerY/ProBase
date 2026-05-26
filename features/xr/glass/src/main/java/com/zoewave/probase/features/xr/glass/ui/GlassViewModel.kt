package com.zoewave.probase.features.xr.glass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.ai.firebase.data.FirebaseLiveSessionManager
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GlassViewModel @Inject constructor(
    private val routineDao: RoutineDao,
    private val cosmeticDao: CosmeticDao,
    private val aiSessionManager: FirebaseLiveSessionManager
) : ViewModel() {

    private val _currentDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    private val _isAiActive = MutableStateFlow(false)
    private val _aiAudioLevel = MutableStateFlow(0f)
    
    val uiState: StateFlow<GlassUiState> = combine(
        _currentDate.flatMapLatest { date ->
            val start = date
            val end = start + 86400000L
            routineDao.getRoutinesForDay(start, end).map { routines ->
                val models = routines.map { it.toModel() }
                val currentTime = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                
                // Smart select: If it's late (after 3 PM), show Evening, otherwise Morning
                val targetTime = if (currentTime >= 15) RoutineTime.EVENING else RoutineTime.MORNING
                models.find { it.time == targetTime } ?: models.firstOrNull()
            }
        },
        _isAiActive,
        _aiAudioLevel
    ) { activeRoutine, isAiActive, aiLevel ->
        GlassUiState(
            morningRoutine = activeRoutine,
            isLoading = false,
            isAiActive = isAiActive,
            aiAudioLevel = aiLevel
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlassUiState())

    init {
        // Mock audio level for visual testing when AI is active
        viewModelScope.launch {
            while (true) {
                if (_isAiActive.value) {
                    _aiAudioLevel.value = (0.1f..0.9f).random()
                } else {
                    _aiAudioLevel.value = 0f
                }
                delay(100)
            }
        }
    }

    private fun ClosedRange<Float>.random() = 
        start + (endInclusive - start) * (java.util.Random().nextFloat())

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
                aiSessionManager.stopConversation()
            }
        }
    }

    private fun toggleAi() {
        _isAiActive.value = !_isAiActive.value
        if (_isAiActive.value) {
            try {
                aiSessionManager.startConversation()
            } catch (e: Exception) {
                _isAiActive.value = false
            }
        } else {
            aiSessionManager.stopConversation()
        }
    }

    private fun toggleStep(stepId: String) {
        android.util.Log.d("GlassRitual", "Toggling step: $stepId")
        viewModelScope.launch {
            val routine = uiState.value.morningRoutine ?: run {
                android.util.Log.e("GlassRitual", "Routine is null")
                return@launch
            }
            val updatedSteps = routine.steps.map { step ->
                if (step.id == stepId) {
                    val newCompleted = !step.isCompleted
                    android.util.Log.d("GlassRitual", "Step ${step.title} -> $newCompleted")
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
