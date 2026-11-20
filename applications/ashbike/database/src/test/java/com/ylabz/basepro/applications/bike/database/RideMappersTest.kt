package com.ylabz.basepro.applications.bike.database

import com.zoewave.ashbike.model.bike.BikeRide
import com.zoewave.ashbike.model.bike.LocationPoint
import com.zoewave.probase.ashbike.database.BikeRideEntity
import com.zoewave.probase.ashbike.database.RideLocationEntity
import com.zoewave.probase.ashbike.database.RideWithLocations
import com.zoewave.probase.ashbike.database.mapper.toBikeRide
import com.zoewave.probase.ashbike.database.mapper.toEntity
import com.zoewave.probase.ashbike.database.mapper.toLocationPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class RideMappersTest {

    @Test
    fun `map RideLocationEntity to LocationPoint`() {
        val entity = RideLocationEntity(
            id = 1L,
            rideId = "ride_1",
            timestamp = 1000L,
            lat = 40.0,
            lng = -70.0,
            elevation = 10f
        )

        val point = entity.toLocationPoint()

        assertEquals(40.0, point.latitude, 0.001)
        assertEquals(-70.0, point.longitude, 0.001)
        assertEquals(10f, point.altitude)
        assertEquals(1000L, point.timestamp)
    }

    @Test
    fun `map BikeRideEntity to BikeRide`() {
        val id = UUID.randomUUID().toString()
        val entity = BikeRideEntity(
            rideId = id,
            startTime = 1000L,
            endTime = 2000L,
            totalDistance = 15f,
            averageSpeed = 20f,
            maxSpeed = 30f,
            elevationGain = 100f,
            elevationLoss = 50f,
            caloriesBurned = 500,
            startLat = 40.0,
            startLng = -70.0,
            endLat = 41.0,
            endLng = -71.0,
            isHealthDataSynced = true,
            isAcknowledged = true,
            healthConnectRecordId = "health_123"
        )

        val model = entity.toBikeRide()

        assertEquals(id, model.rideId)
        assertEquals(1000L, model.startTime)
        assertEquals(15f, model.totalDistance)
        assertTrue(model.isHealthDataSynced)
        assertTrue(model.isAcknowledged)
        assertEquals("health_123", model.healthConnectRecordId)
        assertTrue(model.locations.isEmpty())
    }

    @Test
    fun `map RideWithLocations to BikeRide`() {
        val id = "ride_1"
        val entity = BikeRideEntity(
            rideId = id,
            startTime = 1000L,
            endTime = 2000L,
            totalDistance = 15f,
            averageSpeed = 20f,
            maxSpeed = 30f,
            elevationGain = 100f,
            elevationLoss = 50f,
            caloriesBurned = 500,
            startLat = 40.0,
            startLng = -70.0,
            endLat = 41.0,
            endLng = -71.0
        )

        val locations = listOf(
            RideLocationEntity(id = 1L, rideId = id, timestamp = 1000L, lat = 40.0, lng = -70.0),
            RideLocationEntity(id = 2L, rideId = id, timestamp = 2000L, lat = 41.0, lng = -71.0)
        )

        val rideWithLocations = RideWithLocations(entity, locations)
        val model = rideWithLocations.toBikeRide()

        assertEquals(id, model.rideId)
        assertEquals(2, model.locations.size)
        assertEquals(40.0, model.locations[0].latitude, 0.001)
        assertEquals(41.0, model.locations[1].latitude, 0.001)
    }

    @Test
    fun `map BikeRide to Entity`() {
        val id = "ride_1"
        val model = BikeRide(
            rideId = id,
            startTime = 1000L,
            endTime = 2000L,
            totalDistance = 15f,
            averageSpeed = 20f,
            maxSpeed = 30f,
            elevationGain = 100f,
            elevationLoss = 50f,
            caloriesBurned = 500,
            startLat = 40.0,
            startLng = -70.0,
            endLat = 41.0,
            endLng = -71.0,
            avgHeartRate = null,
            maxHeartRate = null,
            isHealthDataSynced = false,
            isAcknowledged = false,
            healthConnectRecordId = null,
            weatherCondition = null,
            rideType = null,
            notes = null,
            rating = null,
            bikeId = null,
            batteryStart = null,
            batteryEnd = null,
            locations = listOf(LocationPoint(40.0, -70.0, 10f, 1000L))
        )

        val entity = model.toEntity()

        assertEquals(id, entity.rideId)
        assertEquals(1000L, entity.startTime)
        assertEquals(15f, entity.totalDistance)
    }
}
