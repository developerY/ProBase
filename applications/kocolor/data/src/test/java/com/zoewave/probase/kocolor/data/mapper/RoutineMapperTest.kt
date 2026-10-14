package com.zoewave.probase.kocolor.data.mapper

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.RoutineTime
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import org.junit.Test

class RoutineMapperTest {

    @Test
    fun testRoutineEntityToModelAndBack() {
        val entity = RoutineEntity(
            id = 5L,
            title = "Morning Glow Ritual",
            time = RoutineTime.MORNING,
            steps = emptyList(),
            date = System.currentTimeMillis()
        )

        val model = entity.toModel()
        assertThat(model.id).isEqualTo(5L)
        assertThat(model.title).isEqualTo("Morning Glow Ritual")

        val convertedEntity = model.toEntity()
        assertThat(convertedEntity.id).isEqualTo(entity.id)
        assertThat(convertedEntity.title).isEqualTo(entity.title)
    }
}
