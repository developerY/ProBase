package com.zoewave.probase.seaweed.mobile.glass

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.features.ai.vision.financial.FinancialAdvisorEngine
import com.zoewave.probase.seaweed.data.FinancialRepository
import com.zoewave.probase.seaweed.model.FinancialProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeaweedGlassUiState(
    val profile: FinancialProfile? = null,
    val isAnalyzing: Boolean = false,
    val lastAnalysisResult: String? = null,
    val isAiActive: Boolean = false
)

@HiltViewModel
class SeaweedGlassViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val visionEngine: FinancialAdvisorEngine,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _isAnalyzing = MutableStateFlow(false)
    private val _lastAnalysisResult = MutableStateFlow<String?>(null)
    private val _isAiActive = MutableStateFlow(false)

    val uiState: StateFlow<SeaweedGlassUiState> = combine(
        financialRepository.getFinancialProfile(),
        _isAnalyzing,
        _lastAnalysisResult,
        _isAiActive
    ) { profile, isAnalyzing, result, isAiActive ->
        SeaweedGlassUiState(
            profile = profile,
            isAnalyzing = isAnalyzing,
            lastAnalysisResult = result,
            isAiActive = isAiActive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SeaweedGlassUiState()
    )

    fun toggleAi() {
        _isAiActive.value = !_isAiActive.value
    }

    fun analyzeImage(bitmap: Bitmap, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _lastAnalysisResult.value = "Analyzing..."
            
            val currentProfile = uiState.value.profile
            val financialContext = if (currentProfile != null) {
                "Flexible Money Remaining: ${CurrencyUtils.formatCents(currentProfile.flexibleMoneyRemainingCents)}. Month Progress: ${(currentProfile.monthProgress * 100).toInt()}%."
            } else {
                "No financial data available."
            }

            val apiKey = aiSettings.getGeminiApiKey()
            val modelName = aiSettings.aiModelFlow.first()

            if (apiKey == null) {
                val error = "Missing Gemini API Key."
                _lastAnalysisResult.value = error
                _isAnalyzing.value = false
                onComplete(error)
                return@launch
            }

            try {
                val result = visionEngine.analyzeFinancialImpact(
                    bitmap = bitmap,
                    apiKey = apiKey,
                    modelName = modelName,
                    financialContext = financialContext,
                    deviceBranding = "Seaweed Glass"
                )
                _lastAnalysisResult.value = result
                onComplete(result)
            } catch (e: Exception) {
                val error = "Analysis failed: ${e.localizedMessage}"
                _lastAnalysisResult.value = error
                onComplete(error)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    fun clearAnalysis() {
        _lastAnalysisResult.value = null
    }
}
