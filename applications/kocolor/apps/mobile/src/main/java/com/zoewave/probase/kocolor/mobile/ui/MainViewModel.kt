package com.zoewave.probase.kocolor.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.db.KoColorSettings
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.topLevelRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val backStack: PersistentList<KoColorRoute> = persistentListOf(KoColorRoute.Home),
    val theme: String = "SYSTEM",
    val palette: String = "CLASSIC",
    val currentTab: KoColorRoute = KoColorRoute.Home
)

sealed class MainEvent {
    data class NavigateTo(val route: KoColorRoute) : MainEvent()
    data object NavigateBack : MainEvent()
    data class FaceCaptured(val uri: String) : MainEvent()
    data class ClothesCaptured(val uri: String) : MainEvent()
    data class HairCaptured(val uri: String) : MainEvent()
    data class ShoesCaptured(val uri: String) : MainEvent()
    data class InventoryItemCaptured(val uri: String) : MainEvent()
    data class RitualStepCaptured(val routineId: Long, val stepId: String, val uri: String) : MainEvent()
    data class ColorCaptured(val uri: String) : MainEvent()
    data class CodeScanned(val code: String) : MainEvent()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settings: KoColorSettings,
    private val sessionRepository: FashionSessionRepository,
    private val routineDao: com.zoewave.probase.kocolor.db.dao.RoutineDao
) : ViewModel() {

    val hydrationGoalFlow = settings.hydrationGoalFlow

    private val _backStack = MutableStateFlow<PersistentList<KoColorRoute>>(persistentListOf(KoColorRoute.Home))

    val uiState: StateFlow<MainUiState> = combine(
        _backStack,
        settings.appThemeFlow,
        settings.colorPaletteFlow
    ) { backStack, theme, palette ->
        val currentRoute = backStack.last()
        val currentTab = topLevelRoutes.find { it::class == currentRoute::class } 
            ?: topLevelRoutes.first()

        MainUiState(
            backStack = backStack,
            theme = theme,
            palette = palette,
            currentTab = currentTab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.NavigateTo -> navigateTo(event.route)
            MainEvent.NavigateBack -> navigateBack()
            is MainEvent.FaceCaptured -> sessionRepository.setFaceUri(event.uri)
            is MainEvent.ClothesCaptured -> sessionRepository.setClothesUri(event.uri)
            is MainEvent.HairCaptured -> sessionRepository.setHairUri(event.uri)
            is MainEvent.ShoesCaptured -> sessionRepository.setShoesUri(event.uri)
            is MainEvent.InventoryItemCaptured -> sessionRepository.setCapturedItemUri(event.uri)
            is MainEvent.RitualStepCaptured -> handleRitualStepCaptured(event.routineId, event.stepId, event.uri)
            is MainEvent.ColorCaptured -> sessionRepository.setCapturedItemUri(event.uri)
            is MainEvent.CodeScanned -> sessionRepository.setLastScannedCode(event.code)
        }
    }

    private fun navigateTo(route: KoColorRoute) {
        if (route == KoColorRoute.Back) {
            navigateBack()
            return
        }
        if (route in topLevelRoutes) {
            _backStack.value = persistentListOf(route)
        } else {
            _backStack.value = _backStack.value.add(route)
        }
    }

    private fun navigateBack() {
        if (_backStack.value.size > 1) {
            _backStack.value = _backStack.value.removeAt(_backStack.value.size - 1)
        }
    }

    private fun handleRitualStepCaptured(routineId: Long, stepId: String, uri: String) {
        viewModelScope.launch {
            val routine = routineDao.getRoutineById(routineId).first() ?: return@launch
            val model = routine.toModel()
            val updatedSteps = model.steps.map {
                if (it.id == stepId) it.copy(photoUris = it.photoUris + uri) else it
            }
            routineDao.updateRoutine(model.copy(steps = updatedSteps).toEntity())
        }
    }
}
