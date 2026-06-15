package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.core.model.ritual.RoutineStep
import com.zoewave.probase.core.model.ritual.RoutineTime

@Entity(tableName = "beauty_routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val time: RoutineTime,
    val steps: List<RoutineStep>,
    val date: Long
)
