package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.kocolor.db.entity.DailyStylePlanEntity
import com.zoewave.probase.kocolor.db.entity.PlaylistWithDays
import com.zoewave.probase.kocolor.db.entity.StylePlaylistEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun observePlaylist(playlistId: String): Flow<PlaylistWithDays?>
    fun observeLatestPlaylist(): Flow<PlaylistWithDays?>
    suspend fun savePlaylist(playlist: StylePlaylistEntity, plans: List<DailyStylePlanEntity>)
    suspend fun commitDailyOutfit(planId: String, actuallyWornProductIds: List<String>)
}
