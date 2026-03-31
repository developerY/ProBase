# Implementation Plan - Room 3 Migration

Migrate the project from Room 2 to Room 3.0.0-alpha02. This involves updating artifact IDs, package names, and adopting the new reflection-free instantiation pattern required by Room 3's Kotlin Multiplatform focus.

## User Review Required

> [!IMPORTANT]
> Room 3 requires using **Kotlin Symbol Processing (KSP)**. The project already uses KSP for Room via the convention plugin, so this is handled.
> All `@Database` classes will now require a `@ConstructedBy` annotation and a corresponding `RoomDatabaseConstructor` object.

## Proposed Changes

### Build Configuration

#### [libs.versions.toml](file:///Users/developer/AndroidStudioProjects/ProBase/gradle/libs.versions.toml)
- Update `room` version to `3.0.0-alpha02`.
- Update artifact groups from `androidx.room` to `androidx.room3`.
- Update artifact names to include the `3` suffix (e.g., `room3-runtime`).
- Remove `room-ktx` as its functionality is merged into `room3-runtime`.

#### [AndroidRoomConventionPlugin.kt](file:///Users/developer/AndroidStudioProjects/ProBase/build-logic/convention/src/main/kotlin/com/zoewave/probase/convention/AndroidRoomConventionPlugin.kt)
- Remove `room-ktx` dependency.
- Update logic to ensure `room3-runtime` and `room3-compiler` are correctly applied.

---

### Global Source Refactoring

- **Import Update**: Replace all occurrences of `import androidx.room.` with `import androidx.room3.`.
- **Annotation Update**: Update annotations if any names changed (though most remain the same).

---

### Database Instantiation

#### [BaseProDB.kt](file:///Users/developer/AndroidStudioProjects/ProBase/core/database/src/main/java/com/zoewave/probase/core/database/BaseProDB.kt)
#### [BikeRideDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/database/src/main/java/com/zoewave/probase/ashbike/database/BikeRideDatabase.kt)
#### [PhotoDoDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/PhotoDoDatabase.kt)
#### [GoSwiftDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/database/src/main/java/com/zoewave/probase/goswift/database/GoSwiftDatabase.kt)
#### [SeaweedDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/database/src/main/java/com/zoewave/probase/seaweed/database/SeaweedDatabase.kt)

- Add `@ConstructedBy(DatabaseConstructor::class)` to each database class.
- Define an internal object `DatabaseConstructor` implementing `RoomDatabaseConstructor<T>`.

---

### Callback & Driver Updates

#### [DatabaseModule.kt (PhotoDo)](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/di/DatabaseModule.kt)
- Update `onCreate` to use `SQLiteConnection` instead of `SupportSQLiteDatabase`.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure compilation and KSP generation work correctly with the new Room 3 setup.
- Run database unit tests if they exist.

### Manual Verification
- Verify that the app still launches and data is persisted correctly (destructive migration is allowed per user instructions).
- Check the generated Kotlin code in `build/generated/ksp` to confirm Room 3 is generating code.
