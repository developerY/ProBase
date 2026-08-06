package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class PackStatus {
    AVAILABLE, 
    DOWNLOADING, 
    VERIFIED, 
    INSTALLED, 
    UPDATE_AVAILABLE, 
    DEPRECATED, 
    REMOVED
}

@Entity(tableName = "installed_packs")
data class InstalledPackEntity(
    @PrimaryKey val packId: String,
    val name: String,
    val description: String,
    val version: Int,
    val status: PackStatus,
    val itemCount: Int,
    val sizeBytes: Long = 0L,
    val hash: String? = null,
    val packageHash: String? = null,
    val heroImageUrl: String? = null,
    val expiresAt: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
