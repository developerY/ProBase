package com.zoewave.probase.applications.photodo.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithProjects(
    @Embedded
    val category: CategoryEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryId"
    )
    val projects: List<ProjectEntity>
)
