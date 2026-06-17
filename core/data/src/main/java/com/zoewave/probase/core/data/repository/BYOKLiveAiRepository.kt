package com.zoewave.probase.core.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class BYOKLiveAiRepository @Inject constructor(
    private val aiSettings: AiConfigurationSettings,
) : LiveAiRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _isSessionActive = MutableStateFlow(false)
    override val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    override val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    override fun startSession() {
        val apiKey = aiSettings.getGeminiApiKey()
        if (apiKey.isNullOrBlank()) {
            _isSessionActive.value = false
            return
        }

        scope.launch {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )
                model.startChat()
                _isSessionActive.value = true
                
                // Simulating audio level for the UI
                launch {
                    while (_isSessionActive.value) {
                        _audioLevel.value = (0.1f..0.9f).random()
                        kotlinx.coroutines.delay(100.milliseconds)
                    }
                    _audioLevel.value = 0f
                }
            } catch (e: Exception) {
                _isSessionActive.value = false
            }
        }
    }

    override fun stopSession() {
        _isSessionActive.value = false
    }

    private fun ClosedRange<Float>.random() = 
        start + (endInclusive - start) * (java.util.Random().nextFloat())
}
