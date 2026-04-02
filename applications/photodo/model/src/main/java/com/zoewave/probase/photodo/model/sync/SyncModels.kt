package com.zoewave.probase.photodo.model.sync

import kotlinx.serialization.Serializable

@Serializable
data class SyncCategory(
    val id: Long, // Use the Phone's ID!
    val name: String,
    val projects: List<SyncProject>
)

@Serializable
data class SyncProject(
    val id: Long,
    val name: String,
    val totalBudget: Double,
    val spentAmount: Double,
    val tasks: List<SyncTask>,
    val photoCount: Int // Just the integer! 4 bytes of data.
)

@Serializable
data class SyncTask(
    val id: Long,
    val title: String,
    val isCompleted: Boolean
)
