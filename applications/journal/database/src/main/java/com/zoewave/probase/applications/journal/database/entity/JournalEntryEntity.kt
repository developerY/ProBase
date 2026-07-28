package com.zoewave.probase.applications.journal.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String,
    val content: String,
    val timestamp: Long,
    val images: String // Stored as a comma-separated string of Uris
)
