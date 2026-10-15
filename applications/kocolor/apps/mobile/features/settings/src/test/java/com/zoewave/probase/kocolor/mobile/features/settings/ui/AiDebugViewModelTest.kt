package com.zoewave.probase.kocolor.mobile.features.settings.ui

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.core.ByokAiProvider
import com.zoewave.probase.features.ai.firebase.FirebaseAiProvider
import com.zoewave.probase.features.ai.local.data.LocalNanoAiProvider
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditLogger
import com.zoewave.probase.kocolor.data.usecase.CapabilityRouter
import com.zoewave.probase.kocolor.data.usecase.CompactManifestSerializer
import com.zoewave.probase.kocolor.data.usecase.PromptAssembler
import com.zoewave.probase.kocolor.data.usecase.RecommendationValidator
import com.zoewave.probase.kocolor.data.usecase.StyleSimulatorEngine
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaEvaluator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiDebugViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val aiSettings: AiConfigurationSettings = mockk(relaxed = true)
    private val auditLogger: StyleAuditLogger = mockk(relaxed = true)
    private val capabilityRouter: CapabilityRouter = mockk(relaxed = true)
    private val simulatorEngine: StyleSimulatorEngine = mockk(relaxed = true)
    private val wardrobeRepository: WardrobeRepository = mockk(relaxed = true)
    private val cosmeticRepository: CosmeticInventoryRepository = mockk(relaxed = true)
    private val byokProvider: ByokAiProvider = mockk(relaxed = true)
    private val localProvider: LocalNanoAiProvider = mockk(relaxed = true)
    private val firebaseProvider: FirebaseAiProvider = mockk(relaxed = true)
    private val promptAssembler: PromptAssembler = mockk(relaxed = true)
    private val manifestSerializer: CompactManifestSerializer = mockk(relaxed = true)
    private val validator: RecommendationValidator = mockk(relaxed = true)
    private val fashionistaEvaluator: FashionistaEvaluator = mockk(relaxed = true)

    private lateinit var viewModel: AiDebugViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0

        coEvery { aiSettings.aiModelFlow } returns flowOf("gemini-3.8-flash")
        coEvery { aiSettings.useByokKey } returns flowOf(true)
        coEvery { aiSettings.isGeminiApiKeySetFlow } returns flowOf(true)
        coEvery { aiSettings.useLocalAi } returns flowOf(false)
        coEvery { aiSettings.useFirebaseVertexAi } returns flowOf(true)
        every { auditLogger.recentLogs } returns MutableStateFlow(listOf("Log 1", "Log 2"))
        every { auditLogger.recentTrails } returns MutableStateFlow(emptyList())
        coEvery { capabilityRouter.getActiveProviderName() } returns "Firebase AI Logic (Cloud)"

        viewModel = AiDebugViewModel(
            aiSettings,
            auditLogger,
            capabilityRouter,
            simulatorEngine,
            wardrobeRepository,
            cosmeticRepository,
            byokProvider,
            localProvider,
            firebaseProvider,
            promptAssembler,
            manifestSerializer,
            validator,
            fashionistaEvaluator
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testUiStateInitializationAndFlowCombine() = runTest {
        val states = mutableListOf<AiDebugUiState>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }
        testScheduler.advanceUntilIdle()

        val state = states.last()
        assertThat(state.currentModelName).isEqualTo("gemini-3.8-flash")
        assertThat(state.isByokEnabled).isTrue()
        assertThat(state.isFirebaseEnabled).isTrue()
        assertThat(state.recentAuditLogs).containsExactly("Log 1", "Log 2")

        job.cancel()
    }

    @Test
    fun testSelectTargetProvider() = runTest {
        val states = mutableListOf<AiDebugUiState>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }
        testScheduler.advanceUntilIdle()

        viewModel.selectTargetProvider(TestTargetProvider.LOCAL_NANO)
        testScheduler.advanceUntilIdle()

        assertThat(states.last().selectedTarget).isEqualTo(TestTargetProvider.LOCAL_NANO)

        job.cancel()
    }

    @Test
    fun testSelectByokModel() = runTest {
        viewModel.selectByokModel("gemini-2.5-flash")
        testScheduler.advanceUntilIdle()

        coVerify { aiSettings.saveAiModel("gemini-2.5-flash") }
        verify { byokProvider.setModelName("gemini-2.5-flash") }
    }

    @Test
    fun testClearLogs() {
        viewModel.clearLogs()

        assertThat(viewModel.uiState.value.testRunOutput).isNull()
    }
}
