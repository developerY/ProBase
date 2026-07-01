package com.zoewave.probase.rxlogic.db

import androidx.room3.ColumnTypeConverter
import com.zoewave.probase.rxlogic.model.Frequency
import com.zoewave.probase.rxlogic.model.LogStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RxLogicConverters {
    @ColumnTypeConverter
    fun fromFrequency(value: Frequency): String = value.name

    @ColumnTypeConverter
    fun toFrequency(value: String): Frequency = Frequency.valueOf(value)

    @ColumnTypeConverter
    fun fromLogStatus(value: LogStatus): String = value.name

    @ColumnTypeConverter
    fun toLogStatus(value: String): LogStatus = LogStatus.valueOf(value)

    @ColumnTypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

    @ColumnTypeConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)

    @ColumnTypeConverter
    fun fromLocalTime(value: LocalTime): String = value.toString()

    @ColumnTypeConverter
    fun toLocalTime(value: String): LocalTime = LocalTime.parse(value)

    @ColumnTypeConverter
    fun fromLocalTimeList(value: List<LocalTime>): String = Json.encodeToString(value)

    @ColumnTypeConverter
    fun toLocalTimeList(value: String): List<LocalTime> = Json.decodeFromString(value)
}
