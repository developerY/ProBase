package com.zoewave.probase.applications.journal.model

import android.net.Uri

data class JournalEntry(
    val id: Long? = null,
    val title: String,
    val content: String,
    val timestamp: Long,
    val images: List<Uri>
)
