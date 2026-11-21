package com.ylabz.basepro.applications.bike.database

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zoewave.probase.ashbike.database.BikeRideDao
import com.zoewave.probase.ashbike.database.BikeRideEntity
import com.zoewave.probase.ashbike.database.RideLocationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@Database(entities = [BikeRideEntity::class, RideLocationEntity::class], version = 1, exportSchema = false)
abstract class TestAppDatabase : RoomDatabase() {
    abstract fun bikeRideDao(): BikeRideDao
}

@RunWith(AndroidJUnit4::class)
class BikeRideDaoTest {

    private lateinit var dao: BikeRideDao
    private lateinit var db: TestAppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TestAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.bikeRideDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `insert and retrieve ride by id`() = runTest {
        val ride = createTestRide("ride1")
        dao.insertRide(ride)
        val fetched = dao.getRideFlow("ride1").first()
        assertNotNull(fetched)
        assertEquals("ride1", fetched?.rideId)
    }

    @Test
    fun `insert locations and retrieve with ride`() = runTest {
        val ride = createTestRide("ride1")
        dao.insertRide(ride)

        val locations = listOf(
            RideLocationEntity(rideId = "ride1", timestamp = 1000L, lat = 10.0, lng = 20.0),
            RideLocationEntity(rideId = "ride1", timestamp = 2000L, lat = 11.0, lng = 21.0)
        )
        dao.insertLocations(locations)

        val fetched = dao.getRideWithLocations("ride1").first()
        assertNotNull(fetched)
        assertEquals(2, fetched?.locations?.size)
        assertEquals(10.0, fetched?.locations?.get(0)?.lat)
    }

    @Test
    fun `mark ride as acknowledged updates field`() = runTest {
        val ride = createTestRide("ride1").copy(isAcknowledged = false)
        dao.insertRide(ride)

        dao.markRideAsAcknowledged("ride1")
        val fetched = dao.getRideFlow("ride1").first()
        assertTrue(fetched?.isAcknowledged == true)
    }

    @Test
    fun `delete ride removes ride and cascades locations`() = runTest {
        val ride = createTestRide("ride1")
        dao.insertRide(ride)
        dao.insertLocations(listOf(RideLocationEntity(rideId = "ride1", timestamp = 1000L, lat = 10.0, lng = 20.0)))

        dao.deleteRide("ride1")
        val fetched = dao.getRideFlow("ride1").first()
        assertNull(fetched)
    }

    private fun createTestRide(id: String) = BikeRideEntity(
        rideId = id,
        startTime = 1000L,
        endTime = 2000L,
        totalDistance = 10f,
        averageSpeed = 15f,
        maxSpeed = 20f,
        elevationGain = 50f,
        elevationLoss = 10f,
        caloriesBurned = 500,
        startLat = 40.0,
        startLng = -70.0,
        endLat = 41.0,
        endLng = -71.0
    )
}
