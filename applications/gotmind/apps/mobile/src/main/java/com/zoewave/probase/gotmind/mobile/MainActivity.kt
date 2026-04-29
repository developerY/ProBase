package com.zoewave.probase.gotmind.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.gotmind.mobile.ui.GameViewModel
import com.zoewave.probase.gotmind.mobile.ui.components.GameScreen
import com.zoewave.probase.gotmind.mobile.ui.theme.GotMindTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GotMindTheme {
                val viewModel: GameViewModel = hiltViewModel()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen(viewModel = viewModel)
                }
            }
        }
    }
}
