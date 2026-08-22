package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.zoewave.probase.kocolor.model.playlist.DailyPlanStatus
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "daily_style_plans",
    foreignKeys = [
        ForeignKey(
            entity = StylePlaylistEntity::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId")]
)
data class DailyStylePlanEntity(
    @PrimaryKey val planId: String = UUID.randomUUID().toString(),
    val playlistId: String,
    val targetDate: LocalDate,
    val status: DailyPlanStatus = DailyPlanStatus.PLANNED,
    val primaryContext: String,
    val baseOutfitProductIds: List<String>,
    val eveningRemixProductIds: List<String>? = null,
    val cosmeticProductIds: List<String>,
    @Embedded(prefix = "rationale_") val rationale: SelectionRationale,
    @Embedded(prefix = "evidence_") val evidence: SelectionEvidence,
    val isPinnedByUser: Boolean = false
)
