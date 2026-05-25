package com.zoewave.probase.features.xr.glass

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.xr.projected.ProjectedDisplayController
import androidx.xr.projected.ProjectedDisplayController.PresentationMode
import androidx.xr.projected.experimental.ExperimentalProjectedApi

import java.util.function.Consumer

/**
 * Implementation of GlassesLifecycleObserver based on XR SDK patterns.
 * Observes the presentation mode changes and notifies when visuals are on/off.
 */
@OptIn(ExperimentalProjectedApi::class)
class GlassesLifecycleObserver(
    private val controller: ProjectedDisplayController,
    private val onVisualsChanged: (Boolean) -> Unit
) : DefaultLifecycleObserver {

    private val presentationModeListener = Consumer<ProjectedDisplayController.PresentationModeFlags> { flags ->
        onVisualsChanged(flags.hasPresentationMode(PresentationMode.VISUALS_ON))
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        controller.addPresentationModeChangedListener(listener = presentationModeListener)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        controller.removePresentationModeChangedListener(presentationModeListener)
    }
}
