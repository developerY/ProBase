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

import com.zoewave.probase.gotmind.model.GotMindRoute
import com.zoewave.probase.gotmind.model.TopLevelDestination

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
                                        currentRoute == GotMindRoute.GotMindClassic ||
                                        currentRoute == GotMindRoute.SoundMind

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
                                val navigateTo: (GotMindRoute) -> Unit = { dest ->
                                    if (dest == GotMindRoute.Back) {
                                        if (backStack.size > 1) backStack.removeLastOrNull()
                                    } else {
                                        if (dest != route) {
                                            if (dest in topLevelDestinations.map { it.route }) {
                                                backStack.clear()
                                            }
                                            backStack.add(dest)
                                        }
                                    }
                                }

                                NavEntry(route) {
                                    when (route) {
                                        GotMindRoute.Games -> GamesScreen(
                                            uiState = com.zoewave.probase.gotmind.features.games.GamesUiState,
                                            onEvent = {},
                                            navTo = navigateTo
                                        )
                                        GotMindRoute.Leaderboard -> {
                                            val memBloxVm: MemBloxViewModel = hiltViewModel()
                                            val mindWaveVm: MindWaveViewModel = hiltViewModel()
                                            val memBloxScores by memBloxVm.topScores.collectAsState()
                                            val mindWaveScores by mindWaveVm.topScores.collectAsState()
                                            
                                            LeaderboardScreen(
                                                uiState = com.zoewave.probase.gotmind.features.leaderboard.ui.LeaderboardUiState(
                                                    membloxScores = memBloxScores,
                                                    mindwaveScores = mindWaveScores
                                                ),
                                                onEvent = { event ->
                                                    when (event) {
                                                        com.zoewave.probase.gotmind.features.leaderboard.ui.LeaderboardEvent.ClearMemBlox -> memBloxVm.handleEvent(MemBloxEvent.ClearHallOfFame)
                                                        com.zoewave.probase.gotmind.features.leaderboard.ui.LeaderboardEvent.ClearMindWave -> mindWaveVm.handleEvent(MindWaveEvent.ClearHallOfFame)
                                                    }
                                                },
                                                navTo = navigateTo
                                            )
                                        }
                                        GotMindRoute.Settings -> {
                                            val memBloxVm: MemBloxViewModel = hiltViewModel()
                                            SettingsScreen(
                                                uiState = com.zoewave.probase.gotmind.features.settings.ui.SettingsUiState(
                                                    gameSettings = gameSettings,
                                                    themeSettings = themeSettings,
                                                    firebaseId = firebaseId
                                                ),
                                                onEvent = { event ->
                                                    when (event) {
                                                        is com.zoewave.probase.gotmind.features.settings.ui.SettingsScreenEvent.Settings -> settingsVm.handleEvent(event.event)
                                                        is com.zoewave.probase.gotmind.features.settings.ui.SettingsScreenEvent.MemBlox -> memBloxVm.handleEvent(event.event)
                                                    }
                                                },
                                                navTo = navigateTo
                                            )
                                        }
                                        
                                        GotMindRoute.GotMindClassic -> {
                                            val viewModel: GameViewModel = hiltViewModel()
                                            val state by viewModel.gameState.collectAsState()
                                            val topScores by viewModel.topScores.collectAsState()
                                            GameScreen(
                                                uiState = com.zoewave.probase.gotmind.mobile.ui.components.GotMindClassicUiState(
                                                    game = state,
                                                    topScores = topScores
                                                ),
                                                onEvent = { event ->
                                                    when (event) {
                                                        com.zoewave.probase.gotmind.mobile.ui.components.GotMindClassicEvent.GameOver -> viewModel.onGameOver()
                                                        com.zoewave.probase.gotmind.mobile.ui.components.GotMindClassicEvent.ResetGame -> viewModel.resetGame()
                                                        is com.zoewave.probase.gotmind.mobile.ui.components.GotMindClassicEvent.ScoreUpdate -> viewModel.onScoreUpdate(event.delta)
                                                    }
                                                },
                                                navTo = navigateTo
                                            )
                                        }
                                        GotMindRoute.MemBlox -> {
                                            val viewModel: MemBloxViewModel = hiltViewModel()
                                            val state by viewModel.uiState.collectAsState()
                                            val topScores by viewModel.topScores.collectAsState()
                                            val engineType by viewModel.engineType.collectAsState()
                                            MemBloxScreen(
                                                uiState = com.zoewave.probase.gotmind.features.memblox.MemBloxUiState(
                                                    game = state,
                                                    topScores = topScores,
                                                    engineType = engineType
                                                ),
                                                onEvent = { event -> viewModel.handleEvent(event) },
                                                navTo = navigateTo
                                            )
                                        }
                                        GotMindRoute.SoundMind -> {
                                            val viewModel: MindWaveViewModel = hiltViewModel()
                                            val state by viewModel.uiState.collectAsState()
                                            MindWaveScreen(
                                                uiState = state,
                                                onEvent = { event -> viewModel.handleEvent(event) },
                                                navTo = navigateTo
                                            )
                                        }
                                        GotMindRoute.Back -> { /* Back is a signal, not a destination */ }
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
        GotMindRoute.MemBlox, GotMindRoute.GotMindClassic, GotMindRoute.SoundMind -> GotMindRoute.Games
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
