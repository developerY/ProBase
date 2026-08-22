Here is the complete, finalized Markdown document. It is formatted to be copied directly into your documentation repository (e.g., `server/docs/Architecture/Rotation/V2_Playlist_Orchestration.md`), serving as the definitive architectural source of truth for the KoColor V2 platform.

---

# KoColor V2: Personal Style Orchestration & Playlist Architecture

## 1. Executive Summary & Core Philosophy

**The Core Philosophy:** *V1 builds the memory. V2 turns the memory into orchestration.*

KoColor is evolving from a static digital inventory into a **Personal Style Operating System**. The platform no longer functions merely as a reactive "AI Outfit Generator." Instead, it operates as a sophisticated orchestration engine that continuously converts the user's personalization context and behavioral history into an adaptive, stateful **Style Playlist**.

### 1.1 The "Spotify for the Closet" Paradigm

The underlying architecture maps directly to the proven engagement loops of audio streaming, placing the **Playlist** as the central, first-class domain entity that drives the consumer experience.

| Streaming Concept | KoColor Translation | Architecture Component |
| --- | --- | --- |
| **Track** | Garment / Cosmetic | `ClothingItemEntity` / `CosmeticItemEntity` |
| **Playlist** | Outfit Sequence | `StylePlaylistEntity` |
| **Crossfade** | Garment ↔ Cosmetic Transition | Cosmetic Crossfade Engine |
| **Discover Weekly** | 7-Day Style Forecast | Scheduled Generation Loop |
| **Recently Played** | Wear History | V1 `ClothingUsageEntity` |

---

## 2. The Orchestration Loop

The system is strictly divided into **Personalization Context Streams** (the current state of the user and their environment) and the **Feedback Stream** (the historical memory of their actions).

### The Core Orchestration Flow

> **Context** → **Intelligence** → **Playlist** → **Behavior** → **Memory** → **Intelligence**

```text
                    ┌──────────────────────┐
                    │  CONTEXT STREAMS     │
                    │                      │
                    │ 1. Calendar          │
                    │ 2. Location          │
                    │ 3. Weather           │
                    │ 4. Wardrobe          │
                    │ 5. Cosmetics         │
                    │ 6. Color Profile     │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │    STYLE ENGINE      │
                    │                      │
                    │ Compatibility        │
                    │ Rotation             │
                    │ Weather Limits       │
                    │ Context Routing      │
                    │ Color Harmony        │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │   STYLE PLAYLIST     │
                    │                      │
                    │ 7-day plans          │
                    │ base outfits         │
                    │ evening remixes      │
                    │ cosmetic crossfades  │
                    │ rationale/evidence   │
                    └──────────┬───────────┘
                               │
                               ▼
                         USER WEARS
                               │
                               ▼
                    ┌──────────────────────┐
                    │   ROTATION MEMORY    │
                    │  (Feedback Stream)   │
                    │                      │
                    │ useCount             │
                    │ lastUsedTimestamp    │
                    └──────────┬───────────┘
                               │
                               └──────────────► STYLE ENGINE

```

---

## 3. The Killer Feature: The Cosmetic Crossfade

Cosmetics are functional components of styling, not just another inventory category. They act as the algorithmic bridge between the user's **Color & Contrast Profile** (derived via on-device Edge ML) and their chosen garments, creating a closed personalization triangle:
**Garment ↔ User Color Profile ↔ Cosmetic Inventory**.

If a user selects a garment that violates their intrinsic color harmony, the engine queries the **Virtual Vanity** to recommend specific makeup shades that artificially compensate for the harmony gap.

**Execution Flow:**

1. **Garment:** Cool Winter Black Dress
2. **Color Analysis:** Detects item is high-contrast, cool-toned.
3. **User Profile:** Soft Autumn (needs warmth and muted contrast).
4. **Harmony Gap:** Clash detected.
5. **Virtual Vanity:** Queries owned `CosmeticItemEntity` database.
6. **Crossfade:** Recommends warm peach blush + soft brown eyeliner to bridge the visual gap.

---

## 4. The 7-Day Style Playlist Engine

The Playlist is a first-class domain entity containing the logic for *what* to wear, *when* and *where* it makes sense, *how* to adapt it, *which* cosmetics complete it, and *why* the engine selected it.

### 4.1 Projected vs. Committed Rotation State

To prevent the engine from generating seven identical outfits on Sunday morning, KoColor utilizes a simulated state-forwarding loop completely separated from the V1 database.

* **Projected Rotation State:** Exists strictly in-memory during playlist generation. When an item is selected for Day 1, a simulated usage event writes to this temporary state, immediately enforcing the V1 48-hour cooldown across Day 2 and Day 3. This enforces deterministic constraints that promote diversity across the week.
* **Committed Rotation State:** The actual immutable wear history persisted to the V1 `ClothingUsageEntity` only after the user physically commits to the outfit.

```kotlin
class ProjectedRotationState(
    initialCommittedHistory: Map<String, ClothingUsageEntity>
) {
    private val projectedHistory = initialCommittedHistory.toMutableMap()

    fun simulateWear(productId: String, simulatedWearTime: Instant) {
        val currentItem = projectedHistory[productId]
        projectedHistory[productId] = currentItem?.copy(
            useCount = currentItem.useCount + 1,
            lastUsedTimestamp = simulatedWearTime.toEpochMilli()
        ) ?: ClothingUsageEntity(
            productId = productId, 
            useCount = 1, 
            lastUsedTimestamp = simulatedWearTime.toEpochMilli()
        )
    }
}

```

### 4.2 Provenance: Rationale vs. Evidence

To ensure decisions are both explainable to the user and debuggable for engineering, every plan generates two distinct output layers:

```kotlin
// Machine-facing provenance (Stored for debugging engine weights)
data class SelectionEvidence(
    val compatibilityScore: Double,
    val rotationPenalty: Double,
    val weatherScore: Double,
    val contextScore: Double,
    val colorScore: Double,
    val cosmeticScore: Double,
    val combinedFinalScore: Double
)

// User-facing explainability (Rendered in the UI)
data class SelectionRationale(
    val calendarReason: String?, // e.g., "Sharp enough for your Board Review."
    val weatherReason: String?,  // e.g., "Linen blend for the 85° afternoon."
    val locationReason: String?,
    val colorReason: String?,    // e.g., "Navy harmonizes with your Soft Summer contrast."
    val rotationReason: String?, // e.g., "Bringing this out of the archive."
    val cosmeticReason: String?  // e.g., "Warm peach blush bridges the cool tones."
)

```

---

## 5. Playlist Lifecycle & State Machines

The Playlist natively closes the loop between V2 generation and V1 memory through a strict lifecycle, accounting for multi-event days via context routing.

### The Execution State Machine

> `GENERATE` (Engine builds the 7-day forecast from Context Streams) → `PREVIEW` (User reviews the week) → `ACCEPT` (User confirms items) → `LOCKED` (State is saved to Room) → `DAILY ROUTE` (Contextual routing adapts the base plan for evening events) → `WEAR` (User dresses) → `COMMIT` (Atomic DB transaction to V1) → `NEXT PLAYLIST`

```kotlin
enum class PlaylistStatus {
    GENERATED, PREVIEWED, ACCEPTED, LOCKED, COMPLETED, DISCARDED   
}

enum class DailyPlanStatus {
    PLANNED,    // Base outfit generated
    ROUTED,     // Contextually adapted (Evening Remix applied)
    WORN,       // User physically wore it
    COMMITTED,  // Successfully synced to V1 Memory (Terminal for the day)
    SKIPPED     // User ignored the day's plan
}

```

---

## 6. Room Database Schema

The schema utilizes `LocalDate` and `Instant` (via standard `@TypeConverter` classes) to prevent timezone drift, and embeds engine provenance for backward compatibility and debugging.

### 6.1 Parent Entity: `StylePlaylistEntity`

```kotlin
@Entity(tableName = "style_playlists")
data class StylePlaylistEntity(
    @PrimaryKey val playlistId: String = UUID.randomUUID().toString(),
    val generatedAt: Instant,
    val weekStartDate: LocalDate, 
    val engineVersion: String,  // e.g., "playlist-v2.0"
    val scoringVersion: String, // e.g., "rotation-v1.1"
    val status: PlaylistStatus
)

```

### 6.2 Child Entity: `DailyStylePlanEntity`

Tracks execution status independently, guaranteeing idempotency so analytics and use counts are never duplicated.

```kotlin
@Entity(
    tableName = "daily_style_plans",
    foreignKeys = [
        ForeignKey(
            entity = StylePlaylistEntity::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId")]
)
data class DailyStylePlanEntity(
    @PrimaryKey val planId: String = UUID.randomUUID().toString(),
    val playlistId: String,
    val targetDate: LocalDate,
    val status: DailyPlanStatus = DailyPlanStatus.PLANNED,
    val primaryContext: String,
    
    val baseOutfitProductIds: List<String>, 
    val eveningRemixProductIds: List<String>?, 
    val cosmeticProductIds: List<String>,
    
    @Embedded(prefix = "rationale_") val rationale: SelectionRationale,
    @Embedded(prefix = "evidence_") val evidence: SelectionEvidence,
    
    val isPinnedByUser: Boolean = false 
)

```

### 6.3 Relation POJO

```kotlin
data class PlaylistWithDays(
    @Embedded val playlist: StylePlaylistEntity,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "playlistId"
    )
    val dailyPlans: List<DailyStylePlanEntity>
)

```

---

## 7. Explicit Transaction Ownership (The V2 → V1 Boundary)

The V2 `PlaylistRepository` acts as the sole owner of the database transaction when converting a V2 `DailyPlan` into V1 `RotationMemory`. This guarantees atomicity and prevents nested transaction opacity.

```kotlin
class PlaylistRepositoryImpl(
    private val database: KoColorDatabase,
    private val playlistDao: PlaylistDao,
    private val v1RotationDao: RotationDao 
) : PlaylistRepository {

    override suspend fun commitDailyOutfit(
        planId: String, 
        actuallyWornProductIds: List<String>
    ) {
        database.withTransaction {
            // 1. Verify idempotency
            val currentPlan = playlistDao.getDailyPlan(planId)
            if (currentPlan.status == DailyPlanStatus.COMMITTED) return@withTransaction

            // 2. Write to V1 Historical Memory (Feedback Stream)
            v1RotationDao.incrementUsage(
                productIds = actuallyWornProductIds,
                timestamp = Instant.now().toEpochMilli()
            )
            
            // 3. Mark V2 Daily Plan as successfully committed
            playlistDao.updateDailyPlanStatus(planId, DailyPlanStatus.COMMITTED)
            
            // 4. Evaluate Playlist terminal state 
            // (If all 7 days are COMMITTED/SKIPPED, mark parent playlist COMPLETED)
            checkAndCompletePlaylist(currentPlan.playlistId)
        }
    }
}

```