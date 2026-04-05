package com.zoewave.probase.seaweed.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig
import com.zoewave.probase.seaweed.model.UserSettings

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 0, // Single row for user settings
    val monthlyIncome: Double,
    val currency: String,
    val themeConfig: SeaweedThemeConfig,
)

fun UserSettingsEntity.toDomain() = UserSettings(
    monthlyIncome = monthlyIncome,
    currency = currency,
    themeConfig = themeConfig
)

fun UserSettings.toEntity() = UserSettingsEntity(
    monthlyIncome = monthlyIncome,
    currency = currency,
    themeConfig = themeConfig
)
