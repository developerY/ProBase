package com.zoewave.probase.applications.photodo.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithPhotos(
    @Embedded
    val project: ProjectEntity,

    @Relation(
        parentColumn = "projectId",
        entityColumn = "projectId"
    )
    val photos: List<PhotoEntity>
)
