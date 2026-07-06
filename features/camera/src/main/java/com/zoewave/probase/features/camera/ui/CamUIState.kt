package com.zoewave.probase.features.camera.ui

import androidx.camera.core.CameraSelector
import androidx.compose.runtime.Immutable

@Immutable
sealed interface CamUIState {

    val cameraSelector: CameraSelector

    // 1. Initializing the camera or checking permissions
    data class Loading(
        override val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) : CamUIState

    // 2. The camera is active and ready to shoot
    data class Active(
        val lastCapturedUri: String? = null,
        val photoSavedUri: String? = null,
        override val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) : CamUIState

    // 3. Something went wrong (e.g., failed to write file to disk)
    data class Error(
        val message: String,
        override val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) : CamUIState
}