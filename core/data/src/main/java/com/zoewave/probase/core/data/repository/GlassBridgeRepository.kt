package com.zoewave.probase.core.data.repository

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface GlassBridgeRepository {
    val isGlassConnected: StateFlow<Boolean>
    val isGlassSessionActive: StateFlow<Boolean>
    val glassCommands: SharedFlow<String>
    fun updateGlassConnectionState(isConnected: Boolean)
    fun updateGlassSessionState(isActive: Boolean)
    suspend fun sendGlassCommand(command: String)
}
