package com.zoewave.probase.kocolor.features.inventory.domain

import com.zoewave.probase.core.model.ritual.ClothingItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeAnalyticsEngine @Inject constructor() {

    fun computeAnalytics(items: List<ClothingItem>): WardrobeAnalytics {
        val totalCount = items.size


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
            emptyList()
        }

        // 2. DYNAMIC NEUTRAL/WARM/COOL PERCENTAGES
        val neutralsPct = if (validColors.isNotEmpty()) {
            val neutralCount = validColors.count { isNeutral(it.colorHex) }
            ((neutralCount.toFloat() / validColors.size) * 100).toInt()
        } else 0
        
        val warmPct = if (validColors.isNotEmpty()) {
            val warmCount = validColors.count { isWarm(it.colorHex) && !isNeutral(it.colorHex) }
            ((warmCount.toFloat() / validColors.size) * 100).toInt()
        } else 0
        
        val coolPct = if (validColors.isNotEmpty()) {
            val coolCount = validColors.count { !isWarm(it.colorHex) && !isNeutral(it.colorHex) }
            ((coolCount.toFloat() / validColors.size) * 100).toInt()
        } else 0

        val mostWornGarments = items.sortedByDescending { it.usageCount }.take(3).map {
            GarmentSummary(it.internalId.toString(), it.name, it.usageCount)
        }

        val leastWornGarments = items.sortedBy { it.usageCount }.take(2).map {
            GarmentSummary(it.internalId.toString(), it.name, it.usageCount)
        }

        val insights = emptyList<WardrobeInsight>()

        val activeCount = items.count { it.usageCount >= 5 }
        val rarelyWornCount = items.count { it.usageCount in 1..4 }
        val neverWornCount = items.count { it.usageCount == 0 }

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

        val rotationHealthScore = if (totalCount > 0) {
            val rarelyWornRatio = rarelyWornCount.toFloat() / totalCount
            val neverWornRatio = neverWornCount.toFloat() / totalCount
            val deduction = ((rarelyWornRatio * 0.5f) + neverWornRatio) * 100
            (100 - deduction).toInt().coerceIn(0, 100)
        } else 0

        val categoryDist = CategoryDistribution(
            tops = items.count { it.category.name.equals("TOPS", ignoreCase = true) },
            bottoms = items.count { it.category.name.equals("BOTTOMS", ignoreCase = true) },
            dresses = items.count { it.category.name.equals("DRESSES", ignoreCase = true) },
            shoes = items.count { it.category.name.equals("SHOES", ignoreCase = true) },
            outerwear = items.count { it.category.name.equals("OUTERWEAR", ignoreCase = true) },
            activewear = items.count { it.category.name.equals("ACTIVEWEAR", ignoreCase = true) }
        )

        val harmonyScore = if (totalCount == 0 || validColors.isEmpty()) {
            null
        } else {
            val uniqueColorsCount = validColors.map { it.colorHex.uppercase() }.distinct().size
            val neutralRatio = validColors.count { isNeutral(it.colorHex) }.toFloat() / validColors.size
            (0.75f + (neutralRatio * 0.15f) + (uniqueColorsCount.coerceAtMost(5) * 0.02f)).coerceIn(0.50f, 0.98f)
        }

        return WardrobeAnalytics(
            totalItems = totalCount,
            activeItems = activeCount,
            rarelyWornItems = rarelyWornCount,
            neverWornItems = neverWornCount,
            harmonyScore = harmonyScore,
            wearHistory = wearEvents,
            dna = WardrobeDna(
                primaryIdentity = if (validColors.isEmpty()) "Initializing" else if (neutralsPct > 50) "Neutral-led" else "Color-led",
                temperatureBias = if (validColors.isEmpty()) "-" else if (warmPct > coolPct) "Warm-biased" else "Cool-biased",
                depth = if (validColors.isEmpty()) "-" else "Mixed depth",
                contrast = if (validColors.isEmpty()) "-" else "Balanced contrast",
                chroma = if (validColors.isEmpty()) "-" else "Dynamic chroma"
            ),
            colorDistribution = ColorDistribution(
                neutralsPct = neutralsPct,
                warmPct = warmPct,
                coolPct = coolPct,
                topColors = topColorsList
            ),
            categoryDistribution = categoryDist,
            rotation = RotationAnalytics(
                frequentlyWorn = activeCount,
                inRotation = activeCount + rarelyWornCount,
                rarelyWorn = rarelyWornCount,
                healthScore = rotationHealthScore,
                mostWorn = mostWornGarments,
                leastWorn = leastWornGarments
            ),
            versatility = VersatilityAnalytics(
                totalPossibleLooks = 0,
                mostVersatile = VersatileGarment(
                    id = "",
                    name = "Pending Analysis",
                    compatibleLooksCount = 0,
                    compatibleBottoms = 0,
                    compatibleShoes = 0,
                    compatibleOuterwear = 0
                )
            ),
            coverage = WardrobeCoverage(
                warmNeutrals = 0f,
                coolNeutrals = 0f,
                brightAccents = 0f,
                deepColors = 0f
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
