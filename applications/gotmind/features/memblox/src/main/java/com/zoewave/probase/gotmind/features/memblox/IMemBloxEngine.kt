package com.zoewave.probase.gotmind.features.memblox

import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import com.zoewave.probase.gotmind.model.memblox.MemBloxDifficulty
import kotlinx.coroutines.flow.StateFlow

interface IMemBloxEngine {
    val state: StateFlow<MemBloxState>
    
    fun start(difficulty: MemBloxDifficulty)
    fun reset()
    fun togglePause()
    fun updateSpeed(multiplier: Float)
    fun updateDropHeight(height: Int)
    fun updateDropDuration(durationMillis: Int)
    fun setHapticsEnabled(enabled: Boolean)
    fun setSoundEnabled(enabled: Boolean)
    fun onBlockClick(block: MemBloxBlock)
    fun usePowerUp(type: PowerUpType)
    fun onHapticConsumed()
}
