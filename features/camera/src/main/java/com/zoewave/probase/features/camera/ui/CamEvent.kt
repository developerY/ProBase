package com.zoewave.probase.features.camera.ui

sealed interface CamEvent {
    /**
     * Triggered when CameraX successfully saves the image to the local file system.
     * @param name A default name or title for the image.
     * @param description An optional description.
     * @param imgPath The local Uri string where the image is stored.
     */
    data class AddItem(
        val name: String,
        val description: String,
        val imgPath: String
    ) : CamEvent

    /**
     * Triggered if the user wants to clear the last captured image preview.
     */
    data object ClearLastCapture : CamEvent
    data object ConsumePhotoSavedEvent : CamEvent
}