package com.zoewave.probase.gotmind.features.mindwave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.gotmind.analytics.AnalyticsHelper
import com.zoewave.probase.gotmind.analytics.AnalyticsEvent
import com.zoewave.probase.gotmind.analytics.AnalyticsParam
import com.zoewave.probase.gotmind.data.repository.AppSettingsRepository
import com.zoewave.probase.gotmind.database.MindWaveScoreEntity
import com.zoewave.probase.gotmind.database.dao.MindWaveScoreDao
import com.zoewave.probase.gotmind.model.MindWaveMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MindWaveViewModel @Inject constructor(
    private val scoreDao: MindWaveScoreDao,
    private val appSettingsRepository: AppSettingsRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(MindWaveState())
    val uiState: StateFlow<MindWaveState> = _uiState.asStateFlow()

    val topScores = scoreDao.getTopScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Sync persistent settings
        appSettingsRepository.gameSettingsFlow
            .onEach { settings ->
                _uiState.update { it.copy(
                    hapticsEnabled = settings.hapticsEnabled,
                    soundEnabled = settings.soundEnabled,
                    mode = settings.mindWaveMode,
                    grid = createInitialGrid(settings.mindWaveMode)
                ) }
            }
            .launchIn(viewModelScope)
    }

    private fun createInitialGrid(mode: MindWaveMode): List<Node> {
        return List(16) { i ->
            if (mode == MindWaveMode.SYMPHONY) {
                Node(
                    id = i,
                    color = pastelColors[i % pastelColors.size],
                    note = musicNotes[i % musicNotes.size]
                )
            } else {
                Node(id = i)
            }
        }
    }

    private val pastelColors = listOf(
        0xFFFFB7B2L, 0xFFFFDAC1L, 0xFFE2F0CBL, 0xFFB5EAD7L,
        0xFFC7CEEAL, 0xFFF3E5F5L, 0xFFE1F5FEL, 0xFFF1F8E9L,
        0xFFFFF9C4L, 0xFFFFE0B2L, 0xFFFFCDD2L, 0xFFF8BBD0L,
        0xFFE1BEE7L, 0xFFD1C4E9L, 0xFFC5CAE9L, 0xFFBBDEFBL
    )

    private val musicNotes = listOf(
        "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4",
        "C5", "C#5", "D5", "D#5"
    )

    fun handleEvent(event: MindWaveEvent) {
        when (event) {
            MindWaveEvent.StartGame -> startGame()
            is MindWaveEvent.NodeClick -> onNodeClick(event.nodeId)
            MindWaveEvent.ResetGame -> resetGame()
            MindWaveEvent.NextLevel -> startNextLevel()
            MindWaveEvent.TogglePause -> togglePause()
            MindWaveEvent.HapticConsumed -> _uiState.update { it.copy(lastHapticSignal = null) }
            is MindWaveEvent.SetHapticsEnabled -> viewModelScope.launch {
                appSettingsRepository.saveHapticsEnabled(event.enabled)
            }
            is MindWaveEvent.SetSoundEnabled -> viewModelScope.launch {
                appSettingsRepository.saveSoundEnabled(event.enabled)
            }
            MindWaveEvent.ClearHallOfFame -> viewModelScope.launch {
                scoreDao.clearAllScores()
            }
        }
    }

    private fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    private fun startGame() {
        analyticsHelper.logEvent(AnalyticsEvent("mindwave_start"))
        _uiState.update { it.copy(isStarted = true, score = 0, level = 1, isGameOver = false, feedbackMessage = null) }
        generateNewSequence(1)
    }

    private fun generateNewSequence(level: Int) {
        val sequenceLength = 1 + level // Start with 2 nodes
        val newSequence = List(sequenceLength) { Random.nextInt(0, 16) }
        _uiState.update { it.copy(sequence = newSequence, userInput = emptyList(), isPlayingSequence = true) }
        playSequence(newSequence)
    }

    private fun playSequence(sequence: List<Int>) {
        viewModelScope.launch {
            delay(1000)
            sequence.forEach { nodeId ->
                if (_uiState.value.isPaused) {
                    while (_uiState.value.isPaused) { delay(100) }
                }
                
                _uiState.update { state ->
                    state.copy(grid = state.grid.map { node ->
                        if (node.id == nodeId) node.copy(isFlashing = true) else node
                    })
                }
                triggerHaptic(HapticSignal.LIGHT)
                
                val flashDuration = (600L - (uiState.value.level * 30L)).coerceAtLeast(200L)
                delay(flashDuration)
                
                _uiState.update { state ->
                    state.copy(grid = state.grid.map { node ->
                        if (node.id == nodeId) node.copy(isFlashing = false) else node
                    })
                }
                delay(150)
            }
            _uiState.update { it.copy(isPlayingSequence = false) }
        }
    }

    private fun onNodeClick(nodeId: Int) {
        val state = _uiState.value
        if (state.isPlayingSequence || state.isGameOver || state.isPaused) return

        val currentStep = state.userInput.size
        val expectedNodeId = state.sequence[currentStep]

        if (nodeId == expectedNodeId) {
            triggerHaptic(HapticSignal.LIGHT)
            val newUserInput = state.userInput + nodeId
            if (newUserInput.size == state.sequence.size) {
                // Level Complete
                _uiState.update { it.copy(
                    userInput = newUserInput,
                    score = it.score + (it.level * 10),
                    feedbackMessage = "Wave Synced!"
                ) }
                triggerHaptic(HapticSignal.MEDIUM)
                viewModelScope.launch {
                    delay(1200)
                    startNextLevel()
                }
            } else {
                _uiState.update { it.copy(userInput = newUserInput) }
            }
        } else {
            // Game Over
            triggerHaptic(HapticSignal.HEAVY)
            analyticsHelper.logEvent(
                AnalyticsEvent("mindwave_game_over", listOf(AnalyticsParam("level", state.level.toString())))
            )
            val finalScore = state.score
            _uiState.update { it.copy(isGameOver = true, feedbackMessage = "Signal Lost") }
            saveScore(finalScore, state.level)
        }
    }

    private fun startNextLevel() {
        val nextLevel = _uiState.value.level + 1
        _uiState.update { it.copy(level = nextLevel, feedbackMessage = null) }
        generateNewSequence(nextLevel)
    }

    private fun saveScore(score: Int, level: Int) {
        viewModelScope.launch {
            scoreDao.insertScore(MindWaveScoreEntity(score = score, level = level))
        }
    }

    private fun resetGame() {
        _uiState.update { MindWaveState(hapticsEnabled = it.hapticsEnabled, soundEnabled = it.soundEnabled) }
        startGame()
    }

    private fun triggerHaptic(signal: HapticSignal) {
        if (_uiState.value.hapticsEnabled) {
            _uiState.update { it.copy(lastHapticSignal = signal) }
        }
    }
}
