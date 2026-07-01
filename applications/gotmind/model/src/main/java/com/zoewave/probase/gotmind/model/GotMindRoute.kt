package com.zoewave.probase.gotmind.model

import androidx.compose.ui.graphics.vector.ImageVector
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
    @Serializable data object SoundMind : GotMindRoute

    @Serializable data object Back : GotMindRoute
}

data class TopLevelDestination(
    val route: GotMindRoute,
    val icon: ImageVector,
    val labelResId: Int
)
