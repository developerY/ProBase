# Add Project Duration to Database

The goal is to update the `ProjectEntity` to include a `durationMillis` field to track how long a project is expected to take. Since the app is not live, we will update the schema directly without a migration.

## Proposed Changes

### Database Module

#### [ProjectEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/entity/ProjectEntity.kt)

- Add `var durationMillis: Long? = null` to the `ProjectEntity` data class.

```kotlin
data class ProjectEntity(
    // ... existing fields ...
    var durationMillis: Long? = null, // Added
    var globalSyncId: String = UUID.randomUUID().toString(),
    var lastModified: Long = System.currentTimeMillis()
)
```

## Verification Plan

### Automated Tests
- Run `:applications:photodo:db:assembleDebug` to ensure the database module compiles correctly.
- Verify that the exported schema (if any) reflects the new field.

### Manual Verification
- Code inspection to ensure the field is added correctly with a default null value.
