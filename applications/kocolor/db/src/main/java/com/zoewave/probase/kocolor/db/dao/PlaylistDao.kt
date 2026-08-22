package com.zoewave.probase.kocolor.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.zoewave.probase.kocolor.db.entity.DailyStylePlanEntity
import com.zoewave.probase.kocolor.db.entity.PlaylistWithDays
import com.zoewave.probase.kocolor.db.entity.StylePlaylistEntity
import com.zoewave.probase.kocolor.model.playlist.DailyPlanStatus
import com.zoewave.probase.kocolor.model.playlist.PlaylistStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: StylePlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyPlans(plans: List<DailyStylePlanEntity>)

    @Transaction
    @Query("SELECT * FROM style_playlists WHERE playlistId = :playlistId")
    fun observePlaylistWithDays(playlistId: String): Flow<PlaylistWithDays?>

    @Transaction
    @Query("SELECT * FROM style_playlists WHERE playlistId = :playlistId")
    suspend fun getPlaylistWithDays(playlistId: String): PlaylistWithDays?

    @Query("SELECT * FROM daily_style_plans WHERE planId = :planId")
    suspend fun getDailyPlan(planId: String): DailyStylePlanEntity?

    @Query("UPDATE daily_style_plans SET status = :status WHERE planId = :planId")
    suspend fun updateDailyPlanStatus(planId: String, status: DailyPlanStatus)

    @Query("UPDATE style_playlists SET status = :status WHERE playlistId = :playlistId")
    suspend fun updatePlaylistStatus(playlistId: String, status: PlaylistStatus)
}
