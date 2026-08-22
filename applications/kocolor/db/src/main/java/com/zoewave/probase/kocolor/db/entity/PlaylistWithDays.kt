package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class PlaylistWithDays(
    @Embedded val playlist: StylePlaylistEntity,
    @Relation(
        parentColumns = ["playlistId"],
        entityColumns = ["playlistId"]
    )
    val dailyPlans: List<DailyStylePlanEntity>
)
