package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.PlaylistRepository
import com.zoewave.probase.kocolor.data.repository.RotationRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.db.entity.DailyStylePlanEntity
import com.zoewave.probase.kocolor.db.entity.SelectionEvidence
import com.zoewave.probase.kocolor.db.entity.SelectionRationale
import com.zoewave.probase.kocolor.db.entity.StylePlaylistEntity
import com.zoewave.probase.kocolor.model.playlist.PlaylistStatus
import com.zoewave.probase.kocolor.model.playlist.ProjectedRotationState
import com.zoewave.probase.kocolor.model.playlist.UsageSnapshot
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneratePlaylistUseCase @Inject constructor(
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val rotationRepository: RotationRepository,
    private val rotationScoringUseCase: RotationScoringUseCase,
    private val simulatorEngine: StyleSimulatorEngine,
    private val playlistRepository: PlaylistRepository
) {
    suspend fun generateWeeklyPlaylist(startDate: LocalDate): Result<String> {
        val playlistId = UUID.randomUUID().toString()
        val wardrobe = wardrobeRepository.getAllClothing().first()
        val cosmetics = cosmeticRepository.getAllCosmetics().first()
        val initialHistory = rotationRepository.observeAllUsages().first().associate { 
            it.productId to UsageSnapshot(it.useCount.toInt(), it.lastUsedTimestamp)
        }
        
        val projectedState = ProjectedRotationState(initialHistory)
        val dailyPlans = mutableListOf<DailyStylePlanEntity>()

        // 7-day loop
        for (i in 0 until 7) {
            val targetDate = startDate.plusDays(i.toLong())
            val dailyBlueprint = simulatorEngine.architectLocalBlueprint(
                userIntent = "Weekly Rotation", // In a real app, this would come from the calendar
                availableWardrobe = wardrobe,
                availableCosmetics = cosmetics
            )

            // Update projected state for subsequent days
            dailyBlueprint.selectedClothingIds.forEach { id ->
                // Strip the "w_" prefix added by the engine if necessary
                val cleanId = id.removePrefix("w_")
                projectedState.simulateWear(cleanId, Instant.now()) // Approximation
            }

            dailyPlans.add(
                DailyStylePlanEntity(
                    playlistId = playlistId,
                    targetDate = targetDate,
                    primaryContext = "Planned Rotation",
                    baseOutfitProductIds = dailyBlueprint.selectedClothingIds,
                    cosmeticProductIds = dailyBlueprint.selectedCosmeticIds,
                    rationale = SelectionRationale(rotationReason = dailyBlueprint.rationale),
                    evidence = SelectionEvidence(combinedFinalScore = 1.0)
                )
            )
        }

        val playlist = StylePlaylistEntity(
            playlistId = playlistId,
            generatedAt = Instant.now(),
            weekStartDate = startDate,
            engineVersion = "v1.0",
            scoringVersion = "v1.0",
            status = PlaylistStatus.GENERATED
        )

        playlistRepository.savePlaylist(playlist, dailyPlans)
        return Result.success(playlistId)
    }
}
