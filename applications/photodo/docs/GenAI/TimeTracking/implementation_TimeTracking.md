# Add "Dark Code" Time Tracking to Database

The goal is to prepare the database for time tracking as "dark code," ensuring the schema is ready for future release without exposing the feature to the rest of the app yet.

## Proposed Changes

### Database Module

#### [NEW] [TimeLogEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/entity/time/TimeLogEntity.kt)

- Define an `internal` `TimeLogEntity` to store time entries linked to tasks.

```kotlin
@Entity(
    tableName = "time_logs",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["taskId"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
internal data class TimeLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val taskId: Long,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val note: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)
```

#### [NEW] [TimeTrackingDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/TimeTrackingDao.kt)

- Define an `internal` DAO for time tracking operations.

```kotlin
@Dao
internal interface TimeTrackingDao {
    @Upsert
    suspend fun upsertTimeLog(log: TimeLogEntity): Long

    @Query("SELECT * FROM time_logs WHERE taskId = :taskId ORDER BY startTimeMillis DESC")
    fun getTimeLogsForTask(taskId: Long): Flow<List<TimeLogEntity>>

    @Delete
    suspend fun deleteTimeLog(log: TimeLogEntity)
}
```

#### [NEW] [TimeTrackingRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/repo/time/TimeTrackingRepository.kt)

- Define an `internal` repository interface.

#### [NEW] [TimeTrackingRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/repo/time/TimeTrackingRepositoryImpl.kt)

- Implement the `internal` repository using `TimeTrackingDao`.

#### [PhotoDoDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/PhotoDoDatabase.kt)

- Increment `version` to 2.
- Add `TimeLogEntity::class` to `entities`.
- Add `autoMigrations = [AutoMigration(from = 1, to = 2)]`.
- Declare `internal abstract fun timeTrackingDao(): TimeTrackingDao`.

## Verification Plan

### Automated Tests
- Run `:applications:photodo:db:assembleDebug` to ensure the schema is generated and compilation passes.
- Verify that `2.json` is generated in the `schemas` directory.

### Manual Verification
- Code analysis to confirm `internal` visibility and package-level isolation.
- Confirm that no Hilt providers are added for the new components.
