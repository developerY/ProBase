package com.zoewave.probase.features.camera.ui

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CamViewModel @Inject constructor(
    // private val repository: BaseProRepo
) : ViewModel() {

    private val TAG = "CamViewModel"

    // 1. Initialize the UI in the Active state so the Viewfinder opens immediately
    private val _uiState = MutableStateFlow<CamUIState>(CamUIState.Active())
    val uiState: StateFlow<CamUIState> = _uiState.asStateFlow()

    fun setCameraTarget(target: String) {
        val selector = when (target) {
            "face", "face_simulator", "hair" -> CameraSelector.DEFAULT_FRONT_CAMERA
            else -> {
                if (target.startsWith("ritual_step:")) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
            }
        }
        _uiState.update { current ->
            when (current) {
                is CamUIState.Active -> current.copy(cameraSelector = selector, target = target)
                is CamUIState.Loading -> current.copy(cameraSelector = selector, target = target)
                is CamUIState.Error -> current.copy(cameraSelector = selector, target = target)
            }
        }
    }

    // 🚀 1. The new One-Time Event Channel
    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // 2. The single entry point for all UI actions
    fun onEvent(event: CamEvent) {
        when (event) {
            is CamEvent.AddItem -> {
                handleImageCaptured(event.name, event.description, event.imgPath)
            }
            is CamEvent.ClearLastCapture -> {
                clearLastCapture()
            }
            is CamEvent.ConsumePhotoSavedEvent -> {
                consumeSavedEvent()
            }
        }
    }

    // 3. Business Logic: Saving and Updating State
    private fun handleImageCaptured(name: String, description: String, imgPath: String) {
        viewModelScope.launch {
            Log.d("CameraDebug", "1. ViewModel saved image: $imgPath")
            try {
                // TODO: Save to your Room Database!
                // val newPhotoEntity = PhotoEntity(name = name, uri = imgPath, timestamp = System.currentTimeMillis())
                // repository.insertPhoto(newPhotoEntity)

                Log.d(TAG, "Image processed successfully: $imgPath")

                // Update the UI state to display the thumbnail preview using the new URI
                _uiState.update { currentState ->
                    if (currentState is CamUIState.Active) {
                        currentState.copy(lastCapturedUri = imgPath)
                    } else {
                        // Fallback in case it was in an Error state but somehow recovered
                        CamUIState.Active(lastCapturedUri = imgPath)
                    }
                }

                // 🚀 2. Broadcast the URI straight to the UI Channel!
                Log.d("CameraDebug", "2. ViewModel broadcasting URI to Channel")
                _uiEvent.send(imgPath)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to process captured image", e)
                // Safely transition to the Error state so the UI can show a Snackbar or warning
                _uiState.value = CamUIState.Error("Failed to save image: ${e.localizedMessage}")
            }
        }
    }

    private fun consumeSavedEvent() {
        _uiState.update { currentState ->
            if (currentState is CamUIState.Active) {
                currentState.copy(photoSavedUri = null)
            } else {
                currentState
            }
        }
    }

    // 4. Business Logic: Clearing the UI
    private fun clearLastCapture() {
        _uiState.update { currentState ->
            if (currentState is CamUIState.Active) {
                currentState.copy(lastCapturedUri = null)
            } else {
                CamUIState.Active()
            }
        }
    }
}