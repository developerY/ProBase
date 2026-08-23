package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.PlaylistRepository
import com.zoewave.probase.kocolor.data.repository.RotationRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.model.playlist.ProjectedUsage
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GeneratePlaylistUseCaseTest {

    private val wardrobeRepository: WardrobeRepository = mockk()
    private val cosmeticRepository: CosmeticInventoryRepository = mockk()
    private val rotationRepository: RotationRepository = mockk()
    private val rotationScoringUseCase: RotationScoringUseCase = mockk()
    private val simulatorEngine: StyleSimulatorEngine = mockk()
    private val playlistRepository: PlaylistRepository = mockk()

    private lateinit var useCase: GeneratePlaylistUseCase

    @Before
    fun setup() {
        useCase = GeneratePlaylistUseCase(
            wardrobeRepository,
            cosmeticRepository,
            rotationRepository,
            rotationScoringUseCase,
            simulatorEngine,
            playlistRepository
        )
    }

    @Test
    fun `generateWeeklyPlaylist should apply anchors only to Day 1`() = runTest {
        val startDate = LocalDate.now()
        val anchors = listOf(ClothingItem(internalId = 1, remoteId = "garment_1", name = "Blazer", category = ClothingCategory.TOPS, colorHex = "#0000FF"))
        
        coEvery { wardrobeRepository.getAllClothing() } returns flowOf(emptyList())
        coEvery { cosmeticRepository.getAllCosmetics() } returns flowOf(emptyList())
        coEvery { rotationRepository.observeAllUsages() } returns flowOf(emptyList())
        coEvery { rotationScoringUseCase.calculateRotationPenalty(any(), any(), any(), any()) } returns 0.0
        coEvery { playlistRepository.savePlaylist(any(), any()) } just Runs
        
        val blueprint = StyleBlueprint("Rationale", listOf("w_1"), emptyList(), emptyList())
        coEvery { 
            simulatorEngine.architectStyleBlueprint(
                userIntent = any(),
                circadianContext = any(),
                routineCompleted = any(),
                wellnessScore = any(),
                weatherContext = any(),
                availableWardrobe = any(),
                availableCosmetics = any(),
                rotationScores = any(),
                fashionProfile = any(),
                anchoredClothing = any(),
                anchoredCosmetics = any(),
                apiKey = any(),
                modelName = any()
            ) 
        } returns blueprint

        useCase.generateWeeklyPlaylist(startDate, day1Anchors = anchors)

        // Verify day 1 was called with anchors
        coVerify { 
            simulatorEngine.architectStyleBlueprint(
                userIntent = any(),
                circadianContext = any(),
                routineCompleted = any(),
                wellnessScore = any(),
                weatherContext = any(),
                availableWardrobe = any(),
                availableCosmetics = any(),
                rotationScores = any(),
                fashionProfile = any(),
                anchoredClothing = anchors, // Anchors for Day 1
                anchoredCosmetics = emptyList(),
                apiKey = any(),
                modelName = any()
            )
        }
        
        // Verify subsequent days were called WITHOUT anchors
        coVerify(exactly = 6) { 
            simulatorEngine.architectStyleBlueprint(
                userIntent = any(),
                circadianContext = any(),
                routineCompleted = any(),
                wellnessScore = any(),
                weatherContext = any(),
                availableWardrobe = any(),
                availableCosmetics = any(),
                rotationScores = any(),
                fashionProfile = any(),
                anchoredClothing = emptyList(), // No anchors for subsequent days
                anchoredCosmetics = emptyList(),
                apiKey = any(),
                modelName = any()
            )
        }
    }
}
