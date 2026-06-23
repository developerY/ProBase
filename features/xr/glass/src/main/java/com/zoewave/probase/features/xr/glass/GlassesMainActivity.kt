package com.zoewave.probase.features.xr.glass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ExperimentalLensFacing
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.projected.ProjectedDeviceController
import androidx.xr.projected.ProjectedDeviceController.Capability.Companion.CAPABILITY_VISUAL_UI
import androidx.xr.projected.ProjectedDisplayController
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import com.zoewave.probase.core.data.repository.LiveAiRepository
import com.zoewave.probase.features.xr.glass.data.GlassSessionRepository
import com.zoewave.probase.features.xr.glass.ui.GlassApp
import com.zoewave.probase.features.xr.glass.ui.GlimmerSample
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalProjectedApi::class)
@AndroidEntryPoint
class GlassesMainActivity : ComponentActivity() {

    @Inject lateinit var glassBridgeRepository: GlassBridgeRepository
    @Inject lateinit var glassSessionRepository: GlassSessionRepository
    @Inject lateinit var liveAiRepository: LiveAiRepository
    private lateinit var audioInterface: GlassAudioInterface

    private var displayController: ProjectedDisplayController? = null
    private var isVisualUiSupported by mutableStateOf(false)
    private var areVisualsOn by mutableStateOf(true)

    private var initialSample: GlimmerSample? = null

    @androidx.annotation.OptIn(ExperimentalLensFacing::class)
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        ComposeUiFlags.isInitialFocusOnFocusableAvailable = true

        audioInterface = GlassAudioInterface(
            this,
            "", // Empty default to prevent unintentional greeting
        )
        if (initialSample == GlimmerSample.Ritual) {
            audioInterface.speak("Resuming your ritual.")
        }
        lifecycle.addObserver(audioInterface)
        lifecycle.addObserver(liveAiRepository)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                displayController?.close()
                displayController = null
                glassSessionRepository.updateActiveSample(null)
            }
        })

        // Initialize features. Phone app is responsible for pre-requesting permissions.
        initializeGlassesFeatures()

        lifecycleScope.launch {
            glassBridgeRepository.glassCommands.collect { command ->
                if (command == "EXIT") {
                    finish()
                }
            }
        }

        setContent {
            GlimmerTheme {
                GlassApp(
                    areVisualsOn = areVisualsOn,
                    isVisualUiSupported = isVisualUiSupported,
                    onClose = { finish() },
                    onSpeak = { text -> audioInterface.speak(text) },
                    initialSample = initialSample
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent) {
        initialSample = intent.getStringExtra("initial_sample")?.let { 
            try { GlimmerSample.valueOf(it) } catch (e: Exception) { null }
        }
        val requestedTime = intent.getStringExtra("routine_time")
        if (initialSample != null) {
            glassSessionRepository.updateActiveSample(initialSample)
        }
        glassSessionRepository.updateRequestedRoutineTime(requestedTime)
    }

    private fun initializeGlassesFeatures() {
        // Peer Review Adjustment: Move controllers to main thread to avoid lifecycle race conditions.
        // Clarification: create() is a suspend function in alpha08, so we use launch(Main.immediate).
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.Main.immediate) {
            try {
                val projectedDeviceController = ProjectedDeviceController.create(this@GlassesMainActivity)
                val connected = projectedDeviceController.capabilities.isNotEmpty()
                isVisualUiSupported = projectedDeviceController.capabilities.contains(CAPABILITY_VISUAL_UI)
                
                // Keep repository update async as it might be state-driven
                launch {
                    glassSessionRepository.updateConnection(connected)
                }

                val controller = ProjectedDisplayController.create(this@GlassesMainActivity)
                displayController = controller
                val observer = GlassesLifecycleObserver(
                    controller = controller,
                    onVisualsChanged = { visualsOn -> areVisualsOn = visualsOn }
                )
                lifecycle.addObserver(observer)
            } catch (e: Exception) {
                android.util.Log.e("GlassesMain", "Init failed", e)
                launch {
                    glassSessionRepository.updateConnection(false)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            glassBridgeRepository.updateGlassSessionState(isActive = true)
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            glassBridgeRepository.updateGlassSessionState(isActive = false)
        }
    }
}
