package com.zoewave.probase.kocolor.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.db.KoColorSettings
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.topLevelRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class MainUiState(
    val backStack: PersistentList<KoColorRoute> = persistentListOf(KoColorRoute.Home),
    val theme: String = "SYSTEM",
    val palette: String = "CLASSIC",
    val currentTab: KoColorRoute = KoColorRoute.Home
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settings: KoColorSettings,
    private val sessionRepository: FashionSessionRepository
) : ViewModel() {

    val hydrationGoalFlow = settings.hydrationGoalFlow

    private val _backStack = MutableStateFlow<PersistentList<KoColorRoute>>(persistentListOf(KoColorRoute.Home))

    val uiState: StateFlow<MainUiState> = combine(
        _backStack,
        settings.appThemeFlow,
        settings.colorPaletteFlow
    ) { backStack, theme, palette ->
        val currentRoute = backStack.last()
        // Find which top level tab this route belongs to
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

    fun navigateTo(route: KoColorRoute) {
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

    fun navigateBack() {
        if (_backStack.value.size > 1) {
            _backStack.value = _backStack.value.removeAt(_backStack.value.size - 1)
        }
    }

    fun onFaceCaptured(uri: String) {
        sessionRepository.setFaceUri(uri)
    }

    fun onClothesCaptured(uri: String) {
        sessionRepository.setClothesUri(uri)
    }

    fun onHairCaptured(uri: String) {
        sessionRepository.setHairUri(uri)
    }

    fun onShoesCaptured(uri: String) {
        sessionRepository.setShoesUri(uri)
    }

    fun onInventoryItemCaptured(uri: String) {
        sessionRepository.setCapturedItemUri(uri)
    }

    fun onColorCaptured(uri: String) {
        sessionRepository.setCapturedItemUri(uri) // Reusing for simplicity or can have dedicated field
    }

    fun onCodeScanned(code: String) {
        sessionRepository.setLastScannedCode(code)
    }
}
