package com.zoewave.probase.ashbike.data.repository

import com.zoewave.probase.core.data.repository.GlassBridgeRepository
import com.zoewave.probase.core.data.repository.RitualRepository
import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.core.model.ritual.CosmeticItem
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeRitualRepository @Inject constructor() : RitualRepository, GlassBridgeRepository {

    override fun getRoutinesForDay(start: Long, end: Long): Flow<List<BeautyRoutine>> = flowOf(emptyList())
    override suspend fun updateRoutine(routine: BeautyRoutine) {}
    override fun getCosmeticById(id: Long): Flow<CosmeticItem?> = flowOf(null)
    override suspend fun updateCosmetic(item: CosmeticItem) {}

    private val _isConnected = MutableStateFlow(false)
    override val isGlassConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isSessionActive = MutableStateFlow(false)
    override val isGlassSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _commands = MutableSharedFlow<String>()
    override val glassCommands: SharedFlow<String> = _commands.asSharedFlow()

    override fun updateGlassConnectionState(isConnected: Boolean) {
        _isConnected.value = isConnected
    }

    override fun updateGlassSessionState(isActive: Boolean) {
        _isSessionActive.value = isActive
    }

    override suspend fun sendGlassCommand(command: String) {
        _commands.emit(command)
    }
}
