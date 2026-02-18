package com.zoewave.probase.features.health.domain

import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSegment
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

// ✅ Generic Model: No dependency on "AshBike" or specific App logic.
data class HealthRideRequest(
    val id: String,                 // Unique ID (UUID)
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val distanceMeters: Double,
    val caloriesKcal: Double,
    val title: String = "Bike Ride",
    val notes: String? = null,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null
)

@Singleton
class SyncRideUseCase @Inject constructor() {

    operator fun invoke(ride: HealthRideRequest): List<Record> {
        val start = Instant.ofEpochMilli(ride.startEpochMillis)
        val end = Instant.ofEpochMilli(ride.endEpochMillis)
        val offset = ZoneOffset.systemDefault().rules.getOffset(start)

        // Metadata: Using 'clientRecordId' ensures duplicate prevention.
        // If the same ID is synced twice, Health Connect updates the existing record.
        val metadata = Metadata.manualEntry(
            device = Device(type = Device.TYPE_PHONE),
            clientRecordId = ride.id
        )

        // 1. Session Record
        val session = ExerciseSessionRecord(
            metadata = metadata,
            startTime = start,
            startZoneOffset = offset,
            endTime = end,
            endZoneOffset = offset,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_BIKING,
            title = ride.title,
            notes = ride.notes,
            segments = listOf(
                ExerciseSegment(
                    startTime = start,
                    endTime = end,
                    segmentType = ExerciseSegment.EXERCISE_SEGMENT_TYPE_BIKING
                )
            )
        )

        // 2. Distance Record
        val distanceRecord = DistanceRecord(
            metadata = metadata,
            startTime = start,
            startZoneOffset = offset,
            endTime = end,
            endZoneOffset = offset,
            distance = Length.meters(ride.distanceMeters)
        )

        // 3. Calories Record
        val caloriesRecord = TotalCaloriesBurnedRecord(
            metadata = metadata,
            startTime = start,
            startZoneOffset = offset,
            endTime = end,
            endZoneOffset = offset,
            energy = Energy.kilocalories(ride.caloriesKcal)
        )

        // 4. Heart Rate Record (Optional)
        val heartRateRecord: HeartRateRecord? = if (ride.avgHeartRate != null && ride.avgHeartRate > 0) {
            val samples = listOfNotNull(
                // Start Sample
                HeartRateRecord.Sample(
                    time = start,
                    beatsPerMinute = ride.avgHeartRate.toLong()
                ),
                // End/Max Sample
                ride.maxHeartRate?.let { maxHr ->
                    if (maxHr > 0) {
                        HeartRateRecord.Sample(
                            time = end.minusMillis(100), // Slightly before end
                            beatsPerMinute = maxHr.toLong()
                        )
                    } else null
                }
            )

            if (samples.isNotEmpty()) {
                HeartRateRecord(
                    metadata = metadata,
                    startTime = start,
                    startZoneOffset = offset,
                    endTime = end,
                    endZoneOffset = offset,
                    samples = samples
                )
            } else null
        } else null

        // 5. Build Result List
        return buildList {
            add(session)
            add(distanceRecord)
            add(caloriesRecord)
            heartRateRecord?.let { add(it) }
        }
    }
}