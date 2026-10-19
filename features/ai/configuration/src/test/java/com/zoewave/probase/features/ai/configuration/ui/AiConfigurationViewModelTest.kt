package com.zoewave.probase.features.ai.configuration.ui

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.capture.data.SmartCaptureOrchestrator
import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import com.zoewave.probase.features.ai.local.data.NanoState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AiConfigurationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val settings: AiConfigurationSettings = mockk(relaxed = true)
    private val orchestrator: SmartCaptureOrchestrator = mockk(relaxed = true)
    private val localAiEngine: LocalAiEngine = mockk(relaxed = true)

    private lateinit var viewModel: AiConfigurationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { settings.isGeminiApiKeySetFlow } returns flowOf(true)
        coEvery { settings.useByokKey } returns flowOf(true)
        coEvery { settings.isAiEnabledFlow } returns flowOf(true)
        coEvery { settings.aiModelFlow } returns flowOf("gemini-3.8-flash")
        coEvery { settings.useFirebaseVertexAi } returns flowOf(false)
        coEvery { settings.useLocalAi } returns flowOf(false)
        coEvery { localAiEngine.checkCapability() } returns NanoState.Available

        viewModel = AiConfigurationViewModel(settings, orchestrator, localAiEngine)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testUiStateInitialization() = runTest {
        val states = mutableListOf<AiConfigurationUiState>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states.add(it) }
        }
        testScheduler.advanceUntilIdle()

        val state = states.last()
        assertThat(state.isApiKeySet).isTrue()
        assertThat(state.useByokKey).isTrue()
        assertThat(state.currentAiModel).isEqualTo("gemini-3.8-flash")
        assertThat(state.isLocalAiAvailable).isTrue()

        job.cancel()
    }

    @Test
    fun testToggleAiEnabledEvent() = runTest {
        viewModel.onEvent(AiConfigurationEvent.OnAiEnabledToggled(false))
        testScheduler.advanceUntilIdle()

        coVerify { settings.saveAiEnabled(false) }
    }

    @Test
    fun testSelectModelEvent() = runTest {
        viewModel.onEvent(AiConfigurationEvent.OnAiModelSelected("gemini-2.5-flash"))
        testScheduler.advanceUntilIdle()

        coVerify { settings.saveAiModel("gemini-2.5-flash") }
    }
}
