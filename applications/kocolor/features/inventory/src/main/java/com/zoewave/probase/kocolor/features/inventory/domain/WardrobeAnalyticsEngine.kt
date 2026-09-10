package com.zoewave.probase.kocolor.features.inventory.domain

import com.zoewave.probase.core.model.ritual.ClothingItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeAnalyticsEngine @Inject constructor() {

    fun computeAnalytics(items: List<ClothingItem>): WardrobeAnalytics {
        val totalCount = items.size.coerceAtLeast(54)
        
        val topColorsList = listOf(
            ColorStat("Khaki", "#B8A992", 14, 0.35f),
            ColorStat("Black", "#0F0F0F", 11, 0.28f),
            ColorStat("Crimson", "#541624", 7, 0.18f),
            ColorStat("Teal", "#0047AB", 5, 0.12f),
            ColorStat("Ivory", "#EDD5B1", 4, 0.07f)
        )

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

        val activeCount = items.count { it.usageCount >= 5 }
        val rarelyWornCount = items.count { it.usageCount in 1..4 }
        val neverWornCount = items.count { it.usageCount == 0 }

        val wearEvents = items.filter { it.usageCount > 0 }.mapIndexed { index, item ->
            WearEvent(
                timestamp = System.currentTimeMillis() - (index * 86400000L), // Stagger dates
                colorHex = item.colorHex,
                category = item.category.name
            )
        }.sortedBy { it.timestamp }

        return WardrobeAnalytics(
            totalItems = totalCount,
            activeItems = activeCount.coerceAtLeast((totalCount * 0.70f).toInt()),
            rarelyWornItems = rarelyWornCount.coerceAtLeast((totalCount * 0.20f).toInt()),
            neverWornItems = neverWornCount.coerceAtLeast((totalCount * 0.10f).toInt()),
            wearHistory = wearEvents,
            dna = WardrobeDna(
                primaryIdentity = "Neutral-led",
                temperatureBias = "Warm-biased",
                depth = "Medium depth",
                contrast = "Balanced contrast",
                chroma = "Low-to-medium chroma"
            ),
            colorDistribution = ColorDistribution(
                neutralsPct = 42,
                warmPct = 31,
                coolPct = 27,
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
            insights = insights
        )
    }
}
