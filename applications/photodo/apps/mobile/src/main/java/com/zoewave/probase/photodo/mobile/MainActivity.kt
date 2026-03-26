package com.zoewave.probase.photodo.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zoewave.probase.photodo.mobile.core.ui.PhotoDoTheme
import com.zoewave.probase.photodo.mobile.ui.components.PhotoDoMainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoDoTheme {
                PhotoDoMainScreen()
            }
        }
    }
}