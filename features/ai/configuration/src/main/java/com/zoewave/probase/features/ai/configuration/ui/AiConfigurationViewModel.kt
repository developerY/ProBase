package com.zoewave.probase.features.ai.configuration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.capture.data.SmartCaptureOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.work.WorkManager
import java.io.File
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.asFlow
import com.zoewave.probase.features.ai.local.data.LocalModelState
import androidx.work.WorkInfo

import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import com.zoewave.probase.features.ai.local.data.NanoState
import com.zoewave.probase.features.ai.local.data.AiModelDownloadWorker
import androidx.lifecycle.asFlow
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy

@HiltViewModel
class AiConfigurationViewModel @Inject constructor(
    private val settings: AiConfigurationSettings,
    private val orchestrator: SmartCaptureOrchestrator,
    private val localAiEngine: LocalAiEngine,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isTestingKey = MutableStateFlow(false)
    private val _keyTestResult = MutableStateFlow<String?>(null)
    private val _isTestingModel = MutableStateFlow(false)
    private val _modelTestResult = MutableStateFlow<String?>(null)
    private val _fetchedModels = MutableStateFlow<List<String>?>(null)

    @Suppress("UNCHECKED_CAST")
    private val _isLocalAiAvailable = MutableStateFlow(false)
    
    private val _localModelState = MutableStateFlow<LocalModelState>(LocalModelState.NotDownloaded)

    init {
        checkLocalAiAvailability()
        checkLocalModelExistenceOnStart()
        observeDownloadWork()
    }

    private fun checkLocalModelExistenceOnStart() {
        val file = File(context.filesDir, "model.litertlm")
        if (file.exists()) {
            _localModelState.value = LocalModelState.Ready(file.absolutePath, "1.0")
        } else {
            _localModelState.value = LocalModelState.NotDownloaded
        }
    }
    
    private fun observeDownloadWork() {
        viewModelScope.launch {
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData("ai_model_download")
                .asFlow()
                .collect { workInfos ->
                    val info = workInfos.firstOrNull() ?: return@collect
                    val state = info.progress.getString(AiModelDownloadWorker.STATE_KEY)
                    when (info.state) {
                        WorkInfo.State.RUNNING -> {
                            when (state) {
                                "DOWNLOADING" -> {
                                    val downloaded = info.progress.getLong(AiModelDownloadWorker.BYTES_DOWNLOADED_KEY, 0L)
                                    val total = info.progress.getLong(AiModelDownloadWorker.TOTAL_BYTES_KEY, 0L)
                                    val pct = info.progress.getInt(AiModelDownloadWorker.PROGRESS_KEY, 0)
                                    _localModelState.value = LocalModelState.Downloading(downloaded, total, pct)
                                }
                                "VERIFYING" -> {
                                    _localModelState.value = LocalModelState.VerifyingChecksum
                                }
                            }
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            val path = info.outputData.getString("path") ?: ""
                            _localModelState.value = LocalModelState.Ready(path, "1.0")
                        }
                        WorkInfo.State.FAILED -> {
                            val reason = info.outputData.getString("reason") ?: "Unknown error"
                            _localModelState.value = LocalModelState.Error(reason, true)
                        }
                        WorkInfo.State.ENQUEUED -> {
                            // Can stay as downloading or not downloaded
                        }
                        else -> {}
                    }
                }
        }
    }

    private fun checkLocalAiAvailability() {
        viewModelScope.launch {
            val capability = localAiEngine.checkCapability()
            _isLocalAiAvailable.value = capability == NanoState.Available || capability == NanoState.MultimodalAvailable
        }
    }

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<AiConfigurationUiState> = combine(
        settings.isGeminiApiKeySetFlow,
        settings.useByokKey,
        settings.isAiEnabledFlow,
        settings.aiModelFlow,
        settings.useFirebaseVertexAi,
        _isLocalAiAvailable,
        _isTestingKey,
        _keyTestResult,
        _isTestingModel,
        _modelTestResult,
        _fetchedModels,
        settings.useLocalAi,
        settings.useDownloadAi,
        _localModelState
    ) { args: Array<Any?> ->
        AiConfigurationUiState(
            isApiKeySet = args[0] as Boolean,
            useByokKey = args[1] as Boolean,
            isAiEnabled = args[2] as Boolean,
            currentAiModel = args[3] as String,
            useFirebaseVertexAi = args[4] as Boolean,
            isLocalAiAvailable = args[5] as Boolean,
            isTestingKey = args[6] as Boolean,
            keyTestResult = args[7] as String?,
            isTestingModel = args[8] as Boolean,
            modelTestResult = args[9] as String?,
            availableModels = (args[10] as List<String>?) ?: if (args[0] as Boolean) listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemma-4-26b-a4b-it", "gemma-4-31b-it", "gemini-1.5-flash", "gemini-1.5-pro") else emptyList(),
            useLocalAi = args[11] as Boolean,
            useDownloadAi = args[12] as Boolean,
            localModelState = args[13] as LocalModelState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiConfigurationUiState()
    )

    fun onEvent(event: AiConfigurationEvent) {
        when (event) {
            is AiConfigurationEvent.OnAiEnabledToggled -> {
                viewModelScope.launch { settings.saveAiEnabled(event.enabled) }
            }
            is AiConfigurationEvent.OnGeminiApiKeyChanged -> {
                viewModelScope.launch { settings.saveGeminiApiKey(event.apiKey) }
            }
            is AiConfigurationEvent.OnAiModelSelected -> {
                viewModelScope.launch { settings.saveAiModel(event.model) }
            }
            is AiConfigurationEvent.OnUseByokKeyToggled -> {
                viewModelScope.launch { 
                    settings.saveUseByokKey(event.enabled) 
                    if (event.enabled) {
                        settings.saveUseLocalAi(false)
                        settings.saveUseFirebaseVertexAi(false)
                        settings.saveUseDownloadAi(false)
                    }
                }
            }
            is AiConfigurationEvent.OnUseLocalAiToggled -> {
                viewModelScope.launch { 
                    settings.saveUseLocalAi(event.enabled) 
                    if (event.enabled) {
                        settings.saveUseByokKey(false)
                        settings.saveUseFirebaseVertexAi(false)
                        settings.saveUseDownloadAi(false)
                    }
                }
            }
            is AiConfigurationEvent.OnUseFirebaseVertexAiToggled -> {
                viewModelScope.launch { 
                    settings.saveUseFirebaseVertexAi(event.enabled) 
                    if (event.enabled) {
                        settings.saveUseByokKey(false)
                        settings.saveUseLocalAi(false)
                        settings.saveUseDownloadAi(false)
                    }
                }
            }
            is AiConfigurationEvent.OnUseDownloadAiToggled -> {
                viewModelScope.launch {
                    settings.saveUseDownloadAi(event.enabled)
                    if (event.enabled) {
                        settings.saveUseByokKey(false)
                        settings.saveUseLocalAi(false)
                        settings.saveUseFirebaseVertexAi(false)
                    }
                }
            }
            is AiConfigurationEvent.OnTestApiKeyClicked -> {
                testApiKey()
            }
            is AiConfigurationEvent.OnTestModelClicked -> {
                testModel()
            }
            is AiConfigurationEvent.OnStartModelDownloadClicked -> {
                startModelDownload()
            }
            is AiConfigurationEvent.OnCancelModelDownloadClicked -> {
                WorkManager.getInstance(context).cancelUniqueWork("ai_model_download")
            }
            is AiConfigurationEvent.OnRetryModelDownloadClicked -> {
                startModelDownload()
            }
            is AiConfigurationEvent.OnDeleteModelClicked -> {
                val targetFile = File(context.filesDir, "model.litertlm")
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                _localModelState.value = LocalModelState.NotDownloaded
            }
            is AiConfigurationEvent.OnConfirmCellularDownloadClicked -> {
                startModelDownload(forceCellular = true)
            }
        }
    }
    
    private fun startModelDownload(forceCellular: Boolean = false) {
        val constraintsBuilder = androidx.work.Constraints.Builder()
            .setRequiresStorageNotLow(true)
            
        if (!forceCellular) {
            constraintsBuilder.setRequiredNetworkType(androidx.work.NetworkType.UNMETERED)
        } else {
            constraintsBuilder.setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
        }

        val downloadRequest = OneTimeWorkRequestBuilder<AiModelDownloadWorker>()
            .setConstraints(constraintsBuilder.build())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
            
        WorkManager.getInstance(context).enqueueUniqueWork(
            "ai_model_download",
            ExistingWorkPolicy.REPLACE,
            downloadRequest
        )
    }

    private fun testApiKey() {
        viewModelScope.launch {
            _isTestingKey.value = true
            _keyTestResult.value = "Testing connection..."
            
            val key = settings.getGeminiApiKey()
            
            if (key.isNullOrBlank()) {
                _keyTestResult.value = "Error: No API key saved."
                _isTestingKey.value = false
                return@launch
            }

            val result = orchestrator.validateApiKey(key)
            _keyTestResult.value = result.first
            if (result.second.isNotEmpty()) {
                _fetchedModels.value = result.second
            }
            _isTestingKey.value = false
        }
    }

    private fun testModel() {
        viewModelScope.launch {
            val key = settings.getGeminiApiKey()
            val model = settings.aiModelFlow.firstOrNull() ?: "gemini-2.0-flash"

            if (key.isNullOrBlank()) {
                _keyTestResult.value = "Error: No API key saved."
                return@launch
            }

            _isTestingModel.value = true
            _modelTestResult.value = "Pinging model..."

            val result = orchestrator.testModel(key, model)
            _modelTestResult.value = result
            _isTestingModel.value = false
        }
    }
}
