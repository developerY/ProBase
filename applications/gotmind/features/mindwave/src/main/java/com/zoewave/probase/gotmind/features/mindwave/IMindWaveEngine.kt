package com.zoewave.probase.gotmind.features.mindwave

import com.zoewave.probase.gotmind.model.MindWaveMode
import kotlinx.coroutines.flow.StateFlow

interface IMindWaveEngine {
    val state: StateFlow<MindWaveState>
    
    fun start()
    fun reset()
    fun togglePause()
    fun onNodeClick(nodeId: Int)
    fun onHapticConsumed()
    fun setHapticsEnabled(enabled: Boolean)
    fun setSoundEnabled(enabled: Boolean)
    fun setAudioSynthesizer(synthesizer: com.zoewave.probase.core.util.audio.WaveSynthesizer?)
    fun updateSymphonySettings(waveform: com.zoewave.probase.core.util.audio.WaveSynthesizer.Waveform, songMaster: Boolean)
}
