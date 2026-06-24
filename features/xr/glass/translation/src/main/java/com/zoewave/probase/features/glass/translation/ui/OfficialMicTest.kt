package com.zoewave.probase.features.glass.translation.ui

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi

private const val TAG = "OfficialMicTest"

/**
 * Runs the official "Record audio with the glasses' microphone" sample code from the Android XR documentation
 * with extensive high-visibility logging.
 */
@OptIn(ExperimentalProjectedApi::class)
fun runOfficialMicTest(context: Context) {
    Log.e(TAG, "========================================================")
    Log.e(TAG, "STARTING OFFICIAL XR SDK MICROPHONE TEST")
    Log.e(TAG, "========================================================")

    // 1. Get the Projected Context
    Log.e(TAG, "[STEP 1] Creating ProjectedContext...")
    val projectedDeviceContext = try {
        ProjectedContext.createProjectedDeviceContext(context).also {
            Log.e(TAG, "[SUCCESS] ProjectedContext created.")
        }
    } catch (e: IllegalStateException) {
        Log.e(TAG, "[FAILURE] AI Glasses context could not be created. Are they connected?", e)
        return
    }

    try {
        // 2. Configure Audio Format (16kHz, Mono as required by AI Glasses)
        Log.e(TAG, "[STEP 2] Configuring AudioFormat (16kHz, Mono)...")
        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(channelConfig)
            .build()

        // 3. Calculate Buffer Size
        Log.e(TAG, "[STEP 3] Calculating minimum buffer size...")
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT)
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "[FAILURE] Invalid buffer size calculated: $bufferSize")
            return
        }
        Log.e(TAG, "[INFO] Min buffer size: $bufferSize bytes.")

        // 4. Initialize AudioRecord with projected device context
        Log.e(TAG, "[STEP 4] Building AudioRecord with ProjectedContext...")
        val audioRecord = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bufferSize)
            // PASS IN THE PROJECTED DEVICE CONTEXT
            .setContext(projectedDeviceContext)
            .build()

        Log.e(TAG, "[SUCCESS] AudioRecord built successfully.")

        // 5. Start Recording
        Log.e(TAG, "[STEP 5] Calling startRecording()...")
        audioRecord.startRecording()
        
        if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            Log.e(TAG, "========================================================")
            Log.e(TAG, ">>> SUCCESS: OFFICIAL SDK MICROPHONE BINDING COMPLETE <<<")
            Log.e(TAG, ">>> The emulator/device has a virtual projected mic! <<<")
            Log.e(TAG, "========================================================")
        } else {
            Log.e(TAG, "[FAILURE] AudioRecord state is not RECORDING: ${audioRecord.recordingState}")
        }

        // 6. Cleanup
        Log.e(TAG, "[STEP 6] Cleaning up...")
        audioRecord.stop()
        audioRecord.release()
        Log.e(TAG, "[INFO] Cleanup complete.")

    } catch (e: SecurityException) {
        Log.e(TAG, "[FAILURE] Permission denied for RECORD_AUDIO. Ensure it is granted on the PHONE.", e)
    } catch (e: Exception) {
        Log.e(TAG, "[FAILURE] Critical error during AudioRecord setup", e)
        Log.e(TAG, "--------------------------------------------------------")
        Log.e(TAG, "Conclusion: The binding failed. The environment likely lacks a projected mic.")
        Log.e(TAG, "--------------------------------------------------------")
    }
}
