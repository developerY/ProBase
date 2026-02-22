package com.zoewave.probase.ashbike.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the Splash Screen transition
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            // Use Material 3 for Wear OS
            MaterialTheme {
                // The Box is removed. AshBikeApp contains the AppScaffold,
                // which handles centering, curved text, and swipe gestures natively.
                AshBikeWearUI()
            }
        }
    }
}