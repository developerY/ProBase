package com.zoewave.probase.kocolor.features.clothingcapture.ui

import android.content.Context
import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.features.analyzer.data.LocalProductAnalyzer
import com.zoewave.probase.kocolor.features.clothingcapture.ui.state.ClothingCaptureStep
import com.zoewave.probase.kocolor.features.clothingcapture.ui.state.ClothingCaptureUiState
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClothingCaptureViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val aiSettings: AiConfigurationSettings = mockk(relaxed = true)
    private val sessionRepository: FashionSessionRepository = mockk(relaxed = true)
    private val localAnalyzer: LocalProductAnalyzer = mockk(relaxed = true)
    private val localAi: LocalAiEngine = mockk(relaxed = true)
    private val colorRepository: ColorRepository = mockk(relaxed = true)

    private lateinit var viewModel: ClothingCaptureViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0

        every { sessionRepository.discoveryStatus } returns MutableStateFlow(mockk(relaxed = true))

        viewModel = ClothingCaptureViewModel(
            context,
            aiSettings,
            sessionRepository,
            localAnalyzer,
            localAi,
            colorRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialCaptureStepIsFront() {
        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(ClothingCaptureUiState.Idle::class.java)
        val idleState = state as ClothingCaptureUiState.Idle
        assertThat(idleState.currentStep).isEqualTo(ClothingCaptureStep.FRONT)
    }

    @Test
    fun testReset() {
        viewModel.reset()

        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(ClothingCaptureUiState.Idle::class.java)
        val idleState = state as ClothingCaptureUiState.Idle
        assertThat(idleState.currentStep).isEqualTo(ClothingCaptureStep.FRONT)
    }
}
