package com.zoewave.probase.features.xr.glass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.camera.core.ExperimentalLensFacing
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.projected.ProjectedDeviceController
import androidx.xr.projected.ProjectedDisplayController
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import androidx.xr.projected.permissions.ProjectedPermissionsRequestParams
import androidx.xr.projected.permissions.ProjectedPermissionsResultContract
import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import com.zoewave.probase.core.data.repository.LiveAiRepository
import com.zoewave.probase.features.xr.glass.data.GlassSessionRepository
import com.zoewave.probase.features.xr.glass.ui.GlassApp
import com.zoewave.probase.features.xr.glass.ui.GlassViewModel
import com.zoewave.probase.features.xr.glass.ui.GlimmerSample
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalProjectedApi::class)
@AndroidEntryPoint
class GlassesMainActivity : ComponentActivity() {

    private val LIFECYCLE_TAG = "XrLifecycle"

    @Inject lateinit var glassBridgeRepository: GlassBridgeRepository
    @Inject lateinit var glassSessionRepository: GlassSessionRepository
    @Inject lateinit var liveAiRepository: LiveAiRepository
    private lateinit var audioInterface: GlassAudioInterface
    private val viewModel: GlassViewModel by viewModels()

    private var displayController: ProjectedDisplayController? = null
    private var deviceController: ProjectedDeviceController? = null
    private var isVisualUiSupported by mutableStateOf(false)
    private var areVisualsOn by mutableStateOf(true)

    private val requestPermissionLauncher: ActivityResultLauncher<List<ProjectedPermissionsRequestParams>> =
        registerForActivityResult(ProjectedPermissionsResultContract()) { results ->
            if (results[Manifest.permission.CAMERA] == true) {
                android.util.Log.d("GlassesMain", "Camera permission granted")
            }
        }

    private var initialSample: GlimmerSample? = null

    @androidx.annotation.OptIn(ExperimentalLensFacing::class)
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d(LIFECYCLE_TAG, "onCreate: Initializing Glass Session")

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
                android.util.Log.d(LIFECYCLE_TAG, "onDestroy: Cleaning up controllers")
                displayController?.close() // Controller for the Projected device display
                displayController = null
                deviceController?.close() // Controller for the Projected device
                deviceController = null
                glassSessionRepository.updateActiveSample(null)
            }
        })

        // Initialize features. Phone app is responsible for pre-requesting permissions.
        initializeGlassesFeatures()

        // Request hardware permissions for AI glasses features (e.g. Vision, Object Recognition)
        requestHardwarePermissions()

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
                val controller = ProjectedDeviceController.create(this@GlassesMainActivity)
                deviceController = controller
                
                val connected = controller.capabilities.isNotEmpty()
                isVisualUiSupported = ProjectedCapabilities.hasDisplay(controller)
                
                // Keep repository update async as it might be state-driven
                launch {
                    glassSessionRepository.updateConnection(connected)
                }

                val dispController = ProjectedDisplayController.create(this@GlassesMainActivity)
                displayController = dispController
                val observer = GlassesLifecycleObserver(
                    controller = dispController,
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

    private fun requestHardwarePermissions() {
        val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            val params = ProjectedPermissionsRequestParams(
                permissions = missingPermissions,
                rationale = "Camera and Microphone access are required to provide an immersive experience on your glasses."
            )
            // Best Practice: Speak the rationale since instructions aren't audible by default on glasses
            params.rationale?.let { audioInterface.speak(it) }
            requestPermissionLauncher.launch(listOf(params))
        }
    }

    override fun onStart() {
        super.onStart()
        android.util.Log.d(LIFECYCLE_TAG, "onStart: Glass Session is active")
        lifecycleScope.launch {
            glassBridgeRepository.updateGlassSessionState(isActive = true)
        }
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d(LIFECYCLE_TAG, "onResume: Activity focused")
        viewModel.setPaused(false)
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d(LIFECYCLE_TAG, "onPause: Activity backgrounded but still visible")
        viewModel.setPaused(true)
    }

    override fun onStop() {
        super.onStop()
        android.util.Log.d(LIFECYCLE_TAG, "onStop: Glass Session stopped")
        lifecycleScope.launch {
            glassBridgeRepository.updateGlassSessionState(isActive = false)
        }
    }
}
