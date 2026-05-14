package com.zoewave.probase.kocolor.db.converter

import androidx.room3.TypeConverter
import com.zoewave.probase.kocolor.db.entity.InventoryType
import com.zoewave.probase.kocolor.model.ClothingCategory
import com.zoewave.probase.kocolor.model.CosmeticCategory
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.InventoryMetadata
import com.zoewave.probase.kocolor.model.RoutineStep
import com.zoewave.probase.kocolor.model.RoutineTime
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FashionConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromFashionAdvice(value: FashionAdvice): String = json.encodeToString(value)

    @TypeConverter
    fun toFashionAdvice(value: String): FashionAdvice = json.decodeFromString<FashionAdvice>(value)

    @TypeConverter
    fun fromInventoryMetadata(value: InventoryMetadata?): String? = value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toInventoryMetadata(value: String?): InventoryMetadata? = value?.let { json.decodeFromString<InventoryMetadata>(it) }

    @TypeConverter
    fun fromSeasonalType(value: SeasonalType): String = value.name

    @TypeConverter
    fun toSeasonalType(value: String): SeasonalType = try { SeasonalType.valueOf(value) } catch (e: Exception) { SeasonalType.UNKNOWN }

    @TypeConverter
    fun fromUndertone(value: Undertone): String = value.name

    @TypeConverter
    fun toUndertone(value: String): Undertone = try { Undertone.valueOf(value) } catch (e: Exception) { Undertone.UNKNOWN }

    @TypeConverter
    fun fromInventoryType(value: InventoryType): String = value.name

    @TypeConverter
    fun toInventoryType(value: String): InventoryType = try { InventoryType.valueOf(value) } catch (e: Exception) { InventoryType.CLOTHES }

    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = try { json.decodeFromString<List<String>>(value) } catch (e: Exception) { emptyList() }

    @TypeConverter
    fun fromRoutineStepList(value: List<RoutineStep>): String = json.encodeToString(value)

    @TypeConverter
    fun toRoutineStepList(value: String): List<RoutineStep> = try { json.decodeFromString<List<RoutineStep>>(value) } catch (e: Exception) { emptyList() }

    @TypeConverter
    fun fromRoutineTime(value: RoutineTime): String = value.name

    @TypeConverter
    fun toRoutineTime(value: String): RoutineTime = try { RoutineTime.valueOf(value) } catch (e: Exception) { RoutineTime.OTHER }

    @TypeConverter
    fun fromCosmeticCategory(value: CosmeticCategory): String = value.name

    @TypeConverter
    fun toCosmeticCategory(value: String): CosmeticCategory = try { CosmeticCategory.valueOf(value) } catch (e: Exception) { CosmeticCategory.OTHER }

    @TypeConverter
    fun fromClothingCategory(value: ClothingCategory): String = value.name

    @TypeConverter
    fun toClothingCategory(value: String): ClothingCategory = try { ClothingCategory.valueOf(value) } catch (e: Exception) { ClothingCategory.OTHER }
}
