package com.zoewave.probase.applications.photodo.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithTasks(
    @Embedded
    val project: ProjectEntity,

    @Relation(
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val tasks: List<TaskEntity>
)
