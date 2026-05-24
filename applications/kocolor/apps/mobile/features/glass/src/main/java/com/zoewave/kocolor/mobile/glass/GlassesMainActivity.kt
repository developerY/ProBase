package com.zoewave.kocolor.mobile.glass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import com.zoewave.kocolor.mobile.glass.ui.GlassApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GlassesMainActivity : ComponentActivity() {

    private lateinit var audioInterface: KoColorAudioInterface

    @OptIn(ExperimentalProjectedApi::class, ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ComposeUiFlags.isInitialFocusOnFocusableAvailable = true

        audioInterface = KoColorAudioInterface(
            this,
            "Welcome to your morning ritual. Let's get started.",
        )
        lifecycle.addObserver(audioInterface)

        setContent {
            GlimmerTheme {
                GlassApp(
                    onClose = {
                        finish()
                    },
                    onSpeak = { text ->
                        audioInterface.speak(text)
                    }
                )
            }
        }
    }
}
