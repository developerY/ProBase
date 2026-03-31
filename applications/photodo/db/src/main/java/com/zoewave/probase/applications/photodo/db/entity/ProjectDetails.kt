package com.zoewave.probase.applications.photodo.db.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class ProjectDetails(
    @Embedded
    val project: ProjectEntity,

    @Relation(
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val tasks: List<TaskEntity>,

    @Relation(
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val photos: List<PhotoEntity>,

    @Relation(
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val expenses: List<ExpenseEntity>
)
