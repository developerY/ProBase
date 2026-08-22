package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.kocolor.db.KoColorDatabase
import com.zoewave.probase.kocolor.db.dao.PlaylistDao
import com.zoewave.probase.kocolor.db.entity.DailyStylePlanEntity
import com.zoewave.probase.kocolor.db.entity.PlaylistWithDays
import com.zoewave.probase.kocolor.db.entity.StylePlaylistEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val database: KoColorDatabase,
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override fun observePlaylist(playlistId: String): Flow<PlaylistWithDays?> {
        return playlistDao.observePlaylistWithDays(playlistId)
    }

    override suspend fun savePlaylist(playlist: StylePlaylistEntity, plans: List<DailyStylePlanEntity>) {
        database.savePlaylist(playlist, plans)
    }

    override suspend fun commitDailyOutfit(planId: String, actuallyWornProductIds: List<String>) {
        database.commitDailyStylePlan(planId, actuallyWornProductIds)
    }
}
