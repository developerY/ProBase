package com.zoewave.probase.applications.photodo.db.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class ProjectDetails(
    @Embedded
    val project: ProjectEntity,

    @Relation(
        parentColumns = ["projectId"],
        entityColumns = ["projectId"]
    )
    val tasks: List<TaskEntity>,

    @Relation(
        parentColumns = ["projectId"],
        entityColumns = ["projectId"]
    )
    val photos: List<PhotoEntity>,

    @Relation(
        parentColumns = ["projectId"],
        entityColumns = ["projectId"]
    )
    val expenses: List<ExpenseEntity>
)
