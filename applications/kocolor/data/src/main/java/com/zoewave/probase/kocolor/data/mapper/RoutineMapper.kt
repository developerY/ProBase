package com.zoewave.probase.kocolor.data.mapper

import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.kocolor.db.entity.RoutineEntity

fun RoutineEntity.toModel(): BeautyRoutine = BeautyRoutine(
    id = id,
    title = title,
    time = time,
    steps = steps,
    date = date
)

fun BeautyRoutine.toEntity(): RoutineEntity = RoutineEntity(
    id = id,
    title = title,
    time = time,
    steps = steps,
    date = date
)
