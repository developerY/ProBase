package com.zoewave.probase.kocolor.data.usecase

import android.content.Context
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.PlaylistRepositoryImpl
import com.zoewave.probase.kocolor.data.repository.RotationRepositoryImpl
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.db.KoColorDatabase
import com.zoewave.probase.kocolor.db.entity.*
import com.zoewave.probase.kocolor.model.playlist.PlaylistStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class ProjectedVsCommittedIsolationTest {

    private lateinit var db: KoColorDatabase
    private lateinit var useCase: GeneratePlaylistUseCase
    
    private val wardrobeRepository: WardrobeRepository = mockk()
    private val cosmeticRepository: CosmeticInventoryRepository = mockk()
    private val simulatorEngine: StyleSimulatorEngine = mockk()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KoColorDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val rotationRepository = RotationRepositoryImpl(db, db.garmentRotationDao)
        val playlistRepository = PlaylistRepositoryImpl(db, db.playlistDao)
        val rotationScoringUseCase = RotationScoringUseCase(rotationRepository)

        useCase = GeneratePlaylistUseCase(
            wardrobeRepository,
            cosmeticRepository,
            rotationRepository,
            rotationScoringUseCase,
            simulatorEngine,
            playlistRepository
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `playlist generation and commit cycle maintains strict isolation and idempotency`() = runTest {
        // 1. Setup Mock Data
        val product1 = "garment_1"
        coEvery { wardrobeRepository.getAllClothing() } returns flowOf(listOf(
            ClothingItem(internalId = 1, remoteId = product1, name = "Shirt", category = ClothingCategory.TOPS, colorHex = "#FFFFFF")
        ))
        coEvery { cosmeticRepository.getAllCosmetics() } returns flowOf(emptyList())
        coEvery { 
            simulatorEngine.generateBlueprint(any(), any()) 
        } returns StyleBlueprint("Rationale", listOf("w_1"), emptyList(), emptyList())

        // 2. Generate Playlist
        val playlistId = useCase.generateWeeklyPlaylist(LocalDate.now()).getOrThrow()

        // 3. ASSERT: V1 Historical Memory is completely unchanged after generation
        val metrics = db.garmentRotationDao.getGlobalMetrics()
        assertEquals(0L, metrics?.totalOutfitsCommitted ?: 0L)
        val usages = db.garmentRotationDao.getUsagesForProducts(listOf(product1))
        assertEquals(0, usages.size)

        // 4. Fire commitDailyStylePlan for Day 1
        val playlist = db.playlistDao.getPlaylistWithDays(playlistId)!!
        val day1Plan = playlist.dailyPlans.first()
        
        db.commitDailyStylePlan(day1Plan.planId, listOf(product1))

        // 5. ASSERT: V1 Historical Memory is incremented by exactly 1
        val updatedMetrics = db.garmentRotationDao.getGlobalMetrics()
        assertEquals(1L, updatedMetrics?.totalOutfitsCommitted)
        val updatedUsages = db.garmentRotationDao.getUsagesForProducts(listOf(product1))
        assertEquals(1L, updatedUsages.first().useCount)

        // 6. Fire commit again (Simulate Double-Tap)
        db.commitDailyStylePlan(day1Plan.planId, listOf(product1))

        // 7. ASSERT: Idempotency maintained - counts remain at 1
        val finalMetrics = db.garmentRotationDao.getGlobalMetrics()
        assertEquals(1L, finalMetrics?.totalOutfitsCommitted)
        val finalUsages = db.garmentRotationDao.getUsagesForProducts(listOf(product1))
        assertEquals(1L, finalUsages.first().useCount)
    }
}
