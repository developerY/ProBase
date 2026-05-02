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
    EASY(6, 10, 15, 6000L, R.string.applications_gotmind_model_diff_easy),
    MEDIUM(9, 15, 30, 4500L, R.string.applications_gotmind_model_diff_medium),
    EXPERT(12, 20, 50, 3000L, R.string.applications_gotmind_model_diff_expert)
}
