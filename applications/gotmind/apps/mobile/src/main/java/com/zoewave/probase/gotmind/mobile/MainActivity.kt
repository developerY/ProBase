package com.zoewave.probase.gotmind.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.gotmind.features.games.GamesScreen
import com.zoewave.probase.gotmind.features.memblox.MemBloxViewModel
import com.zoewave.probase.gotmind.features.memblox.ui.MemBloxScreen
import com.zoewave.probase.gotmind.mobile.ui.GameViewModel
import com.zoewave.probase.gotmind.mobile.ui.components.GameScreen
import com.zoewave.probase.gotmind.mobile.ui.theme.GotMindTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@Serializable
sealed interface GotMindRoute {
    @Serializable data object Games : GotMindRoute
    @Serializable data object GotMindClassic : GotMindRoute
    @Serializable data object MemBlox : GotMindRoute
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GotMindTheme {
                val backStack = remember { mutableStateListOf<GotMindRoute>(GotMindRoute.Games) }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = { route ->
                            NavEntry(route) {
                                when (route) {
                                    GotMindRoute.Games -> GamesScreen(
                                        onLaunchGotMindClassic = { backStack.add(GotMindRoute.GotMindClassic) },
                                        onLaunchMemBlox = { backStack.add(GotMindRoute.MemBlox) }
                                    )
                                    GotMindRoute.GotMindClassic -> {
                                        val viewModel: GameViewModel = hiltViewModel()
                                        GameScreen(viewModel = viewModel)
                                    }
                                    GotMindRoute.MemBlox -> {
                                        val viewModel: MemBloxViewModel = hiltViewModel()
                                        MemBloxScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
