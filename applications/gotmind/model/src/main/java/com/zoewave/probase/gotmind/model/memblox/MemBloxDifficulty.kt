package com.zoewave.probase.gotmind.model.memblox

import com.zoewave.probase.gotmind.model.R
import kotlinx.serialization.Serializable

@Serializable
enum class MemBloxDifficulty(
    val cols: Int,
    val rows: Int,
    val targetPairs: Int,
    val spawnDelayMillis: Long,
    val labelResId: Int
) {
    EASY(6, 10, 15, 2000L, R.string.applications_gotmind_model_diff_easy),
    MEDIUM(9, 15, 30, 1500L, R.string.applications_gotmind_model_diff_medium),
    EXPERT(12, 20, 50, 1000L, R.string.applications_gotmind_model_diff_expert)
}
