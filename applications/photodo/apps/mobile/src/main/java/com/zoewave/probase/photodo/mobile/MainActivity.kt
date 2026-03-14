package com.zoewave.probase.photodo.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zoewave.probase.photodo.mobile.ui.PhotoDoMainScreen
import com.zoewave.probase.photodo.mobile.ui.theme.ProBaseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProBaseTheme {
                PhotoDoMainScreen()
            }
        }
    }
}