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

    fun handleEvent(event: MemBloxEvent) {
        when (event) {
            is MemBloxEvent.StartGame -> engine.start(event.difficulty)
            is MemBloxEvent.BlockClick -> engine.onBlockClick(event.block)
            is MemBloxEvent.UsePowerUp -> engine.usePowerUp(event.type)
            MemBloxEvent.ResetToSelection -> engine.reset()
            MemBloxEvent.HapticConsumed -> engine.onHapticConsumed()
            MemBloxEvent.TogglePause -> engine.togglePause()
            is MemBloxEvent.UpdateSpeed -> engine.updateSpeed(event.multiplier)
            is MemBloxEvent.UpdateDropHeight -> engine.updateDropHeight(event.height)
            is MemBloxEvent.UpdateDropDuration -> engine.updateDropDuration(event.durationMillis)
        }
    }

    private fun saveScore() {
        val state = uiState.value
        viewModelScope.launch {
            scoreDao.insertScore(
                MemBloxScoreEntity(
                    score = state.score,
                    difficulty = state.difficulty.name,
                    bestStreak = state.bestMatchStreak,
                    accuracy = state.matchAccuracy
                )
            )
        }
    }
}
