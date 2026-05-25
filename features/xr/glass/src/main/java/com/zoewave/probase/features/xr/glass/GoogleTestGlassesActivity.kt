package com.zoewave.probase.features.xr.glass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.ListItem
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.list.GlimmerLazyColumn
import androidx.xr.glimmer.surface
import androidx.xr.projected.ProjectedDeviceController
import androidx.xr.projected.ProjectedDeviceController.Capability.Companion.CAPABILITY_VISUAL_UI
import androidx.xr.projected.ProjectedDisplayController
import androidx.xr.projected.ProjectedDisplayController.PresentationMode
import androidx.xr.projected.ProjectedDisplayController.PresentationModeFlags
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import androidx.xr.projected.permissions.ProjectedPermissionsRequestParams
import androidx.xr.projected.permissions.ProjectedPermissionsResultContract
import kotlinx.coroutines.launch
import java.util.function.Consumer

@OptIn(ExperimentalProjectedApi::class)
class GoogleTestGlassesActivity : ComponentActivity() {

    private var displayController: ProjectedDisplayController? = null
    private var isVisualUiSupported by mutableStateOf(false)
    private var areVisualsOn by mutableStateOf(true)
    private var isPermissionDenied by mutableStateOf(false)

    // Register the permissions launcher using the ProjectedPermissionsResultContract.
    private val requestPermissionLauncher: ActivityResultLauncher<List<ProjectedPermissionsRequestParams>> =
        registerForActivityResult(ProjectedPermissionsResultContract()) { results ->
            if (results[Manifest.permission.CAMERA] == true) {
                isPermissionDenied = false
                initializeGlassesFeatures()
            } else {
                // Handle permission denial.
                isPermissionDenied = true
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("GoogleXRTestActivity", "onCreate started")

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                displayController?.close()
                displayController = null
            }
        })

        if (hasCameraPermission()) {
            initializeGlassesFeatures()
        } else {
            requestHardwarePermissions()
        }

        setContent {
            GlimmerTheme {
                GoogleTestHomeScreen(
                    areVisualsOn = areVisualsOn,
                    isVisualUiSupported = isVisualUiSupported,
                    isPermissionDenied = isPermissionDenied,
                    onRetryPermission = { requestHardwarePermissions() },
                    onClose = { finish() }
                )
            }
        }
    }

    private fun initializeGlassesFeatures() {
        lifecycleScope.launch {
            // Check device capabilities
            val projectedDeviceController = ProjectedDeviceController.create(this@GoogleTestGlassesActivity)
            isVisualUiSupported = projectedDeviceController.capabilities.contains(CAPABILITY_VISUAL_UI)

            val controller = ProjectedDisplayController.create(this@GoogleTestGlassesActivity)
            displayController = controller
            val observer = GoogleExampleObserver(
                controller = controller,
                onVisualsChanged = { visualsOn -> areVisualsOn = visualsOn }
            )
            lifecycle.addObserver(observer)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestHardwarePermissions() {
        val params = ProjectedPermissionsRequestParams(
            permissions = listOf(Manifest.permission.CAMERA),
            rationale = "Camera access is required to overlay digital content on your physical environment."
        )
        requestPermissionLauncher.launch(listOf(params))
    }
}

@OptIn(ExperimentalProjectedApi::class)
class GoogleExampleObserver(
    private val controller: ProjectedDisplayController,
    private val onVisualsChanged: (Boolean) -> Unit
) : DefaultLifecycleObserver {

    private val presentationModeListener = Consumer<PresentationModeFlags> { flags ->
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

@OptIn(ExperimentalProjectedApi::class)
@Composable
fun GoogleTestHomeScreen(
    areVisualsOn: Boolean,
    isVisualUiSupported: Boolean,
    isPermissionDenied: Boolean,
    onRetryPermission: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .surface()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isPermissionDenied) {
            Card(
                title = { Text("Permissions") },
                action = { Button(onClick = onClose) { Text("Exit") } }
            ) {
                Text("Camera access is needed to use AI glasses features.")
                Button(onClick = onRetryPermission) { Text("Retry") }
            }
        } else if (isVisualUiSupported) {
            Card(
                title = { Text("Android XR Test") },
                action = {
                    Button(onClick = onClose) {
                        Text("Close")
                    }
                }
            ) {
                var clickCountA by remember { mutableStateOf(0) }
                var clickCountB by remember { mutableStateOf(0) }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Interactive List Test:")
                    
                    GlimmerLazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        item {
                            ListItem(
                                onClick = { 
                                    clickCountA++
                                    android.util.Log.d("GoogleXRTest", "Button A clicked: $clickCountA")
                                },
                                content = { Text("Button A - Clicked: $clickCountA") }
                            )
                        }
                        item {
                            ListItem(
                                onClick = { 
                                    clickCountB++ 
                                    android.util.Log.d("GoogleXRTest", "Button B clicked: $clickCountB")
                                },
                                content = { Text("Button B - Clicked: $clickCountB") }
                            )
                        }
                    }
                }
            }
        } else {
            Text("Audio Guidance Mode Active")
        }
    }
}
