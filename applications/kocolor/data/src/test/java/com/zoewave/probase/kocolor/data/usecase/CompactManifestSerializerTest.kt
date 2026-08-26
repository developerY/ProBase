package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import org.junit.Test

class CompactManifestSerializerTest {

    private val serializer = CompactManifestSerializer()

    @Test
    fun `serialize should produce MINIMAL output`() {
        val item = ClothingItem(internalId = 55, name = "Trench", category = ClothingCategory.TOPS, colorHex = "#B8A992")
        val result = serializer.serialize(listOf(item), SerializationDetailLevel.MINIMAL)

        assertThat(result).isEqualTo("[w_55|TOPS|Trench|#B8A992]")
    }

    @Test
    fun `serialize should produce EXPANDED output`() {
        val item = ClothingItem(
            internalId = 55, 
            name = "Trench", 
            category = ClothingCategory.TOPS, 
            colorHex = "#B8A992",
            colorTemperature = "Warm",
            seasonalPalette = "Deep",
            material = "Cotton"
        )
        val result = serializer.serialize(listOf(item), SerializationDetailLevel.EXPANDED)

        assertThat(result).isEqualTo("[w_55|TOPS|Trench|#B8A992|Warm|Deep|Cotton]")
    }
}
