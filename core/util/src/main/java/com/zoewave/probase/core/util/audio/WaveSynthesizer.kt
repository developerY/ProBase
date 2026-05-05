package com.zoewave.probase.core.util.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

@Singleton
class WaveSynthesizer @Inject constructor() {
    private val sampleRate = 44100
    private val minBufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    // Use a much larger buffer (3x min) to prevent underruns during calculation
    private val bufferSize = (minBufferSize * 3).coerceAtLeast(8192)
    
    private val writeMutex = Mutex()

    private var audioTrack: AudioTrack? = createAudioTrack()

    private fun createAudioTrack(): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        
        track.play()
        return track
    }

    enum class Waveform { SINE, SQUARE, TRIANGLE }

    suspend fun playTone(
        frequency: Double, 
        durationMillis: Long, 
        waveform: Waveform = Waveform.SINE
    ) = withContext(Dispatchers.Default) {
        val numSamples = (durationMillis * sampleRate / 1000).toInt()
        val samples = ShortArray(numSamples)
        
        // 1. Calculate the wave data in parallel (before locking the track)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val angle = 2.0 * PI * frequency * t
            
            val rawValue = when (waveform) {
                Waveform.SINE -> sin(angle)
                Waveform.SQUARE -> if (sin(angle) >= 0) 1.0 else -1.0
                Waveform.TRIANGLE -> {
                    val period = 1.0 / frequency
                    val relativeT = t % period
                    val fraction = relativeT / period
                    if (fraction < 0.25) 4.0 * fraction
                    else if (fraction < 0.75) 2.0 - 4.0 * fraction
                    else 4.0 * fraction - 4.0
                }
            }

            val envelope = when {
                i < 441 -> i / 441.0 // 10ms fade in
                i > numSamples - 441 -> (numSamples - i) / 441.0 // 10ms fade out
                else -> 1.0
            }
            samples[i] = (rawValue * Short.MAX_VALUE * 0.4 * envelope).toInt().toShort()
        }
        
        // 2. Safely write to the audio track one tone at a time
        writeMutex.withLock {
            try {
                val track = audioTrack ?: return@withLock
                
                // If track was disabled or underrun happened, ensure it's in playing state
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                        track.play()
                    }
                    track.write(samples, 0, numSamples, AudioTrack.WRITE_BLOCKING)
                }
            } catch (e: Exception) {
                // Recover from native errors or state changes
                audioTrack = createAudioTrack()
            }
        }
    }

    fun stop() {
        writeMutex.tryLock() // Try to prevent mid-write release
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
