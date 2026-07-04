package com.zoewave.probase.kocolor.db.converter

import androidx.room3.ColumnTypeConverter
import com.zoewave.probase.kocolor.db.entity.InventoryType
import com.zoewave.probase.kocolor.db.entity.EnrichmentStatus
import com.zoewave.probase.core.model.ritual.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FashionConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @ColumnTypeConverter
    fun fromEnrichmentStatus(value: EnrichmentStatus): String = value.name

    @ColumnTypeConverter
    fun toEnrichmentStatus(value: String): EnrichmentStatus = try { 
        EnrichmentStatus.valueOf(value) 
    } catch (e: Exception) { 
        EnrichmentStatus.PENDING 
    }

    @ColumnTypeConverter
    fun fromStringMap(value: Map<String, String>): String = json.encodeToString(value)

    @ColumnTypeConverter
    fun toStringMap(value: String): Map<String, String> = try { 
        json.decodeFromString<Map<String, String>>(value) 
    } catch (e: Exception) { 
        emptyMap() 
    }

    @ColumnTypeConverter
    fun fromFashionAdvice(value: FashionAdvice): String = json.encodeToString(value)

    @ColumnTypeConverter
    fun toFashionAdvice(value: String): FashionAdvice = json.decodeFromString<FashionAdvice>(value)

    @ColumnTypeConverter
    fun fromInventoryMetadata(value: InventoryMetadata?): String? = value?.let { json.encodeToString(it) }

    @ColumnTypeConverter
    fun toInventoryMetadata(value: String?): InventoryMetadata? = value?.let { json.decodeFromString<InventoryMetadata>(it) }

    @ColumnTypeConverter
    fun fromSeasonalType(value: SeasonalType): String = value.name

    @ColumnTypeConverter
    fun toSeasonalType(value: String): SeasonalType = try { SeasonalType.valueOf(value) } catch (e: Exception) { SeasonalType.UNKNOWN }

    @ColumnTypeConverter
    fun fromUndertone(value: Undertone): String = value.name

    @ColumnTypeConverter
    fun toUndertone(value: String): Undertone = try { Undertone.valueOf(value) } catch (e: Exception) { Undertone.UNKNOWN }

    @ColumnTypeConverter
    fun fromInventoryType(value: InventoryType): String = value.name

    @ColumnTypeConverter
    fun toInventoryType(value: String): InventoryType = try { InventoryType.valueOf(value) } catch (e: Exception) { InventoryType.CLOTHES }

    @ColumnTypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @ColumnTypeConverter
    fun toStringList(value: String): List<String> = try { json.decodeFromString<List<String>>(value) } catch (e: Exception) { emptyList() }

    @ColumnTypeConverter
    fun fromRoutineStepList(value: List<RoutineStep>): String = json.encodeToString(value)

    @ColumnTypeConverter
    fun toRoutineStepList(value: String): List<RoutineStep> = try { json.decodeFromString<List<RoutineStep>>(value) } catch (e: Exception) { emptyList() }

    @ColumnTypeConverter
    fun fromRoutineTime(value: RoutineTime): String = value.name

    @ColumnTypeConverter
    fun toRoutineTime(value: String): RoutineTime = try { RoutineTime.valueOf(value) } catch (e: Exception) { RoutineTime.OTHER }

    @ColumnTypeConverter
    fun fromMacroCategory(value: MacroCategory): String = value.name

    @ColumnTypeConverter
    fun toMacroCategory(value: String): MacroCategory = try { MacroCategory.valueOf(value) } catch (e: Exception) { MacroCategory.TOOLS }

    @ColumnTypeConverter
    fun fromMicroCategory(value: MicroCategory): String = value.name

    @ColumnTypeConverter
    fun toMicroCategory(value: String): MicroCategory = try { MicroCategory.valueOf(value) } catch (e: Exception) { MicroCategory.OTHER }

    @ColumnTypeConverter
    fun fromFormulation(value: Formulation): String = value.name

    @ColumnTypeConverter
    fun toFormulation(value: String): Formulation = try { Formulation.valueOf(value) } catch (e: Exception) { Formulation.UNKNOWN }

    @ColumnTypeConverter
    fun fromChemistryBase(value: ChemistryBase): String = value.name

    @ColumnTypeConverter
    fun toChemistryBase(value: String): ChemistryBase = try { ChemistryBase.valueOf(value) } catch (e: Exception) { ChemistryBase.UNKNOWN }

    @ColumnTypeConverter
    fun fromFinish(value: Finish): String = value.name

    @ColumnTypeConverter
    fun toFinish(value: String): Finish = try { Finish.valueOf(value) } catch (e: Exception) { Finish.UNKNOWN }

    @ColumnTypeConverter
    fun fromCoverage(value: Coverage): String = value.name

    @ColumnTypeConverter
    fun toCoverage(value: String): Coverage = try { Coverage.valueOf(value) } catch (e: Exception) { Coverage.NOT_APPLICABLE }

    @ColumnTypeConverter
    fun fromClothingCategory(value: ClothingCategory): String = value.name

    @ColumnTypeConverter
    fun toClothingCategory(value: String): ClothingCategory = try { ClothingCategory.valueOf(value) } catch (e: Exception) { ClothingCategory.OTHER }
}
