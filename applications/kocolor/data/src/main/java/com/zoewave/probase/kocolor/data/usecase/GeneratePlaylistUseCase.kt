package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
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
import com.zoewave.probase.kocolor.model.playlist.ProjectedUsage
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
    suspend fun generateWeeklyPlaylist(
        startDate: LocalDate,
        day1Anchors: List<ClothingItem> = emptyList(),
        day1CosmeticAnchors: List<CosmeticItem> = emptyList()
    ): Result<String> {
        val playlistId = UUID.randomUUID().toString()
        val wardrobe = wardrobeRepository.getAllClothing().first()
        val cosmetics = cosmeticRepository.getAllCosmetics().first()
        val initialHistory = rotationRepository.observeAllUsages().first().associate { 
            it.productId to ProjectedUsage(it.useCount.toInt(), it.lastUsedTimestamp)
        }
        
        val projectedState = ProjectedRotationState(initialHistory)
        val dailyPlans = mutableListOf<DailyStylePlanEntity>()

        // 7-day loop
        for (i in 0 until 7) {
            val targetDate = startDate.plusDays(i.toLong())
            
            // Only apply user-funnel anchors to Day 1
            val currentAnchors = if (i == 0) day1Anchors else emptyList()
            val currentCosmeticAnchors = if (i == 0) day1CosmeticAnchors else emptyList()

            // Calculate rotation scores based on current PROJECTED state
            val rotationScores = wardrobe.associate { item ->
                val usage = projectedState.getUsage(item.remoteId!!)
                item.remoteId!! to rotationScoringUseCase.calculateRotationPenalty(
                    productId = item.remoteId!!,
                    category = item.category.name,
                    customUseCount = usage?.useCount,
                    customLastUsed = usage?.lastUsedTimestamp
                )
            }

            val context = StyleRequestContext(
                intent = "Weekly Rotation",
                weather = "Dynamic Weather", // Placeholder
                appearanceTelemetry = "Neutral", // Placeholder
                rotationScores = rotationScores,
                anchoredClothingIds = currentAnchors.map { "w_${it.internalId}" },
                anchoredCosmeticIds = currentCosmeticAnchors.map { "c_${it.internalId}" }
            )

            val dailyBlueprint = simulatorEngine.generateBlueprint(wardrobe, context)

            // COMPLETE State Forwarding: Simulate EVERY item picked into the projected state
            dailyBlueprint.selectedClothingIds.forEach { id ->
                val cleanId = id.removePrefix("w_")
                projectedState.simulateWear(cleanId, Instant.now()) 
            }

            dailyPlans.add(
                DailyStylePlanEntity(
                    playlistId = playlistId,
                    targetDate = targetDate,
                    primaryContext = "Planned Rotation",
                    baseOutfitProductIds = dailyBlueprint.selectedClothingIds,
                    cosmeticProductIds = dailyBlueprint.selectedCosmeticIds,
                    recommendedPalette = dailyBlueprint.recommendedPalette,
                    rationale = SelectionRationale(rotationReason = dailyBlueprint.rationale),
                    evidence = SelectionEvidence(
                        combinedFinalScore = 1.0,
                        scoringVersion = "rotation-v1.0"
                    )
                )
            )
        }

        val playlist = StylePlaylistEntity(
            playlistId = playlistId,
            generatedAt = Instant.now(),
            weekStartDate = startDate,
            engineVersion = "v1.0",
            scoringVersion = "rotation-v1.0",
            status = PlaylistStatus.GENERATED
        )

        playlistRepository.savePlaylist(playlist, dailyPlans)
        return Result.success(playlistId)
    }
}
