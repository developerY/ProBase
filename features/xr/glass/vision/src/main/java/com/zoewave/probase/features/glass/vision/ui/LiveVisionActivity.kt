package com.zoewave.probase.features.glass.vision.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.xr.projected.ProjectedDeviceController
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import androidx.xr.projected.permissions.ProjectedPermissionsRequestParams
import androidx.xr.projected.permissions.ProjectedPermissionsResultContract
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalProjectedApi::class)
@AndroidEntryPoint
class LiveVisionActivity : ComponentActivity() {

    private val projectedPermissionLauncher =
        registerForActivityResult(ProjectedPermissionsResultContract()) { results ->
            if (results[Manifest.permission.CAMERA] == true) {
                Log.d("LiveVisionActivity", "Projected camera permission granted")
            } else {
                Log.e("LiveVisionActivity", "Projected camera permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkAndRequestPermissions()

        setContent {
            val viewModel: VisionViewModel = hiltViewModel()
            var isGlassesConnected by remember { mutableStateOf(false) }

            // Check for glasses connection
            LaunchedEffect(Unit) {
                try {
                    val controller = ProjectedDeviceController.create(this@LiveVisionActivity)
                    isGlassesConnected = controller.capabilities.isNotEmpty()
                } catch (e: Exception) {
                    isGlassesConnected = false
                }
            }

            MaterialTheme {
                if (isGlassesConnected) {
                    // Note: In a real multi-display setup, the system decides which UI to show.
                    // For debugging, we want the Unified Diagnostic Hub on the phone.
                    UnifiedVisionScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = {
                            // Navigate to settings logic
                        },
                        onRequestGlassesPermission = {
                            android.util.Log.d("LiveVisionActivity", "onRequestGlassesPermission triggered in Activity")
                            val params = ProjectedPermissionsRequestParams(
                                permissions = listOf(Manifest.permission.CAMERA),
                                rationale = "Camera access is needed on your AI glasses to describe what you see."
                            )
                            projectedPermissionLauncher.launch(listOf(params))
                        }
                    )
                    
                    // The Glimmer UI for glasses is usually handled by a separate 
                    // Projection activity, but we can also trigger it here if needed.
                } else {
                    // Fallback UI for the phone screen
                    VisionCompanionScreen()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permission = Manifest.permission.CAMERA
        // Use attribution context for XR hardware access tracking
        val attributionContext = createAttributionContext("xr_projected")
        val permissionStatus = ContextCompat.checkSelfPermission(attributionContext, permission)

        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            val params = ProjectedPermissionsRequestParams(
                permissions = listOf(permission),
                rationale = "Camera access is needed on your AI glasses to describe what you see."
            )
            projectedPermissionLauncher.launch(listOf(params))
        }
    }
}

@Composable
fun VisionCompanionScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Connect your glasses to start Vision AI.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
