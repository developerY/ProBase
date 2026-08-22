# Implementation Plan - KoColor Style Playlist Orchestration Engine

This plan covers the implementation of the persistence and domain layer for the V2 Style Playlist feature. This includes Room entities for managing 7-day style plans, their execution status, and the logic for projected rotation states.

## Proposed Changes

### Domain Layer (Models & Enums)
I will create the necessary data classes and enums to support the playlist orchestration logic.

#### [NEW] [PlaylistModels.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/model/src/main/java/com/zoewave/probase/kocolor/model/playlist/PlaylistModels.kt)
- Define `PlaylistStatus` enum: `GENERATED`, `PREVIEWED`, `ACCEPTED`, `LOCKED`, `COMPLETED`, `DISCARDED`.
- Define `DailyPlanStatus` enum: `PLANNED`, `ROUTED`, `WORN`, `COMMITTED`, `SKIPPED`.
- Define `SelectionEvidence` data class for machine-facing scoring metadata.
- Define `SelectionRationale` data class for user-facing explainability.

#### [NEW] [ProjectedRotationState.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/model/src/main/java/com/zoewave/probase/kocolor/model/playlist/ProjectedRotationState.kt)
- Implement `ProjectedRotationState` as an in-memory domain construct for simulating wear events during the 7-day generation loop. moved to `model` module to adhere to Clean Architecture.

### Persistence Layer (Room)
I will implement the Room entities, relations, and DAOs required to store and retrieve playlists.

#### [NEW] [StylePlaylistEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/StylePlaylistEntity.kt)
- Define the `style_playlists` table.

#### [NEW] [DailyStylePlanEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/DailyStylePlanEntity.kt)
- Define the `daily_style_plans` table with a foreign key to `style_playlists`.

#### [NEW] [PlaylistWithDays.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/PlaylistWithDays.kt)
- Create a dedicated file for the relational POJO containing `@Embedded val playlist: StylePlaylistEntity` and an `@Relation` mapping to the daily plans.

#### [NEW] [KoColorTypeConverters.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/converter/KoColorTypeConverters.kt)
- Implement bidirectional converters for `Instant`, `LocalDate`, `List<String>`, and Enums.

#### [NEW] [PlaylistDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/dao/PlaylistDao.kt)
- Define operations for inserting and updating playlists and daily plans.
- Uses `PlaylistWithDays` for relational fetching.

#### [MODIFY] [KoColorDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/KoColorDatabase.kt)
- Register the new entities and DAO.
- Add `KoColorTypeConverters` to the `@ColumnTypeConverters`.

### Repository Layer
I will implement the repository to manage playlist data with strict transaction boundaries.

#### [NEW] [PlaylistRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/repository/PlaylistRepository.kt)
- Define the interface for playlist management.

#### [NEW] [PlaylistRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/repository/PlaylistRepositoryImpl.kt)
- Implement `commitDailyOutfit` with idempotency logic and atomic transaction boundary.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - `ProjectedRotationStateTest`: Verify that `simulateWear` correctly updates the in-memory state.
    - `PlaylistRepositoryTest`: Verify `commitDailyOutfit` idempotency and correct interaction with `RotationDao`.
    - `KoColorTypeConvertersTest`: Verify that `List<String>` JSON serialization correctly handles empty lists and doesn't deserialize into `[""]` (a list with one empty string).
- **Database Tests**:
    - Verify Room entity relationships and cascade deletes.

### Manual Verification
- Build the `:applications:kocolor:apps:mobile` project to ensure all generated code is valid and compiles correctly.
