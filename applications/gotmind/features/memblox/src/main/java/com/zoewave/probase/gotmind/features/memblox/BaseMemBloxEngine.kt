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

abstract class BaseMemBloxEngine(
    protected val scope: CoroutineScope,
    protected val onGameOver: (Int) -> Unit
) : IMemBloxEngine {
    
    protected var currentDifficulty = MemBloxDifficulty.EXPERT
    protected val emojis = generateSupportedEmojiList()
    
    protected val pendingPairs = mutableListOf<String>()
    protected var spawnCount = 0
    protected var lastMatchTime = 0L

    protected val _state = MutableStateFlow(MemBloxState())
    override val state: StateFlow<MemBloxState> = _state.asStateFlow()

    protected var gameJob: Job? = null

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

    override fun start(difficulty: MemBloxDifficulty) {
        currentDifficulty = difficulty
        _state.value = MemBloxState(
            cols = difficulty.cols,
            rows = difficulty.rows,
            targetPairs = difficulty.targetPairs,
            difficulty = difficulty,
            isStarted = true,
            powerUps = mapOf(
                PowerUpType.FREEZE to 2, 
                PowerUpType.REVEAL to 1, 
                PowerUpType.NUKE to 1, 
                PowerUpType.HINT to 2,
                PowerUpType.EQUALIZER to 0,
                PowerUpType.SLOW to 2,
                PowerUpType.TIDY to 1,
                PowerUpType.AUTO_MATCH to 1,
                PowerUpType.SCAN to 2
            )
        )
        pendingPairs.clear()
        spawnCount = 0
        lastMatchTime = 0L
        gameJob?.cancel()
        gameJob = scope.launch {
            while (!_state.value.isGameOver && !_state.value.isVictory) {
                val progress = _state.value.pairsMatched.toFloat() / _state.value.targetPairs
                val progressFactor = 1.0f - (progress * 0.4f)
                val slowFactor = if (_state.value.isSlowed) 2.0f else 1.0f
                val frenzyFactor = if (_state.value.isFrenzy) 0.66f else 1.0f
                val userSpeedFactor = 1.0f / _state.value.speedMultiplier
                val speedFactor = progressFactor * slowFactor * frenzyFactor * userSpeedFactor
                
                // Stress Check (Overheat)
                val boardLoad = _state.value.grid.size.toFloat() / (difficulty.cols * difficulty.rows)
                _state.update { it.copy(isStressed = boardLoad > 0.75f) }
                
                if (!_state.value.isFrozen && !_state.value.isPaused) {
                    delay((difficulty.spawnDelayMillis * speedFactor).toLong())
                    spawnLogic()
                } else {
                    delay(200)
                }
            }
        }
    }

    protected abstract suspend fun spawnLogic()

    override fun togglePause() {
        _state.update { it.copy(isPaused = !it.isPaused) }
    }

    override fun updateSpeed(multiplier: Float) {
        _state.update { it.copy(speedMultiplier = multiplier) }
    }

    override fun updateDropHeight(height: Int) {
        _state.update { it.copy(dropHeight = height) }
    }

    override fun updateDropDuration(durationMillis: Int) {
        _state.update { it.copy(dropDurationMillis = durationMillis) }
    }

    override fun reset() {
        gameJob?.cancel()
        _state.value = MemBloxState()
    }

    override fun onHapticConsumed() {
        _state.update { it.copy(lastHapticSignal = null) }
    }

    protected fun triggerHaptic(signal: HapticSignal) {
        _state.update { it.copy(lastHapticSignal = signal) }
    }

    protected fun applyGravity() {
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

    override fun onBlockClick(block: MemBloxBlock) {
        if (_state.value.isGameOver || _state.value.isVictory || _state.value.isRevealed || _state.value.isPaused) return
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
                delay(if (_state.value.isSlowed) 800 else 400)
                checkMatch()
            }
        }
    }

    protected fun checkMatch(forcedMatch: List<MemBloxBlock>? = null) {
        val now = System.currentTimeMillis()
        _state.update { state ->
            val flipped = forcedMatch ?: state.flippedBlocks
            if (flipped.size == 2 && flipped[0].emoji == flipped[1].emoji) {
                // Match!
                triggerHaptic(HapticSignal.MEDIUM)
                val matchedIds = flipped.map { it.id }.toSet()
                val matchBursts = flipped.map { ConfettiBurst(col = it.col, row = it.row) }
                val ghosts = flipped.map { MatchGhost(emoji = it.emoji, col = it.col, row = it.row) }
                
                val newGrid = state.grid.filterNot { it.id in matchedIds }
                val newPairsMatched = state.pairsMatched + 1
                
                val isCombo = lastMatchTime != 0L && (now - lastMatchTime) < 3000L
                val newCombo = if (isCombo) state.combo + 1 else 1
                val newPeakCombo = maxOf(state.peakCombo, newCombo)
                val baseMultiplier = 1.0f + (newCombo - 1) * 0.5f
                val frenzyMultiplier = if (state.isFrenzy) 2.0f else 1.0f
                val points = (10 * baseMultiplier * frenzyMultiplier).toInt()
                
                // Frenzy trigger: 10% chance after 5x combo
                if (!state.isFrenzy && newCombo >= 5 && (1..10).random() == 1) {
                    scope.launch {
                        _state.update { it.copy(isFrenzy = true) }
                        delay(10000)
                        _state.update { it.copy(isFrenzy = false) }
                    }
                }

                // Award Equalizer on high combo
                val updatedPowerUps = if (newCombo == 5) {
                    state.powerUps + (PowerUpType.EQUALIZER to (state.powerUps[PowerUpType.EQUALIZER] ?: 0) + 1)
                } else state.powerUps

                // Floating Text Announcer
                val announcerResId = when {
                    newCombo == 3 -> R.string.applications_gotmind_features_memblox_great
                    newCombo == 5 -> R.string.applications_gotmind_features_memblox_excellent
                    newCombo == 8 -> R.string.applications_gotmind_features_memblox_unstoppable
                    newCombo >= 10 -> R.string.applications_gotmind_features_memblox_godlike
                    else -> null
                }
                val newFloatingTexts = announcerResId?.let {
                    state.floatingTexts + FloatingTextEffect(textResId = it, col = flipped[0].col, row = flipped[0].row)
                } ?: state.floatingTexts

                val newStreak = state.currentMatchStreak + 1
                val newBestStreak = maxOf(state.bestMatchStreak, newStreak)
                val matchTime = now - state.firstFlipTimestamp
                val newTotalMatchTime = state.totalMatchTimeMs + matchTime
                val newAvgMatchTime = newTotalMatchTime / (state.successfulMatches + 1)
                
                lastMatchTime = now
                
                val isVictory = newPairsMatched >= state.targetPairs && newGrid.isEmpty()
                val newAccuracy = (state.successfulMatches + 1).toFloat() / (state.successfulMatches + state.missedMatches + 1)
                
                val scorePopup = ScorePopup(score = points, col = flipped[0].col, row = flipped[0].row)

                state.copy(
                    grid = newGrid,
                    flippedBlocks = emptyList(),
                    score = state.score + points,
                    pairsMatched = newPairsMatched,
                    isVictory = isVictory,
                    combo = newCombo,
                    multiplier = baseMultiplier * frenzyMultiplier,
                    peakCombo = newPeakCombo,
                    successfulMatches = state.successfulMatches + 1,
                    matchAccuracy = newAccuracy,
                    currentMatchStreak = newStreak,
                    bestMatchStreak = newBestStreak,
                    totalMatchTimeMs = newTotalMatchTime,
                    avgMatchTimeMs = newAvgMatchTime,
                    confettiBursts = state.confettiBursts + matchBursts,
                    floatingTexts = newFloatingTexts,
                    matchGhosts = state.matchGhosts + ghosts,
                    powerUps = updatedPowerUps,
                    floatingScores = state.floatingScores + scorePopup,
                    finalRank = if (isVictory) calculateRank(state.copy(score = state.score + points, matchAccuracy = newAccuracy, bestMatchStreak = newBestStreak)) else ""
                ).also {
                    scope.launch { applyGravity() }
                    scope.launch {
                        delay(1500)
                        val burstIds = matchBursts.map { it.id }.toSet()
                        _state.update { s -> s.copy(confettiBursts = s.confettiBursts.filterNot { burst -> burst.id in burstIds }) }
                    }
                    scope.launch {
                        delay(1000)
                        val ghostIds = ghosts.map { it.id }.toSet()
                        _state.update { s -> s.copy(matchGhosts = s.matchGhosts.filterNot { g -> g.id in ghostIds }) }
                    }
                    scope.launch {
                        delay(1000)
                        _state.update { s -> s.copy(floatingScores = s.floatingScores.filter { f -> f.id != scorePopup.id }) }
                    }
                    if (announcerResId != null) {
                        scope.launch {
                            delay(1500)
                            _state.update { s -> s.copy(floatingTexts = s.floatingTexts.filter { f -> f.textResId != announcerResId }) }
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

    protected fun calculateRank(state: MemBloxState): String {
        val score = state.score
        val accuracy = state.matchAccuracy
        val streak = state.bestMatchStreak
        
        return when {
            accuracy >= 0.9f && streak >= 10 -> "S"
            accuracy >= 0.7f && score > 500 -> "A"
            accuracy >= 0.5f && score > 250 -> "B"
            score > 100 -> "C"
            else -> "D"
        }
    }

    override fun usePowerUp(type: PowerUpType) {
        val count = _state.value.powerUps[type] ?: 0
        if (count <= 0 || _state.value.isGameOver || _state.value.isVictory) return

        _state.update { it.copy(
            powerUps = it.powerUps + (type to count - 1),
            powerUpsUsed = it.powerUpsUsed + 1
        ) }

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
                    
                    // Trigger Shockwave
                    val shock = Shockwave(col = tallestCol, row = tallestColBlocks.firstOrNull()?.row ?: 10)
                    _state.update { it.copy(activeShockwaves = it.activeShockwaves + shock) }
                    scope.launch {
                        delay(1000)
                        _state.update { s -> s.copy(activeShockwaves = s.activeShockwaves.filter { it.id != shock.id }) }
                    }

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
            PowerUpType.EQUALIZER -> {
                val grid = _state.value.grid
                if (grid.isEmpty()) return
                val targetEmoji = grid.random().emoji
                val targets = grid.filter { it.emoji == targetEmoji }
                val targetIds = targets.map { it.id }.toSet()
                
                scope.launch {
                    val shock = Shockwave(col = currentDifficulty.cols / 2, row = currentDifficulty.rows / 2)
                    _state.update { it.copy(activeShockwaves = it.activeShockwaves + shock, nukingBlockIds = targetIds.associateWith { 0xFF00BCD4.toInt() }, shakeIntensity = 3f) }
                    triggerHaptic(HapticSignal.MEDIUM)
                    delay(800)
                    _state.update { state ->
                        state.copy(
                            grid = state.grid.filterNot { it.id in targetIds },
                            nukingBlockIds = state.nukingBlockIds - targetIds,
                            shakeIntensity = 0f,
                            score = state.score + (targets.size / 2) * 20,
                            activeShockwaves = state.activeShockwaves.filter { it.id != shock.id }
                        )
                    }
                    applyGravity()
                }
            }
            PowerUpType.SLOW -> {
                scope.launch {
                    _state.update { it.copy(isSlowed = true) }
                    delay(10000)
                    _state.update { it.copy(isSlowed = false) }
                }
            }
            PowerUpType.TIDY -> {
                _state.update { state ->
                    if (state.grid.isEmpty()) return@update state
                    val maxRow = state.grid.maxOf { it.row }
                    val targetIds = state.grid.filter { it.row == maxRow }.map { it.id }.toSet()
                    state.copy(
                        grid = state.grid.filterNot { it.id in targetIds },
                        score = state.score + (targetIds.size * 5)
                    )
                }
                applyGravity()
            }
            PowerUpType.AUTO_MATCH -> {
                val grid = _state.value.grid
                val match = grid.groupBy { it.emoji }.filter { it.value.size >= 2 }.values.firstOrNull()
                if (match != null) {
                    checkMatch(match.take(2))
                }
            }
            PowerUpType.SCAN -> {
                scope.launch {
                    val grid = _state.value.grid
                    val matches = grid.groupBy { it.emoji }.filter { it.value.size >= 2 }
                    
                    matches.forEach { (_, blocks) ->
                        val pairIds = blocks.take(2).map { it.id }.toSet()
                        _state.update { it.copy(initiallyRevealedBlockIds = it.initiallyRevealedBlockIds + pairIds) }
                        delay(600)
                        _state.update { it.copy(initiallyRevealedBlockIds = it.initiallyRevealedBlockIds - pairIds) }
                        delay(150)
                    }
                }
            }
        }
    }
}
