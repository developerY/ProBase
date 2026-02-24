package com.zoewave.probase.ashbike.wear


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zoewave.probase.ashbike.features.main.service.BikeServiceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 1. Inject your brilliant manager
    @Inject
    lateinit var serviceManager: BikeServiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Your Compose UI theme wrapper goes here
            AshBikeWearUI()
        }
    }

    // 2. Start and Bind the service when the app becomes visible
    override fun onStart() {
        super.onStart()
        serviceManager.bindService(this)
    }

    // 3. Unbind when the user swipes away the app
    override fun onStop() {
        super.onStop()
        serviceManager.unbindService(this)
    }
}