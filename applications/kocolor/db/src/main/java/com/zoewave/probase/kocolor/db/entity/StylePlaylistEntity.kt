package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.kocolor.model.playlist.PlaylistStatus
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "style_playlists")
data class StylePlaylistEntity(
    @PrimaryKey val playlistId: String = UUID.randomUUID().toString(),
    val generatedAt: Instant,
    val weekStartDate: LocalDate,
    val engineVersion: String,
    val scoringVersion: String,
    val status: PlaylistStatus
)
