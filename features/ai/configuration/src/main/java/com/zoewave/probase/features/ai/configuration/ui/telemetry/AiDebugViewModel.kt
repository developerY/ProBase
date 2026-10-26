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

enum class TestTargetProvider(val label: String) {
    LOCAL_NANO("1. Local Nano"),
    BYOK("2. BYOK Cloud"),
    FIREBASE_AI("3. Firebase AI"),
    GEMMA_4("4. Gemma 4 Cloud"),
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
    val isFirebaseEnabled: Boolean = true,
    val isGemmaEnabled: Boolean = false,
    val isRunningTest: Boolean = false,
    val diagnosticSteps: List<DiagnosticStep> = emptyList(),
    val recentAuditLogs: List<String> = emptyList(),
    val testRunOutput: String? = null
)

@HiltViewModel
class AiDebugViewModel @Inject constructor(
    private val aiSettings: AiConfigurationSettings,
    private val byokProvider: ByokAiProvider,
    private val localProvider: LocalNanoAiProvider,
    private val firebaseProvider: FirebaseAiProvider,
    private val gemmaProvider: GemmaRestFallbackProvider,
    private val appDiagnosticDelegate: AppDiagnosticDelegate
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
        _selectedTarget,
        _isTesting,
        _diagnosticSteps,
        _testOutput
    ) { args ->
        val isAiEnabled = args[0] as Boolean
        val aiModel = args[1] as String
        val isKeySet = args[2] as Boolean
        val useByok = args[3] as Boolean
        val useLocal = args[4] as Boolean
        val useFirebase = args[5] as Boolean
        val selectedTarget = args[6] as TestTargetProvider
        val isTesting = args[7] as Boolean
        @Suppress("UNCHECKED_CAST")
        val steps = args[8] as List<DiagnosticStep>
        val testOutput = args[9] as String?

        val activeProviderLabel = when {
            useLocal -> "On-Device Local AI (Gemini Nano)"
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
            isFirebaseEnabled = useFirebase,
            isGemmaEnabled = !useByok && !useLocal && !useFirebase,
            isRunningTest = isTesting,
            diagnosticSteps = steps,
            testRunOutput = testOutput
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
            
            // Loop through all 4 providers
            val targets = listOf(
                TestTargetProvider.LOCAL_NANO,
                TestTargetProvider.BYOK,
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
                    break // Stop testing on first success for the Matrix flow (or let it finish, depending on preferred UX)
                }
            }
            
            _isTesting.value = false
        }
    }

    private suspend fun resolveTargetProvider(target: TestTargetProvider): AiProvider {
        return when (target) {
            TestTargetProvider.LOCAL_NANO -> localProvider
            TestTargetProvider.BYOK -> byokProvider
            TestTargetProvider.FIREBASE_AI -> firebaseProvider
            TestTargetProvider.GEMMA_4 -> gemmaProvider
            TestTargetProvider.AUTO -> {
                val useLocal = aiSettings.useLocalAi.firstOrNull() ?: false
                val useByok = aiSettings.useByokKey.firstOrNull() ?: false
                val useFirebase = aiSettings.useFirebaseVertexAi.firstOrNull() ?: false
                when {
                    useLocal -> localProvider
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


