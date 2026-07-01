package com.zoewave.probase.gotmind.mobile.ui.components

import com.zoewave.probase.gotmind.model.GameState
import com.zoewave.probase.gotmind.model.Score

data class GotMindClassicUiState(
    val game: GameState = GameState(),
    val topScores: List<Score> = emptyList()
)

sealed interface GotMindClassicEvent {
    data object ResetGame : GotMindClassicEvent
    data class ScoreUpdate(val delta: Int) : GotMindClassicEvent
    data object GameOver : GotMindClassicEvent
}
