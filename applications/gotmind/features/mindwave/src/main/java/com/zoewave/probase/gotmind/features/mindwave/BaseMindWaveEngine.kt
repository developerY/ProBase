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
    protected val mode: com.zoewave.probase.gotmind.model.MindWaveMode,
    protected val scope: CoroutineScope,
    protected val onGameOver: (Int, Int) -> Unit
) : IMindWaveEngine {

    protected val _state = MutableStateFlow(MindWaveState(mode = mode))
    override val state: StateFlow<MindWaveState> = _state.asStateFlow()

    protected var gameJob: Job? = null
    protected var synthesizer: com.zoewave.probase.core.util.audio.WaveSynthesizer? = null

    override fun setAudioSynthesizer(synthesizer: com.zoewave.probase.core.util.audio.WaveSynthesizer?) {
        this.synthesizer = synthesizer
    }

    override fun start() {
        _state.update { it.copy(
            isStarted = true, 
            score = 0, 
            level = 1, 
            isGameOver = false, 
            feedbackMessage = null, 
            grid = createInitialGrid(),
            mode = mode,
            sequencePath = emptyList() // Reset path on start
        ) }
        generateNewSequence(1)
    }

    protected abstract fun createInitialGrid(): List<Node>

    protected fun generateNewSequence(level: Int) {
        val isSongMaster = _state.value.currentSongTitle != null // We'll set this based on settings
        
        val newSequence = if (isSongMaster) {
            val melody = MelodyLibrary.getForLevel(level)
            _state.update { it.copy(currentSongTitle = melody.title) }
            melody.sequence
        } else {
            val sequenceLength = 1 + level // Start with 2 nodes
            List(sequenceLength) { Random.nextInt(0, 16) }
        }

        _state.update { it.copy(sequence = newSequence, userInput = emptyList(), isPlayingSequence = true, sequencePath = emptyList()) }
        playSequence(newSequence)
    }

    protected fun playSequence(sequence: List<Int>) {
        gameJob?.cancel()
        gameJob = scope.launch {
            delay(1000)
            val path = mutableListOf<Int>()
            sequence.forEach { nodeId ->
                if (_state.value.isPaused) {
                    while (_state.value.isPaused) { delay(100) }
                }
                
                path.add(nodeId)
                _state.update { state ->
                    state.copy(
                        grid = state.grid.map { node ->
                            if (node.id == nodeId) node.copy(isFlashing = true) else node
                        },
                        activeNodeId = nodeId,
                        sequencePath = path.toList()
                    )
                }
                triggerHaptic(HapticSignal.LIGHT)
                playNodeSound(nodeId)
                
                val flashDuration = (600L - (_state.value.level * 30L)).coerceAtLeast(200L)
                delay(flashDuration)
                
                _state.update { state ->
                    state.copy(
                        grid = state.grid.map { node ->
                            if (node.id == nodeId) node.copy(isFlashing = false) else node
                        },
                        activeNodeId = null
                    )
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
            playNodeSound(nodeId)
            val newUserInput = state.userInput + nodeId
            _state.update { it.copy(activeNodeId = nodeId) }
            scope.launch {
                delay(400)
                _state.update { it.copy(activeNodeId = null) }
            }

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
            playGameOverSound()
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

    override fun updateSymphonySettings(
        waveform: com.zoewave.probase.core.util.audio.WaveSynthesizer.Waveform, 
        songMaster: Boolean,
        nodeShape: com.zoewave.probase.gotmind.model.NodeShape
    ) {
        _state.update { it.copy(
            activeWaveform = waveform,
            currentSongTitle = if (songMaster) "Ready for Melody" else null,
            nodeShape = nodeShape
        ) }
    }

    protected fun triggerHaptic(signal: HapticSignal) {
        if (_state.value.hapticsEnabled) {
            _state.update { it.copy(lastHapticSignal = signal) }
        }
    }

    protected fun playNodeSound(nodeId: Int) {
        if (!_state.value.soundEnabled) return
        val synth = synthesizer ?: return
        val frequency = nodeFrequencies[nodeId] ?: 440.0
        scope.launch {
            synth.playTone(frequency, 400, _state.value.activeWaveform)
        }
    }

    protected fun playGameOverSound() {
        if (!_state.value.soundEnabled) return
        val synth = synthesizer ?: return
        scope.launch {
            synth.playTone(150.0, 600)
        }
    }

    private val nodeFrequencies = mapOf(
        0 to 523.25, // C5
        1 to 554.37, // C#5
        2 to 587.33, // D5
        3 to 622.25, // D#5
        4 to 392.00, // G4
        5 to 415.30, // G#4
        6 to 440.00, // A4
        7 to 466.16, // A#4
        8 to 493.88, // B4
        9 to 329.63, // E4
        10 to 349.23, // F4
        11 to 369.99, // F#4
        12 to 261.63, // C4
        13 to 277.18, // C#4
        14 to 293.66, // D4
        15 to 311.13  // D#4
    )
}
