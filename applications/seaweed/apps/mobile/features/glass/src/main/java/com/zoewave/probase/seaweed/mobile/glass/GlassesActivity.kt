package com.zoewave.probase.seaweed.mobile.glass

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.features.ai.firebase.data.FirebaseLiveSessionManager
import com.zoewave.probase.seaweed.data.FinancialRepository
import dagger.hilt.android.AndroidEntryPoint
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

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalProjectedApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recommended for XR glasses to handle initial focus correctly
        ComposeUiFlags.isInitialFocusOnFocusableAvailable = true

        audioInterface = SeaweedAudioInterface(this, "Seaweed Glass active.")
        lifecycle.addObserver(audioInterface)
        lifecycle.addObserver(firebaseLiveSessionManager)

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

                    profile?.let {
                        SeaweedGlassApp(
                            profile = it,
                            analysisResult = analysisResult,
                            onTalkToGemini = {
                                firebaseLiveSessionManager.startConversation()
                            },
                            onCaptureImage = {
                                captureAndAnalyze(imageCapture) { result ->
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

    private fun captureAndAnalyze(imageCapture: ImageCapture, onResult: (String) -> Unit) {
        lifecycleScope.launch {
            val apiKey = aiSettings.getGeminiApiKey() ?: return@launch
            
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(this@GlassesActivity),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                        val bitmap = image.toBitmap()
                        image.close()
                        
                        lifecycleScope.launch {
                            val result = visionEngine.analyzeImage(bitmap, apiKey)
                            onResult(result)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        onResult("Capture failed: ${exception.localizedMessage}")
                    }
                }
            )
        }
    }
}
