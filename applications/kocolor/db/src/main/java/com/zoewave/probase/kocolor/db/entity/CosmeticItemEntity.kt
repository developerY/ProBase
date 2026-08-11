package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.core.model.ritual.ChemistryBase
import com.zoewave.probase.core.model.ritual.ColorFamily
import com.zoewave.probase.core.model.ritual.Coverage
import com.zoewave.probase.core.model.ritual.Finish
import com.zoewave.probase.core.model.ritual.Formulation
import com.zoewave.probase.core.model.ritual.InventorySource
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.core.model.ritual.Temperature

@Entity(tableName = "cosmetic_items")
data class CosmeticItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val macroCategory: MacroCategory,
    val microCategory: MicroCategory,
    
    // Professional Metadata
    val formulation: Formulation = Formulation.UNKNOWN,
    val chemistryBase: ChemistryBase = ChemistryBase.UNKNOWN,
    val finish: Finish = Finish.UNKNOWN,
    val coverage: Coverage = Coverage.NOT_APPLICABLE,
    val temperature: Temperature = Temperature.UNKNOWN,
    
    val colorHex: String,
    val colorFamily: ColorFamily = ColorFamily.UNKNOWN,
    val shadeName: String? = null,
    val imageUrl: String? = null,
    val notes: String? = null,
    /** Official manufacturer instructions for use. */
    val instructions: String? = null,
    val timestamp: Long = System.currentTimeMillis(),

    // --- Professional Inventory & Logistics ---
    val batchCode: String? = null,
    val openedDate: Long? = null,
    val paoMonths: Int? = null,
    val expiryDate: Long? = null,
    val price: Double? = null,
    val volume: String? = null,

    // --- Usage & Consumption Engine ---
    val isOpened: Boolean = false,
    val isFinished: Boolean = false,
    val isArchived: Boolean = false,
    val usageCount: Int = 0,
    val amountRemaining: Double? = null,
    val amountPerUse: Double? = null,
    
    // --- Algorithmic & AI Insights ---
    val heroIngredient: String? = null,
    val skinCompatibility: String? = null,
    val containsFragrance: Boolean? = null,
    val ingredients: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    
    // --- Sustainability & Eco-Impact ---
    val ecoScore: String? = null,
    val isVegan: Boolean? = null,
    val isCrueltyFree: Boolean? = null,
    val recyclingInstructions: String? = null,
    
    // --- Ritual Context ---
    val ritualPlacement: String? = null,
    val sourceType: InventorySource = InventorySource.USER_SCAN,
    val sourceName: String? = null,
    @Embedded(prefix = "provenance_") val provenance: Provenance? = null,
    val parentItemId: String? = null,
    val isHidden: Boolean = false,

    // --- Engine Enrichment (Calculated at Compile Time) ---
    val calculatedChemistryPhase: String? = null,
    val calculatedCielabL: Double? = null,
    val calculatedCielabA: Double? = null,
    val calculatedCielabB: Double? = null,
    val calculatedHueAngle: Double? = null,
    val blurhash: String? = null,
    val isSiliconeFree: Boolean? = null,
    val isParabenFree: Boolean? = null,
    val isSulfateFree: Boolean? = null,
    val heroActives: List<String> = emptyList(),
    val calculatedUnitPrice: Double? = null,
    val searchTokens: List<String> = emptyList(),

    // --- FDA & Clinical Safety ---
    val fdaRecallStatus: String? = null,
    val fdaAdverseEventCount: Int = 0,
    val fdaClinicalWarnings: List<String> = emptyList(),
    val fdaTopReactions: List<String> = emptyList(),
    val fdaActiveIngredients: List<String> = emptyList(),
    val fdaDataVerified: Boolean = false
)
