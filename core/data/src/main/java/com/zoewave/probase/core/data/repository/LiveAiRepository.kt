package com.zoewave.probase.core.data.repository

import androidx.lifecycle.LifecycleObserver
import kotlinx.coroutines.flow.StateFlow

interface LiveAiRepository : LifecycleObserver {
    val isSessionActive: StateFlow<Boolean>
    val audioLevel: StateFlow<Float>

    fun startSession()
    fun stopSession()
}
