package com.zoewave.ashbike.mobile.rides.domain

import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import com.zoewave.ashbike.model.bike.BikeRide
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRideUseCase @Inject constructor() {

    operator fun invoke(ride: BikeRide): List<Record> {
        // Convert Long (millis) to ZonedDateTime to match your preferred pattern
        val start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ride.startTime), ZoneId.systemDefault())
        val end = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ride.endTime), ZoneId.systemDefault())

        // 1. Core Records (Session, Distance, Calories)
        val coreRecords = listOf(
            ExerciseSessionRecord(
                // We add clientRecordId here so you don't get duplicates if you sync twice
                metadata = Metadata.manualEntry(
                    clientRecordId = ride.rideId,
                    device = Device(type = Device.TYPE_PHONE)
                ),
                startTime = start.toInstant(),
                startZoneOffset = start.offset,
                endTime = end.toInstant(),
                endZoneOffset = end.offset,
                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_BIKING,
                title = ride.notes ?: "AshBike Ride",
                notes = ride.notes
            ),
            DistanceRecord(
                metadata = Metadata.manualEntry(),
                startTime = start.toInstant(),
                startZoneOffset = start.offset,
                endTime = end.toInstant(),
                endZoneOffset = end.offset,
                distance = Length.meters(ride.totalDistance.toDouble())
            ),
            TotalCaloriesBurnedRecord(
                metadata = Metadata.manualEntry(),
                startTime = start.toInstant(),
                startZoneOffset = start.offset,
                endTime = end.toInstant(),
                endZoneOffset = end.offset,
                // CRITICAL: Use kilocalories so the graph isn't empty
                energy = Energy.kilocalories(ride.caloriesBurned.toDouble())
            )
        )

        // 2. Add Heart Rate (if available)
        return if (ride.avgHeartRate != null && ride.avgHeartRate!! > 0) {
            coreRecords + buildHeartRateSeries(start, end, ride)
        } else {
            coreRecords
        }
    }

    /**
     * Builds a HeartRateRecord.
     * Since BikeRide only has Avg/Max (not a full list of samples),
     * we create a simplified graph with two points (Start and End).
     */
    private fun buildHeartRateSeries(
        start: ZonedDateTime,
        end: ZonedDateTime,
        ride: BikeRide
    ): HeartRateRecord {
        val samples = mutableListOf<HeartRateRecord.Sample>()

        // Point 1: Start at Average HR
        samples.add(
            HeartRateRecord.Sample(
                time = start.toInstant(),
                beatsPerMinute = ride.avgHeartRate?.toLong() ?: 1L
            )
        )

        // Point 2: End at Max HR (or Avg if Max is missing)
        samples.add(
            HeartRateRecord.Sample(
                time = end.toInstant(),
                beatsPerMinute = ride.maxHeartRate?.toLong() ?: ride.avgHeartRate?.toLong() ?: 1L
            )
        )

        return HeartRateRecord(
            metadata = Metadata.manualEntry(),
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            samples = samples
        )
    }
}