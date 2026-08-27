package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.color.ColorHarmonyEngine
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditLogger
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SelectionCascadeTest {

    private val colorEngine = ColorHarmonyEngine()
    private val roleGapAnalyzer = RoleGapAnalyzer()
    private val rotationScoringUseCase = mockk<RotationScoringUseCase>(relaxed = true)
    private val repository = mockk<WardrobeRepository>(relaxed = true)
    private val auditLogger = mockk<StyleAuditLogger>(relaxed = true)

    private lateinit var engine: DeterministicContextEngine

    @Before
    fun setup() {
        engine = DeterministicContextEngine(
            colorEngine,
            roleGapAnalyzer,
            rotationScoringUseCase,
            repository,
            auditLogger
        )
    }

    @Test
    fun `cascade test - forced heavy coat in summer bypasses elimination`() = runTest {
        val coat = ClothingItem(internalId = 1, remoteId = "w_1", name = "Heavy Wool Coat", category = ClothingCategory.OUTERWEAR, colorHex = "#1F2937")
        val tshirt = ClothingItem(internalId = 2, remoteId = "w_2", name = "Linen Tee", category = ClothingCategory.TOPS, colorHex = "#FFFFFF")
        val inventory = listOf(coat, tshirt)

        val context = StyleRequestContext(
            intent = "Summer Party",
            weather = "Temp: 32C",
            weatherTempC = 32f,
            lockedConstraints = listOf(UserConstraint(itemId = "w_1", category = "OUTERWEAR", tier = SelectionTier.FORCED))
        )

        val state = engine.generateSelectionState(inventory, context.lockedConstraints, context)

        assertThat(state.activeAnchors).contains(coat)
        assertThat(state.missingRoleRequirements.map { it.role }).contains(ClothingCategory.TOPS.name)
    }

    @Test
    fun `cascade test - locking neutral trousers preserves hue vector and calculates gap roles`() = runTest {
        val trousers = ClothingItem(internalId = 1, remoteId = "w_1", name = "Charcoal Trousers", category = ClothingCategory.BOTTOMS, colorHex = "#374151")
        val jacket = ClothingItem(internalId = 2, remoteId = "w_2", name = "Burgundy Jacket", category = ClothingCategory.OUTERWEAR, colorHex = "#800020")
        val shirt = ClothingItem(internalId = 3, remoteId = "w_3", name = "White Shirt", category = ClothingCategory.TOPS, colorHex = "#FFFFFF")
        val inventory = listOf(trousers, jacket, shirt)

        // Step 1: Lock Trousers
        val context1 = StyleRequestContext(
            intent = "Business Casual",
            occasion = "Office",
            lockedConstraints = listOf(UserConstraint(itemId = "w_1", category = "BOTTOMS", tier = SelectionTier.LOCKED))
        )

        val state1 = engine.generateSelectionState(inventory, context1.lockedConstraints, context1)

        assertThat(state1.activeAnchors).contains(trousers)
        val missingRoles1 = state1.missingRoleRequirements.map { it.role }
        assertThat(missingRoles1).contains(ClothingCategory.TOPS.name)
        assertThat(missingRoles1).contains(ClothingCategory.SHOES.name)

        // Step 2: Lock Burgundy Jacket as second anchor
        val context2 = StyleRequestContext(
            intent = "Business Casual",
            occasion = "Office",
            lockedConstraints = listOf(
                UserConstraint(itemId = "w_1", category = "BOTTOMS", tier = SelectionTier.LOCKED),
                UserConstraint(itemId = "w_2", category = "OUTERWEAR", tier = SelectionTier.LOCKED)
            )
        )

        val state2 = engine.generateSelectionState(inventory, context2.lockedConstraints, context2)

        assertThat(state2.activeAnchors).containsExactly(trousers, jacket)
        val missingRoles2 = state2.missingRoleRequirements.map { it.role }
        assertThat(missingRoles2).contains(ClothingCategory.TOPS.name)
        assertThat(missingRoles2).contains(ClothingCategory.SHOES.name)
        assertThat(missingRoles2).doesNotContain(ClothingCategory.OUTERWEAR.name)
    }
}
