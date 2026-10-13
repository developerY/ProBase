package com.zoewave.probase.kocolor.mobile.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditLogger
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditTrail
import com.zoewave.probase.kocolor.data.usecase.CapabilityRouter
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import com.zoewave.probase.kocolor.data.usecase.StyleSimulatorEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiDebugUiState(
    val activeProviderName: String = "Loading...",
    val currentModelName: String = "gemini-1.5-flash",
    val isByokEnabled: Boolean = false,
    val isByokKeySet: Boolean = false,
    val isLocalAiEnabled: Boolean = false,
    val isFirebaseEnabled: Boolean = true,
    val isRunningTest: Boolean = false,
    val recentAuditLogs: List<String> = emptyList(),
    val recentTrails: List<StyleAuditTrail> = emptyList(),
    val testRunOutput: String? = null
)

@HiltViewModel
class AiDebugViewModel @Inject constructor(
    private val aiSettings: AiConfigurationSettings,
    private val auditLogger: StyleAuditLogger,
    private val capabilityRouter: CapabilityRouter,
    private val simulatorEngine: StyleSimulatorEngine,
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository
) : ViewModel() {

    private val _isTesting = MutableStateFlow(false)
    private val _testOutput = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AiDebugUiState> = combine(
        aiSettings.aiModelFlow,
        aiSettings.useByokKey,
        aiSettings.isGeminiApiKeySetFlow,
        aiSettings.useLocalAi,
        aiSettings.useFirebaseVertexAi,
        auditLogger.recentLogs,
        auditLogger.recentTrails,
        _isTesting,
        _testOutput
    ) { array ->
        val modelName = array[0] as String
        val useByok = array[1] as Boolean
        val isKeySet = array[2] as Boolean
        val useLocal = array[3] as Boolean
        val useFirebase = array[4] as Boolean
        @Suppress("UNCHECKED_CAST")
        val logs = array[5] as List<String>
        @Suppress("UNCHECKED_CAST")
        val trails = array[6] as List<StyleAuditTrail>
        val isTesting = array[7] as Boolean
        val testOutput = array[8] as String?

        AiDebugUiState(
            activeProviderName = capabilityRouter.getActiveProviderName(),
            currentModelName = modelName,
            isByokEnabled = useByok,
            isByokKeySet = isKeySet,
            isLocalAiEnabled = useLocal,
            isFirebaseEnabled = useFirebase,
            isRunningTest = isTesting,
            recentAuditLogs = logs,
            recentTrails = trails,
            testRunOutput = testOutput
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiDebugUiState()
    )

    fun clearLogs() {
        auditLogger.clearLogs()
        _testOutput.value = null
    }

    fun runTestInference() {
        viewModelScope.launch {
            _isTesting.value = true
            _testOutput.value = "Executing Test Inference across AI Router..."
            try {
                val wardrobe = wardrobeRepository.getAllClothing().firstOrNull() ?: emptyList()
                val cosmetics = cosmeticRepository.getAllCosmetics().firstOrNull() ?: emptyList()

                val context = StyleRequestContext(
                    requestId = "TEST_DEBUG_${System.currentTimeMillis()}",
                    intent = "Test Diagnostics",
                    occasion = "Debug Test",
                    weather = "Clear",
                    weatherTempC = 22f,
                    uvIndex = 3f
                )

                val result = simulatorEngine.generateBlueprint(wardrobe, cosmetics, context)
                _testOutput.value = "Test Completed Successfully!\nProvider Used: ${capabilityRouter.getActiveProviderName()}\nRationale: ${result.rationale}\nSelected Clothes: ${result.selectedClothingIds}\nSelected Cosmetics: ${result.selectedCosmeticIds}\nPalette: ${result.recommendedPalette}"
            } catch (e: Exception) {
                _testOutput.value = "Test Failed with Exception: ${e.localizedMessage}\n${e.stackTraceToString()}"
            } finally {
                _isTesting.value = false
            }
        }
    }
}
