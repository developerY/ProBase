package com.zoewave.probase.kocolor.features.routines.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import com.zoewave.probase.kocolor.features.routines.data.RoutineDefaults
import com.zoewave.probase.kocolor.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutinesUiState(
    val morningRoutine: BeautyRoutine? = null,
    val mealsRoutine: BeautyRoutine? = null,
    val eveningRoutine: BeautyRoutine? = null,
    val isLoading: Boolean = true,
    val allProducts: List<CosmeticItem> = emptyList(),
    val activeEditRoutineId: Long? = null,
    val showEditDialog: Boolean = false,
    val draftStep: RoutineStep = RoutineStep(title = ""),
    val glassButtonState: GlassButtonState = GlassButtonState.NO_GLASSES
) {
    val activeEditRoutine: BeautyRoutine?
        get() = when (activeEditRoutineId) {
            morningRoutine?.id -> morningRoutine
            mealsRoutine?.id -> mealsRoutine
            eveningRoutine?.id -> eveningRoutine
            else -> null
        }
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
    data class LinkMeal(val routineId: Long, val stepId: String, val mealId: String?) : RoutinesEvent()
    data class ReorderSteps(val routineId: Long, val fromIndex: Int, val toIndex: Int) : RoutinesEvent()
    data class ResetRoutine(val routineId: Long) : RoutinesEvent()
    data class ProjectToGlass(val time: RoutineTime) : RoutinesEvent()
    data class AddJournalEntry(val routineId: Long, val stepId: String, val text: String) : RoutinesEvent()
    data class DeleteJournalEntry(val routineId: Long, val stepId: String, val entryId: String) : RoutinesEvent()
    data class AddStepPhoto(val routineId: Long, val stepId: String, val uri: String) : RoutinesEvent()
    data class RemoveStepPhoto(val routineId: Long, val stepId: String, val uri: String) : RoutinesEvent()
}

sealed class RoutinesSideEffect {
    data class LaunchGlassProjection(val time: RoutineTime) : RoutinesSideEffect()
}

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineDao: RoutineDao,
    private val cosmeticDao: CosmeticDao,
    private val fashionRepository: FashionRepository
) : ViewModel() {

    private val _sideEffect = MutableSharedFlow<RoutinesSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    private val _currentDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    private val _activeEditRoutineId = MutableStateFlow<Long?>(null)
    private val _showEditDialog = MutableStateFlow(false)
    private val _draftStep = MutableStateFlow(RoutineStep(title = ""))

    val uiState: StateFlow<RoutinesUiState> = combine(
        _currentDate.flatMapLatest { date ->
            val start = date
            val end = start + 86400000L
            routineDao.getRoutinesForDay(start, end).onEach { entities ->
                if (entities.size < 3) {
                    initializeDay(date, entities)
                } else {
                    patchRoutineMetadata(entities)
                }
            }
        },
        cosmeticDao.getAllCosmetics(),
        _activeEditRoutineId,
        _showEditDialog,
        _draftStep,
        fashionRepository.isGlassConnected,
        fashionRepository.isGlassSessionActive
    ) { array ->
        val modelList = array[0] as List<RoutineEntity>
        val cosmeticList = array[1] as List<com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity>
        val activeEditRoutineIdVal = array[2] as Long?
        val showEditDialogVal = array[3] as Boolean
        val draftStepVal = array[4] as RoutineStep
        val isGlassConnectedVal = array[5] as Boolean
        val isGlassSessionActiveVal = array[6] as Boolean

        val models = modelList.map { it.toModel() }
        
        val btnState = when {
            !isGlassConnectedVal -> GlassButtonState.NO_GLASSES
            isGlassSessionActiveVal -> GlassButtonState.PROJECTING
            else -> GlassButtonState.READY_TO_START
        }

        RoutinesUiState(
            morningRoutine = models.find { it.time == RoutineTime.MORNING },
            mealsRoutine = models.find { it.time == RoutineTime.MEALS },
            eveningRoutine = models.find { it.time == RoutineTime.EVENING },
            isLoading = false,
            allProducts = cosmeticList.map { it.toModel() },
            activeEditRoutineId = activeEditRoutineIdVal,
            showEditDialog = showEditDialogVal,
            draftStep = draftStepVal,
            glassButtonState = btnState
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoutinesUiState())

    fun updateGlassConnection(isConnected: Boolean) {
        fashionRepository.updateGlassConnectionState(isConnected)
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun initializeDay(date: Long, existing: List<RoutineEntity> = emptyList()) {
        val existingTimes = existing.map { it.time }.toSet()
        viewModelScope.launch {
            if (RoutineTime.MORNING !in existingTimes) {
                routineDao.insertRoutine(RoutineEntity(
                    title = "morning beautiful routine",
                    time = RoutineTime.MORNING,
                    steps = RoutineDefaults.getMorningRoutine(),
                    date = date
                ))
            }
            if (RoutineTime.MEALS !in existingTimes) {
                routineDao.insertRoutine(RoutineEntity(
                    title = "Meals Routine",
                    time = RoutineTime.MEALS,
                    steps = RoutineDefaults.getMealsRoutine(),
                    date = date
                ))
            }
            if (RoutineTime.EVENING !in existingTimes) {
                routineDao.insertRoutine(RoutineEntity(
                    title = "Evening Routine",
                    time = RoutineTime.EVENING,
                    steps = RoutineDefaults.getEveningRoutine(),
                    date = date
                ))
            }
        }
    }

    private fun patchRoutineMetadata(entities: List<RoutineEntity>) {
        viewModelScope.launch {
            entities.forEach { entity ->
                // Identify mislabeled meals routines: 5 steps but labeled as Evening
                if (entity.time == RoutineTime.EVENING && entity.steps.size == 5) {
                    routineDao.updateRoutine(entity.copy(
                        title = "Meals Routine",
                        time = RoutineTime.MEALS
                    ))
                }
            }
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
            is RoutinesEvent.LinkMeal -> linkMealToStep(event.routineId, event.stepId, event.mealId)
            is RoutinesEvent.ReorderSteps -> reorderSteps(event.routineId, event.fromIndex, event.toIndex)
            is RoutinesEvent.ResetRoutine -> resetRoutine(event.routineId)
            is RoutinesEvent.ProjectToGlass -> {
                viewModelScope.launch {
                    if (uiState.value.glassButtonState == GlassButtonState.PROJECTING) {
                        fashionRepository.sendGlassCommand("EXIT")
                    } else {
                        _sideEffect.emit(RoutinesSideEffect.LaunchGlassProjection(event.time))
                    }
                }
            }
            is RoutinesEvent.AddJournalEntry -> addJournalEntry(event.routineId, event.stepId, event.text)
            is RoutinesEvent.DeleteJournalEntry -> deleteJournalEntry(event.routineId, event.stepId, event.entryId)
            is RoutinesEvent.AddStepPhoto -> addStepPhoto(event.routineId, event.stepId, event.uri)
            is RoutinesEvent.RemoveStepPhoto -> removeStepPhoto(event.routineId, event.stepId, event.uri)
        }
    }

    private fun toggleStep(routineId: Long, stepId: String) {
        viewModelScope.launch {
            val morning = uiState.value.morningRoutine
            val meals = uiState.value.mealsRoutine
            val evening = uiState.value.eveningRoutine
            val routine = when {
                morning?.id == routineId -> morning
                meals?.id == routineId -> meals
                evening?.id == routineId -> evening
                else -> null
            } ?: return@launch
            
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

    private fun linkMealToStep(routineId: Long, stepId: String, mealId: String?) {
        val morning = uiState.value.morningRoutine
        val meals = uiState.value.mealsRoutine
        val evening = uiState.value.eveningRoutine
        val routine = when {
            morning?.id == routineId -> morning
            meals?.id == routineId -> meals
            evening?.id == routineId -> evening
            else -> null
        } ?: return

        val updatedSteps = routine.steps.map { step ->
            if (step.id == stepId) step.copy(linkedMealId = mealId) else step
        }
        updateRoutine(routine.copy(steps = updatedSteps))
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

    private fun resetRoutine(routineId: Long) {
        viewModelScope.launch {
            val routine = (uiState.value.morningRoutine ?: uiState.value.eveningRoutine)?.takeIf { it.id == routineId }
                ?: return@launch
            
            val resetSteps = routine.steps.map { it.copy(isCompleted = false) }
            routineDao.updateRoutine(routine.copy(steps = resetSteps).toEntity())
        }
    }

    private fun addJournalEntry(routineId: Long, stepId: String, text: String) {
        val routine = getRoutine(routineId) ?: return
        val updatedSteps = routine.steps.map {
            if (it.id == stepId) {
                it.copy(journalEntries = (it.journalEntries + JournalEntry(text = text)).sortedByDescending { e -> e.timestamp })
            } else it
        }
        updateRoutine(routine.copy(steps = updatedSteps))
    }

    private fun deleteJournalEntry(routineId: Long, stepId: String, entryId: String) {
        val routine = getRoutine(routineId) ?: return
        val updatedSteps = routine.steps.map {
            if (it.id == stepId) {
                it.copy(journalEntries = it.journalEntries.filter { e -> e.id != entryId })
            } else it
        }
        updateRoutine(routine.copy(steps = updatedSteps))
    }

    private fun addStepPhoto(routineId: Long, stepId: String, uri: String) {
        val routine = getRoutine(routineId) ?: return
        val updatedSteps = routine.steps.map {
            if (it.id == stepId) it.copy(photoUris = it.photoUris + uri) else it
        }
        updateRoutine(routine.copy(steps = updatedSteps))
    }

    private fun removeStepPhoto(routineId: Long, stepId: String, uri: String) {
        val routine = getRoutine(routineId) ?: return
        val updatedSteps = routine.steps.map {
            if (it.id == stepId) it.copy(photoUris = it.photoUris - uri) else it
        }
        updateRoutine(routine.copy(steps = updatedSteps))
    }

    private fun getRoutine(routineId: Long): BeautyRoutine? {
        return if (uiState.value.morningRoutine?.id == routineId) uiState.value.morningRoutine 
               else uiState.value.eveningRoutine?.takeIf { it.id == routineId }
    }
}
