package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.color.ColorHarmonyEngine
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.telemetry.PruningRecord
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeterministicContextEngineTest {

    private val colorEngine = ColorHarmonyEngine()
    private val roleGapAnalyzer = RoleGapAnalyzer()
    private val rotationScoringUseCase = mockk<RotationScoringUseCase>(relaxed = true)
    private val repository = mockk<WardrobeRepository>(relaxed = true)
    private val auditLogger = mockk<StyleAuditLogger>(relaxed = true)

    private lateinit var engine: DeterministicContextEngine

    @Before
    fun setUp() {
        io.mockk.mockkStatic(android.util.Log::class)
        io.mockk.every { android.util.Log.d(any(), any()) } returns 0
        io.mockk.every { android.util.Log.i(any(), any()) } returns 0
        io.mockk.every { android.util.Log.w(any(), any<String>()) } returns 0
        io.mockk.every { android.util.Log.e(any(), any<String>()) } returns 0

        engine = DeterministicContextEngine(
            colorEngine,
            roleGapAnalyzer,
            rotationScoringUseCase,
            repository,
            auditLogger
        )
    }

    @Test
    fun trueAuditFunnel_tracksSequentialCounts() = runTest {
        val hotCoat = ClothingItem(internalId = 1, name = "Wool Overcoat", category = ClothingCategory.OUTERWEAR, colorHex = "#000")
        val top = ClothingItem(internalId = 2, name = "Tee", category = ClothingCategory.TOPS, colorHex = "#000")
        val bottom = ClothingItem(internalId = 3, name = "Jeans", category = ClothingCategory.BOTTOMS, colorHex = "#000")
        val shoes = ClothingItem(internalId = 4, name = "Sneakers", category = ClothingCategory.SHOES, colorHex = "#000")
        val inventory = listOf(hotCoat, top, bottom, shoes)

        val context = StyleRequestContext(intent = "work", weather = "Clear, Temp: 28°C")

        val recordSlot = slot<PruningRecord>()
        every { auditLogger.logDeterministicPruning(any(), capture(recordSlot)) } returns Unit

        engine.generateSelectionState(inventory, emptyList(), context)

        val record = recordSlot.captured
        assertThat(record.initialCount).isEqualTo(4)
        assertThat(record.afterWeather).isLessThan(record.initialCount)
        assertThat(record.afterRotation).isAtMost(record.afterWeather)
        assertThat(record.finalEligible).isEqualTo(record.afterRotation)
    }
}
