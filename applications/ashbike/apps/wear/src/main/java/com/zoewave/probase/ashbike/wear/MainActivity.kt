package com.zoewave.probase.ashbike.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
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
                // Box + Alignment.Center handles round screens correctly
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // ✅ Reusing the EXACT same screen code as Mobile!
                    Text("Hello, World!")
                    //AshBikeSharedScreen(platformName = "AshBike Watch")
                }
            }
        }
    }
}