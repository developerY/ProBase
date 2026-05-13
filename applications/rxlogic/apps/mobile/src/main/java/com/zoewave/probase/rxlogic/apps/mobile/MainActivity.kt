package com.zoewave.probase.rxlogic.apps.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.rxlogic.features.reminders.RemindersScreen
import com.zoewave.probase.rxlogic.features.reminders.RemindersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                val viewModel: RemindersViewModel = hiltViewModel()
                RemindersScreen(viewModel = viewModel)
            }
        }
    }
}
