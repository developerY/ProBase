package com.zoewave.probase.kocolor.features.inventory.domain

import com.zoewave.probase.core.model.ritual.ClothingItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeAnalyticsEngine @Inject constructor() {

    fun computeAnalytics(items: List<ClothingItem>): WardrobeAnalytics {
        val totalCount = if (items.isNotEmpty()) items.size else 54


        // 1. DYNAMIC COLOR CLUSTERING (Level 2: Dynamic Wardrobe Palette)
        val validColors = items.filter { it.colorHex.isNotBlank() }
        val topColorsList = if (validColors.isNotEmpty()) {
            val colorGroups = validColors.groupBy {
                val cleanHex = if (it.colorHex.startsWith("#")) it.colorHex else "#${it.colorHex}"
                cleanHex.uppercase()
            }.toList().sortedByDescending { it.second.size }
            
            val totalColors = validColors.size
            val selectedClusters = if (totalColors < 10) colorGroups.take(3) else colorGroups.take(5)
            
            selectedClusters.map { (hex, group) ->
                ColorStat(
                    name = mapHexToSemanticName(hex), // Use controlled vocabulary
                    hex = hex,
                    count = group.size,
                    percentage = group.size.toFloat() / totalColors.toFloat()
                )
            }
        } else {
            // Cold start mock (Level 1 Standard Coordinates fallback)
            listOf(
                ColorStat("Sand Linen", "#D4C4B7", 19, 0.35f),
                ColorStat("Noir Espresso", "#2C2A29", 14, 0.25f),
                ColorStat("Olive Sage", "#7A8B76", 10, 0.18f),
                ColorStat("Warm Ochre", "#C18C5D", 6, 0.12f),
                ColorStat("Rose Accents", "#C28F90", 5, 0.10f)
            )
        }

        // 2. DYNAMIC NEUTRAL/WARM/COOL PERCENTAGES
        val neutralsPct = if (validColors.isNotEmpty()) {
            val neutralCount = validColors.count { isNeutral(it.colorHex) }
            ((neutralCount.toFloat() / validColors.size) * 100).toInt()
        } else 42
        
        val warmPct = if (validColors.isNotEmpty()) {
            val warmCount = validColors.count { isWarm(it.colorHex) && !isNeutral(it.colorHex) }
            ((warmCount.toFloat() / validColors.size) * 100).toInt()
        } else 31
        
        val coolPct = if (validColors.isNotEmpty()) {
            val coolCount = validColors.count { !isWarm(it.colorHex) && !isNeutral(it.colorHex) }
            ((coolCount.toFloat() / validColors.size) * 100).toInt()
        } else 27

        val mostWornGarments = if (items.isNotEmpty()) {
            items.sortedByDescending { it.usageCount }.take(3).map {
                GarmentSummary(it.internalId.toString(), it.name, it.usageCount)
            }
        } else {
            listOf(
                GarmentSummary("w_41", "Universal Khaki Button-Down", 18),
                GarmentSummary("w_48", "Camel Leather Boots", 14),
                GarmentSummary("w_38", "Warm Terracotta Crop", 11)
            )
        }

        val leastWornGarments = if (items.isNotEmpty()) {
            items.sortedBy { it.usageCount }.take(2).map {
                GarmentSummary(it.internalId.toString(), it.name, it.usageCount)
            }
        } else {
            listOf(
                GarmentSummary("w_24", "Midnight Crimson Wool Overcoat", 1),
                GarmentSummary("w_1", "Digital Lavender Sports Bra", 0)
            )
        }

        val insights = listOf(
            WardrobeInsight("A saturated cool accent would expand your color coverage.", isActionable = true),
            WardrobeInsight("A versatile mid-tone bottom would connect your activewear tops.", isActionable = true),
            WardrobeInsight("A lighter casual shoe would complement your neutral linen items.", isActionable = true)
        )

        val activeCount = if (items.isNotEmpty()) items.count { it.usageCount >= 5 } else 37
        val rarelyWornCount = if (items.isNotEmpty()) items.count { it.usageCount in 1..4 } else 14
        val neverWornCount = if (items.isNotEmpty()) items.count { it.usageCount == 0 } else 3

        val wearEvents = items.filter { it.usageCount > 0 }.mapIndexed { index, item ->
            WearEvent(
                timestamp = System.currentTimeMillis() - (index * 86400000L), // Stagger dates
                colorHex = item.colorHex,
                category = item.category.name,
                itemId = item.internalId,
                itemName = item.name,
                imageUrl = item.imageUrl
            )
        }.sortedBy { it.timestamp }

        val totalWears = items.sumOf { it.usageCount }
        val itemsWithPrice = items.count { it.price != null }
        val itemsWithColor = items.count { it.colorHex.isNotBlank() }
        val earliestWear = wearEvents.minOfOrNull { it.timestamp } ?: (System.currentTimeMillis() - 86400000L * 90)

        val analyticsCoverage = AnalyticsCoverage(
            inventoryCoverage = 1.0f,
            wearHistoryCoverage = if (totalCount > 0) activeCount.toFloat() / totalCount else 0f,
            financialCoverage = if (totalCount > 0) itemsWithPrice.toFloat() / totalCount else 0f,
            colorDataCoverage = if (totalCount > 0) itemsWithColor.toFloat() / totalCount else 0f,
            totalWearRecords = totalWears,
            periodStart = earliestWear,
            periodEnd = System.currentTimeMillis()
        )

        return WardrobeAnalytics(
            totalItems = totalCount,
            activeItems = activeCount,
            rarelyWornItems = rarelyWornCount,
            neverWornItems = neverWornCount,
            wearHistory = wearEvents,
            dna = WardrobeDna(
                primaryIdentity = "Neutral-led",
                temperatureBias = "Warm-biased",
                depth = "Medium depth",
                contrast = "Balanced contrast",
                chroma = "Low-to-medium chroma"
            ),
            colorDistribution = ColorDistribution(
                neutralsPct = neutralsPct,
                warmPct = warmPct,
                coolPct = coolPct,
                topColors = topColorsList
            ),
            categoryDistribution = CategoryDistribution(
                tops = 16,
                bottoms = 9,
                dresses = 8,
                shoes = 7,
                outerwear = 6,
                activewear = 8
            ),
            rotation = RotationAnalytics(
                frequentlyWorn = 18,
                inRotation = 29,
                rarelyWorn = 7,
                healthScore = 61,
                mostWorn = mostWornGarments,
                leastWorn = leastWornGarments
            ),
            versatility = VersatilityAnalytics(
                totalPossibleLooks = 312,
                mostVersatile = VersatileGarment(
                    id = "w_41",
                    name = "Universal Khaki Button-Down",
                    compatibleLooksCount = 18,
                    compatibleBottoms = 8,
                    compatibleShoes = 5,
                    compatibleOuterwear = 3
                )
            ),
            coverage = WardrobeCoverage(
                warmNeutrals = 0.85f,
                coolNeutrals = 0.50f,
                brightAccents = 0.30f,
                deepColors = 0.65f
            ),
            insights = insights,
            analyticsCoverage = analyticsCoverage
        )
    }

    private fun mapHexToSemanticName(hex: String): String {
        val cleanHex = hex.uppercase().removePrefix("#")
        return when {
            cleanHex.startsWith("FF5F") || cleanHex.startsWith("FFA0") || cleanHex.startsWith("FF7") -> "Coral"
            cleanHex.startsWith("EDD") || cleanHex.startsWith("F3E") || cleanHex.startsWith("FFF") || cleanHex.startsWith("FAF") -> "Ivory"
            cleanHex.startsWith("BDA") || cleanHex.startsWith("8B4") || cleanHex.startsWith("C5") || cleanHex.startsWith("D2") -> "Camel"
            cleanHex.startsWith("004") || cleanHex.startsWith("0B0") || cleanHex.startsWith("1E3") -> "Cobalt"
            cleanHex.startsWith("2C2") || cleanHex.startsWith("1F2") || cleanHex.startsWith("000") || cleanHex.startsWith("11") -> "Charcoal"
            cleanHex.startsWith("DC1") || cleanHex.startsWith("FF0") || cleanHex.startsWith("C7") || cleanHex.startsWith("E6") -> "Terracotta"
            else -> "Harmonic Accent"
        }
    }

    private fun isNeutral(hex: String): Boolean {
        return try {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
            hsv[1] < 0.15f || hsv[2] < 0.2f || hsv[2] > 0.9f
        } catch (e: Exception) { true }
    }

    private fun isWarm(hex: String): Boolean {
        return try {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
            val hue = hsv[0]
            hue in 0f..90f || hue in 300f..360f
        } catch (e: Exception) { true }
    }
}
