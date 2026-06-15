package com.zoewave.probase.data.repository

import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultGlassBridgeRepository @Inject constructor() : GlassBridgeRepository {
    private val _isGlassConnected = MutableStateFlow(false)
    override val isGlassConnected: StateFlow<Boolean> = _isGlassConnected.asStateFlow()

    private val _isGlassSessionActive = MutableStateFlow(false)
    override val isGlassSessionActive: StateFlow<Boolean> = _isGlassSessionActive.asStateFlow()

    private val _glassCommands = MutableSharedFlow<String>()
    override val glassCommands: SharedFlow<String> = _glassCommands.asSharedFlow()

    override fun updateGlassConnectionState(isConnected: Boolean) {
        _isGlassConnected.value = isConnected
    }

    override fun updateGlassSessionState(isActive: Boolean) {
        _isGlassSessionActive.value = isActive
    }

    override suspend fun sendGlassCommand(command: String) {
        _glassCommands.emit(command)
    }
}
