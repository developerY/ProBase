package com.zoewave.probase.gotmind.features.memblox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.gotmind.database.MemBloxScoreEntity
import com.zoewave.probase.gotmind.database.dao.MemBloxScoreDao
import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import com.zoewave.probase.gotmind.model.memblox.MemBloxDifficulty
import com.zoewave.probase.gotmind.analytics.AnalyticsHelper
import com.zoewave.probase.gotmind.analytics.AnalyticsEvent
import com.zoewave.probase.gotmind.analytics.AnalyticsParam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MemBloxViewModel @Inject constructor(
    private val scoreDao: MemBloxScoreDao,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _engineType = MutableStateFlow(MemBloxEngineType.STATIC)
    val engineType: StateFlow<MemBloxEngineType> = _engineType.asStateFlow()

    private val _engine = MutableStateFlow<IMemBloxEngine>(createEngine(_engineType.value))
    val uiState: StateFlow<MemBloxState> = _engine.flatMapLatest { it.state }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MemBloxState())

    val topScores = scoreDao.getAllTopScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun createEngine(type: MemBloxEngineType): IMemBloxEngine {
        return when (type) {
            MemBloxEngineType.FALLING -> FallingMemBloxEngine(viewModelScope) { saveScore() }
            MemBloxEngineType.STATIC -> StaticMemBloxEngine(viewModelScope) { saveScore() }
        }
    }

    fun handleEvent(event: MemBloxEvent) {
        val currentEngine = _engine.value
        when (event) {
            is MemBloxEvent.StartGame -> {
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "game_start",
                        extras = listOf(
                            AnalyticsParam("difficulty", event.difficulty.name),
                            AnalyticsParam("engine_type", _engineType.value.name)
                        )
                    )
                )
                currentEngine.start(event.difficulty)
            }
            is MemBloxEvent.BlockClick -> currentEngine.onBlockClick(event.block)
            is MemBloxEvent.UsePowerUp -> {
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "power_up_used",
                        extras = listOf(AnalyticsParam("type", event.type.name))
                    )
                )
                currentEngine.usePowerUp(event.type)
            }
            MemBloxEvent.ResetToSelection -> currentEngine.reset()
            MemBloxEvent.HapticConsumed -> currentEngine.onHapticConsumed()
            MemBloxEvent.TogglePause -> currentEngine.togglePause()
            is MemBloxEvent.UpdateSpeed -> currentEngine.updateSpeed(event.multiplier)
            is MemBloxEvent.UpdateDropHeight -> currentEngine.updateDropHeight(event.height)
            is MemBloxEvent.UpdateDropDuration -> currentEngine.updateDropDuration(event.durationMillis)
            is MemBloxEvent.SetEngineType -> {
                if (_engineType.value == event.type) return
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "engine_swapped",
                        extras = listOf(AnalyticsParam("new_type", event.type.name))
                    )
                )
                currentEngine.reset()
                _engineType.value = event.type
                _engine.value = createEngine(event.type)
            }
        }
    }

    private fun saveScore() {
        val state = uiState.value
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = "game_over",
                extras = listOf(
                    AnalyticsParam("score", state.score.toString()),
                    AnalyticsParam("difficulty", state.difficulty.name),
                    AnalyticsParam("is_victory", state.isVictory.toString())
                )
            )
        )
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
