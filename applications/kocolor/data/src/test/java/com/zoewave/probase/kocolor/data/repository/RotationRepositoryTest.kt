package com.zoewave.probase.kocolor.data.repository

import android.content.Context
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zoewave.probase.kocolor.db.KoColorDatabase
import com.zoewave.probase.kocolor.db.dao.GarmentRotationDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RotationRepositoryTest {

    private lateinit var database: KoColorDatabase
    private lateinit var rotationDao: GarmentRotationDao
    private lateinit var repository: RotationRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KoColorDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        rotationDao = database.garmentRotationDao
        repository = RotationRepositoryImpl(database, rotationDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `commitOutfit success increments global and distinct items`() = runBlocking {
        val selectedOutfit = listOf("item1", "item2", "item3", "item1")

        repository.commitOutfit(selectedOutfit)

        val globalMetrics = rotationDao.getGlobalMetrics()
        assertNotNull(globalMetrics)
        assertEquals(1L, globalMetrics!!.totalOutfitsCommitted)

        val item1Usage = rotationDao.getUsageForProduct("item1")
        val item2Usage = rotationDao.getUsageForProduct("item2")

        assertEquals(1L, item1Usage?.useCount)
        assertEquals(1L, item2Usage?.useCount)
        assertNotNull(item1Usage?.lastUsedTimestamp)
    }

    @Test
    fun `commitOutfit increments counts correctly over multiple sessions`() = runBlocking {
        repository.commitOutfit(listOf("item1"))
        repository.commitOutfit(listOf("item1", "item2"))

        val metrics = rotationDao.getGlobalMetrics()
        assertEquals(2L, metrics?.totalOutfitsCommitted)

        val usage1 = rotationDao.getUsageForProduct("item1")
        val usage2 = rotationDao.getUsageForProduct("item2")

        assertEquals(2L, usage1?.useCount)
        assertEquals(1L, usage2?.useCount)
    }
}
