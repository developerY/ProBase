package com.zoewave.probase.features.xr.glass

import androidx.xr.projected.ProjectedDeviceController
import androidx.xr.projected.ProjectedDeviceController.Capability.Companion.CAPABILITY_VISUAL_UI
import androidx.xr.projected.experimental.ExperimentalProjectedApi

/**
 * Helper object to check for Android XR device capabilities.
 * Matches the terminology used in documentation and DroidCon materials.
 */
@OptIn(ExperimentalProjectedApi::class)
object ProjectedCapabilities {
    /**
     * Constant representing the ability to show a visual UI (display).
     */
    val CAPABILITY_VISUAL_UI = ProjectedDeviceController.Capability.CAPABILITY_VISUAL_UI

    /**
     * Checks if the connected device supports visual UI.
     */
    fun hasDisplay(controller: ProjectedDeviceController): Boolean {
        return controller.capabilities.contains(CAPABILITY_VISUAL_UI)
    }
}
