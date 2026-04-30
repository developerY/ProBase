package com.zoewave.probase.gotmind.features.memblox

import android.graphics.Paint
import android.graphics.Color as AndroidColor
import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import com.zoewave.probase.gotmind.model.memblox.MemBloxDifficulty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class PowerUpType(val label: String, val icon: String) {
    FREEZE("Freeze", "❄️"),
    REVEAL("Reveal", "👁️"),
    NUKE("Nuke", "☢️"),
    HINT("Hint", "💡")
}

enum class HapticSignal { LIGHT, MEDIUM, HEAVY }

data class ConfettiBurst(
    val id: String = UUID.randomUUID().toString(),
    val col: Int,
    val row: Int
)

data class FloatingTextEffect(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val col: Int,
    val row: Int,
    val color: Int = 0xFFFFEB3B.toInt()
)

data class MemBloxState(
    val grid: List<MemBloxBlock> = emptyList(),
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val flippedBlocks: List<MemBloxBlock> = emptyList(),
    val pairsMatched: Int = 0,
    val totalPairsSpawned: Int = 0,
    val targetPairs: Int = 50,
    val cols: Int = 12,
    val rows: Int = 20,
    val difficulty: MemBloxDifficulty = MemBloxDifficulty.EXPERT,
    val isStarted: Boolean = false,
    
    // Analytics & New Mechanics
    val combo: Int = 0,
    val multiplier: Float = 1.0f,
    val peakCombo: Int = 0,
    val totalClicks: Int = 0,
    val successfulMatches: Int = 0,
    val missedMatches: Int = 0,
    val matchAccuracy: Float = 0f,
    val powerUps: Map<PowerUpType, Int> = mapOf(PowerUpType.FREEZE to 2, PowerUpType.REVEAL to 1, PowerUpType.NUKE to 1, PowerUpType.HINT to 2),
    val isFrozen: Boolean = false,
    val isRevealed: Boolean = false,
    val nukingBlockIds: Map<String, Int> = emptyMap(),
    val initiallyRevealedBlockIds: Set<String> = emptySet(),
    val confettiBursts: List<ConfettiBurst> = emptyList(),
    
    // 5-Star Polish VFX State
    val shakeIntensity: Float = 0f,
    val frostAlpha: Float = 0f,
    val hintedBlockIds: Set<String> = emptySet(),
    val floatingTexts: List<FloatingTextEffect> = emptyList(),
    val lastHapticSignal: HapticSignal? = null,
    
    // Skill Tracking
    val bestMatchStreak: Int = 0,
    val currentMatchStreak: Int = 0,
    val avgMatchTimeMs: Long = 0,
    val totalMatchTimeMs: Long = 0,
    val peakBoardBlocks: Int = 0,
    val firstFlipTimestamp: Long = 0
)

class MemBloxEngine(
    private val scope: CoroutineScope,
    private val onGameOver: (Int) -> Unit
) {
    private var currentDifficulty = MemBloxDifficulty.EXPERT
    private val emojis = generateSupportedEmojiList()
    
    private val pendingPairs = mutableListOf<String>()
    private var spawnCount = 0
    private var lastMatchTime = 0L

    private fun generateSupportedEmojiList(): List<String> {
        val paint = Paint()
        val emojiList = mutableListOf<String>()
        val ranges = listOf(
            0x1F600..0x1F64F, 0x1F300..0x1F5FF, 0x1F680..0x1F6FF, 0x1F900..0x1F9FF, 0x1FA70..0x1FAFF
        )
        for (range in ranges) {
            for (codePoint in range) {
                val emoji = String(Character.toChars(codePoint))
                if (paint.hasGlyph(emoji)) emojiList.add(emoji)
            }
        }
        return if (emojiList.size > 200) emojiList.shuffled().take(200) else emojiList.ifEmpty { 
            listOf("🍎", "🍌", "🍇", "🍊", "🍓", "🍒", "🍍", "🥝", "🍉", "🍑") 
        }
    }

    private val _state = MutableStateFlow(MemBloxState())
    val state: StateFlow<MemBloxState> = _state.asStateFlow()

    private var gameJob: Job? = null

    fun start(difficulty: MemBloxDifficulty) {
        currentDifficulty = difficulty
        _state.value = MemBloxState(
            cols = difficulty.cols,
            rows = difficulty.rows,
            targetPairs = difficulty.targetPairs,
            difficulty = difficulty,
            isStarted = true,
            powerUps = mapOf(PowerUpType.FREEZE to 2, PowerUpType.REVEAL to 1, PowerUpType.NUKE to 1, PowerUpType.HINT to 2)
        )
        pendingPairs.clear()
        spawnCount = 0
        lastMatchTime = 0L
        gameJob?.cancel()
        gameJob = scope.launch {
            while (!_state.value.isGameOver && !_state.value.isVictory) {
                val progress = _state.value.pairsMatched.toFloat() / _state.value.targetPairs
                val speedFactor = 1.0f - (progress * 0.4f)
                
                if (!_state.value.isFrozen) {
                    delay((difficulty.spawnDelayMillis * speedFactor).toLong())
                    spawnBlock()
                    applyGravity()
                } else {
                    delay(500)
                }
            }
        }
    }

    fun reset() {
        gameJob?.cancel()
        _state.value = MemBloxState()
    }

    fun onHapticConsumed() {
        _state.update { it.copy(lastHapticSignal = null) }
    }

    private fun triggerHaptic(signal: HapticSignal) {
        _state.update { it.copy(lastHapticSignal = signal) }
    }

    private fun spawnBlock() {
        val col = (0 until currentDifficulty.cols).random()
        if (_state.value.grid.any { it.row == 0 && it.col == col }) {
            _state.update { it.copy(isGameOver = true) }
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
            row = 0,
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

        scope.launch {
            delay(800)
            _state.update { it.copy(initiallyRevealedBlockIds = it.initiallyRevealedBlockIds - newBlock.id) }
        }

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
                    if (block.row < currentDifficulty.rows - 1 && !newGrid.any { it.row == block.row + 1 && it.col == block.col }) {
                        newGrid[i] = block.copy(row = block.row + 1)
                        changed = true
                    }
                }
            }
            state.copy(grid = newGrid)
        }
    }

    fun onBlockClick(block: MemBloxBlock) {
        if (_state.value.isGameOver || _state.value.isVictory || _state.value.isRevealed) return
        if (block.isMatched || block.isFlipped || _state.value.flippedBlocks.size >= 2) return

        triggerHaptic(HapticSignal.LIGHT)
        val now = System.currentTimeMillis()
        _state.update { state ->
            val newGrid = state.grid.map { if (it.id == block.id) it.copy(isFlipped = true) else it }
            val newFlipped = state.flippedBlocks + block.copy(isFlipped = true)
            val newTotalClicks = state.totalClicks + 1
            val newFirstFlipTimestamp = if (newFlipped.size == 1) now else state.firstFlipTimestamp
            
            state.copy(
                grid = newGrid, 
                flippedBlocks = newFlipped, 
                totalClicks = newTotalClicks,
                firstFlipTimestamp = newFirstFlipTimestamp,
                hintedBlockIds = state.hintedBlockIds - block.id
            )
        }

        if (_state.value.flippedBlocks.size == 2) {
            scope.launch {
                delay(400)
                checkMatch()
            }
        }
    }

    private fun checkMatch() {
        val now = System.currentTimeMillis()
        _state.update { state ->
            val flipped = state.flippedBlocks
            if (flipped[0].emoji == flipped[1].emoji) {
                // Match!
                triggerHaptic(HapticSignal.MEDIUM)
                val matchedIds = flipped.map { it.id }.toSet()
                val matchBursts = flipped.map { ConfettiBurst(col = it.col, row = it.row) }
                val newGrid = state.grid.filterNot { it.id in matchedIds }
                val newPairsMatched = state.pairsMatched + 1
                
                val isCombo = lastMatchTime != 0L && (now - lastMatchTime) < 3000L
                val newCombo = if (isCombo) state.combo + 1 else 1
                val newPeakCombo = maxOf(state.peakCombo, newCombo)
                val multiplier = 1.0f + (newCombo - 1) * 0.5f
                val points = (10 * multiplier).toInt()
                
                // Floating Text Announcer
                val announcerText = when {
                    newCombo == 3 -> "GREAT!"
                    newCombo == 5 -> "EXCELLENT!!"
                    newCombo == 8 -> "UNSTOPPABLE!!!"
                    newCombo >= 10 -> "GODLIKE!!!!"
                    else -> null
                }
                val newFloatingTexts = announcerText?.let {
                    state.floatingTexts + FloatingTextEffect(text = it, col = flipped[0].col, row = flipped[0].row)
                } ?: state.floatingTexts

                val newStreak = state.currentMatchStreak + 1
                val newBestStreak = maxOf(state.bestMatchStreak, newStreak)
                val matchTime = now - state.firstFlipTimestamp
                val newTotalMatchTime = state.totalMatchTimeMs + matchTime
                val newAvgMatchTime = newTotalMatchTime / (state.successfulMatches + 1)
                
                lastMatchTime = now
                
                val isVictory = newPairsMatched >= state.targetPairs && newGrid.isEmpty()
                val newAccuracy = (state.successfulMatches + 1).toFloat() / (state.successfulMatches + state.missedMatches + 1)
                
                state.copy(
                    grid = newGrid,
                    flippedBlocks = emptyList(),
                    score = state.score + points,
                    pairsMatched = newPairsMatched,
                    isVictory = isVictory,
                    combo = newCombo,
                    multiplier = multiplier,
                    peakCombo = newPeakCombo,
                    successfulMatches = state.successfulMatches + 1,
                    matchAccuracy = newAccuracy,
                    currentMatchStreak = newStreak,
                    bestMatchStreak = newBestStreak,
                    totalMatchTimeMs = newTotalMatchTime,
                    avgMatchTimeMs = newAvgMatchTime,
                    confettiBursts = state.confettiBursts + matchBursts,
                    floatingTexts = newFloatingTexts
                ).also {
                    scope.launch { applyGravity() }
                    scope.launch {
                        delay(1500)
                        val burstIds = matchBursts.map { it.id }.toSet()
                        _state.update { s -> s.copy(confettiBursts = s.confettiBursts.filterNot { burst -> burst.id in burstIds }) }
                    }
                    if (announcerText != null) {
                        scope.launch {
                            delay(1500)
                            _state.update { s -> s.copy(floatingTexts = s.floatingTexts.drop(1)) }
                        }
                    }
                    if (isVictory) {
                        triggerHaptic(HapticSignal.HEAVY)
                        onGameOver(it.score)
                    }
                }
            } else {
                // No match
                val newGrid = state.grid.map { it.copy(isFlipped = false) }
                val newAccuracy = (state.successfulMatches).toFloat() / (state.successfulMatches + state.missedMatches + 1)
                state.copy(
                    grid = newGrid, 
                    flippedBlocks = emptyList(), 
                    combo = 0, 
                    multiplier = 1.0f,
                    missedMatches = state.missedMatches + 1,
                    matchAccuracy = newAccuracy,
                    currentMatchStreak = 0
                )
            }
        }
    }

    fun usePowerUp(type: PowerUpType) {
        val count = _state.value.powerUps[type] ?: 0
        if (count <= 0 || _state.value.isGameOver || _state.value.isVictory) return

        _state.update { it.copy(powerUps = it.powerUps + (type to count - 1)) }

        when (type) {
            PowerUpType.FREEZE -> {
                scope.launch {
                    _state.update { it.copy(isFrozen = true, frostAlpha = 0.5f) }
                    delay(5000)
                    _state.update { it.copy(isFrozen = false, frostAlpha = 0f) }
                }
            }
            PowerUpType.REVEAL -> {
                scope.launch {
                    val originalGrid = _state.value.grid
                    _state.update { state ->
                        state.copy(
                            isRevealed = true,
                            grid = state.grid.map { it.copy(isFlipped = true) }
                        )
                    }
                    delay(1500)
                    _state.update { state ->
                        state.copy(
                            isRevealed = false,
                            grid = state.grid.map { block ->
                                block.copy(isFlipped = originalGrid.find { it.id == block.id }?.isFlipped ?: false)
                            }
                        )
                    }
                }
            }
            PowerUpType.NUKE -> {
                scope.launch {
                    val grid = _state.value.grid
                    if (grid.isEmpty()) return@launch

                    val randomBlocks = grid.shuffled().take(3)
                    val tallestCol = (0 until currentDifficulty.cols).maxByOrNull { col -> grid.count { it.col == col } } ?: 0
                    val tallestColBlocks = grid.filter { it.col == tallestCol }.sortedByDescending { it.row }.take(2)
                    
                    val targets = (randomBlocks + tallestColBlocks).distinctBy { it.id }
                    val targetIds = targets.map { it.id }.toSet()

                    val green = 0xFF4CAF50.toInt()
                    val yellow = 0xFFFFEB3B.toInt()
                    val red = 0xFFF44336.toInt()

                    _state.update { it.copy(nukingBlockIds = targetIds.associateWith { green }, shakeIntensity = 2f) }
                    triggerHaptic(HapticSignal.LIGHT)
                    delay(500)
                    _state.update { it.copy(nukingBlockIds = targetIds.associateWith { yellow }, shakeIntensity = 5f) }
                    triggerHaptic(HapticSignal.MEDIUM)
                    delay(500)
                    _state.update { it.copy(nukingBlockIds = targetIds.associateWith { red }, shakeIntensity = 10f) }
                    triggerHaptic(HapticSignal.HEAVY)
                    delay(500)

                    _state.update { state ->
                        state.copy(
                            grid = state.grid.filterNot { it.id in targetIds },
                            nukingBlockIds = emptyMap(),
                            shakeIntensity = 0f
                        )
                    }
                    applyGravity()
                }
            }
            PowerUpType.HINT -> {
                val grid = _state.value.grid
                val match = grid.groupBy { it.emoji }.filter { it.value.size >= 2 }.values.firstOrNull()
                if (match != null) {
                    _state.update { it.copy(hintedBlockIds = match.map { b -> b.id }.toSet()) }
                }
            }
        }
    }
}
