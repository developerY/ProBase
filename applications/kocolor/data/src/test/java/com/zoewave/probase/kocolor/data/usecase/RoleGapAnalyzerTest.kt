package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import org.junit.Before
import org.junit.Test

class RoleGapAnalyzerTest {

    private lateinit var analyzer: RoleGapAnalyzer

    @Before
    fun setUp() {
        analyzer = RoleGapAnalyzer()
    }

    @Test
    fun determineRoleRequirements_emptyAnchors_requiresTopsBottomsShoes() {
        val reqs = analyzer.determineRoleRequirements(emptyList(), "Daily", 22f)
        val roles = reqs.map { it.role }

        assertThat(roles).contains("TOPS")
        assertThat(roles).contains("BOTTOMS")
        assertThat(roles).contains("SHOES")
    }

    @Test
    fun determineRoleRequirements_lockedDressAnchor_satisfiesUpperAndLowerBody() {
        val dress = ClothingItem(internalId = 10, name = "Summer Dress", category = ClothingCategory.DRESSES, colorHex = "#FFF")
        val reqs = analyzer.determineRoleRequirements(listOf(dress), "Daily", 22f)
        val roles = reqs.map { it.role }

        assertThat(roles).doesNotContain("TOPS")
        assertThat(roles).doesNotContain("BOTTOMS")
        assertThat(roles).contains("SHOES")
    }
}
