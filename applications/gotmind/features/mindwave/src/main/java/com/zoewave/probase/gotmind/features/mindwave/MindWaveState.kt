package com.zoewave.probase.gotmind.features.mindwave

import com.zoewave.probase.gotmind.model.MindWaveMode
import com.zoewave.probase.gotmind.model.NodeShape
import java.util.UUID

data class Node(
    val id: Int, // 0..15 for a 4x4 grid
    val isFlashing: Boolean = false,
    val isCorrect: Boolean? = null, // true if correctly clicked, false if wrong, null otherwise
    val color: Long? = null, // Pastel color for Symphony mode
    val note: String? = null // Musical note for Symphony mode
)

data class MindWaveState(
    val grid: List<Node> = List(16) { Node(it) },
    val sequence: List<Int> = emptyList(),
    val userInput: List<Int> = emptyList(),
    val level: Int = 1,
    val isPlayingSequence: Boolean = false,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val score: Int = 0,
    val highScore: Int = 0,
    val isStarted: Boolean = false,
    val feedbackMessageResId: Int? = null,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val lastHapticSignal: HapticSignal? = null,
    val isPaused: Boolean = false,
    val mode: MindWaveMode = MindWaveMode.CLASSIC,
    val nodeShape: NodeShape = NodeShape.CIRCLE,
    val activeNodeId: Int? = null, // For visual staff notation
    val sequencePath: List<Int> = emptyList(), // Path of the sequence for constellation drawing
    val currentSongTitleResId: Int? = null,
    val activeWaveform: com.zoewave.probase.core.util.audio.WaveSynthesizer.Waveform = com.zoewave.probase.core.util.audio.WaveSynthesizer.Waveform.SINE
)

enum class HapticSignal { LIGHT, MEDIUM, HEAVY }

sealed interface MindWaveEvent {
    data object StartGame : MindWaveEvent
    data class NodeClick(val nodeId: Int) : MindWaveEvent
    data object ResetGame : MindWaveEvent
    data object NextLevel : MindWaveEvent
    data object TogglePause : MindWaveEvent
    data object HapticConsumed : MindWaveEvent
    data class SetHapticsEnabled(val enabled: Boolean) : MindWaveEvent
    data class SetSoundEnabled(val enabled: Boolean) : MindWaveEvent
    data object ClearHallOfFame : MindWaveEvent
    data class SetInstrument(val instrument: com.zoewave.probase.gotmind.model.InstrumentType) : MindWaveEvent
    data class SetSongMaster(val enabled: Boolean) : MindWaveEvent
}
