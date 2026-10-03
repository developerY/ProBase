package com.zoewave.probase.gotmind.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.gotmind.data.repository.GotMindRepository
import com.zoewave.probase.gotmind.model.GameState
import com.zoewave.probase.gotmind.model.Score
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GotMindRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    val topScores: StateFlow<List<Score>> = repository.getTopScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onScoreUpdate(points: Int) {
        _gameState.update { state ->
            val newScore = state.currentScore + points
            val isCleared = newScore >= state.targetScoreForStage
            state.copy(
                currentScore = newScore,
                isStageCleared = isCleared
            )
        }
    }

    fun onNextStage() {
        _gameState.update { state ->
            val nextStageNum = state.stage + 1
            state.copy(
                stage = nextStageNum,
                targetScoreForStage = nextStageNum * 50,
                isStageCleared = false
            )
        }
    }

    fun onGameOver() {
        val score = _gameState.value.currentScore
        viewModelScope.launch {
            repository.saveScore(score)
        }
        _gameState.update { it.copy(isGameOver = true) }
    }

    fun resetGame() {
        _gameState.value = GameState()
    }
}
