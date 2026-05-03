package com.zoewave.probase.gotmind.features.mindwave

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

abstract class BaseMindWaveEngine(
    protected val scope: CoroutineScope,
    protected val onGameOver: (Int, Int) -> Unit
) : IMindWaveEngine {

    protected val _state = MutableStateFlow(MindWaveState())
    override val state: StateFlow<MindWaveState> = _state.asStateFlow()

    protected var gameJob: Job? = null

    override fun start() {
        _state.update { it.copy(isStarted = true, score = 0, level = 1, isGameOver = false, feedbackMessage = null, grid = createInitialGrid()) }
        generateNewSequence(1)
    }

    protected abstract fun createInitialGrid(): List<Node>

    protected fun generateNewSequence(level: Int) {
        val sequenceLength = 1 + level // Start with 2 nodes
        val newSequence = List(sequenceLength) { Random.nextInt(0, 16) }
        _state.update { it.copy(sequence = newSequence, userInput = emptyList(), isPlayingSequence = true) }
        playSequence(newSequence)
    }

    protected fun playSequence(sequence: List<Int>) {
        gameJob?.cancel()
        gameJob = scope.launch {
            delay(1000)
            sequence.forEach { nodeId ->
                if (_state.value.isPaused) {
                    while (_state.value.isPaused) { delay(100) }
                }
                
                _state.update { state ->
                    state.copy(grid = state.grid.map { node ->
                        if (node.id == nodeId) node.copy(isFlashing = true) else node
                    })
                }
                triggerHaptic(HapticSignal.LIGHT)
                
                val flashDuration = (600L - (_state.value.level * 30L)).coerceAtLeast(200L)
                delay(flashDuration)
                
                _state.update { state ->
                    state.copy(grid = state.grid.map { node ->
                        if (node.id == nodeId) node.copy(isFlashing = false) else node
                    })
                }
                delay(150)
            }
            _state.update { it.copy(isPlayingSequence = false) }
        }
    }

    override fun onNodeClick(nodeId: Int) {
        val state = _state.value
        if (state.isPlayingSequence || state.isGameOver || state.isPaused || !state.isStarted) return

        val currentStep = state.userInput.size
        val expectedNodeId = state.sequence[currentStep]

        if (nodeId == expectedNodeId) {
            triggerHaptic(HapticSignal.LIGHT)
            val newUserInput = state.userInput + nodeId
            if (newUserInput.size == state.sequence.size) {
                // Level Complete
                _state.update { it.copy(
                    userInput = newUserInput,
                    score = it.score + (it.level * 10),
                    feedbackMessage = "Wave Synced!"
                ) }
                triggerHaptic(HapticSignal.MEDIUM)
                scope.launch {
                    delay(1200)
                    startNextLevel()
                }
            } else {
                _state.update { it.copy(userInput = newUserInput) }
            }
        } else {
            // Game Over
            triggerHaptic(HapticSignal.HEAVY)
            val finalScore = state.score
            val finalLevel = state.level
            _state.update { it.copy(isGameOver = true, feedbackMessage = "Signal Lost") }
            onGameOver(finalScore, finalLevel)
        }
    }

    protected fun startNextLevel() {
        val nextLevel = _state.value.level + 1
        _state.update { it.copy(level = nextLevel, feedbackMessage = null) }
        generateNewSequence(nextLevel)
    }

    override fun reset() {
        gameJob?.cancel()
        val current = _state.value
        _state.value = MindWaveState(
            hapticsEnabled = current.hapticsEnabled,
            soundEnabled = current.soundEnabled
        )
    }

    override fun togglePause() {
        _state.update { it.copy(isPaused = !it.isPaused) }
    }

    override fun onHapticConsumed() {
        _state.update { it.copy(lastHapticSignal = null) }
    }

    override fun setHapticsEnabled(enabled: Boolean) {
        _state.update { it.copy(hapticsEnabled = enabled) }
    }

    override fun setSoundEnabled(enabled: Boolean) {
        _state.update { it.copy(soundEnabled = enabled) }
    }

    protected fun triggerHaptic(signal: HapticSignal) {
        if (_state.value.hapticsEnabled) {
            _state.update { it.copy(lastHapticSignal = signal) }
        }
    }
}
