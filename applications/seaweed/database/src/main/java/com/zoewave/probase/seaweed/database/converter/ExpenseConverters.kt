package com.zoewave.probase.seaweed.database.converter

import androidx.room3.TypeConverter
import com.zoewave.probase.seaweed.model.ExpenseCategory
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.ThemeMode

class ExpenseConverters {
    @TypeConverter
    fun fromFrequency(frequency: ExpenseFrequency): String = frequency.name

    @TypeConverter
    fun toFrequency(value: String): ExpenseFrequency = ExpenseFrequency.valueOf(value)

    @TypeConverter
    fun fromCategory(category: ExpenseCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)

    @TypeConverter
    fun fromThemeConfig(config: SeaweedThemeConfig): String = config.name

    @TypeConverter
    fun toThemeConfig(value: String): SeaweedThemeConfig = SeaweedThemeConfig.valueOf(value)

    @TypeConverter
    fun fromThemeMode(mode: ThemeMode): String = mode.name

    @TypeConverter
    fun toThemeMode(value: String): ThemeMode = ThemeMode.valueOf(value)

    @TypeConverter
    fun fromImportance(type: SpendingType): String = type.name

    @TypeConverter
    fun toImportance(value: String): SpendingType = SpendingType.valueOf(value)
}
