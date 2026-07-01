package com.zoewave.probase.seaweed.database.converter

import androidx.room3.ColumnTypeConverter
import com.zoewave.probase.seaweed.model.ExpenseCategory
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.ThemeMode

class ExpenseConverters {
    @ColumnTypeConverter
    fun fromFrequency(frequency: ExpenseFrequency): String = frequency.name

    @ColumnTypeConverter
    fun toFrequency(value: String): ExpenseFrequency = ExpenseFrequency.valueOf(value)

    @ColumnTypeConverter
    fun fromCategory(category: ExpenseCategory): String = category.name

    @ColumnTypeConverter
    fun toCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)

    @ColumnTypeConverter
    fun fromThemeConfig(config: SeaweedThemeConfig): String = config.name

    @ColumnTypeConverter
    fun toThemeConfig(value: String): SeaweedThemeConfig = SeaweedThemeConfig.valueOf(value)

    @ColumnTypeConverter
    fun fromThemeMode(mode: ThemeMode): String = mode.name

    @ColumnTypeConverter
    fun toThemeMode(value: String): ThemeMode = ThemeMode.valueOf(value)

    @ColumnTypeConverter
    fun fromImportance(type: SpendingType): String = type.name

    @ColumnTypeConverter
    fun toImportance(value: String): SpendingType = SpendingType.valueOf(value)
}
