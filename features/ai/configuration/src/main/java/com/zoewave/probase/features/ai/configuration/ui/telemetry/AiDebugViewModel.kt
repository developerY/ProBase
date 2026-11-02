package com.zoewave.probase.features.ai.configuration.ui.telemetry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.ByokAiProvider
import com.zoewave.probase.features.ai.firebase.FirebaseAiProvider
import com.zoewave.probase.features.ai.firebase.GemmaRestFallbackProvider
import com.zoewave.probase.features.ai.local.data.LocalNanoAiProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import com.zoewave.probase.features.ai.local.data.LocalModelAssetManager
import com.zoewave.probase.features.ai.local.data.LocalModelState

enum class TestTargetProvider(val label: String) {
    LOCAL_NANO("1. Local Nano"),
    BYOK("2. BYOK Cloud"),
    DOWNLOAD_AI("3. Download AI"),
    FIREBASE_AI("4. Firebase AI"),
    GEMMA_4("5. Gemma 4 Cloud"),
    AUTO("Auto Router")
}

enum class DiagnosticStepState {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED
}

data class DiagnosticStep(
    val id: Int,
    val title: String,
    val description: String,
    val state: DiagnosticStepState = DiagnosticStepState.PENDING,
    val detailMessage: String? = null
)

data class AiDebugUiState(
    val activeProviderName: String = "Loading...",
    val currentModelName: String = "gemini-3.8-flash",
    val availableByokModels: List<String> = listOf("gemini-3.8-flash", "gemini-2.5-flash", "gemini-2.5-pro", "gemma-4-26b-a4b-it", "gemma-4-31b-it"),
    val selectedTarget: TestTargetProvider = TestTargetProvider.AUTO,
    val isByokEnabled: Boolean = false,
    val isByokKeySet: Boolean = false,
    val isLocalAiEnabled: Boolean = false,
    val isDownloadAiEnabled: Boolean = false,
    val isFirebaseEnabled: Boolean = true,
    val isGemmaEnabled: Boolean = false,
    val isRunningTest: Boolean = false,
    val diagnosticSteps: List<DiagnosticStep> = emptyList(),
    val recentAuditLogs: List<String> = emptyList(),
    val testRunOutput: String? = null,
    val localModelState: LocalModelState = LocalModelState.NotDownloaded
)

@HiltViewModel
class AiDebugViewModel @Inject constructor(
    private val aiSettings: AiConfigurationSettings,
    private val byokProvider: ByokAiProvider,
    private val localProvider: LocalNanoAiProvider,
    private val downloadProvider: com.zoewave.probase.features.ai.local.data.LocalDownloadAiProvider,
    private val firebaseProvider: FirebaseAiProvider,
    private val gemmaProvider: GemmaRestFallbackProvider,
    private val appDiagnosticDelegate: AppDiagnosticDelegate,
    private val localModelAssetManager: LocalModelAssetManager
) : ViewModel() {

    private val _selectedTarget = MutableStateFlow(TestTargetProvider.AUTO)
    private val _isTesting = MutableStateFlow(false)
    private val _testOutput = MutableStateFlow<String?>(null)
    private val _diagnosticSteps = MutableStateFlow(appDiagnosticDelegate.getInitialSteps())

    val uiState: StateFlow<AiDebugUiState> = combine(
        aiSettings.isAiEnabledFlow,
        aiSettings.aiModelFlow,
        aiSettings.isGeminiApiKeySetFlow,
        aiSettings.useByokKey,
        aiSettings.useLocalAi,
        aiSettings.useFirebaseVertexAi,
        aiSettings.useDownloadAi,
        _selectedTarget,
        _isTesting,
        _diagnosticSteps,
        _testOutput,
        localModelAssetManager.localModelState
    ) { args ->
        val isAiEnabled = args[0] as Boolean
        val aiModel = args[1] as String
        val isKeySet = args[2] as Boolean
        val useByok = args[3] as Boolean
        val useLocal = args[4] as Boolean
        val useFirebase = args[5] as Boolean
        val useDownloadAi = args[6] as Boolean
        val selectedTarget = args[7] as TestTargetProvider
        val isTesting = args[8] as Boolean
        @Suppress("UNCHECKED_CAST")
        val steps = args[9] as List<DiagnosticStep>
        val testOutput = args[10] as String?
        val localModelState = args[11] as LocalModelState

        val activeProviderLabel = when {
            useLocal -> "On-Device Local AI (Gemini Nano)"
            useDownloadAi -> "Download AI (Local Execution)"
            useByok -> "BYOK Cloud AI ($aiModel)"
            useFirebase -> "Firebase AI Logic ($aiModel)"
            else -> "Gemma 4 Cloud (Free Tier)"
        }

        AiDebugUiState(
            activeProviderName = activeProviderLabel,
            currentModelName = aiModel.ifBlank { "gemini-3.8-flash" },
            selectedTarget = selectedTarget,
            isByokEnabled = useByok,
            isByokKeySet = isKeySet,
            isLocalAiEnabled = useLocal,
            isDownloadAiEnabled = useDownloadAi,
            isFirebaseEnabled = useFirebase,
            isGemmaEnabled = !useByok && !useLocal && !useDownloadAi && !useFirebase,
            isRunningTest = isTesting,
            diagnosticSteps = steps,
            recentAuditLogs = appDiagnosticDelegate.getAuditLogs(),
            testRunOutput = testOutput,
            localModelState = localModelState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiDebugUiState()
    )

    fun selectTarget(target: TestTargetProvider) {
        _selectedTarget.value = target
        _diagnosticSteps.value = appDiagnosticDelegate.getInitialSteps()
        _testOutput.value = null
    }

    fun selectCloudModel(modelName: String) {
        viewModelScope.launch {
            aiSettings.saveAiModel(modelName)
            gemmaProvider.setModelName(modelName)
        }
    }

    fun runLiveDiagnostic() {
        if (_isTesting.value) return
        viewModelScope.launch {
            _isTesting.value = true
            _testOutput.value = null
            _diagnosticSteps.value = appDiagnosticDelegate.getInitialSteps()

            try {
                // Step 1: Handshake & Provider Resolution (handled uniformly here)
                updateStep(1, DiagnosticStepState.RUNNING, "Verifying Provider Readiness...")
                val targetProvider = resolveTargetProvider(_selectedTarget.value)
                val providerName = targetProvider.capability.displayName
                
                // Actual Network/Handshake Verification
                if (!targetProvider.isAvailable()) {
                    updateStep(1, DiagnosticStepState.FAILED, "$providerName is not available/configured")
                    return@launch
                }
                updateStep(1, DiagnosticStepState.SUCCESS, "Handshake ok: $providerName (ID=${targetProvider.capability.id})")

                // Let the app-specific delegate run its domain diagnostic pipeline
                val report = appDiagnosticDelegate.runDiagnostic(targetProvider) { id, state, msg ->
                    updateStep(id, state, msg)
                }
                
                _testOutput.value = report.rawResponseJson

            } catch (e: Exception) {
                updateStep(3, DiagnosticStepState.FAILED, "Unexpected error: ${e.localizedMessage}")
            } finally {
                _isTesting.value = false
            }
        }
    }

    fun runFullProviderMatrixTest() {
        if (_isTesting.value) return
        viewModelScope.launch {
            _isTesting.value = true
            _testOutput.value = null
            _diagnosticSteps.value = appDiagnosticDelegate.getInitialSteps()
            
            // Loop through all 5 providers
            val targets = listOf(
                TestTargetProvider.LOCAL_NANO,
                TestTargetProvider.BYOK,
                TestTargetProvider.DOWNLOAD_AI,
                TestTargetProvider.FIREBASE_AI,
                TestTargetProvider.GEMMA_4
            )
            
            val reports = mutableListOf<DiagnosticRunReport>()
            
            for (target in targets) {
                _selectedTarget.value = target
                updateStep(1, DiagnosticStepState.RUNNING, "Verifying Provider Readiness: ${target.label}...")
                
                val provider = resolveTargetProvider(target)
                if (!provider.isAvailable()) {
                    updateStep(1, DiagnosticStepState.FAILED, "${target.label} is unavailable/unconfigured")
                    reports.add(DiagnosticRunReport(false, provider.capability.displayName, 0L, null, "Unavailable"))
                    continue
                }
                updateStep(1, DiagnosticStepState.SUCCESS, "Handshake ok: ${provider.capability.displayName}")
                
                val report = appDiagnosticDelegate.runDiagnostic(provider) { id, state, msg ->
                    updateStep(id, state, msg)
                }
                reports.add(report)
                
                // Allow UI to breathe between tests
                kotlinx.coroutines.delay(500)
                if (report.passed) {
                    _testOutput.value = report.rawResponseJson
                    break // Stop testing on first success for the Matrix flow
                }
            }
            
            _isTesting.value = false
            
            // Format a nice final output for the Matrix Test
            val finalOutputBuilder = StringBuilder()
            finalOutputBuilder.appendLine("DIAGNOSTIC TEST COMPLETE!")
            finalOutputBuilder.appendLine()
            finalOutputBuilder.appendLine("• Target Tested: Auto Router")
            
            val successfulReport = reports.find { it.passed }
            if (successfulReport != null) {
                finalOutputBuilder.appendLine("• Provider Used: ${successfulReport.providerName}")
                finalOutputBuilder.appendLine("• Model Name: ${aiSettings.aiModelFlow.firstOrNull() ?: "gemini-3.8-flash"}")
                finalOutputBuilder.appendLine("• Latency: ${successfulReport.latencyMs}ms")
                finalOutputBuilder.appendLine()
                finalOutputBuilder.appendLine("=== RAW RESPONSE ===")
                finalOutputBuilder.appendLine(successfulReport.rawResponseJson ?: "")
            } else {
                finalOutputBuilder.appendLine("• ALL PROVIDERS FAILED")
                reports.forEach { report ->
                    finalOutputBuilder.appendLine("  - ${report.providerName}: ${report.failureReason}")
                }
            }
            
            _testOutput.value = finalOutputBuilder.toString()
        }
    }

    private suspend fun resolveTargetProvider(target: TestTargetProvider): AiProvider {
        return when (target) {
            TestTargetProvider.LOCAL_NANO -> localProvider
            TestTargetProvider.BYOK -> byokProvider
            TestTargetProvider.DOWNLOAD_AI -> downloadProvider
            TestTargetProvider.FIREBASE_AI -> firebaseProvider
            TestTargetProvider.GEMMA_4 -> gemmaProvider
            TestTargetProvider.AUTO -> {
                val useLocal = aiSettings.useLocalAi.firstOrNull() ?: false
                val useDownloadAi = aiSettings.useDownloadAi.firstOrNull() ?: false
                val useByok = aiSettings.useByokKey.firstOrNull() ?: false
                val useFirebase = aiSettings.useFirebaseVertexAi.firstOrNull() ?: false
                when {
                    useLocal -> localProvider
                    useDownloadAi -> downloadProvider
                    useByok -> byokProvider
                    useFirebase -> firebaseProvider
                    else -> gemmaProvider
                }
            }
        }
    }

    private fun updateStep(id: Int, state: DiagnosticStepState, message: String) {
        _diagnosticSteps.value = _diagnosticSteps.value.map { step ->
            if (step.id == id) step.copy(state = state, detailMessage = message) else step
        }
    }
}


