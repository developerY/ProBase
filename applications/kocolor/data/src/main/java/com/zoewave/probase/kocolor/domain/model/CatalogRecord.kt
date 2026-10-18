package com.zoewave.probase.kocolor.domain.model

import com.zoewave.probase.kocolor.domain.taxonomy.*
import kotlinx.serialization.Serializable

@Serializable
data class ProvenanceField<T>(
    val value: T? = null,
    val provenance: ProvenanceType = ProvenanceType.UNKNOWN
)

@Serializable
data class ProvenanceListField<T>(
    val values: List<T> = emptyList(),
    val provenance: ProvenanceType = ProvenanceType.UNKNOWN,
    val derivationMethod: String? = null,
    val ruleVersion: String? = null
)

@Serializable
data class CommercialMeta(
    val priceUsd: Double = 0.0,
    val volumeOrSize: String = "",
    val sustainabilityGrade: String = "UNRATED"
)

@Serializable
data class ColorMeta(
    val hex: String,
    val colorName: String = "",
    val chromaticBehavior: String = "OPAQUE",
    val hueFamily: ProvenanceField<HueFamily>,
    val colorTemperature: ProvenanceField<ColorTemperature>,
    val lStar: Double,
    val aStar: Double = 0.0,
    val bStar: Double = 0.0,
    val cStar: Double,
    val hDeg: Double,
    val colorAlgorithmVersion: String = "1.0.0",
    val colorClassificationRuleVersion: String = "1.0.0"
)

@Serializable
data class MaterialEntry(
    val material: String,
    val percentage: Int
)

@Serializable
data class MaterialMeta(
    val composition: List<MaterialEntry> = emptyList(),
    val provenance: ProvenanceType = ProvenanceType.UNKNOWN
)

@Serializable
data class CommonMetadata(
    val itemId: String,
    val name: String,
    val source: String = "INTERNAL",
    val commercial: CommercialMeta = CommercialMeta(),
    val color: ColorMeta,
    val material: MaterialMeta = MaterialMeta(),
    val metadataVersion: Int = 1,
    val ruleVersion: String = "1.0.0"
)

@Serializable
data class ConstructionMeta(
    val thermalWeight: ProvenanceField<ThermalWeight>,
    val breathability: ProvenanceField<Breathability> = ProvenanceField(),
    val insulation: ProvenanceField<InsulationLevel>,
    val features: List<String> = emptyList(),
    val derivationMethod: String? = null,
    val ruleVersion: String? = null
)

@Serializable
data class LayeringRoleMeta(
    val primary: LayeringRole,
    val compatible: List<LayeringRole> = emptyList(),
    val provenance: ProvenanceType = ProvenanceType.RULE_DERIVED
)

@Serializable
data class WardrobeMetadata(
    val catalogCategory: String,
    val productType: ProvenanceField<ProductType>,
    val garmentSlot: ProvenanceField<String>, // TOP, BOTTOM, OUTERWEAR, ONE_PIECE, FOOTWEAR, ACCESSORY
    val occasionTags: List<ProvenanceField<OccasionTag>> = emptyList(),
    val formality: ProvenanceField<FormalityLevel>,
    val thermalSuitability: ProvenanceListField<ThermalBand>,
    val weatherProtection: ProvenanceListField<WeatherProtection> = ProvenanceListField(),
    val construction: ConstructionMeta,
    val layeringRole: LayeringRoleMeta,
    val silhouette: ProvenanceField<SilhouetteFit>,
    val seasons: ProvenanceListField<SeasonTag> = ProvenanceListField(),
    val activities: ProvenanceListField<ActivityTag> = ProvenanceListField()
)

@Serializable
data class CosmeticsMetadata(
    val cosmeticCategory: String, // EYE, CHEEK, LIP, NAIL, PREP, COMPLEXION, HAIR, BODY
    val applicationRole: String,
    val shade: String = "",
    val finish: ProvenanceField<String> = ProvenanceField(),
    val formulationAttributes: ProvenanceListField<String> = ProvenanceListField()
)

@Serializable
data class CatalogRecord(
    val domain: String, // "WARDROBE" or "COSMETICS"
    val commonMetadata: CommonMetadata,
    val wardrobeMetadata: WardrobeMetadata? = null,
    val cosmeticsMetadata: CosmeticsMetadata? = null
)
