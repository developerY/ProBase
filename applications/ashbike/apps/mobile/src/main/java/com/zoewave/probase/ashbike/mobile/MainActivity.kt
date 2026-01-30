package com.zoewave.probase.ashbike.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge for that modern Android 15 look
        enableEdgeToEdge()

        setContent {
            // Use your Core UI theme (or MaterialTheme if not ready)
            MaterialTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call the shared code we created earlier
                    Text("hi from AshBike")
                    //AshBikeSharedScreen(platformName = "AshBike Mobile")
                }
            }
        }
    }
}