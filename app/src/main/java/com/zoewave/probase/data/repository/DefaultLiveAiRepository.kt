package com.zoewave.probase.data.repository

import com.zoewave.probase.core.data.repository.LiveAiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultLiveAiRepository @Inject constructor() : LiveAiRepository {
    private val _isSessionActive = MutableStateFlow(false)
    override val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    override val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    override fun startSession() {
        _isSessionActive.value = true
    }

    override fun stopSession() {
        _isSessionActive.value = false
    }
}
