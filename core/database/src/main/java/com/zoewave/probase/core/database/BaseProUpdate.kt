package com.zoewave.probase.core.database

import androidx.room3.ColumnInfo

data class BaseProUpdate(
    @ColumnInfo(name = "id") val todoId: Int,
)
