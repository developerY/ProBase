package com.zoewave.probase.features.camera.ui

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CamUIState {

    // 1. Initializing the camera or checking permissions
    data object Loading : CamUIState

    // 2. The camera is active and ready to shoot
    data class Active(
        // You can hold the last captured image here if you want
        // the ViewModel to drive the UI thumbnail preview!
        val lastCapturedUri: String? = null,

        // Example: val galleryImages: List<String> = emptyList()
    ) : CamUIState

    // 3. Something went wrong (e.g., failed to write file to disk)
    data class Error(val message: String) : CamUIState
}