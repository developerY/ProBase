package com.zoewave.probase.features.ai.configuration.ui

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.capture.data.SmartCaptureOrchestrator
import com.zoewave.probase.features.ai.local.data.AiModelDownloadWorker
import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import com.zoewave.probase.features.ai.local.data.LocalLiteRtLmEngineManager
import com.zoewave.probase.features.ai.local.data.LocalModelState
import com.zoewave.probase.features.ai.local.data.NanoState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AiConfigurationViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settings: AiConfigurationSettings
    private lateinit var orchestrator: SmartCaptureOrchestrator
    private lateinit var localAiEngine: LocalAiEngine
    private lateinit var engineManager: LocalLiteRtLmEngineManager
    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var workInfoLiveData: MutableLiveData<List<WorkInfo>>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        settings = mockk(relaxed = true) {
            every { isAiEnabledFlow } returns MutableStateFlow(true)
            every { aiModelFlow } returns MutableStateFlow("gemini-1.5-flash")
            every { isGeminiApiKeySetFlow } returns MutableStateFlow(false)
            every { useByokKey } returns MutableStateFlow(false)
            every { useLocalAi } returns MutableStateFlow(false)
            every { useFirebaseVertexAi } returns MutableStateFlow(false)
            every { useDownloadAi } returns MutableStateFlow(true)
        }

        orchestrator = mockk(relaxed = true)

        localAiEngine = mockk(relaxed = true) {
            coEvery { checkCapability() } returns NanoState.Unsupported
        }

        engineManager = mockk(relaxed = true)

        context = androidx.test.core.app.ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        val testWorkManager = WorkManager.getInstance(context)
        workManager = io.mockk.spyk(testWorkManager)

        workInfoLiveData = MutableLiveData()

        io.mockk.mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns workManager
        every { workManager.getWorkInfosForUniqueWorkLiveData("ai_model_download") } returns workInfoLiveData
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
        val fakeFilesDir = File(System.getProperty("java.io.tmpdir")!!, "probase_test_files")
        fakeFilesDir.deleteRecursively()
    }

    @Test
    fun `test WorkManager DOWNLOADING updates UI state to Downloading with progress`() = runTest {
        val viewModel = AiConfigurationViewModel(settings, orchestrator, localAiEngine, engineManager, context)
        testDispatcher.scheduler.advanceUntilIdle()

        val progressData = Data.Builder()
            .putString(AiModelDownloadWorker.STATE_KEY, "DOWNLOADING")
            .putLong(AiModelDownloadWorker.BYTES_DOWNLOADED_KEY, 500L)
            .putLong(AiModelDownloadWorker.TOTAL_BYTES_KEY, 1000L)
            .putInt(AiModelDownloadWorker.PROGRESS_KEY, 50)
            .build()

        val workInfo = mockk<WorkInfo>()
        every { workInfo.state } returns WorkInfo.State.RUNNING
        every { workInfo.progress } returns progressData

        workInfoLiveData.value = listOf(workInfo)
        testDispatcher.scheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value.localModelState
        assertThat(currentState).isInstanceOf(LocalModelState.Downloading::class.java)

        val dlState = currentState as LocalModelState.Downloading
        assertThat(dlState.bytesDownloaded).isEqualTo(500L)
        assertThat(dlState.totalBytes).isEqualTo(1000L)
        assertThat(dlState.progressPercent).isEqualTo(50)
    }

    @Test
    fun `test WorkManager FAILED updates UI state to Error`() = runTest {
        val viewModel = AiConfigurationViewModel(settings, orchestrator, localAiEngine, engineManager, context)
        testDispatcher.scheduler.advanceUntilIdle()

        val outputData = Data.Builder()
            .putString("reason", "HTTP Error: 404")
            .build()

        val workInfo = mockk<WorkInfo>()
        every { workInfo.state } returns WorkInfo.State.FAILED
        every { workInfo.outputData } returns outputData
        every { workInfo.progress } returns Data.EMPTY

        workInfoLiveData.value = listOf(workInfo)
        testDispatcher.scheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value.localModelState
        assertThat(currentState).isInstanceOf(LocalModelState.Error::class.java)

        val errorState = currentState as LocalModelState.Error
        assertThat(errorState.reason).isEqualTo("HTTP Error: 404")
        assertThat(errorState.isRetryable).isTrue()
    }
}
