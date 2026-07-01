package com.zoewave.probase.core.database

import androidx.room3.ColumnInfo
import androidx.room3.ColumnTypeConverters
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.core.database.converter.Converters

@Entity(tableName = "basepro_table")
@ColumnTypeConverters(Converters::class)
data class BaseProEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val todoId: Int = 0,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "image_path") val imgPath: String? = null
)
