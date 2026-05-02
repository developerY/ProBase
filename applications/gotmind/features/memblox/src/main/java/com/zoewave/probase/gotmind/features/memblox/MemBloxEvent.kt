package com.zoewave.probase.gotmind.features.memblox

import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import com.zoewave.probase.gotmind.model.memblox.MemBloxDifficulty

sealed interface MemBloxEvent {
    data class StartGame(val difficulty: MemBloxDifficulty) : MemBloxEvent
    data class BlockClick(val block: MemBloxBlock) : MemBloxEvent
    data class UsePowerUp(val type: PowerUpType) : MemBloxEvent
    data object ResetToSelection : MemBloxEvent
    data object HapticConsumed : MemBloxEvent
    data object TogglePause : MemBloxEvent
    data class UpdateSpeed(val multiplier: Float) : MemBloxEvent
    data class UpdateDropHeight(val height: Int) : MemBloxEvent
    data class UpdateDropDuration(val durationMillis: Int) : MemBloxEvent
    data class SetEngineType(val type: MemBloxEngineType) : MemBloxEvent
    data object ClearHallOfFame : MemBloxEvent
}
