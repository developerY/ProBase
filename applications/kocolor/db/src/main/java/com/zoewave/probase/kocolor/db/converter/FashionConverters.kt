package com.zoewave.probase.kocolor.db.converter

import androidx.room3.TypeConverter
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FashionConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromFashionAdvice(value: FashionAdvice): String = json.encodeToString(value)

    @TypeConverter
    fun toFashionAdvice(value: String): FashionAdvice = json.decodeFromString<FashionAdvice>(value)

    @TypeConverter
    fun fromSeasonalType(value: SeasonalType): String = value.name

    @TypeConverter
    fun toSeasonalType(value: String): SeasonalType = try { SeasonalType.valueOf(value) } catch (e: Exception) { SeasonalType.UNKNOWN }

    @TypeConverter
    fun fromUndertone(value: Undertone): String = value.name

    @TypeConverter
    fun toUndertone(value: String): Undertone = try { Undertone.valueOf(value) } catch (e: Exception) { Undertone.UNKNOWN }
}
