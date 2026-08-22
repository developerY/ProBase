package com.zoewave.probase.kocolor.db.converter

import androidx.room3.ColumnTypeConverter
import com.zoewave.probase.kocolor.model.playlist.DailyPlanStatus
import com.zoewave.probase.kocolor.model.playlist.PlaylistStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class KoColorTypeConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @ColumnTypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @ColumnTypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @ColumnTypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.format(DateTimeFormatter.ISO_LOCAL_DATE)

    @ColumnTypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }

    @ColumnTypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let { json.encodeToString(it) }

    @ColumnTypeConverter
    fun toStringList(value: String?): List<String>? = value?.let { 
        if (it.isBlank() || it == "[]") emptyList() 
        else try { json.decodeFromString<List<String>>(it) } catch (e: Exception) { emptyList() }
    }

    @ColumnTypeConverter
    fun fromPlaylistStatus(value: PlaylistStatus): String = value.name

    @ColumnTypeConverter
    fun toPlaylistStatus(value: String): PlaylistStatus = try { 
        PlaylistStatus.valueOf(value) 
    } catch (e: Exception) { 
        PlaylistStatus.GENERATED 
    }

    @ColumnTypeConverter
    fun fromDailyPlanStatus(value: DailyPlanStatus): String = value.name

    @ColumnTypeConverter
    fun toDailyPlanStatus(value: String): DailyPlanStatus = try { 
        DailyPlanStatus.valueOf(value) 
    } catch (e: Exception) { 
        DailyPlanStatus.PLANNED 
    }
}
