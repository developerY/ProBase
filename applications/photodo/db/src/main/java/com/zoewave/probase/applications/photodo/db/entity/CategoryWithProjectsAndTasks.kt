package com.zoewave.probase.applications.photodo.db.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class CategoryWithProjectsAndTasks(
    @Embedded
    val category: CategoryEntity,

    @Relation(
        entity = ProjectEntity::class,
        parentColumns = ["categoryId"],
        entityColumns = ["categoryId"]
    )
    val projects: List<ProjectWithTasks>
)
