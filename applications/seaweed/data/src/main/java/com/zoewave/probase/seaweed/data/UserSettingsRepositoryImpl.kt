package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.database.UserSettingsDao
import com.zoewave.probase.seaweed.database.toDomain
import com.zoewave.probase.seaweed.database.toEntity
import com.zoewave.probase.seaweed.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserSettingsRepositoryImpl @Inject constructor(
    private val dao: UserSettingsDao
) : UserSettingsRepository {

    override fun getUserSettings(): Flow<UserSettings> =
        dao.getUserSettings().map { it?.toDomain() ?: UserSettings() }

    override suspend fun saveUserSettings(settings: UserSettings) {
        dao.saveUserSettings(settings.toEntity())
    }
}
