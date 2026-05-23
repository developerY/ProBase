package com.zoewave.probase.seaweed.mobile.smartcamera.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.features.ai.vision.financial.FinancialAdvisorEngine
import com.zoewave.probase.seaweed.data.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmartCameraUiState(
    val isAnalyzing: Boolean = false,
    val analysisResult: String? = null
)

@HiltViewModel
class SmartCameraViewModel @Inject constructor(
    private val financialRepo: FinancialRepository,
    private val visionEngine: FinancialAdvisorEngine,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartCameraUiState())
    val uiState: StateFlow<SmartCameraUiState> = _uiState.asStateFlow()

    fun analyze(imageCapture: ImageCapture, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, analysisResult = "Analyzing view...")
            
            val profile = financialRepo.getFinancialProfile().firstOrNull()
            val apiKey = aiSettings.getGeminiApiKey() ?: run {
                _uiState.value = _uiState.value.copy(isAnalyzing = false, analysisResult = "API Key not found in settings.")
                return@launch
            }
            val modelName = aiSettings.aiModelFlow.firstOrNull() ?: "gemini-1.5-flash"

            imageCapture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val bitmap = image.toBitmap()
                        image.close()
                        
                        performAnalysis(bitmap, profile, apiKey, modelName)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        _uiState.value = _uiState.value.copy(isAnalyzing = false, analysisResult = "Capture failed: ${exception.localizedMessage}")
                    }
                }
            )
        }
    }

    private fun performAnalysis(bitmap: Bitmap, profile: com.zoewave.probase.seaweed.model.FinancialProfile?, apiKey: String, modelName: String) {
        viewModelScope.launch {
            val financialContext = profile?.let {
                "Flexible Money Remaining: ${CurrencyUtils.formatCents(it.flexibleMoneyRemainingCents)}. Month Progress: ${(it.monthProgress * 100).toInt()}%."
            } ?: "No budget info available."

            val result = visionEngine.analyzeFinancialImpact(
                bitmap = bitmap,
                apiKey = apiKey,
                modelName = modelName,
                financialContext = financialContext,
                deviceBranding = "phone"
            )
            
            _uiState.value = _uiState.value.copy(isAnalyzing = false, analysisResult = result)
        }
    }
}
