package com.zoewave.probase.kocolor.data.mapper

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import org.junit.Test

class CosmeticMapperTest {

    @Test
    fun testCosmeticItemEntityToModelAndBack() {
        val entity = CosmeticItemEntity(
            internalId = 101L,
            remoteId = "cosmetic_101",
            name = "Velvet Lipstick",
            brand = "KoColor",
            macroCategory = MacroCategory.LIPS,
            microCategory = MicroCategory.LIPSTICK,
            colorHex = "#D4AF37",
            price = 35.0
        )

        val model = entity.toModel()
        assertThat(model.internalId).isEqualTo(101L)
        assertThat(model.name).isEqualTo("Velvet Lipstick")
        assertThat(model.macroCategory).isEqualTo(MacroCategory.LIPS)

        val convertedEntity = model.toEntity()
        assertThat(convertedEntity.internalId).isEqualTo(entity.internalId)
        assertThat(convertedEntity.name).isEqualTo(entity.name)
        assertThat(convertedEntity.macroCategory).isEqualTo(entity.macroCategory)
    }
}
