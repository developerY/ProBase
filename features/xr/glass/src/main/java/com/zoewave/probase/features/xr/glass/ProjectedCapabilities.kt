package com.zoewave.probase.features.xr.glass

import androidx.xr.projected.ProjectedDeviceController
import androidx.xr.projected.experimental.ExperimentalProjectedApi

/**
 * DESIGN PRINCIPLE: Why we use this abstraction for Android XR Capabilities
 *
 * 1. INITIALIZATION: Capabilities are intrinsic to the hardware. We query once 
 *    at startup (GlassesMainActivity) for efficiency.
 *
 * 2. STATE HOISTING: We hoist 'isVisualUiSupported' to the top-level GlassApp.
 *    This keeps sub-composables (RitualLayout) reactive and decoupled from hardware.
 *
 * 3. GRACEFUL FALLBACK: If CAPABILITY_VISUAL_UI is missing, we pivot to 
 *    Audio Guidance Mode rather than showing a broken or empty UI.
 */
@OptIn(ExperimentalProjectedApi::class)
object ProjectedCapabilities {
    /**
     * Constant representing the ability to show a visual UI (display).
     * Usage: controller.capabilities.contains(CAPABILITY_VISUAL_UI)
     */
    val CAPABILITY_VISUAL_UI = ProjectedDeviceController.Capability.CAPABILITY_VISUAL_UI

    /**
     * Helper to check if the connected device supports visual UI.
     * Recommended for cleaner call sites: ProjectedCapabilities.hasDisplay(controller)
     */
    fun hasDisplay(controller: ProjectedDeviceController): Boolean {
        return controller.capabilities.contains(CAPABILITY_VISUAL_UI)
    }
}
