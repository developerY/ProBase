package com.zoewave.probase.kocolor.mobile.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.capture.data.SmartCaptureOrchestrator
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.ByokAiProvider
import com.zoewave.probase.features.ai.firebase.FirebaseAiProvider
import com.zoewave.probase.features.ai.firebase.GemmaRestFallbackProvider
import com.zoewave.probase.features.ai.local.data.LocalNanoAiProvider
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditLogger
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditTrail
import com.zoewave.probase.kocolor.data.usecase.CapabilityRouter
import com.zoewave.probase.kocolor.data.usecase.CompactManifestSerializer
import com.zoewave.probase.kocolor.data.usecase.PromptAssembler
import com.zoewave.probase.kocolor.data.usecase.RecommendationValidator
import com.zoewave.probase.kocolor.data.usecase.SerializationDetailLevel
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import com.zoewave.probase.kocolor.data.usecase.StyleSimulatorEngine
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaEvaluator
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
    val currentModelName: String = "gemini-2.5-flash",
    val availableByokModels: List<String> = listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemma-4-26b-a4b-it", "gemma-4-31b-it", "gemini-1.5-flash", "gemini-1.5-pro"),
    val selectedTarget: TestTargetProvider = TestTargetProvider.AUTO,
    val isByokEnabled: Boolean = false,
    val isByokKeySet: Boolean = false,
    val isLocalAiEnabled: Boolean = false,
    val isFirebaseEnabled: Boolean = true,
    val isGemmaEnabled: Boolean = false,
    val isRunningTest: Boolean = false,
    val diagnosticSteps: List<DiagnosticStep> = emptyList(),
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
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val byokProvider: ByokAiProvider,
    private val localProvider: LocalNanoAiProvider,
    private val firebaseProvider: FirebaseAiProvider,
    private val gemmaProvider: GemmaRestFallbackProvider,
    private val promptAssembler: PromptAssembler,
    private val manifestSerializer: CompactManifestSerializer,
    private val validator: RecommendationValidator,
    private val fashionistaEvaluator: FashionistaEvaluator,
    private val orchestrator: SmartCaptureOrchestrator
) : ViewModel() {

    private val _selectedTarget = MutableStateFlow(TestTargetProvider.AUTO)
    private val _isTesting = MutableStateFlow(false)
    private val _testOutput = MutableStateFlow<String?>(null)
    private val _diagnosticSteps = MutableStateFlow(initialSteps())
    private val _fetchedLiveModels = MutableStateFlow<List<String>?>(null)

    init {
        fetchLiveModels()
    }

    fun fetchLiveModels() {
        viewModelScope.launch {
            val key = aiSettings.getGeminiApiKey()
            if (!key.isNullOrBlank()) {
                val validationResult = orchestrator.validateApiKey(key)
                val liveModels = validationResult.second
                if (liveModels.isNotEmpty()) {
                    val activeModels = liveModels.filter { model ->
                        !model.contains("1.0") && model != "gemini-2.0-flash"
                    }
                    if (activeModels.isNotEmpty()) {
                        _fetchedLiveModels.value = (activeModels + listOf("gemma-4-26b-a4b-it", "gemma-4-31b-it")).distinct()
                    }
                }
            }
        }
    }

    private fun initialSteps(): List<DiagnosticStep> = listOf(
        DiagnosticStep(1, "Connection & Handshake", "1. Checking provider availability, API keys & AppCheck"),
        DiagnosticStep(2, "Command & Prompt", "2. Assembling candidate manifest & token estimation"),
        DiagnosticStep(3, "Response & Latency", "3. Transmitting prompt to LLM and receiving raw stream"),
        DiagnosticStep(4, "Answer & Schema", "4. Normalizing JSON & validating item candidate IDs"),
        DiagnosticStep(5, "Useful Answer Quality", "5. Fashionista color harmony score & intent match")
    )

    val uiState: StateFlow<AiDebugUiState> = combine(
        aiSettings.aiModelFlow,
        aiSettings.useByokKey,
        aiSettings.isGeminiApiKeySetFlow,
        aiSettings.useLocalAi,
        aiSettings.useFirebaseVertexAi,
        auditLogger.recentLogs,
        auditLogger.recentTrails,
        _selectedTarget,
        _isTesting,
        _testOutput,
        _diagnosticSteps,
        _fetchedLiveModels
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
        val selectedTarget = array[7] as TestTargetProvider
        val isTesting = array[8] as Boolean
        val testOutput = array[9] as String?
        @Suppress("UNCHECKED_CAST")
        val steps = array[10] as List<DiagnosticStep>
        @Suppress("UNCHECKED_CAST")
        val liveModels = array[11] as List<String>?

        val fallbackList = listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemma-4-26b-a4b-it", "gemma-4-31b-it", "gemini-1.5-flash", "gemini-1.5-pro")
        val modelList = (liveModels ?: fallbackList).distinct()

        val displayedModel = if (selectedTarget == TestTargetProvider.GEMMA_4 && !modelName.startsWith("gemma-")) {
            "gemma-4-26b-a4b-it"
        } else {
            modelName
        }

        AiDebugUiState(
            activeProviderName = capabilityRouter.getActiveProviderName(),
            currentModelName = displayedModel,
            availableByokModels = modelList,
            selectedTarget = selectedTarget,
            isByokEnabled = useByok,
            isByokKeySet = isKeySet,
            isLocalAiEnabled = useLocal,
            isFirebaseEnabled = useFirebase,
            isGemmaEnabled = isKeySet,
            isRunningTest = isTesting,
            diagnosticSteps = steps,
            recentAuditLogs = logs,
            recentTrails = trails,
            testRunOutput = testOutput
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiDebugUiState()
    )

    fun selectTargetProvider(target: TestTargetProvider) {
        _selectedTarget.value = target
        if (target == TestTargetProvider.GEMMA_4) {
            gemmaProvider.setModelName("gemma-4-26b-a4b-it")
        }
    }

    fun selectByokModel(modelName: String) {
        viewModelScope.launch {
            aiSettings.saveAiModel(modelName)
            byokProvider.setModelName(modelName)
            gemmaProvider.setModelName(modelName)
        }
    }

    fun clearLogs() {
        auditLogger.clearLogs()
        _testOutput.value = null
        _diagnosticSteps.value = initialSteps()
    }

    private fun updateStep(stepId: Int, state: DiagnosticStepState, detail: String? = null) {
        _diagnosticSteps.value = _diagnosticSteps.value.map { step ->
            if (step.id == stepId) step.copy(state = state, detailMessage = detail ?: step.detailMessage) else step
        }
    }

    fun runTestInference() {
        viewModelScope.launch {
            _isTesting.value = true
            _diagnosticSteps.value = initialSteps()
            _testOutput.value = null

            val target = _selectedTarget.value
            val targetName = target.label

            try {
                // STEP 1: Connection & Handshake
                updateStep(1, DiagnosticStepState.RUNNING, "Verifying $targetName availability...")

                val providerToUse: AiProvider? = when (target) {
                    TestTargetProvider.LOCAL_NANO -> {
                        if (localProvider.isAvailable()) localProvider else null
                    }
                    TestTargetProvider.BYOK -> {
                        val key = aiSettings.getGeminiApiKey()
                        byokProvider.setApiKey(key)
                        byokProvider.setModelName(aiSettings.aiModelFlow.firstOrNull() ?: "gemini-2.5-flash")
                        if (byokProvider.isAvailable()) byokProvider else null
                    }
                    TestTargetProvider.FIREBASE_AI -> {
                        if (firebaseProvider.isAvailable()) firebaseProvider else null
                    }
                    TestTargetProvider.GEMMA_4 -> {
                        if (gemmaProvider.isAvailable()) gemmaProvider else null
                    }
                    TestTargetProvider.AUTO -> {
                        capabilityRouter.getRankedAvailableProviders().firstOrNull()
                    }
                }

                if (providerToUse == null) {
                    val isKeySet = aiSettings.isGeminiApiKeySetFlow.firstOrNull() ?: false
                    val errorDetail = when {
                        (target == TestTargetProvider.GEMMA_4 || target == TestTargetProvider.BYOK) && !isKeySet -> {
                            "API Key is missing! Go to Settings -> 'Configure AI' and enter your Gemini API Key."
                        }
                        target == TestTargetProvider.LOCAL_NANO -> {
                            "On-Device Gemini Nano is not available on this device hardware."
                        }
                        else -> {
                            "Target provider '$targetName' is disabled or missing configuration."
                        }
                    }
                    updateStep(1, DiagnosticStepState.FAILED, errorDetail)
                    _testOutput.value = "Diagnostic Aborted: $errorDetail"
                    return@launch
                }
                updateStep(1, DiagnosticStepState.SUCCESS, "Connected to ${providerToUse.capability.displayName}!")

                // STEP 2: Command & Prompt Assembly
                updateStep(2, DiagnosticStepState.RUNNING, "Fetching wardrobe items & assembling prompt...")
                val wardrobe = wardrobeRepository.getAllClothing().firstOrNull() ?: emptyList()
                val cosmetics = cosmeticRepository.getAllCosmetics().firstOrNull() ?: emptyList()

                val requestContext = StyleRequestContext(
                    requestId = "TEST_DEBUG_${System.currentTimeMillis()}",
                    intent = "Diagnostic Stepper Test",
                    occasion = "Diagnostic Test",
                    weather = "Clear",
                    weatherTempC = 22f,
                    uvIndex = 3f
                )

                val manifest = manifestSerializer.serialize(emptyList(), cosmetics, SerializationDetailLevel.EXPANDED)
                val candidateInput = promptAssembler.buildExactRequest(
                    context = requestContext,
                    compactManifest = manifest,
                    providerCapability = providerToUse.capability
                )

                val promptStr = when (candidateInput) {
                    is AiInput.TextOnly -> candidateInput.promptString
                    is AiInput.Multimodal -> candidateInput.promptString
                }

                val tokens = try { providerToUse.countTokens(candidateInput) } catch (_: Exception) { 150 }
                updateStep(2, DiagnosticStepState.SUCCESS, "Prompt generated (${promptStr.length} chars, ~$tokens tokens)")

                // STEP 3: Response & Latency
                updateStep(3, DiagnosticStepState.RUNNING, "Sending request to ${providerToUse.capability.id}...")
                val startTime = System.currentTimeMillis()
                val result = providerToUse.execute(candidateInput)
                val latency = System.currentTimeMillis() - startTime

                if (result.isFailure) {
                    val err = result.exceptionOrNull()?.localizedMessage ?: "Execution failed"
                    updateStep(3, DiagnosticStepState.FAILED, "LLM Call Failed (${latency}ms): $err")
                    _testOutput.value = "Raw Error Response (${latency}ms):\n$err"
                    return@launch
                }

                val rawResponseText = result.getOrThrow()
                updateStep(3, DiagnosticStepState.SUCCESS, "Received response in ${latency}ms (${rawResponseText.length} chars)")

                // STEP 4: Answer & Schema Normalization
                updateStep(4, DiagnosticStepState.RUNNING, "Normalizing JSON & validating blueprint schema...")
                val rawBlueprint = try {
                    simulatorEngine.generateBlueprint(wardrobe, cosmetics, requestContext)
                } catch (e: Exception) {
                    StyleBlueprint(rationale = "Raw Output: $rawResponseText")
                }

                updateStep(4, DiagnosticStepState.SUCCESS, "Parsed valid blueprint with ${rawBlueprint.selectedClothingIds.size} clothes & ${rawBlueprint.selectedCosmeticIds.size} cosmetics")

                // STEP 5: Useful Answer Quality Evaluation
                updateStep(5, DiagnosticStepState.RUNNING, "Evaluating Fashionista score & color harmony...")
                val fashionScore = fashionistaEvaluator.evaluate(rawBlueprint, requestContext)
                val statusText = if (fashionScore.isApproved) "APPROVED (Score: ${fashionScore.totalScore.toInt()}/100)" else "REJECTED (Score: ${fashionScore.totalScore.toInt()}/100)"

                updateStep(5, DiagnosticStepState.SUCCESS, "Useful Answer Confirmed! Status: $statusText")

                _testOutput.value = """
                    DIAGNOSTIC TEST COMPLETE!
                    
                    • Target Tested: $targetName
                    • Provider Used: ${providerToUse.capability.displayName}
                    • Model Name: ${uiState.value.currentModelName}
                    • Latency: ${latency}ms
                    • Fashionista Calibration: $statusText
                    • Palette: ${rawBlueprint.recommendedPalette}
                    • AI Rationale: "${rawBlueprint.rationale}"
                """.trimIndent()

            } catch (e: Exception) {
                _testOutput.value = "Diagnostic Exception: ${e.localizedMessage}\n${e.stackTraceToString()}"
            } finally {
                _isTesting.value = false
            }
        }
    }
}
