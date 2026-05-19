package com.zoewave.probase.kocolor.data.engine

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.kocolor.model.ClothingCategory
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class WardrobeColorEngineTest {

    private lateinit var analyzer: WardrobeAnalyzer
    private lateinit var engine: WardrobeColorEngine

    @Before
    fun setup() {
        analyzer = mockk()
        engine = WardrobeColorEngine(analyzer)
    }

    @Test
    fun `processGarment should correctly map analytical data to ClothingItem`() {
        // Arrange
        val bitmap = mockk<Bitmap>()
        val baseItem = ClothingItem(
            name = "Test Item",
            category = ClothingCategory.TOPS
        )
        
        val signature = GarmentColorSignature(
            dominantHex = "#FF0000", // Bright Red (Warm Spring/Autumn)
            vibrantHex = "#FF5555",
            mutedHex = "#AA2222",
            secondaryPalette = listOf("#FF0000", "#00FF00"),
            allSwatches = listOf("#FF0000", "#00FF00")
        )
        
        every { analyzer.extractColorSignature(any()) } returns signature
        every { analyzer.calculateContrastLevel(any()) } returns "HIGH"

        // Act
        val result = engine.processGarment(bitmap, baseItem)

        // Assert
        assertThat(result.dominantHex).isEqualTo("#FF0000")
        assertThat(result.colorTemperature).isEqualTo(Undertone.WARM.name)
        assertThat(result.seasonalPalette).isAnyOf(SeasonalType.SPRING.name, SeasonalType.AUTUMN.name)
        assertThat(result.contrastLevel).isEqualTo("HIGH")
        assertThat(result.koColorGroup).contains(Undertone.WARM.name)
    }
    
    @Test
    fun `processGarment should handle neutral colors correctly`() {
        // Arrange
        val bitmap = mockk<Bitmap>()
        val baseItem = ClothingItem(name = "Neutral Item", category = ClothingCategory.OTHER)
        
        val signature = GarmentColorSignature(
            dominantHex = "#808080", // Gray
            allSwatches = listOf("#808080")
        )
        
        every { analyzer.extractColorSignature(any()) } returns signature
        every { analyzer.calculateContrastLevel(any()) } returns "LOW"

        // Act
        val result = engine.processGarment(bitmap, baseItem)

        // Assert
        assertThat(result.colorTemperature).isEqualTo(Undertone.NEUTRAL.name)
        assertThat(result.contrastLevel).isEqualTo("LOW")
    }
}
