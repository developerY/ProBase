package com.zoewave.probase.applications.photodo.db.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class CategoryWithProjectsAndTasks(
    @Embedded
    val category: CategoryEntity,

    @Relation(
        entity = ProjectEntity::class,
        parentColumn = "categoryId",
        entityColumn = "categoryId"
    )
    val projects: List<ProjectWithTasks>
)
