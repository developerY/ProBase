package com.zoewave.probase.applications.photodo.db.entity

import androidx.room3.Embedded
import androidx.room3.Relation

data class ProjectWithPhotos(
    @Embedded
    val project: ProjectEntity,

    @Relation(
        parentColumns = ["projectId"],
        entityColumns = ["projectId"]
    )
    val photos: List<PhotoEntity>
)
