package com.zoewave.probase.gotmind.features.memblox

import android.graphics.Color as AndroidColor
import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import java.util.UUID

class FallingMemBloxEngine(
    scope: CoroutineScope,
    onGameOver: (Int) -> Unit
) : BaseMemBloxEngine(scope, onGameOver) {

    override suspend fun spawnLogic() {
        val col = (0 until currentDifficulty.cols).random()
        if (_state.value.grid.any { it.row == 0 && it.col == col }) {
            _state.update { it.copy(isGameOver = true, finalRank = calculateRank(it)) }
            triggerHaptic(HapticSignal.HEAVY)
            onGameOver(_state.value.score)
            return
        }

        val spawnNewPair = when {
            pendingPairs.isEmpty() -> true
            pendingPairs.size >= currentDifficulty.cols -> false
            spawnCount < 10 -> true
            else -> (0..1).random() == 0
        }

        val emoji = if (spawnNewPair) {
            val e = emojis.random()
            pendingPairs.add(e)
            _state.update { it.copy(totalPairsSpawned = it.totalPairsSpawned + 1) }
            e
        } else {
            pendingPairs.removeAt((0 until pendingPairs.size).random())
        }

        val hue = (0..359).random().toFloat()
        val saturation = 0.35f
        val value = 0.95f
        val color = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))

        val newBlock = MemBloxBlock(
            id = UUID.randomUUID().toString(),
            emoji = emoji,
            row = -_state.value.dropHeight,
            col = col,
            color = color
        )
        _state.update { 
            it.copy(
                grid = it.grid + newBlock,
                peakBoardBlocks = maxOf(it.peakBoardBlocks, it.grid.size + 1)
            ) 
        }
        
        triggerHaptic(HapticSignal.LIGHT)
        spawnCount++
    }
}
