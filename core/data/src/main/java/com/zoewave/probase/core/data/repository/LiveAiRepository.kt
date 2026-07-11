package com.zoewave.probase.core.data.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.StateFlow

interface LiveAiRepository : DefaultLifecycleObserver {
    val isSessionActive: StateFlow<Boolean>
    val audioLevel: StateFlow<Float>

    fun startSession()
    fun stopSession()

    // 1. Automatically kill the AI session when the glasses are taken off
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        stopSession()
    }

    // 2. (Optional) Auto-start the session when the activity resumes
    // override fun onStart(owner: LifecycleOwner) {
    //     super.onStart(owner)
    //     startSession()
    // }
}
