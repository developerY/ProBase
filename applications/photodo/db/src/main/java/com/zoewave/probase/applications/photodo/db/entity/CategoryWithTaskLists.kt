package com.zoewave.probase.applications.photodo.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithTaskLists(
    @Embedded
    val category: CategoryEntity,

    @Relation(
        parentColumn = "categoryId", // The PK in CategoryEntity
        entityColumn = "categoryId"  // The FK in TaskListEntity
    )
    val taskLists: List<TaskListEntity>
)