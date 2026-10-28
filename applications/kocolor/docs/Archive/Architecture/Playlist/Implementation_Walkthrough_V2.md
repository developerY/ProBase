# Implementation Walkthrough: KoColor Style Playlist Orchestration Engine (V2)

This document provides a detailed technical record of the implementation of the V2 Persistence and Domain layer for Style Playlists. This system enables the transition from a single-outfit generator to a stateful, 7-day style orchestration engine.

---

## 1. Domain Layer Refinement (`model` module)

To ensure the engine can generate a 7-day forecast without violating the 48-hour cooldown rule (Recency Penalty), I implemented an in-memory state simulator.

### Key Deliverables:
- **`PlaylistModels.kt`**: Defines `PlaylistStatus` (Lifecycle of the 7-day plan) and `DailyPlanStatus` (Execution status of individual days).
- **`ProjectedRotationState.kt`**: A pure domain construct that clones the committed wear history into an in-memory mutable map. This allows the AI engine to "simulate" wearing an item on Monday and immediately see the resulting penalty applied on Tuesday/Wednesday during the generation loop.
- **`UsageSnapshot`**: A decoupled data class containing `useCount` and `lastUsedAt`, ensuring the `model` module has zero dependency on the `db` (Room) layer.

---

## 2. Persistence Layer (`db` module)

I implemented the Room schema required to store complex, multi-day style plans with strict relational integrity.

### Room Entities:
- **`StylePlaylistEntity`**: The parent container for a 7-day window.
- **`DailyStylePlanEntity`**: The child entity containing specific garment/cosmetic IDs, rationale, and evidence.
- **Relational POJO (`PlaylistWithDays`)**: Enables atomic fetching of a playlist and all its associated daily plans in a single query.

### Robust Type Conversion (`KoColorTypeConverters.kt`):
Implemented bidirectional converters for:
- `java.time.Instant` ↔ `Long` (Epoch Millis).
- `java.time.LocalDate` ↔ `String` (ISO-8601).
- `List<String>` ↔ `JSON String` (via `kotlinx.serialization`).
    - **Safety Fix**: Added explicit handling to ensure empty lists are not deserialized into invalid single-empty-string lists (`[""]`).

---

## 3. Atomic Transaction Boundary (`db` & `data` modules)

A critical requirement was ensuring that committing a "Planned" outfit into "Real" history remains atomic and preserves V1 Cold Start counters.

### `KoColorDatabase.commitDailyStylePlan`:
This is an internal `@Transaction` method that performs four operations:
1.  Fetches the current `DailyStylePlanEntity` and verifies it isn't already `COMMITTED` (Idempotency).
2.  Calls the V1 `commitOutfitUsage` logic to increment the global outfit counter and individual garment wear counts.
3.  Updates the status of the V2 `DailyPlan` to `COMMITTED`.
4.  Evaluates the terminal state of the parent Playlist (if all 7 days are finished, mark Playlist as `COMPLETED`).

### `PlaylistRepositoryImpl`:
Acts as the public facade, delegating the complex multi-table transaction to the database while maintaining a clean, suspendable API.

---

## 4. Verification & Build Stability

I performed extensive verification to ensure the system is both robust and architecturally sound.

### Resolved Issues:
- **KSP Type Missing**: Discovered that `@Embedded` classes must reside in the same module as the `@Entity` for KSP processing. Moved `SelectionEvidence` and `SelectionRationale` to the `db` module to resolve compilation errors.
- **V1 Test Regression**: Updated `RotationScoringUseCaseTest` to match the finalized V1 penalty matrix (0.85 frequency penalty vs the earlier proportional model).
- **Compilation Error**: Fixed `StyleSimulatorViewModel` to use the updated `commitOutfitUsage` repository API.

### Unit Tests Verified:
- **`ProjectedRotationStateTest`**: Confirmed simulated increments work correctly.
- **`KoColorTypeConvertersTest`**: Confirmed list-to-JSON stability, especially for empty arrays.
- **`PlaylistRepositoryTest`**: Confirmed repository correctly delegates to the atomic transaction method.

---

## 5. Final Module Dependency Graph (V2)

```text
[:applications:kocolor:data] 
      │
      ├── Depends on [:applications:kocolor:db] (Persistence)
      └── Depends on [:applications:kocolor:model] (Pure Domain)
               │
               └── Depends on [:core:model] (Foundation)
```

The system is now fully implemented and ready to power the V2 Style Playlist feature.
