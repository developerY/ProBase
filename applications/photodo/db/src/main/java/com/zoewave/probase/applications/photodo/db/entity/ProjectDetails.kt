package com.zoewave.probase.applications.photodo.db.entity

import androidx.room.Embedded
import androidx.room.Relation

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
