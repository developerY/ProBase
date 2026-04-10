# Add "Dark" Calendar Integration

The goal is to integrate with the Android Calendar Provider API as a reusable, "dark code" feature module. This prepares all ProBase applications for future calendar sync capabilities.

## Proposed Changes

### Core Features Module

#### [NEW] Feature Module: `:features:calendar`

- Create a new module under `features/calendar` to house the reusable calendar logic.
- **`CalendarRepository`**: An internal interface for querying and managing system calendar events.
- **`CalendarEventModel`**: A domain model representing a calendar event.
- **`AndroidCalendarProvider`**: Implementation of the repository using `ContentResolver`.

### Database Module (PhotoDo)

#### [NEW] [CalendarSyncEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/entity/time/CalendarSyncEntity.kt)

- Track the relationship between PhotoDo tasks/projects and system calendar events.

```kotlin
@Entity(
    tableName = "calendar_sync",
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
internal data class CalendarSyncEntity(
    @PrimaryKey val taskId: Long,
    val calendarEventId: Long,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
```

#### [PhotoDoDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/PhotoDoDatabase.kt)

- Increment `version` to 4.
- Add `CalendarSyncEntity::class` to `entities`.
- Add `AutoMigration(from = 3, to = 4)`.

## Verification Plan

### Automated Tests
- Run `:features:calendar:assembleDebug` to ensure the new module compiles.
- Run `:applications:photodo:db:assembleDebug` to verify schema version 4 and AutoMigration.

### Manual Verification
- Code review to confirm `internal` visibility and module isolation.
- No Hilt providers will be added to public modules yet.
