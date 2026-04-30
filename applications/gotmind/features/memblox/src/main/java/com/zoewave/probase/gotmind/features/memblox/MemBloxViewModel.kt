package com.zoewave.probase.gotmind.features.memblox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.gotmind.database.MemBloxScoreEntity
import com.zoewave.probase.gotmind.database.dao.MemBloxScoreDao
import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import com.zoewave.probase.gotmind.model.memblox.MemBloxDifficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemBloxViewModel @Inject constructor(
    private val scoreDao: MemBloxScoreDao
) : ViewModel() {

    private val engine = MemBloxEngine(
        scope = viewModelScope,
        onGameOver = { _ -> saveScore() }
    )

    val uiState: StateFlow<MemBloxState> = engine.state

    val topScores = scoreDao.getAllTopScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startGame(difficulty: MemBloxDifficulty) {
        engine.start(difficulty)
    }

    fun onBlockClick(block: MemBloxBlock) {
        engine.onBlockClick(block)
    }

    fun usePowerUp(type: PowerUpType) {
        engine.usePowerUp(type)
    }

    fun resetToDifficultySelection() {
        engine.reset()
    }

    fun onHapticConsumed() {
        engine.onHapticConsumed()
    }

    private fun saveScore() {
        val state = uiState.value
        viewModelScope.launch {
            scoreDao.insertScore(
                MemBloxScoreEntity(
                    score = state.score,
                    difficulty = state.difficulty.label,
                    bestStreak = state.bestMatchStreak,
                    accuracy = state.matchAccuracy
                )
            )
        }
    }
}
