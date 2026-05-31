package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

enum class SeasonalType {
    SPRING, SUMMER, AUTUMN, WINTER, UNKNOWN
}

enum class Undertone {
    WARM, COOL, NEUTRAL, UNKNOWN
}

enum class InventoryItemType {
    FACE, HAIR, SHOES, CLOTHES
}

@Serializable
data class FashionProfile(
    val id: String = "default",
    val seasonalType: SeasonalType = SeasonalType.UNKNOWN,
    val undertone: Undertone = Undertone.UNKNOWN,
    val skinToneHex: String? = null,
    val eyeColor: String? = null,
    val hairColor: String? = null,
    val notes: String? = null,
    val recommendedPalette: List<String> = emptyList()
)

@Serializable
data class ColorPalette(
    val seasonalType: SeasonalType,
    val primaryColors: List<String>,
    val secondaryColors: List<String>,
    val neutralColors: List<String>,
    val avoidColors: List<String>
)

@Serializable
data class MakeupSuggestion(
    val category: String, // Foundation, Lip, Eye, etc.
    val advice: String,
    val recommendedColors: List<String>,
    val productId: Long? = null,
    val suggestedProductName: String? = null,
    val suggestedProductImageUrl: String? = null
)

@Serializable
data class OutfitSuggestion(
    val occasion: String,
    val advice: String,
    val keyPieces: List<String>,
    val colorCombinations: List<String>,
    val wardrobeItemIds: List<Long> = emptyList(),
    val suggestedItems: List<SuggestedPiece> = emptyList()
)

@Serializable
data class SuggestedPiece(
    val name: String,
    val category: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val isOwned: Boolean = false
)

@Serializable
data class FashionAdvice(
    val title: String? = null,
    val summary: String,
    val seasonalType: SeasonalType,
    val undertone: Undertone,
    val makeupSuggestions: List<MakeupSuggestion>,
    val outfitSuggestions: List<OutfitSuggestion>,
    val recommendedPalette: List<String>,
    val faceUri: String? = null,
    val hairUri: String? = null,
    val shoesUri: String? = null,
    val clothesUri: String? = null
)

@Serializable
data class SavedAnalysis(
    val id: Long,
    val timestamp: Long,
    val advice: FashionAdvice
)

@Serializable
data class InventoryItem(
    val id: Long = 0,
    val type: InventoryItemType,
    val uri: String,
    val clippedUri: String? = null,
    val timestamp: Long,
    val metadata: InventoryMetadata? = null
)

@Serializable
data class InventoryMetadata(
    // Common
    val colorHex: String? = null,
    val material: String? = null,
    val style: String? = null,
    
    // Face specific
    val skinToneHex: String? = null,
    val eyeColor: String? = null,
    val hairColor: String? = null,
    
    // Garment specific
    val silhouette: String? = null,
    val category: String? = null,
    
    // Footwear specific
    val heelHeight: String? = null,
    val toeShape: String? = null
)
