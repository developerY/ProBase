package com.zoewave.probase.goswift.mobile.shots.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.goswift.data.ShotRepository
import com.zoewave.probase.goswift.model.CaffeineShot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class AddShotUiState(
    val mg: String = "20",
    val isSuccess: Boolean = false
)

sealed interface AddShotUiEvent {
    data class MgChanged(val value: String) : AddShotUiEvent
    object SaveShot : AddShotUiEvent
    object BackClicked : AddShotUiEvent
}

@HiltViewModel
class AddShotViewModel @Inject constructor(
    private val repository: ShotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddShotUiState())
    val uiState: StateFlow<AddShotUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddShotUiEvent) {
        when (event) {
            is AddShotUiEvent.MgChanged -> _uiState.update { it.copy(mg = event.value) }
            AddShotUiEvent.SaveShot -> saveShot()
            AddShotUiEvent.BackClicked -> { /* Handled in Route */ }
        }
    }

    private fun saveShot() {
        val mgValue = _uiState.value.mg.toIntOrNull() ?: 0
        val shot = CaffeineShot(
            id = UUID.randomUUID().toString(),
            mg = mgValue,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.addShot(shot)
            _uiState.update { it.copy(isSuccess = true) }
        }
    }
}
