package com.zoewave.probase.features.xr.glass

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.xr.projected.ProjectedActivityCompat
import androidx.xr.projected.ProjectedDeviceController
import androidx.xr.projected.ProjectedDisplayController
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import com.zoewave.probase.core.data.repository.LiveAiRepository
import com.zoewave.probase.features.xr.glass.data.GlassSessionRepository
import com.zoewave.probase.features.xr.glass.ui.GlassApp
import com.zoewave.probase.features.xr.glass.ui.GlassViewModel
import com.zoewave.probase.features.xr.glass.ui.GlimmerSample
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalProjectedApi::class)
@AndroidEntryPoint
class GlassesMainActivity : ComponentActivity() {

    companion object {
        private const val LIFECYCLE_TAG = "XrLifecycle"
        private const val HARDWARE_PERMISSION_REQUEST_CODE = 1001
    }

    @Inject
    lateinit var glassBridgeRepository: GlassBridgeRepository

    @Inject
    lateinit var glassSessionRepository: GlassSessionRepository

    @Inject
    lateinit var liveAiRepository: LiveAiRepository

    private lateinit var audioInterface: GlassAudioInterface

    private val viewModel: GlassViewModel by viewModels()

    private var displayController: ProjectedDisplayController? = null
    private var deviceController: ProjectedDeviceController? = null

    private var isVisualUiSupported by mutableStateOf(false)
    private var areVisualsOn by mutableStateOf(true)

    private var initialSample: GlimmerSample? = null

    @androidx.annotation.OptIn(ExperimentalLensFacing::class)
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        android.util.Log.d(
            LIFECYCLE_TAG,
            "onCreate: Initializing Glass Session"
        )

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

        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    android.util.Log.d(
                        LIFECYCLE_TAG,
                        "onDestroy: Cleaning up controllers"
                    )

                    displayController?.close()
                    displayController = null

                    deviceController?.close()
                    deviceController = null

                    glassSessionRepository.updateActiveSample(null)
                }
            }
        )

        // ProjectedDeviceController.create() requires API 34.
        initializeGlassesFeatures()

        // ProjectedActivityCompat.requestPermissions() requires API 35.
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
                    onSpeak = { text ->
                        audioInterface.speak(text)
                    },
                    initialSample = initialSample
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        initialSample = intent
            .getStringExtra("initial_sample")
            ?.let { value ->
                try {
                    GlimmerSample.valueOf(value)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }

        val requestedTime = intent.getStringExtra("routine_time")

        if (initialSample != null) {
            glassSessionRepository.updateActiveSample(initialSample)
        }

        glassSessionRepository.updateRequestedRoutineTime(requestedTime)
    }

    private fun initializeGlassesFeatures() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            android.util.Log.w(
                LIFECYCLE_TAG,
                "Projected XR APIs require Android 14/API 34 or newer."
            )

            lifecycleScope.launch {
                glassSessionRepository.updateConnection(false)
            }

            return
        }

        lifecycleScope.launch(Dispatchers.Main.immediate) {
            try {
                val controller =
                    ProjectedDeviceController.create(this@GlassesMainActivity)

                deviceController = controller

                val connected = controller.capabilities.isNotEmpty()

                isVisualUiSupported =
                    ProjectedDeviceController.Capability.CAPABILITY_VISUAL_UI in
                        controller.capabilities

                launch {
                    glassSessionRepository.updateConnection(connected)
                }

                val dispController =
                    ProjectedDisplayController.create(this@GlassesMainActivity)

                displayController = dispController

                val observer = GlassesLifecycleObserver(
                    controller = dispController,
                    onVisualsChanged = { visualsOn ->
                        areVisualsOn = visualsOn
                    }
                )

                lifecycle.addObserver(observer)

            } catch (e: Exception) {
                android.util.Log.e(
                    LIFECYCLE_TAG,
                    "Projected XR initialization failed",
                    e
                )

                launch {
                    glassSessionRepository.updateConnection(false)
                }
            }
        }
    }

    private fun requestHardwarePermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val missingPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            android.util.Log.d(
                LIFECYCLE_TAG,
                "Camera and audio permissions are already granted."
            )
            return
        }

        // ProjectedActivityCompat.requestPermissions() was added for
        // the Projected permission flow and requires API 35.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            android.util.Log.w(
                LIFECYCLE_TAG,
                "Projected hardware permission flow requires Android 15/API 35."
            )
            return
        }

        val rationale =
            "Camera and Microphone access are required to provide an immersive experience on your glasses."

        // Give the user the explanation audibly because the permission
        // flow ultimately involves the host device.
        audioInterface.speak(rationale)

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                ProjectedActivityCompat.requestPermissions(
                    activity = this@GlassesMainActivity,
                    permissions = missingPermissions.toTypedArray(),
                    requestCode = HARDWARE_PERMISSION_REQUEST_CODE
                )
            } catch (e: Exception) {
                android.util.Log.e(
                    LIFECYCLE_TAG,
                    "Failed to request Projected hardware permissions",
                    e
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode != HARDWARE_PERMISSION_REQUEST_CODE) {
            return
        }

        // Read the final permission state rather than assuming the
        // callback contains both permissions. Only missing permissions
        // were passed to requestPermissions().
        val cameraGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        val audioGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        android.util.Log.d(
            LIFECYCLE_TAG,
            "Permissions Result: Camera=$cameraGranted, Audio=$audioGranted"
        )

        if (cameraGranted) {
            // Initialize Vision features if needed.
        }

        if (!audioGranted) {
            android.util.Log.w(
                LIFECYCLE_TAG,
                "Audio permission denied. Some features will be limited."
            )
        }
    }

    override fun onStart() {
        super.onStart()

        android.util.Log.d(
            LIFECYCLE_TAG,
            "onStart: Glass Session is active"
        )

        lifecycleScope.launch {
            glassBridgeRepository.updateGlassSessionState(
                isActive = true
            )
        }
    }

    override fun onResume() {
        super.onResume()

        android.util.Log.d(
            LIFECYCLE_TAG,
            "onResume: Activity focused"
        )

        viewModel.setPaused(false)
    }

    override fun onPause() {
        super.onPause()

        android.util.Log.d(
            LIFECYCLE_TAG,
            "onPause: Activity backgrounded but still visible"
        )

        viewModel.setPaused(true)
    }

    override fun onStop() {
        super.onStop()

        android.util.Log.d(
            LIFECYCLE_TAG,
            "onStop: Glass Session stopped"
        )

        lifecycleScope.launch {
            glassBridgeRepository.updateGlassSessionState(
                isActive = false
            )
        }
    }
}