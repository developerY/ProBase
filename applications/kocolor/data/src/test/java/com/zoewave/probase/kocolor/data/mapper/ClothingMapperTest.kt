package com.zoewave.probase.kocolor.data.mapper

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import org.junit.Test

class ClothingMapperTest {

    @Test
    fun testClothingItemEntityToModelAndBack() {
        val entity = ClothingItemEntity(
            internalId = 42L,
            remoteId = "clothing_42",
            name = "Silk Blouse",
            brand = "KoColor Studio",
            category = ClothingCategory.TOPS,
            colorHex = "#1E88E5",
            price = 120.0
        )

        val model = entity.toModel()
        assertThat(model.internalId).isEqualTo(42L)
        assertThat(model.name).isEqualTo("Silk Blouse")
        assertThat(model.category).isEqualTo(ClothingCategory.TOPS)
        assertThat(model.price).isEqualTo(120.0)

        val convertedEntity = model.toEntity()
        assertThat(convertedEntity.internalId).isEqualTo(entity.internalId)
        assertThat(convertedEntity.name).isEqualTo(entity.name)
        assertThat(convertedEntity.category).isEqualTo(entity.category)
    }
}
