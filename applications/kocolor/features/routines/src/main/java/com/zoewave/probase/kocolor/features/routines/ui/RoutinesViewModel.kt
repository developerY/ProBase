package com.zoewave.probase.kocolor.features.routines.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import com.zoewave.probase.kocolor.features.routines.data.RoutineDefaults
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.RoutineStep
import com.zoewave.probase.kocolor.model.RoutineTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutinesUiState(
    val morningRoutine: BeautyRoutine? = null,
    val eveningRoutine: BeautyRoutine? = null,
    val isLoading: Boolean = true,
    val allProducts: List<CosmeticItem> = emptyList(),
    val activeEditRoutineId: Long? = null,
    val showEditDialog: Boolean = false,
    val draftStep: RoutineStep = RoutineStep(title = "")
) {
    val activeEditRoutine: BeautyRoutine?
        get() = if (morningRoutine?.id == activeEditRoutineId) morningRoutine else eveningRoutine?.takeIf { it.id == activeEditRoutineId }
}

sealed class RoutinesEvent {
    data class ToggleStep(val routineId: Long, val stepId: String) : RoutinesEvent()
    data class StartEditing(val routineId: Long) : RoutinesEvent()
    data class UpdateRoutine(val routine: BeautyRoutine) : RoutinesEvent()
    data object CloseEditDialog : RoutinesEvent()
    data class UpdateDraftStep(val step: RoutineStep) : RoutinesEvent()
    data class AddStep(val routineId: Long) : RoutinesEvent()
    data class RemoveStep(val routineId: Long, val stepId: String) : RoutinesEvent()
    data class LinkProduct(val routineId: Long, val stepId: String, val productId: Long) : RoutinesEvent()
    data class ReorderSteps(val routineId: Long, val fromIndex: Int, val toIndex: Int) : RoutinesEvent()
}

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineDao: RoutineDao,
    private val cosmeticDao: CosmeticDao
) : ViewModel() {

    private val _currentDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    private val _activeEditRoutineId = MutableStateFlow<Long?>(null)
    private val _showEditDialog = MutableStateFlow(false)
    private val _draftStep = MutableStateFlow(RoutineStep(title = ""))

    val uiState: StateFlow<RoutinesUiState> = combine(
        _currentDate.flatMapLatest { date ->
            val start = date
            val end = start + 86400000L
            routineDao.getRoutinesForDay(start, end).onEach { entities ->
                if (entities.isEmpty()) {
                    initializeDay(date)
                }
            }
        },
        cosmeticDao.getAllCosmetics(),
        _activeEditRoutineId,
        _showEditDialog,
        _draftStep
    ) { routines, cosmetics, activeEditId, showDialog, draft ->
        val models = routines.map { it.toModel() }
        RoutinesUiState(
            morningRoutine = models.find { it.time == RoutineTime.MORNING },
            eveningRoutine = models.find { it.time == RoutineTime.EVENING },
            isLoading = false,
            allProducts = cosmetics.map { it.toModel() },
            activeEditRoutineId = activeEditId,
            showEditDialog = showDialog,
            draftStep = draft
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoutinesUiState())

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

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
            is RoutinesEvent.ToggleStep -> toggleStep(event.routineId, event.stepId)
            is RoutinesEvent.StartEditing -> {
                _activeEditRoutineId.value = event.routineId
                _showEditDialog.value = true
            }
            is RoutinesEvent.CloseEditDialog -> {
                _showEditDialog.value = false
                _activeEditRoutineId.value = null
            }
            is RoutinesEvent.UpdateRoutine -> updateRoutine(event.routine)
            is RoutinesEvent.UpdateDraftStep -> _draftStep.value = event.step
            is RoutinesEvent.AddStep -> addStepToActive(event.routineId)
            is RoutinesEvent.RemoveStep -> removeStepFromActive(event.routineId, event.stepId)
            is RoutinesEvent.LinkProduct -> linkProductToStep(event.routineId, event.stepId, event.productId)
            is RoutinesEvent.ReorderSteps -> reorderSteps(event.routineId, event.fromIndex, event.toIndex)
        }
    }

    private fun toggleStep(routineId: Long, stepId: String) {
        viewModelScope.launch {
            val routine = (uiState.value.morningRoutine ?: uiState.value.eveningRoutine)?.takeIf { it.id == routineId }
                ?: return@launch
            
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

    private fun updateRoutine(routine: BeautyRoutine) {
        viewModelScope.launch {
            routineDao.updateRoutine(routine.toEntity())
        }
    }

    private fun addStepToActive(routineId: Long) {
        val routine = uiState.value.activeEditRoutine ?: return
        val newStep = _draftStep.value
        if (newStep.title.isBlank()) return
        
        val updatedRoutine = routine.copy(steps = routine.steps + newStep)
        _draftStep.value = RoutineStep(title = "")
        updateRoutine(updatedRoutine)
    }

    private fun removeStepFromActive(routineId: Long, stepId: String) {
        val routine = uiState.value.activeEditRoutine ?: return
        val updatedRoutine = routine.copy(steps = routine.steps.filter { it.id != stepId })
        updateRoutine(updatedRoutine)
    }

    private fun linkProductToStep(routineId: Long, stepId: String, productId: Long) {
        val routine = uiState.value.activeEditRoutine ?: return
        val updatedSteps = routine.steps.map { step ->
            if (step.id == stepId) {
                val newIds = if (step.productIds.contains(productId)) step.productIds - productId else step.productIds + productId
                step.copy(productIds = newIds)
            } else step
        }
        val updatedRoutine = routine.copy(steps = updatedSteps)
        updateRoutine(updatedRoutine)
    }

    private fun reorderSteps(routineId: Long, fromIndex: Int, toIndex: Int) {
        val currentRoutine = (uiState.value.morningRoutine ?: uiState.value.eveningRoutine)?.takeIf { it.id == routineId }
            ?: return
        
        val updatedSteps = currentRoutine.steps.toMutableList().apply {
            val item = removeAt(fromIndex)
            add(toIndex, item)
        }.mapIndexed { index, step ->
            step.copy(layeringOrder = index)
        }
        
        updateRoutine(currentRoutine.copy(steps = updatedSteps))
    }

    private fun RoutineEntity.toModel() = BeautyRoutine(
        id = id,
        title = title,
        time = time,
        steps = steps,
        date = date
    )

    private fun BeautyRoutine.toEntity() = RoutineEntity(
        id = id,
        title = title,
        time = time,
        steps = steps,
        date = date
    )

    private fun CosmeticItemEntity.toModel() = CosmeticItem(
        id = id, name = name, brand = brand, category = category,
        colorHex = colorHex, shadeName = shadeName, imageUrl = imageUrl,
        price = price, volume = volume, usageCount = usageCount,
        openedDate = openedDate, paoMonths = paoMonths, timestamp = timestamp,
        amountRemaining = amountRemaining, amountPerUse = amountPerUse
    )

    private fun CosmeticItem.toEntity() = CosmeticItemEntity(
        id = id, name = name, brand = brand, category = category,
        colorHex = colorHex, shadeName = shadeName, imageUrl = imageUrl,
        price = price, volume = volume, usageCount = usageCount,
        openedDate = openedDate, paoMonths = paoMonths, timestamp = timestamp,
        amountRemaining = amountRemaining, amountPerUse = amountPerUse
    )
}
