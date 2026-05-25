package com.zoewave.probase.features.ar.facelab.ui

import androidx.lifecycle.ViewModel
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class FaceLabUiState(
    val colorHex: String = "#FF0000",
    val category: String = "LIPS",
    val isFrontCamera: Boolean = true,
    val latestResult: FaceLandmarkerResult? = null,
    val inputImageWidth: Int = 1,
    val inputImageHeight: Int = 1
)

sealed class FaceLabEvent {
    data class OnColorChanged(val hex: String) : FaceLabEvent()
    data class OnCategoryChanged(val category: String) : FaceLabEvent()
    data object OnToggleCamera : FaceLabEvent()
    data class OnTrackingResult(
        val result: FaceLandmarkerResult,
        val inputWidth: Int,
        val inputHeight: Int
    ) : FaceLabEvent()
}

@HiltViewModel
class FaceLabViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(FaceLabUiState())
    val uiState: StateFlow<FaceLabUiState> = _uiState.asStateFlow()

    fun onEvent(event: FaceLabEvent) {
        when (event) {
            is FaceLabEvent.OnColorChanged -> {
                _uiState.value = _uiState.value.copy(colorHex = event.hex)
            }
            is FaceLabEvent.OnCategoryChanged -> {
                _uiState.value = _uiState.value.copy(category = event.category)
            }
            is FaceLabEvent.OnToggleCamera -> {
                _uiState.value = _uiState.value.copy(isFrontCamera = !_uiState.value.isFrontCamera)
            }
            is FaceLabEvent.OnTrackingResult -> {
                _uiState.value = _uiState.value.copy(
                    latestResult = event.result,
                    inputImageWidth = event.inputWidth,
                    inputImageHeight = event.inputHeight
                )
            }
        }
    }
}
