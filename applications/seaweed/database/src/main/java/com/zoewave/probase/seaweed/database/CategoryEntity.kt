package com.zoewave.probase.seaweed.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.seaweed.model.Category
import com.zoewave.probase.seaweed.model.SpendingType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val defaultType: SpendingType,
    val icon: String? = null
)

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    defaultType = defaultType,
    icon = icon
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    defaultType = defaultType,
    icon = icon
)
