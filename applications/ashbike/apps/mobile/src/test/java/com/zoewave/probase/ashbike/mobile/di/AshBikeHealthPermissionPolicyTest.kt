package com.zoewave.probase.ashbike.mobile.di

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import com.zoewave.ashbike.mobile.rides.domain.SyncRideUseCase
import com.zoewave.ashbike.model.bike.BikeRide
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards AshBike's Health Connect permission set against the record types the app actually
 * reads and writes. A type that is requested nowhere can be granted nowhere, so any gap here
 * silently breaks ride sync (Nov 2026 outage).
 */
class AshBikeHealthPermissionPolicyTest {

    private val policy = AshBikeHealthPermissionPolicy()

    @Test
    fun policy_coversEveryRecordSyncWrites() {
        // SyncRideUseCase emits 3 records without HR data and 4 with it; cover both branches.
        listOf(fakeRide(withHeartRate = false), fakeRide(withHeartRate = true)).forEach { ride ->
            SyncRideUseCase()(ride).forEach { record ->
                assertTrue(
                    "AshBike policy is missing the write permission for ${record::class.simpleName}",
                    policy.permissions.contains(HealthPermission.getWritePermission(record::class))
                )
            }
        }
    }

    @Test
    fun policy_coversDashboardReads() {
        // HealthViewModel.initialLoad reads these types; without requesting them the dashboard
        // silently shows empty sessions, so synced-ride icons never light up.
        assertTrue(policy.permissions.contains(HealthPermission.getReadPermission(ExerciseSessionRecord::class)))
        assertTrue(policy.permissions.contains(HealthPermission.getReadPermission(DistanceRecord::class)))
        assertTrue(policy.permissions.contains(HealthPermission.getReadPermission(HeartRateRecord::class)))
    }

    private fun fakeRide(withHeartRate: Boolean) = BikeRide(
        rideId = "test-ride",
        startTime = 1_700_000_000_000L,
        endTime = 1_700_000_000_000L + 3_600_000L,
        totalDistance = 12_500f,
        averageSpeed = 3.5f,
        maxSpeed = 9.0f,
        elevationGain = 120f,
        elevationLoss = 115f,
        caloriesBurned = 320,
        avgHeartRate = if (withHeartRate) 128 else null,
        maxHeartRate = if (withHeartRate) 151 else null,
        isHealthDataSynced = false,
        isAcknowledged = false,
        healthConnectRecordId = null,
        weatherCondition = null,
        rideType = "Road",
        notes = null,
        rating = null,
        bikeId = null,
        batteryStart = null,
        batteryEnd = null,
        startLat = 40.0,
        startLng = -74.0,
        endLat = 40.1,
        endLng = -74.1,
        locations = emptyList()
    )
}
