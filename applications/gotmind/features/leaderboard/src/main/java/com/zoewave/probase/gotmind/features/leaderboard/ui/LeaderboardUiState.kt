package com.zoewave.probase.gotmind.features.leaderboard.ui

import com.zoewave.probase.gotmind.database.MemBloxScoreEntity
import com.zoewave.probase.gotmind.database.MindWaveScoreEntity

data class LeaderboardUiState(
    val membloxScores: List<MemBloxScoreEntity> = emptyList(),
    val mindwaveScores: List<MindWaveScoreEntity> = emptyList()
)

sealed interface LeaderboardEvent {
    data object ClearMemBlox : LeaderboardEvent
    data object ClearMindWave : LeaderboardEvent
}
