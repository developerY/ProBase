package com.zoewave.probase.gotmind.features.memblox

import android.graphics.Paint
import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MemBloxState(
    val grid: List<MemBloxBlock> = emptyList(),
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val flippedBlocks: List<MemBloxBlock> = emptyList(),
    val pairsMatched: Int = 0,
    val totalPairsSpawned: Int = 0,
    val targetPairs: Int = 50
)

class MemBloxEngine(
    private val scope: CoroutineScope,
    private val onGameOver: (Int) -> Unit
) {
    private val cols = 12
    private val rows = 20
    private val emojis = generateSupportedEmojiList()
    
    // Internal queue to ensure matches are possible
    private val pendingPairs = mutableListOf<String>()
    private var spawnCount = 0

    private fun generateSupportedEmojiList(): List<String> {
        val paint = Paint()
        val emojiList = mutableListOf<String>()
        val ranges = listOf(
            0x1F600..0x1F64F, // Emoticons
            0x1F300..0x1F5FF, // Misc Symbols & Pictographs
            0x1F680..0x1F6FF, // Transport and Map
            0x1F900..0x1F9FF, // Supplemental Symbols
            0x1FA70..0x1FAFF  // Symbols Extended-A
        )

        for (range in ranges) {
            for (codePoint in range) {
                val emoji = String(Character.toChars(codePoint))
                if (paint.hasGlyph(emoji)) {
                    emojiList.add(emoji)
                }
            }
        }
        
        return if (emojiList.size > 200) emojiList.shuffled().take(200) else emojiList.ifEmpty { 
            listOf("🍎", "🍌", "🍇", "🍊", "🍓", "🍒", "🍍", "🥝", "🍉", "🍑") 
        }
    }

    private val _state = MutableStateFlow(MemBloxState())
    val state: StateFlow<MemBloxState> = _state.asStateFlow()

    private var gameJob: Job? = null

    fun start() {
        _state.value = MemBloxState()
        pendingPairs.clear()
        spawnCount = 0
        gameJob?.cancel()
        gameJob = scope.launch {
            while (!_state.value.isGameOver && !_state.value.isVictory) {
                delay(1000)
                spawnBlock()
                applyGravity()
            }
        }
    }

    private fun spawnBlock() {
        val col = (0 until cols).random()
        // Check if top row is full
        if (_state.value.grid.any { it.row == 0 && it.col == col }) {
            _state.update { it.copy(isGameOver = true) }
            onGameOver(_state.value.score)
            return
        }

        // Logic to ensure matches
        val spawnNewPair = when {
            pendingPairs.isEmpty() -> true
            pendingPairs.size >= 12 -> false // Force match if board is cluttered
            spawnCount < 10 -> true // Initial variety
            else -> (0..1).random() == 0 // 50/50 chance to finish a pair or start a new one
        }

        val emoji = if (spawnNewPair) {
            val e = emojis.random()
            pendingPairs.add(e)
            _state.update { it.copy(totalPairsSpawned = it.totalPairsSpawned + 1) }
            e
        } else {
            pendingPairs.removeAt((0 until pendingPairs.size).random())
        }

        // Generate random pastel coral color
        val r = 255
        val g = (160..210).random()
        val b = (140..190).random()
        val color = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

        val newBlock = MemBloxBlock(
            id = UUID.randomUUID().toString(),
            emoji = emoji,
            row = 0,
            col = col,
            color = color
        )
        _state.update { it.copy(grid = it.grid + newBlock) }
        spawnCount++
    }

    private fun applyGravity() {
        _state.update { state ->
            val newGrid = state.grid.toMutableList()
            var changed = true
            while (changed) {
                changed = false
                for (i in newGrid.indices) {
                    val block = newGrid[i]
                    if (block.row < rows - 1 && !newGrid.any { it.row == block.row + 1 && it.col == block.col }) {
                        newGrid[i] = block.copy(row = block.row + 1)
                        changed = true
                    }
                }
            }
            state.copy(grid = newGrid)
        }
    }

    fun onBlockClick(block: MemBloxBlock) {
        if (_state.value.isGameOver || _state.value.isVictory) return
        if (block.isMatched || block.isFlipped || _state.value.flippedBlocks.size >= 2) return

        _state.update { state ->
            val newGrid = state.grid.map { if (it.id == block.id) it.copy(isFlipped = true) else it }
            val newFlipped = state.flippedBlocks + block.copy(isFlipped = true)
            state.copy(grid = newGrid, flippedBlocks = newFlipped)
        }

        if (_state.value.flippedBlocks.size == 2) {
            scope.launch {
                delay(500)
                checkMatch()
            }
        }
    }

    private fun checkMatch() {
        _state.update { state ->
            val flipped = state.flippedBlocks
            if (flipped[0].emoji == flipped[1].emoji) {
                // Match!
                val matchedIds = flipped.map { it.id }.toSet()
                val newGrid = state.grid.filterNot { it.id in matchedIds }
                val newPairsMatched = state.pairsMatched + 1
                val isVictory = newPairsMatched >= state.targetPairs && newGrid.isEmpty()
                
                state.copy(
                    grid = newGrid,
                    flippedBlocks = emptyList(),
                    score = state.score + 10,
                    pairsMatched = newPairsMatched,
                    isVictory = isVictory
                ).also {
                    scope.launch { applyGravity() }
                    if (isVictory) {
                        onGameOver(it.score)
                    }
                }
            } else {
                // No match
                val newGrid = state.grid.map { it.copy(isFlipped = false) }
                state.copy(grid = newGrid, flippedBlocks = emptyList())
            }
        }
    }
}
