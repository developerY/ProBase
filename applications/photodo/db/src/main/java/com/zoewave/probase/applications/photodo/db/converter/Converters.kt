package com.zoewave.probase.applications.photodo.db.converter

import android.net.Uri
import androidx.core.net.toUri
import androidx.room3.ColumnTypeConverter
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.model.Category
import kotlin.time.Instant


class PhotoDoConverters {

    // --- Date/Time ---
    @ColumnTypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @ColumnTypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }

    // --- URIs ---
    @ColumnTypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @ColumnTypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.toUri()
    }

    // --- String Lists ---
    @ColumnTypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(separator = ",")
    }

    @ColumnTypeConverter
    fun toStringList(data: String?): List<String>? {
        return data?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    }
}

// This function converts the database object to the UI model
fun CategoryEntity.toDomainModel(): Category {
    return Category(
        categoryId = this.categoryId,
        name = this.name,
        description = this.description
    )
}
