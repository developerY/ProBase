package com.zoewave.probase.gotmind.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.zoewave.probase.gotmind.features.games.GamesScreen
import com.zoewave.probase.gotmind.features.leaderboard.ui.LeaderboardScreen
import com.zoewave.probase.gotmind.features.memblox.MemBloxEvent
import com.zoewave.probase.gotmind.features.memblox.MemBloxViewModel
import com.zoewave.probase.gotmind.features.memblox.ui.MemBloxScreen
import com.zoewave.probase.gotmind.features.mindwave.MindWaveEvent
import com.zoewave.probase.gotmind.features.mindwave.MindWaveViewModel
import com.zoewave.probase.gotmind.features.mindwave.ui.MindWaveScreen
import com.zoewave.probase.gotmind.features.settings.ui.SettingsScreen
import com.zoewave.probase.gotmind.features.settings.ui.SettingsViewModel
import com.zoewave.probase.gotmind.mobile.ui.GameViewModel
import com.zoewave.probase.gotmind.mobile.ui.components.GameScreen
import com.zoewave.probase.gotmind.mobile.ui.theme.GotMindTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@Serializable
sealed interface GotMindRoute {
    // Top Level Tabs
    @Serializable data object Games : GotMindRoute
    @Serializable data object Leaderboard : GotMindRoute
    @Serializable data object Settings : GotMindRoute

    // Fullscreen Game Screens
    @Serializable data object GotMindClassic : GotMindRoute
    @Serializable data object MemBlox : GotMindRoute
    @Serializable data object MindWave : GotMindRoute
}

data class TopLevelDestination(
    val route: GotMindRoute,
    val icon: ImageVector,
    val labelResId: Int
)

val topLevelDestinations = listOf(
    TopLevelDestination(GotMindRoute.Games, Icons.Default.Games, R.string.nav_games),
    TopLevelDestination(GotMindRoute.Leaderboard, Icons.Default.EmojiEvents, R.string.nav_leaderboard),
    TopLevelDestination(GotMindRoute.Settings, Icons.Default.Settings, R.string.nav_settings)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val themeSettings by settingsVm.themeSettings.collectAsState()
            val gameSettings by settingsVm.gameSettings.collectAsState()
            val firebaseId by settingsVm.firebaseId.collectAsState()

            GotMindTheme(
                appTheme = themeSettings.theme,
                palette = themeSettings.palette
            ) {
                val backStack = remember { mutableStateListOf<GotMindRoute>(GotMindRoute.Games) }
                val currentRoute = backStack.lastOrNull() ?: GotMindRoute.Games
                
                // Keep tabs visible for games to maintain consistent app structure
                val shouldShowBottomBar = currentRoute in topLevelDestinations.map { it.route } || 
                                        currentRoute == GotMindRoute.MemBlox || 
                                        currentRoute == GotMindRoute.GotMindClassic

                Scaffold(
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            GotMindBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    if (route != currentRoute) {
                                        // Clear stack when switching main tabs to avoid state leaks
                                        backStack.clear()
                                        backStack.add(route)
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = androidx.compose.ui.graphics.Color(0xFF0F0F0F)
                    ) {
                        NavDisplay(
                            backStack = backStack,
                            modifier = Modifier.padding(if (shouldShowBottomBar) innerPadding else androidx.compose.foundation.layout.PaddingValues(0.dp)),
                            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            entryProvider = { route ->
                                NavEntry(route) {
                                    when (route) {
                                        GotMindRoute.Games -> GamesScreen(
                                            onNav = { dest -> 
                                                when (dest) {
                                                    "CLASSIC" -> backStack.add(GotMindRoute.GotMindClassic)
                                                    "MEMBLOX" -> backStack.add(GotMindRoute.MemBlox)
                                                    "MINDWAVE" -> backStack.add(GotMindRoute.MindWave)
                                                }
                                            }
                                        )
                                        GotMindRoute.Leaderboard -> {
                                            val memBloxVm: MemBloxViewModel = hiltViewModel()
                                            val mindWaveVm: MindWaveViewModel = hiltViewModel()
                                            val memBloxScores by memBloxVm.topScores.collectAsState()
                                            val mindWaveScores by mindWaveVm.topScores.collectAsState()
                                            
                                            LeaderboardScreen(
                                                membloxScores = memBloxScores,
                                                mindwaveScores = mindWaveScores,
                                                onClearMemBlox = { memBloxVm.handleEvent(MemBloxEvent.ClearHallOfFame) },
                                                onClearMindWave = { mindWaveVm.handleEvent(MindWaveEvent.ResetGame) } // Need a clear event for MindWave
                                            )
                                        }
                                        GotMindRoute.Settings -> {
                                            val memBloxVm: MemBloxViewModel = hiltViewModel()
                                            SettingsScreen(
                                                gameSettings = gameSettings,
                                                themeSettings = themeSettings,
                                                firebaseId = firebaseId,
                                                onMemBloxEvent = { memBloxVm.handleEvent(it) },
                                                onSettingsEvent = { settingsVm.handleEvent(it) },
                                                onBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                                            )
                                        }
                                        
                                        GotMindRoute.GotMindClassic -> {
                                            val viewModel: GameViewModel = hiltViewModel()
                                            GameScreen(viewModel = viewModel)
                                        }
                                        GotMindRoute.MemBlox -> {
                                            val viewModel: MemBloxViewModel = hiltViewModel()
                                            val state by viewModel.uiState.collectAsState()
                                            val topScores by viewModel.topScores.collectAsState()
                                            val engineType by viewModel.engineType.collectAsState()
                                            MemBloxScreen(
                                                uiState = state,
                                                topScores = topScores,
                                                engineType = engineType,
                                                onNav = { if (it == "BACK") backStack.removeLastOrNull() },
                                                onEvent = { event -> viewModel.handleEvent(event) }
                                            )
                                        }
                                        GotMindRoute.MindWave -> {
                                            val viewModel: MindWaveViewModel = hiltViewModel()
                                            val state by viewModel.uiState.collectAsState()
                                            MindWaveScreen(
                                                uiState = state,
                                                onNav = { if (it == "BACK") backStack.removeLastOrNull() },
                                                onEvent = { event -> viewModel.handleEvent(event) }
                                            )
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
}

@Composable
fun GotMindBottomBar(
    currentRoute: GotMindRoute,
    onNavigate: (GotMindRoute) -> Unit
) {
    // Map sub-routes back to their parent tabs for highlighting
    val selectedRoute = when (currentRoute) {
        GotMindRoute.MemBlox, GotMindRoute.GotMindClassic -> GotMindRoute.Games
        else -> currentRoute
    }

    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color(0xFF1E1E1E).copy(alpha = 0.9f), // Glassmorphism look
        contentColor = androidx.compose.ui.graphics.Color.White,
        tonalElevation = 8.dp
    ) {
        topLevelDestinations.forEach { dest ->
            NavigationBarItem(
                selected = selectedRoute == dest.route,
                onClick = { onNavigate(dest.route) },
                icon = { Icon(dest.icon, contentDescription = stringResource(dest.labelResId)) },
                label = { Text(stringResource(dest.labelResId)) }
            )
        }
    }
}
