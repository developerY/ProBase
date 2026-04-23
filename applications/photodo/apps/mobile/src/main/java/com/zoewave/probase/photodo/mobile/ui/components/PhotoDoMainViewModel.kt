package com.zoewave.probase.photodo.mobile.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.applications.photodo.db.repo.AppSettingsRepository
import com.zoewave.probase.photodo.model.navigation.PhotoTodoRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PhotoDoMainViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _backStack = MutableStateFlow<List<PhotoTodoRoute>>(
        listOf(PhotoTodoRoute.Home)
    )

    val uiState: StateFlow<PhotoDoMainUiState> = combine(
        appSettingsRepository.isAiEnabledFlow,
        appSettingsRepository.themePreferenceFlow,
        appSettingsRepository.palettePreferenceFlow,
        appSettingsRepository.paneContrastFlow,
        _backStack
    ) { isAiEnabled, theme, palette, contrast, backStack ->
        PhotoDoMainUiState(
            isAiEnabled = isAiEnabled,
            theme = theme,
            palette = palette,
            paneContrast = contrast,
            backStack = backStack,
            currentRoute = backStack.lastOrNull() ?: PhotoTodoRoute.Home
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PhotoDoMainUiState()
    )

    fun onEvent(event: PhotoDoMainEvent) {
        when (event) {
            is PhotoDoMainEvent.OnNavigateTo -> {
                _backStack.update { currentStack ->
                    if (event.route == PhotoTodoRoute.Home) {
                        listOf(PhotoTodoRoute.Home)
                    } else if (event.route != currentStack.lastOrNull()) {
                        currentStack + event.route
                    } else {
                        currentStack
                    }
                }
            }
            PhotoDoMainEvent.OnNavigateBack -> {
                _backStack.update { currentStack ->
                    if (currentStack.size > 1) currentStack.dropLast(1) else currentStack
                }
            }
        }
    }
}

data class PhotoDoMainUiState(
    val isAiEnabled: Boolean = false,
    val theme: String = "SYSTEM",
    val palette: String = "DEFAULT",
    val paneContrast: String = "TINTED",
    val backStack: List<PhotoTodoRoute> = listOf(PhotoTodoRoute.Home),
    val currentRoute: PhotoTodoRoute = PhotoTodoRoute.Home
)

sealed interface PhotoDoMainEvent {
    data class OnNavigateTo(val route: PhotoTodoRoute) : PhotoDoMainEvent
    data object OnNavigateBack : PhotoDoMainEvent
}
