package com.zoewave.probase.seaweed.mobile.glass

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.googlefonts.createGoogleSansFlexTypography
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import androidx.xr.projected.permissions.ProjectedPermissionsRequestParams
import androidx.xr.projected.permissions.ProjectedPermissionsResultContract
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.features.ai.firebase.data.FirebaseLiveSessionManager
import com.zoewave.probase.seaweed.data.FinancialRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GlassesActivity : ComponentActivity() {

    @Inject
    lateinit var financialRepository: FinancialRepository

    @Inject
    lateinit var firebaseLiveSessionManager: FirebaseLiveSessionManager

    @Inject
    lateinit var visionEngine: SeaweedGlassVisionEngine

    @Inject
    lateinit var aiSettings: AiConfigurationSettings

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
        lifecycle.addObserver(firebaseLiveSessionManager)

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
                    val profile by financialRepository.getFinancialProfile()
                        .collectAsStateWithLifecycle(initialValue = null)

                    val lifecycleOwner = LocalLifecycleOwner.current
                    val imageCapture = remember { ImageCapture.Builder().build() }
                    var analysisResult by remember { mutableStateOf<String?>(null) }

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
                                
                                // Check if this provider actually sees ANY camera before unbinding others
                                val hasBack = try { provider.hasCamera(selector) } catch (_: Exception) { false }
                                val hasAny = try { provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) } catch (_: Exception) { false }
                                
                                if (hasBack || hasAny) {
                                    val finalSelector = if (hasBack) selector else CameraSelector.DEFAULT_FRONT_CAMERA
                                    provider.unbindAll()
                                    provider.bindToLifecycle(lifecycleOwner, finalSelector, imageCapture)
                                    Log.d("GlassesActivity", "SUCCESS: Bound to $name camera (${if (hasBack) "BACK" else "FRONT"})")
                                    bound = true
                                } else {
                                    Log.w("GlassesActivity", "$name context sees 0 cameras.")
                                }
                            } catch (e: Exception) {
                                Log.w("GlassesActivity", "Failed to probe $name context: ${e.message}")
                            }
                        }

                        if (!bound) {
                            Log.e("GlassesActivity", "CRITICAL: No cameras found in any context.")
                            audioInterface.speak("Camera hardware not found. Visual features disabled.")
                        }
                    }

                    profile?.let { currentProfile ->
                        SeaweedGlassApp(
                            profile = currentProfile,
                            analysisResult = analysisResult,
                            onTalkToGemini = {
                                firebaseLiveSessionManager.startConversation()
                            },
                            onCaptureImage = {
                                val financialContext = "Flexible Money Remaining: ${CurrencyUtils.formatCents(currentProfile.flexibleMoneyRemainingCents)}. Month Progress: ${(currentProfile.monthProgress * 100).toInt()}%."
                                audioInterface.speak("Analyzing...", flush = true)
                                captureAndAnalyze(imageCapture, financialContext) { result ->
                                    analysisResult = result
                                    audioInterface.speak(result)
                                }
                            }
                        )
                    }
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
                rationale = "Seaweed needs camera access to analyze receipts and products from your glasses."
            )
            requestPermissionLauncher.launch(listOf(params))
        }
    }

    private fun captureAndAnalyze(imageCapture: ImageCapture, financialContext: String, onResult: (String) -> Unit) {
        lifecycleScope.launch {
            val apiKey = aiSettings.getGeminiApiKey() ?: return@launch
            val modelName = aiSettings.aiModelFlow.firstOrNull() ?: "gemini-1.5-flash"
            
            try {
                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(this@GlassesActivity),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                            val bitmap = image.toBitmap()
                            image.close()
                            
                            lifecycleScope.launch {
                                val result = visionEngine.analyzeImage(bitmap, apiKey, modelName, financialContext)
                                onResult(result)
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            onResult("Capture failed: ${exception.localizedMessage}")
                        }
                    }
                )
            } catch (e: Exception) {
                onResult("Capture system error: ${e.localizedMessage}")
            }
        }
    }
}
