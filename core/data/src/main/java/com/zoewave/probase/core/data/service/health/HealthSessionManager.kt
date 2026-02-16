package com.zoewave.probase.core.data.service.health

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources.NotFoundException
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.InsertRecordsResponse
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Mass
import com.zoewave.probase.core.model.health.SleepSessionData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.io.InvalidObjectException
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.reflect.KClass

const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1
private const val TAG = "HealthSessionManager"

/**
 * Manager for accessing and aggregating health data from Health Connect.
 */
class HealthSessionManager(private val context: Context) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val healthConnectCompatibleApps by lazy {
        val intent = Intent("androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE")

        // FIX: Call queryIntentActivities inside the if/else to handle the different parameter types
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_ALL
            )
        }

        packages.associate {
            val icon = try {
                context.packageManager.getApplicationIcon(it.activityInfo.packageName)
            } catch (e: NotFoundException) {
                null
            }
            val label = context.packageManager.getApplicationLabel(it.activityInfo.applicationInfo)
                .toString()
            it.activityInfo.packageName to
                    HealthConnectAppInfo(
                        packageName = it.activityInfo.packageName,
                        icon = icon,
                        appLabel = label
                    )
        }
    }

    private val _availability = MutableStateFlow(SDK_UNAVAILABLE)
    val availability: StateFlow<Int> get() = _availability

    init {
        checkAvailability()
    }

    fun checkAvailability() {
        _availability.value = HealthConnectClient.getSdkStatus(context)
    }

    suspend fun hasAllPermissions(permissions: Set<String>): Boolean =
        healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    suspend fun revokeAllPermissions() {
        healthConnectClient.permissionController.revokeAllPermissions()
    }

    /**
     * Reads distance records for a specific time range.
     */
    suspend fun readDistance(start: Instant, end: Instant): List<DistanceRecord> =
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = DistanceRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records

    /**
     * Reads total calories burned records for a specific time range.
     */
    suspend fun readTotalCalories(start: Instant, end: Instant): List<TotalCaloriesBurnedRecord> =
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records

    /**
     * Reads in existing [StepsRecord]s for a specific time range.
     */
    suspend fun readSteps(start: Instant, end: Instant): List<StepsRecord> =
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records

    /**
     * Obtains a list of [ExerciseSessionRecord]s in a specified time frame.
     */
    suspend fun readExerciseSessions(start: Instant, end: Instant): List<ExerciseSessionRecord> =
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records

    suspend fun insertRecords(records: List<Record>): InsertRecordsResponse {
        Log.d("DebugSync", "HealthSessionManager.insertRecords → ${records.map { it::class.simpleName }}")

        val res = healthConnectClient.insertRecords(records)

        Log.d("DebugSync", "HealthSessionManager.insertRecords ← ${res.recordIdsList}")

        val details = records.mapIndexed { index, record ->
            val typeName = record::class.simpleName
            val clientId = record.metadata.clientRecordId
            val serverId = res.recordIdsList.getOrNull(index)
            "$typeName(clientId=$clientId)->serverId=$serverId"
        }.joinToString(separator = "\n    ", prefix = "\n    ")

        Log.d(TAG, "insertRecords: inserted ${res.recordIdsList.size} record(s):$details")
        return res
    }

    suspend fun insertBikeRideWithAssociatedData(rideId: String, records: List<Record>): String {
        Log.d(TAG, "Attempting to insert bike ride data for rideId: $rideId consisting of ${records.map { it::class.simpleName }}")
        try {
            val response = healthConnectClient.insertRecords(records)
            Log.d(TAG, "Successfully inserted ${response.recordIdsList.size} records for rideId $rideId. HC IDs: ${response.recordIdsList}")
            return rideId
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting bike ride data for rideId $rideId into Health Connect", e)
            throw e
        }
    }

    /**
     * Logs every record currently stored in Health Connect for the common record types.
     */
    @SuppressLint("RestrictedApi")
    suspend fun showAllRecs() {
        val now = Instant.now()
        val filter = TimeRangeFilter.before(now)

        suspend fun logType(type: KClass<out Record>) {
            val response = healthConnectClient.readRecords(ReadRecordsRequest(type, filter))
            Log.d(TAG, "${type.simpleName}: ${response.records.size} record(s)")
            response.records.forEach { Log.d(TAG, it.toString()) }
        }

        logType(ExerciseSessionRecord::class)
        logType(StepsRecord::class)
        logType(DistanceRecord::class)
        logType(TotalCaloriesBurnedRecord::class)
        logType(HeartRateRecord::class)
    }

    suspend fun writeExerciseSessionNotUse(start: ZonedDateTime, end: ZonedDateTime): InsertRecordsResponse {
        Log.d(TAG, "Writing exercise session")
        return healthConnectClient.insertRecords(
            listOf(
                StepsRecord(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    count = (1000 + 1000 * Random.nextInt(3)).toLong(),
                    metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_WATCH))
                ),
                DistanceRecord(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    distance = Length.meters((1000 + 100 * Random.nextInt(20)).toDouble()),
                    metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_WATCH))
                ),
                TotalCaloriesBurnedRecord(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    energy = Energy.calories(140 + (Random.nextInt(20)) * 0.01),
                    metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_WATCH))
                )
            ) + buildHeartRateSeries(start, end)
        )
    }

    @SuppressLint("RestrictedApi")
    suspend fun writeExerciseSessionTest() {
        val start = ZonedDateTime.now()
        val sessionDuration = Duration.ofMinutes(20)
        val end = start.plus(sessionDuration)

        Log.d(TAG, "Writing exercise session START")

        healthConnectClient.insertRecords(
            listOf(
                ExerciseSessionRecord(
                    metadata = Metadata.manualEntry(),
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                    title = "My Run #${Random.nextInt(0, 60)}"
                ),
                StepsRecord(
                    metadata = Metadata.manualEntry(),
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    count = (1000 + 1000 * Random.nextInt(3)).toLong()
                ),
                TotalCaloriesBurnedRecord(
                    metadata = Metadata.manualEntry(),
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    energy = Energy.calories((140 + Random.nextInt(20)) * 0.01)
                )
            ) + buildHeartRateSeries(start, end)
        )

        Log.d(TAG, "Writing exercise session END")
    }

    @SuppressLint("RestrictedApi")
    suspend fun insertBikeSessionRecord(
        start: ZonedDateTime,
        end: ZonedDateTime,
        title: String = "Bike Ride",
        notes: String? = null
    ): InsertRecordsResponse {
        val session = ExerciseSessionRecord(
            metadata = Metadata.manualEntry(),
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_BIKING,
            title = title,
            notes = notes
        )
        return healthConnectClient.insertRecords(listOf(session))
    }

    @SuppressLint("RestrictedApi")
    suspend fun insertBikeExerciseSession(
        start: ZonedDateTime,
        end: ZonedDateTime,
        title: String = "Bike Ride",
        notes: String? = null
    ): InsertRecordsResponse {
        val session = ExerciseSessionRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_BIKING,
            title = title,
            notes = notes,
            metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_PHONE))
        )

        val steps = StepsRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            count = 1L,
            metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_PHONE))
        )

        val distance = DistanceRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            distance = Length.meters(0.0),
            metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_PHONE))
        )

        val calories = TotalCaloriesBurnedRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            energy = Energy.calories(0.0),
            metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_PHONE))
        )

        val heartRate = buildHeartRateSeries(start, end)

        return healthConnectClient.insertRecords(
            listOf(session, steps, distance, calories, heartRate)
        )
    }

    suspend fun deleteExerciseSession(uid: String) {
        val exerciseSession = healthConnectClient.readRecord(ExerciseSessionRecord::class, uid)
        healthConnectClient.deleteRecords(
            ExerciseSessionRecord::class,
            recordIdsList = listOf(uid),
            clientRecordIdsList = emptyList()
        )
        val timeRangeFilter = TimeRangeFilter.between(
            exerciseSession.record.startTime,
            exerciseSession.record.endTime
        )
        val rawDataTypes = setOf(
            HeartRateRecord::class,
            SpeedRecord::class,
            DistanceRecord::class,
            StepsRecord::class,
            TotalCaloriesBurnedRecord::class
        )
        rawDataTypes.forEach { rawType ->
            healthConnectClient.deleteRecords(rawType, timeRangeFilter)
        }
    }

    suspend fun readAssociatedSessionData(uid: String): ExerciseSessionData {
        val exerciseSession = healthConnectClient.readRecord(ExerciseSessionRecord::class, uid)
        val timeRangeFilter = TimeRangeFilter.between(
            startTime = exerciseSession.record.startTime,
            endTime = exerciseSession.record.endTime
        )
        val aggregateDataTypes = setOf(
            ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
            StepsRecord.COUNT_TOTAL,
            DistanceRecord.DISTANCE_TOTAL,
            TotalCaloriesBurnedRecord.ENERGY_TOTAL,
            HeartRateRecord.BPM_AVG,
            HeartRateRecord.BPM_MAX,
            HeartRateRecord.BPM_MIN,
        )
        val dataOriginFilter = setOf(exerciseSession.record.metadata.dataOrigin)
        val aggregateRequest = AggregateRequest(
            metrics = aggregateDataTypes,
            timeRangeFilter = timeRangeFilter,
            dataOriginFilter = dataOriginFilter
        )
        val aggregateData = healthConnectClient.aggregate(aggregateRequest)

        Log.d(TAG, "aggregateData: $aggregateData")
        Log.d(TAG, "exerciseSession: ${exerciseSession.record.title}")

        return ExerciseSessionData(
            uid = uid,
            totalActiveTime = aggregateData[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL],
            totalSteps = aggregateData[StepsRecord.COUNT_TOTAL],
            totalDistance = aggregateData[DistanceRecord.DISTANCE_TOTAL],
            totalEnergyBurned = aggregateData[TotalCaloriesBurnedRecord.ENERGY_TOTAL],
            minHeartRate = aggregateData[HeartRateRecord.BPM_MIN],
            maxHeartRate = aggregateData[HeartRateRecord.BPM_MAX],
            avgHeartRate = aggregateData[HeartRateRecord.BPM_AVG],
        )
    }

    suspend fun deleteAllSleepData() {
        healthConnectClient.deleteRecords(
            SleepSessionRecord::class,
            TimeRangeFilter.before(Instant.now())
        )
    }

    suspend fun <T : Record> deleteRecordsOfType(type: KClass<T>, sessionRange: TimeRangeFilter) {
        val request = ReadRecordsRequest(type, sessionRange)
        val existing: List<T> = healthConnectClient.readRecords(request).records

        Log.d(TAG, "Found ${existing.size} ${type.simpleName} to delete")

        if (existing.isNotEmpty()) {
            val recordIds = existing.map { it.metadata.id }
            val clientIds = existing.mapNotNull { it.metadata.clientRecordId }

            Log.d(TAG, "Deleting ${type.simpleName}: recordIds=$recordIds, clientIds=$clientIds")
            healthConnectClient.deleteRecords(
                recordType = type,
                recordIdsList = recordIds,
                clientRecordIdsList = clientIds
            )
            Log.d(TAG, "✅ Deleted all ${type.simpleName}")
        }
    }

    suspend fun deleteAllSessionData() {
        val now = Instant.now()
        val sessionRange = TimeRangeFilter.before(now)

        deleteRecordsOfType(ExerciseSessionRecord::class, sessionRange)
        deleteRecordsOfType(StepsRecord::class, sessionRange)
        deleteRecordsOfType(DistanceRecord::class, sessionRange)
        deleteRecordsOfType(TotalCaloriesBurnedRecord::class, sessionRange)
        deleteRecordsOfType(HeartRateRecord::class, sessionRange)

        deleteAllHealthData()
    }

    /**
     * Deletes ALL existing session data.
     */
    suspend fun deleteAllSessionDataType() {
        val now = Instant.now()
        val sessionRange = TimeRangeFilter.before(now)

        val typesToDelete = listOf(
            ExerciseSessionRecord::class,
            StepsRecord::class,
            DistanceRecord::class,
            TotalCaloriesBurnedRecord::class,
            HeartRateRecord::class,
        )

        typesToDelete.forEach { recordType ->
            try {
                Log.d(TAG, "⏳ Reading ${recordType.simpleName} before $now…")
                deleteRecordsOfType(recordType, sessionRange)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting ${recordType.simpleName}", e)
            }
        }
        Log.d(TAG, "🏁 deleteAllSessionData() completed")
    }

    /**
     * Deletes *all* records of the common Health Connect types up until now.
     */
    @SuppressLint("RestrictedApi")
    suspend fun deleteAllHealthData() {
        val now = Instant.now()
        val filter = TimeRangeFilter.before(now)

        val recordTypes = listOf(
            ExerciseSessionRecord::class,
            StepsRecord::class,
            DistanceRecord::class,
            TotalCaloriesBurnedRecord::class,
            HeartRateRecord::class,
            WeightRecord::class
        )

        recordTypes.forEach { type ->
            try {
                Log.d(TAG, "🗑 Deleting all ${type.simpleName} records before $now…")
                healthConnectClient.deleteRecords(type, filter)
                Log.d(TAG, "✅ Deleted all ${type.simpleName} records")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to delete ${type.simpleName}", e)
            }
        }
        Log.d(TAG, "🏁 deleteAllHealthData() complete")
    }

    /**
     * Reads and logs *all* records of the common Health Connect types up until now.
     */
    @SuppressLint("RestrictedApi")
    suspend fun logAllHealthData() {
        val now = Instant.now()
        val filter = TimeRangeFilter.before(now)

        val recordTypes = listOf(
            ExerciseSessionRecord::class,
            StepsRecord::class,
            DistanceRecord::class,
            TotalCaloriesBurnedRecord::class,
            HeartRateRecord::class,
            WeightRecord::class
        )

        recordTypes.forEach { type ->
            try {
                Log.d(TAG, "📖 Reading all ${type.simpleName} records before $now…")
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = type,
                        timeRangeFilter = filter,
                        ascendingOrder = true
                    )
                )
                Log.d(TAG, "🗂 ${type.simpleName}: found ${response.records.size} record(s)")
                response.records.forEach { record ->
                    Log.d(TAG, record.toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error reading ${type.simpleName}", e)
            }
        }
        Log.d(TAG, "🏁 logAllHealthData() complete")
    }

    suspend fun deleteExerciseSessionData() {
        healthConnectClient.deleteRecords(
            ExerciseSessionRecord::class,
            TimeRangeFilter.before(Instant.now())
        )
    }

    suspend fun generateSleepData() {
        val records = mutableListOf<Record>()
        val lastDay = ZonedDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS)
        val notes = arrayOf("good", "bad", "ok", "good", "bad", "ok", "good")

        for (i in 0..7) {
            val wakeUp = lastDay.minusDays(i.toLong())
                .withHour(Random.nextInt(7, 10))
                .withMinute(Random.nextInt(0, 60))
            val bedtime = wakeUp.minusDays(1)
                .withHour(Random.nextInt(19, 22))
                .withMinute(Random.nextInt(0, 60))
            val sleepSession = SleepSessionRecord(
                notes = notes[Random.nextInt(0, notes.size)],
                startTime = bedtime.toInstant(),
                startZoneOffset = bedtime.offset,
                endTime = wakeUp.toInstant(),
                endZoneOffset = wakeUp.offset,
                stages = generateSleepStages(bedtime, wakeUp),
                metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_WATCH))
            )
            records.add(sleepSession)
        }
        healthConnectClient.insertRecords(records)
    }

    suspend fun readSleepSessions(): List<SleepSessionData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
            .minusDays(1)
            .withHour(12)
        val firstDay = lastDay.minusDays(7)

        val sessions = mutableListOf<SleepSessionData>()
        val sleepSessionRequest = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(firstDay.toInstant(), lastDay.toInstant()),
            ascendingOrder = false
        )
        val sleepSessions = healthConnectClient.readRecords(sleepSessionRequest)
        sleepSessions.records.forEach { session ->
            val sessionTimeFilter = TimeRangeFilter.between(session.startTime, session.endTime)
            val durationAggregateRequest = AggregateRequest(
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                timeRangeFilter = sessionTimeFilter
            )
            val aggregateResponse = healthConnectClient.aggregate(durationAggregateRequest)
            sessions.add(
                SleepSessionData(
                    uid = session.metadata.id,
                    title = session.title,
                    notes = session.notes,
                    startTime = session.startTime,
                    startZoneOffset = session.startZoneOffset,
                    endTime = session.endTime,
                    endZoneOffset = session.endZoneOffset,
                    duration = aggregateResponse[SleepSessionRecord.SLEEP_DURATION_TOTAL],
                    stages = session.stages
                )
            )
        }
        return sessions
    }

    suspend fun writeWeightInput(weight: WeightRecord) {
        healthConnectClient.insertRecords(listOf(weight))
    }

    suspend fun readWeightInputs(start: Instant, end: Instant): List<WeightRecord> =
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = WeightRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        ).records

    suspend fun readSessionInputs(): List<ExerciseSessionRecord> {
        val allTime = TimeRangeFilter.before(Instant.now())
        Log.d("DebugSync", "Reading all sessions before ${Instant.now()}")

        val recs = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = allTime
            )
        ).records

        Log.d("DebugSync", "Found ${recs.size} session(s): ${recs.map { it.metadata.id }}")
        return recs
    }

    suspend fun computeWeeklyAverage(start: Instant, end: Instant): Mass? {
        val request = AggregateRequest(
            metrics = setOf(WeightRecord.WEIGHT_AVG),
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        return healthConnectClient.aggregate(request)[WeightRecord.WEIGHT_AVG]
    }

    suspend fun deleteWeightInput(uid: String) {
        healthConnectClient.deleteRecords(
            WeightRecord::class,
            recordIdsList = listOf(uid),
            clientRecordIdsList = emptyList()
        )
    }

    suspend fun getChangesToken(dataTypes: Set<KClass<out Record>>): String =
        healthConnectClient.getChangesToken(ChangesTokenRequest(dataTypes))

    suspend fun getChanges(token: String): Flow<ChangesMessage> = flow {
        var nextChangesToken = token
        do {
            val response = healthConnectClient.getChanges(nextChangesToken)
            if (response.changesTokenExpired) {
                throw IOException("Changes token has expired")
            }
            emit(ChangesMessage.ChangeList(response.changes))
            nextChangesToken = response.nextChangesToken
        } while (response.hasMore)
        emit(ChangesMessage.NoMoreChanges(nextChangesToken))
    }

    private fun generateSleepStages(start: ZonedDateTime, end: ZonedDateTime): List<SleepSessionRecord.Stage> {
        val sleepStages = mutableListOf<SleepSessionRecord.Stage>()
        var stageStart = start
        while (stageStart < end) {
            val stageEnd = stageStart.plusMinutes(Random.nextLong(30, 120))
            val checkedEnd = if (stageEnd > end) end else stageEnd
            sleepStages.add(
                SleepSessionRecord.Stage(
                    stage = randomSleepStage(),
                    startTime = stageStart.toInstant(),
                    endTime = checkedEnd.toInstant()
                )
            )
            stageStart = checkedEnd
        }
        return sleepStages
    }

    suspend fun fetchSeriesRecordsFromUid(
        recordType: KClass<out Record>,
        uid: String,
        seriesRecordsType: KClass<out Record>
    ): List<Record> {
        val recordResponse = healthConnectClient.readRecord(recordType, uid)
        val timeRangeFilter = when (recordResponse.record) {
            is ExerciseSessionRecord -> {
                val record = recordResponse.record as ExerciseSessionRecord
                TimeRangeFilter.between(startTime = record.startTime, endTime = record.endTime)
            }
            is SleepSessionRecord -> {
                val record = recordResponse.record as SleepSessionRecord
                TimeRangeFilter.between(startTime = record.startTime, endTime = record.endTime)
            }
            else -> {
                throw InvalidObjectException("Record with unregistered data type returned")
            }
        }

        val dataOriginFilter = setOf(recordResponse.record.metadata.dataOrigin)
        val request = ReadRecordsRequest(
            recordType = seriesRecordsType,
            dataOriginFilter = dataOriginFilter,
            timeRangeFilter = timeRangeFilter
        )
        return healthConnectClient.readRecords(request).records
    }

    suspend fun readLatestHeartRateSample(): HeartRateRecord.Sample? {
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.before(Instant.now()),
            ascendingOrder = false,
            pageSize = 1
        )
        val response = healthConnectClient.readRecords(request)
        val record = response.records.firstOrNull() ?: return null
        return record.samples.maxByOrNull { it.time }
    }

    private fun buildHeartRateSeries(
        sessionStartTime: ZonedDateTime,
        sessionEndTime: ZonedDateTime
    ): HeartRateRecord {
        val samples = mutableListOf<HeartRateRecord.Sample>()
        var time = sessionStartTime
        while (time.isBefore(sessionEndTime)) {
            samples.add(
                HeartRateRecord.Sample(
                    time = time.toInstant(),
                    beatsPerMinute = (80 + Random.nextInt(80)).toLong()
                )
            )
            time = time.plusSeconds(30)
        }
        return HeartRateRecord(
            startTime = sessionStartTime.toInstant(),
            startZoneOffset = sessionStartTime.offset,
            endTime = sessionEndTime.toInstant(),
            endZoneOffset = sessionEndTime.offset,
            samples = samples,
            metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_WATCH))
        )
    }

    fun isFeatureAvailable(feature: Int): Boolean =
        healthConnectClient.features.getFeatureStatus(feature) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE

    sealed class ChangesMessage {
        data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
        data class ChangeList(val changes: List<Change>) : ChangesMessage()
    }
}