package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.kocolor.db.KoColorDatabase
import com.zoewave.probase.kocolor.db.dao.PlaylistDao
import com.zoewave.probase.kocolor.db.entity.DailyStylePlanEntity
import com.zoewave.probase.kocolor.model.playlist.DailyPlanStatus
import com.zoewave.probase.kocolor.db.entity.SelectionEvidence
import com.zoewave.probase.kocolor.db.entity.SelectionRationale
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class PlaylistRepositoryTest {

    private val database: KoColorDatabase = mockk(relaxed = true)
    private val playlistDao: PlaylistDao = mockk(relaxed = true)
    private lateinit var repository: PlaylistRepositoryImpl

    @Before
    fun setup() {
        repository = PlaylistRepositoryImpl(database, playlistDao)
    }

    @Test
    fun `commitDailyOutfit delegates to database transaction method`() = runTest {
        val planId = "plan2"
        val productIds = listOf("p1", "p2")

        repository.commitDailyOutfit(planId, productIds)

        coVerify { database.commitDailyStylePlan(planId, productIds) }
    }
}
