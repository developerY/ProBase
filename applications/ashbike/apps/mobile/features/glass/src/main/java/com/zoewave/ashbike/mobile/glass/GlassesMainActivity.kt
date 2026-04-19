package com.zoewave.ashbike.mobile.glass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import androidx.xr.projected.permissions.ProjectedPermissionsRequestParams
import androidx.xr.projected.permissions.ProjectedPermissionsResultContract
import com.zoewave.ashbike.data.repository.bike.BikeRepository
import com.zoewave.ashbike.mobile.glass.audio.VoiceGearController
import com.zoewave.ashbike.mobile.glass.ui.GlassApp
import com.zoewave.probase.features.ai.firebase.data.FirebaseLiveSessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalProjectedApi::class, ExperimentalProjectedApi::class)
@kotlin.OptIn(ExperimentalProjectedApi::class)
@AndroidEntryPoint // <--- Required for Hilt injection
class GlassesMainActivity : ComponentActivity() {

    // Inject the shared repository instance
    @Inject lateinit var repository: BikeRepository
    @Inject lateinit var firebaseLiveSessionManager: FirebaseLiveSessionManager
    private lateinit var audioInterface: AudioInterface
    private lateinit var voiceGearController: VoiceGearController

    private val requestPermissionLauncher =
        registerForActivityResult(ProjectedPermissionsResultContract()) { results ->
            if (results[Manifest.permission.RECORD_AUDIO] == true) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        audioInterface = AudioInterface(
            this,
            getString(R.string.applications_ashbike_apps_mobile_features_glass_hello_ai_glasses)
        )
        lifecycle.addObserver(audioInterface)

        voiceGearController = VoiceGearController(this, repository) { command ->
            if (command == "AI Assistant") {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    firebaseLiveSessionManager.startConversation()
                }
            } else {
                audioInterface.speak("Changing $command")
            }
        }
        lifecycle.addObserver(voiceGearController)
        lifecycle.addObserver(firebaseLiveSessionManager)

        checkAndRequestAudioPermission()

        setContent {
            GlimmerTheme {
                GlassApp(
                    onClose = {

                        /*audioInterface.speak("Goodbye!")
                        // Delay slightly or ensure speak finishes if possible, then finish
                        finish()*/

                        // 2. LAUNCH COROUTINE: Handle the "Goodbye" delay
                        lifecycleScope.launch {
                            audioInterface.speak("Goodbye!")
                            delay(1500) // Wait for audio to finish (approx)
                            finish()    // Close the Activity
                        }
                    }
                )
            }
        }
    }

    private fun checkAndRequestAudioPermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        val permissionStatus = ContextCompat.checkSelfPermission(this, permission)

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
        } else {
            requestAudioPermission()
        }
    }

    @OptIn(ExperimentalProjectedApi::class)
    private fun requestAudioPermission() {
        val params = ProjectedPermissionsRequestParams(
            permissions = listOf(Manifest.permission.RECORD_AUDIO),
            rationale = "Microphone access is essential for hands-free gear changes on these AI glasses."
        )
        // Speak rationale as recommended by XR docs
        audioInterface.speak("Please review the microphone permission request on your phone to enable voice commands.")
        requestPermissionLauncher.launch(listOf(params))
    }

    private fun onPermissionGranted() {
        voiceGearController.startListening()
    }

    private fun onPermissionDenied() {
        Log.w("GlassesMainActivity", "Microphone permission denied. Voice commands will be disabled.")
        // We could also finish() here if voice is considered critical, 
        // but for now we just log it as the UI buttons still work.
    }


    override fun onStart() {
        super.onStart()
        // ProjectionState.setProjecting(true)
        // Do things to make the user aware that this activity is active (for
        // example, play audio), when the display state is off
        // 2. User Feedback: Confirm the system is ready/visible
        // This runs on first launch AND when the screen wakes up from sleep
        // audioInterface.speak(getString(R.string.hello_ai_glasses))
        // This updates the flow that BikeViewModel is watching
        // --- THE FIX: Tell the Repository "I am here!" ---
        lifecycleScope.launch {
            // This updates the flow that BikeViewModel is watching
            repository.updateGlassSessionState(true)
        }
    }

    override fun onStop() {
        super.onStop()
        // ProjectionState.setProjecting(false)
        //Stop all the data source access
        // 3. Battery Saving: If you had heavy sensors (like Camera), pause them here.
        // For simple data syncing, you can often leave it running.
        // If AudioInterface is an Observer, it might auto-mute here.

        // --- THE FIX: Tell the Repository "I am gone!" ---
        lifecycleScope.launch {
            // This resets the button on the phone to "Start Projection"
            repository.updateGlassSessionState(false)
        }
        // ProjectionState.setProjecting(false)
        //Stop all the data source access
        // 3. Battery Saving: If you had heavy sensors (like Camera), pause them here.
        // For simple data syncing, you can often leave it running.
        // If AudioInterface is an Observer, it might auto-mute here.
    }

    override fun onDestroy() {
        super.onDestroy()
        // 2a. Tell the phone we are gone.
        // 4. Cleanup: Tell the phone the connection is fully closed
        // Best Practice: Use the Process scope.
        // This survives the Activity destruction but is tied to the App lifecycle.
    }
}