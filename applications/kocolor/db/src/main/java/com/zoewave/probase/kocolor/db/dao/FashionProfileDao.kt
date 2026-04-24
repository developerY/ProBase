package com.zoewave.probase.kocolor.db.dao

import androidx.room3.*
import com.zoewave.probase.kocolor.db.entity.FashionProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FashionProfileDao {
    @Query("SELECT * FROM fashion_profiles WHERE id = :id")
    fun getProfile(id: String = "default"): Flow<FashionProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: FashionProfileEntity)

    @Query("DELETE FROM fashion_profiles WHERE id = :id")
    suspend fun deleteProfile(id: String = "default")
}
