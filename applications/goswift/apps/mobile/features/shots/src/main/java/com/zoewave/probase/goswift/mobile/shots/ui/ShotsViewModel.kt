package com.zoewave.probase.goswift.mobile.shots.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.goswift.data.ShotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShotsViewModel @Inject constructor(
    private val repository: ShotRepository
) : ViewModel() {

    val uiState: StateFlow<ShotsUiState> = repository.getAllShots()
        .map { shots ->
            ShotsUiState.Success(shots = shots)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ShotsUiState.Loading
        )

    fun onEvent(event: ShotsUiEvent) {
        when (event) {
            is ShotsUiEvent.DeleteShot -> {
                viewModelScope.launch {
                    repository.deleteShot(event.id)
                }
            }
        }
    }
}
