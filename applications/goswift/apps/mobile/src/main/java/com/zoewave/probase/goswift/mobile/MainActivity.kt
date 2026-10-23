package com.zoewave.probase.goswift.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zoewave.probase.goswift.mobile.ui.components.GoSwiftMainScreen
import com.zoewave.probase.goswift.mobile.ui.theme.GoSwiftTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoSwiftTheme {
                GoSwiftMainScreen()
            }
        }
    }
}
