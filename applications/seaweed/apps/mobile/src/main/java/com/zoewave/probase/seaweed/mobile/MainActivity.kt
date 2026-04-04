package com.zoewave.probase.seaweed.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.zoewave.probase.seaweed.mobile.core.ui.theme.v1.SeaweedTheme
import com.zoewave.probase.seaweed.mobile.ui.components.SeaweedMainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseAnalytics = Firebase.analytics
        // Tags all future events from this user's phone
        firebaseAnalytics.setUserProperty("device_platform", "mobile")


        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            SeaweedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SeaweedMainScreen(windowSizeClass = windowSizeClass)
                }
            }
        }
    }
}
