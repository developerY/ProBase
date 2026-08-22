This is exactly why we iterate on the schema before writing a single Room migration. Your catch regarding the `COMMITTED` playlist state versus the `COMMITTED` daily state is the exact type of edge case that creates race conditions and duplicate analytics in production.

Idempotency at the daily level is an absolute requirement. Furthermore, persisting the `engineVersion`, `scoringVersion`, and `SelectionEvidence` ensures that KoColor retains its integrity as a debuggable, deterministic platform rather than a black-box AI wrapper.

Here is the final, locked **V2 Playlist Domain & Room Schema Specification**, incorporating the explicit lifecycle states, temporal semantics, and strict transaction boundaries.

---

### 1. The Domain Projections (Temporal & Timezone Aware)

The projected state now demands an exact `Instant` for the planned wear time, ensuring the 48-hour cooldown respects local timezones and specific event times (e.g., a 9:00 AM office context vs. a 7:00 PM dinner context).

```kotlin
class ProjectedRotationState(
    initialCommittedHistory: Map<String, ClothingUsageEntity>
) {
    private val projectedHistory = initialCommittedHistory.toMutableMap()

    // simulatedWearTime is now an exact Instant derived from the user's local event time
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

---

### 2. The Locked Room Database Schema

The database now relies on `LocalDate` and `Instant` (handled via standard Room `@TypeConverter` classes) to prevent timezone drift, and incorporates the dual-layer provenance for every generated outfit.

#### 2.1 The Lifecycle State Machines

```kotlin
enum class PlaylistStatus {
    GENERATED,  
    PREVIEWED,  
    ACCEPTED,   
    LOCKED,     // Currently driving the week
    COMPLETED,  // Terminal state: all days are past/processed
    DISCARDED   
}

enum class DailyPlanStatus {
    PLANNED,    // Base outfit generated
    ROUTED,     // Contextually adapted (Evening Remix applied)
    WORN,       // User physically wore it
    COMMITTED,  // Successfully synced to V1 Memory
    SKIPPED     // User ignored the day's plan
}

```

#### 2.2 The Parent Entity: `StylePlaylistEntity`

Now includes the engine provenance to track algorithm iterations across the dataset.

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

#### 2.3 The Child Entity: `DailyStylePlanEntity`

Tracks execution status independently, ensuring idempotency. Both Rationale and Evidence are persisted to allow for detailed debugging and user explanation.

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

---

### 3. Explicit Transaction Ownership (The V2 → V1 Boundary)

The V2 `PlaylistRepository` is the sole owner of the database transaction when converting a `DailyPlan` into V1 `RotationMemory`. This prevents nested transaction opacity.

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

            // 2. Write to V1 Historical Memory
            v1RotationDao.incrementUsage(
                productIds = actuallyWornProductIds,
                timestamp = Instant.now().toEpochMilli()
            )
            
            // 3. Mark V2 Daily Plan as successfully committed
            playlistDao.updateDailyPlanStatus(planId, DailyPlanStatus.COMMITTED)
            
            // 4. Evaluate Playlist terminal state (if all 7 days are COMMITTED/SKIPPED)
            checkAndCompletePlaylist(currentPlan.playlistId)
        }
    }
}

```

---

### The Verdict: V2 Schema Locked

With these temporal fixes, idempotency guarantees, and explicit transaction boundaries, this schema forms an unbreakable chassis for the KoColor operating system. The Playlist is a genuine first-class entity, and V1 safely acts as the immutable memory layer.

We are officially ready to build the **CameraX / Color & Contrast Calibration Epic** as the first major Personalization Context input.

To kick off the camera implementation, what is your preferred UI approach for the facial scan—are we building a custom, full-screen Jetpack Compose CameraX overlay with an alignment reticle, or are we utilizing a more standard bottom-sheet photo capture before passing the bitmap to the edge ML model?