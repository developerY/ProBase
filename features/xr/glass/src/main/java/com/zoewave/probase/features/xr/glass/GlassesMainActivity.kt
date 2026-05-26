package com.zoewave.probase.features.xr.glass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.zoewave.probase.features.xr.glass.data.GlassSessionRepository
import com.zoewave.probase.features.xr.glass.ui.GlassApp
import com.zoewave.probase.features.xr.glass.ui.GlimmerSample
import com.zoewave.probase.features.ai.firebase.data.FirebaseLiveSessionManager
import com.zoewave.probase.kocolor.data.FashionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalProjectedApi::class)
@AndroidEntryPoint
class GlassesMainActivity : ComponentActivity() {

    @Inject lateinit var fashionRepository: FashionRepository
    @Inject lateinit var glassSessionRepository: GlassSessionRepository
    @Inject lateinit var firebaseLiveSessionManager: FirebaseLiveSessionManager
    private lateinit var audioInterface: KoColorAudioInterface

    private var displayController: ProjectedDisplayController? = null
    private var isVisualUiSupported by mutableStateOf(false)
    private var areVisualsOn by mutableStateOf(true)

    private var initialSample: GlimmerSample? = null

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        ComposeUiFlags.isInitialFocusOnFocusableAvailable = true

        audioInterface = KoColorAudioInterface(
            this,
            "Resuming your ritual.",
        )
        lifecycle.addObserver(audioInterface)
        lifecycle.addObserver(firebaseLiveSessionManager)

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
            fashionRepository.glassCommands.collect { command ->
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
        lifecycleScope.launch {
            try {
                val projectedDeviceController = ProjectedDeviceController.create(this@GlassesMainActivity)
                val connected = projectedDeviceController.capabilities.isNotEmpty()
                isVisualUiSupported = projectedDeviceController.capabilities.contains(CAPABILITY_VISUAL_UI)
                glassSessionRepository.updateConnection(connected)

                val controller = ProjectedDisplayController.create(this@GlassesMainActivity)
                displayController = controller
                val observer = GlassesLifecycleObserver(
                    controller = controller,
                    onVisualsChanged = { visualsOn -> areVisualsOn = visualsOn }
                )
                lifecycle.addObserver(observer)
            } catch (e: Exception) {
                android.util.Log.e("GlassesMain", "Init failed", e)
                glassSessionRepository.updateConnection(false)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            fashionRepository.updateGlassSessionState(isActive = true)
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            fashionRepository.updateGlassSessionState(isActive = false)
        }
    }
}
