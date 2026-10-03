package com.zoewave.probase.gotmind.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val currentScore: Int = 0,
    val highHighScore: Int = 0,
    val isGameOver: Boolean = false,
    val level: Int = 1,
    val stage: Int = 1,
    val targetScoreForStage: Int = 50,
    val isStageCleared: Boolean = false
)
