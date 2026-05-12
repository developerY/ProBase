package com.zoewave.probase.gotmind.features.memblox

import android.graphics.Color as AndroidColor
import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class StaticMemBloxEngine(
    scope: CoroutineScope,
    onGameOver: (Int) -> Unit
) : BaseMemBloxEngine(scope, onGameOver) {

    override suspend fun spawnLogic() {
        val col = (0 until currentDifficulty.cols).random()
        
        // Calculate final resting row for static spawn
        val restingRow = (currentDifficulty.rows - 1 downTo 0).firstOrNull { r ->
            _state.value.grid.none { it.row == r && it.col == col }
        } ?: -1

        if (restingRow == -1) {
            _state.update { it.copy(isGameOver = true, finalRankResId = calculateRankResId(it)) }
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
            row = restingRow,
            col = col,
            color = color
        )
        
        _state.update { 
            it.copy(
                grid = it.grid + newBlock,
                peakBoardBlocks = maxOf(it.peakBoardBlocks, it.grid.size + 1),
                initiallyRevealedBlockIds = it.initiallyRevealedBlockIds + newBlock.id
            ) 
        }
        
        triggerHaptic(HapticSignal.LIGHT)
        
        // Show emoji for a "longer time" (e.g., 2.5 seconds)
        scope.launch {
            delay(2500)
            _state.update { it.copy(initiallyRevealedBlockIds = it.initiallyRevealedBlockIds - newBlock.id) }
        }

        spawnCount++
    }
}
