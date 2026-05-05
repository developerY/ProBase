package com.zoewave.probase.gotmind.features.mindwave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.util.audio.WaveSynthesizer
import com.zoewave.probase.gotmind.analytics.AnalyticsEvent
import com.zoewave.probase.gotmind.analytics.AnalyticsHelper
import com.zoewave.probase.gotmind.analytics.AnalyticsParam
import com.zoewave.probase.gotmind.data.repository.AppSettingsRepository
import com.zoewave.probase.gotmind.database.MindWaveScoreEntity
import com.zoewave.probase.gotmind.database.dao.MindWaveScoreDao
import com.zoewave.probase.gotmind.model.MindWaveMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MindWaveViewModel @Inject constructor(
    private val scoreDao: MindWaveScoreDao,
    private val appSettingsRepository: AppSettingsRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val waveSynthesizer: WaveSynthesizer
) : ViewModel() {

    private val _mode = MutableStateFlow(MindWaveMode.CLASSIC)
    val mode: StateFlow<MindWaveMode> = _mode.asStateFlow()

    private val _engine = MutableStateFlow<IMindWaveEngine>(createEngine(_mode.value))
    val uiState: StateFlow<MindWaveState> = _engine.flatMapLatest { it.state }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MindWaveState())

    val topScores = scoreDao.getTopScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Sync persistent settings
        appSettingsRepository.gameSettingsFlow
            .onEach { settings ->
                if (_mode.value != settings.mindWaveMode) {
                    _mode.value = settings.mindWaveMode
                    _engine.value = createEngine(settings.mindWaveMode)
                }
                val engine = _engine.value
                engine.setHapticsEnabled(settings.hapticsEnabled)
                engine.setSoundEnabled(settings.soundEnabled)
                engine.setAudioSynthesizer(waveSynthesizer)
                
                // New: Sync node shape
                _engine.value.state.value.let { 
                    engine.updateSymphonySettings(
                        waveform = when (settings.instrumentType) {
                            com.zoewave.probase.gotmind.model.InstrumentType.CLEAN_SYNTH -> com.zoewave.probase.core.util.audio.WaveSynthesizer.Waveform.SINE
                            com.zoewave.probase.gotmind.model.InstrumentType.RETRO_8BIT -> com.zoewave.probase.core.util.audio.WaveSynthesizer.Waveform.SQUARE
                            com.zoewave.probase.gotmind.model.InstrumentType.ZEN_TRIANGLE -> com.zoewave.probase.core.util.audio.WaveSynthesizer.Waveform.TRIANGLE
                        },
                        songMaster = settings.songMasterEnabled,
                        nodeShape = settings.mindWaveNodeShape
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun createEngine(mode: MindWaveMode): IMindWaveEngine {
        return when (mode) {
            MindWaveMode.CLASSIC -> ClassicMindWaveEngine(viewModelScope) { score, level -> saveScore(score, level) }
            MindWaveMode.SYMPHONY -> SymphonyMindWaveEngine(viewModelScope) { score, level -> saveScore(score, level) }
            MindWaveMode.HARMONIC_ARC -> HarmonicArcMindWaveEngine(viewModelScope) { score, level -> saveScore(score, level) }
            MindWaveMode.HARMONIC_RING -> HarmonicRingMindWaveEngine(viewModelScope) { score, level -> saveScore(score, level) }
        }
    }

    fun handleEvent(event: MindWaveEvent) {
        val currentEngine = _engine.value
        when (event) {
            MindWaveEvent.StartGame -> currentEngine.start()
            is MindWaveEvent.NodeClick -> currentEngine.onNodeClick(event.nodeId)
            MindWaveEvent.ResetGame -> currentEngine.reset()
            MindWaveEvent.NextLevel -> { /* Handled internally by engine for now */ }
            MindWaveEvent.TogglePause -> currentEngine.togglePause()
            MindWaveEvent.HapticConsumed -> currentEngine.onHapticConsumed()
            is MindWaveEvent.SetHapticsEnabled -> viewModelScope.launch {
                appSettingsRepository.saveHapticsEnabled(event.enabled)
            }
            is MindWaveEvent.SetSoundEnabled -> viewModelScope.launch {
                appSettingsRepository.saveSoundEnabled(event.enabled)
            }
            is MindWaveEvent.SetInstrument -> viewModelScope.launch {
                appSettingsRepository.saveInstrumentType(event.instrument)
            }
            is MindWaveEvent.SetSongMaster -> viewModelScope.launch {
                appSettingsRepository.saveSongMasterEnabled(event.enabled)
            }
            MindWaveEvent.ClearHallOfFame -> viewModelScope.launch {
                scoreDao.clearAllScores()
            }
        }
    }

    private fun saveScore(score: Int, level: Int) {
        analyticsHelper.logEvent(
            AnalyticsEvent("mindwave_game_over", listOf(AnalyticsParam("level", level.toString())))
        )
        viewModelScope.launch {
            scoreDao.insertScore(MindWaveScoreEntity(score = score, level = level))
        }
    }
}
