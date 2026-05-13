package com.zoewave.probase.rxlogic.db

import androidx.room3.TypeConverter
import com.zoewave.probase.rxlogic.model.Frequency
import com.zoewave.probase.rxlogic.model.LogStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RxLogicConverters {
    @TypeConverter
    fun fromFrequency(value: Frequency): String = value.name

    @TypeConverter
    fun toFrequency(value: String): Frequency = Frequency.valueOf(value)

    @TypeConverter
    fun fromLogStatus(value: LogStatus): String = value.name

    @TypeConverter
    fun toLogStatus(value: String): LogStatus = LogStatus.valueOf(value)

    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)

    @TypeConverter
    fun fromLocalTime(value: LocalTime): String = value.toString()

    @TypeConverter
    fun toLocalTime(value: String): LocalTime = LocalTime.parse(value)

    @TypeConverter
    fun fromLocalTimeList(value: List<LocalTime>): String = Json.encodeToString(value)

    @TypeConverter
    fun toLocalTimeList(value: String): List<LocalTime> = Json.decodeFromString(value)
}
