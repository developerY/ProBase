package com.zoewave.probase.features.ar.naillab.ui

import androidx.lifecycle.ViewModel
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class NailLabUiState(
    val colorHex: String = "#FF0000",
    val finish: String = "MATTE",
    val latestResult: HandLandmarkerResult? = null
)

sealed class NailLabEvent {
    data class OnColorChanged(val hex: String) : NailLabEvent()
    data class OnFinishChanged(val finish: String) : NailLabEvent()
    data class OnTrackingResult(val result: HandLandmarkerResult) : NailLabEvent()
}

@HiltViewModel
class NailLabViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NailLabUiState())
    val uiState: StateFlow<NailLabUiState> = _uiState.asStateFlow()

    fun onEvent(event: NailLabEvent) {
        when (event) {
            is NailLabEvent.OnColorChanged -> {
                _uiState.value = _uiState.value.copy(colorHex = event.hex)
            }
            is NailLabEvent.OnFinishChanged -> {
                _uiState.value = _uiState.value.copy(finish = event.finish)
            }
            is NailLabEvent.OnTrackingResult -> {
                _uiState.value = _uiState.value.copy(latestResult = event.result)
            }
        }
    }
}
