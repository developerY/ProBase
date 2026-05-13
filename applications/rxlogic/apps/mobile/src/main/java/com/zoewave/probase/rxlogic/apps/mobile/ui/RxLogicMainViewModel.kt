package com.zoewave.probase.rxlogic.apps.mobile.ui

import androidx.lifecycle.ViewModel
import com.zoewave.probase.rxlogic.model.navigation.RxLogicRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class RxLogicMainUiState(
    val currentRoute: RxLogicRoute = RxLogicRoute.Main,
    val backStack: List<RxLogicRoute> = listOf(RxLogicRoute.Main)
)

@HiltViewModel
class RxLogicMainViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RxLogicMainUiState())
    val uiState: StateFlow<RxLogicMainUiState> = _uiState.asStateFlow()

    fun navigateTo(route: RxLogicRoute) {
        _uiState.update { it.copy(currentRoute = route, backStack = it.backStack + route) }
    }

    fun navigateBack() {
        _uiState.update { 
            if (it.backStack.size > 1) {
                val newStack = it.backStack.dropLast(1)
                it.copy(currentRoute = newStack.last(), backStack = newStack)
            } else it
        }
    }
}
