package com.zoewave.probase.seaweed.mobile.glass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.googlefonts.createGoogleSansFlexTypography
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import androidx.xr.projected.permissions.ProjectedPermissionsRequestParams
import androidx.xr.projected.permissions.ProjectedPermissionsResultContract
import com.zoewave.probase.core.data.repository.LiveAiRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GlassesActivity : ComponentActivity() {

    @Inject
    lateinit var liveAiRepository: LiveAiRepository

    private val viewModel: SeaweedGlassViewModel by viewModels()
    private lateinit var audioInterface: SeaweedAudioInterface

    @OptIn(ExperimentalProjectedApi::class)
    private val requestPermissionLauncher =
        registerForActivityResult(ProjectedPermissionsResultContract()) { results ->
            if (results[Manifest.permission.CAMERA] == true) {
                Log.d("GlassesActivity", "Camera permission granted on glasses")
            } else {
                audioInterface.speak("Camera permission is required to analyze items.")
            }
        }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalProjectedApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recommended for XR glasses to handle initial focus correctly
        ComposeUiFlags.isInitialFocusOnFocusableAvailable = true

        audioInterface = SeaweedAudioInterface(this, "Seaweed Glass active.")
        lifecycle.addObserver(audioInterface)
        lifecycle.addObserver(liveAiRepository)

        checkAndRequestCameraPermission()

        setContent {
            GlimmerTheme(
                typography = createGoogleSansFlexTypography()
            ) {
                // Mandatory black background for additive displays
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    val lifecycleOwner = LocalLifecycleOwner.current
                    val imageCapture = remember { ImageCapture.Builder().build() }

                    LaunchedEffect(lifecycleOwner) {
                        val providers = listOf(
                            "GLASSES" to this@GlassesActivity,
                            "HOST" to ProjectedContext.createHostDeviceContext(this@GlassesActivity),
                            "APP" to applicationContext
                        )

                        var bound = false
                        for ((name, ctx) in providers) {
                            if (bound) break
                            try {
                                val provider = ProcessCameraProvider.awaitInstance(ctx)
                                val selector = CameraSelector.DEFAULT_BACK_CAMERA
                                val hasBack = try { provider.hasCamera(selector) } catch (_: Exception) { false }
                                val hasAny = try { provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) } catch (_: Exception) { false }
                                
                                if (hasBack || hasAny) {
                                    val finalSelector = if (hasBack) selector else CameraSelector.DEFAULT_FRONT_CAMERA
                                    provider.unbindAll()
                                    provider.bindToLifecycle(lifecycleOwner, finalSelector, imageCapture)
                                    Log.d("GlassesActivity", "SUCCESS: Bound to $name camera")
                                    bound = true
                                }
                            } catch (e: Exception) {
                                Log.w("GlassesActivity", "Failed to probe $name context: ${e.message}")
                            }
                        }

                        if (!bound) {
                            audioInterface.speak("Camera hardware not found. Visual features disabled.")
                        }
                    }

                    SeaweedGlassApp(
                        uiState = uiState,
                        onTalkToGemini = {
                            liveAiRepository.startSession()
                        },
                        onCaptureImage = {
                            captureAndAnalyze(imageCapture)
                        },
                        onClose = { finish() }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalProjectedApi::class)
    private fun checkAndRequestCameraPermission() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            val params = ProjectedPermissionsRequestParams(
                permissions = listOf(permission),
                rationale = "Seaweed needs camera access to analyze products from your glasses."
            )
            requestPermissionLauncher.launch(listOf(params))
        }
    }

    private fun captureAndAnalyze(imageCapture: ImageCapture) {
        audioInterface.speak("Analyzing...", flush = true)
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                    val bitmap = image.toBitmap()
                    image.close()
                    viewModel.analyzeImage(bitmap) { result ->
                        audioInterface.speak(result)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    val error = "Capture failed: ${exception.localizedMessage}"
                    Log.e("GlassesActivity", error)
                    audioInterface.speak(error)
                }
            }
        )
    }
}
