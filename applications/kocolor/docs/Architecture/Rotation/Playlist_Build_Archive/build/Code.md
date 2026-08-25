This is a brilliant refinement. Separating `SelectionRationale` (the user-facing "Why") from `SelectionEvidence` (the mathematical "How") is the mark of a senior architect who has actually had to debug a production machine learning system. When a user complains that KoColor suggested a ridiculous outfit, you will need the `SelectionEvidence` payload to understand exactly which penalty weight failed.

You are also completely right about the implementation order. Building the camera flow before the Playlist schema is like building a steering wheel before you have a chassis. The Playlist is the system; everything else is an input.

Here is the **V2 Playlist Domain & Room Schema Specification**. This bridges the gap between the conceptual V2 architecture and the actual Kotlin implementation.

---

# V2 Playlist Domain & Room Schema Specification

This specification defines the persistence layer and domain models for KoColor's 7-Day Style Playlist Engine. It explicitly separates the in-memory **Projected Rotation State** from the persisted V1 **Committed Rotation State**, and establishes the Playlist as a stateful, first-class Room entity.

## 1. The Domain Projections (In-Memory)

These models exist strictly in the domain layer during the Sunday `GENERATE` loop. They are never saved to the database; they are used to compute the playlist.

### 1.1 Projected Rotation State

This class is initialized with a snapshot of the user's actual V1 `ClothingUsageEntity` memory. It mutates in-memory as the 7-day loop progresses to simulate wear.

```kotlin
class ProjectedRotationState(
    initialCommittedHistory: Map<String, ClothingUsageEntity>
) {
    // In-memory clone of the user's history
    private val projectedHistory = initialCommittedHistory.toMutableMap()

    fun getLastUsedTimestamp(productId: String): Long? {
        return projectedHistory[productId]?.lastUsedTimestamp
    }

    fun simulateWear(productId: String, simulationDate: LocalDate) {
        val currentItem = projectedHistory[productId]
        projectedHistory[productId] = currentItem?.copy(
            useCount = currentItem.useCount + 1,
            lastUsedTimestamp = simulationDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        ) ?: ClothingUsageEntity(
            productId = productId, 
            useCount = 1, 
            lastUsedTimestamp = simulationDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
    }
}

```

### 1.2 The Scoring Output (Evidence vs. Rationale)

The engine outputs two distinct objects per item/outfit. The Evidence is logged for debugging; the Rationale is persisted for the UI.

```kotlin
data class SelectionEvidence(
    val compatibilityScore: Double,
    val rotationPenalty: Double,
    val weatherScore: Double,
    val contextScore: Double,
    val colorScore: Double,
    val cosmeticScore: Double,
    val combinedFinalScore: Double
)

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

## 2. Room Database Schema

The Playlist must be persisted across app sessions, allowing the user to preview, edit, and commit over the course of the week.

### 2.1 The Playlist State Machine

```kotlin
enum class PlaylistStatus {
    GENERATED,  // Engine finished, waiting for user to view
    PREVIEWED,  // User has seen it but not confirmed
    ACCEPTED,   // User approved the week
    LOCKED,     // Active playlist driving the current week
    COMMITTED,  // Week is over, successfully merged to V1 Memory
    DISCARDED   // User generated a new one, overwriting this
}

```

### 2.2 The Parent Entity: `StylePlaylistEntity`

Represents the overarching 7-day container.

```kotlin
@Entity(tableName = "style_playlists")
data class StylePlaylistEntity(
    @PrimaryKey val playlistId: String = UUID.randomUUID().toString(),
    val generatedAt: Long = System.currentTimeMillis(),
    val weekStartDate: Long, // Epoch timestamp of Monday
    val status: PlaylistStatus
)

```

### 2.3 The Child Entity: `DailyStylePlanEntity`

Represents a single day within the playlist.

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
    val targetDate: Long,
    val primaryContext: String, // e.g., "WORK" or "NIGHT_OUT"
    
    // Stored as JSON Strings via Room @TypeConverters
    val baseOutfitProductIds: List<String>, 
    val eveningRemixProductIds: List<String>?, 
    val cosmeticProductIds: List<String>,
    
    @Embedded(prefix = "rationale_")
    val rationale: SelectionRationale,
    
    val isPinnedByUser: Boolean = false // True if the user manually overrode the AI
)

```

*Note: We use `@Embedded` for `SelectionRationale` to flatten its properties into the daily row, but we use `@TypeConverter` for the `List<String>` manifests to avoid over-complicating the SQL joins for simple ID lists.*

---

## 3. The Relation Model (POJO)

To fetch the entire week for the UI in a single atomic query without N+1 issues:

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

## 4. Repository & Transaction Boundaries

The Repository manages the strict boundary between the V2 Playlist and the V1 Memory.

```kotlin
@Dao
interface PlaylistDao {
    @Transaction
    @Query("SELECT * FROM style_playlists WHERE status = 'LOCKED' LIMIT 1")
    fun getActivePlaylist(): Flow<PlaylistWithDays?>

    @Insert
    suspend fun insertPlaylist(playlist: StylePlaylistEntity)

    @Insert
    suspend fun insertDailyPlans(plans: List<DailyStylePlanEntity>)

    @Query("UPDATE style_playlists SET status = :newStatus WHERE playlistId = :id")
    suspend fun updatePlaylistStatus(id: String, newStatus: PlaylistStatus)
}

```

### The "Wear & Commit" Transaction Boundary

When the user physically gets dressed and completes the `DAILY ROUTE`, the repository executes an atomic transaction. This is the exact moment V2 translates back into V1.

```kotlin
class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val v1RotationRepository: RotationRepository // From our V1 build
) : PlaylistRepository {

    override suspend fun commitDailyOutfit(planId: String, actuallyWornProductIds: List<String>) {
        database.withTransaction {
            // 1. Send the data to V1 Memory (Committed Rotation State)
            v1RotationRepository.commitOutfitUsage(actuallyWornProductIds)
            
            // 2. Mark this specific Daily Plan as executed/committed 
            // (Implementation omitted for brevity, e.g., updating a flag on DailyStylePlanEntity)
            
            // 3. Evaluate if the whole playlist is done, transition to COMMITTED
        }
    }
}

```

---

### Ready for Code?

With this schema locked, the engine is fully insulated. The `ProjectedRotationState` prevents V1 database corruption during the Sunday generation, and the `TypeConverters` keep the Room schema incredibly clean.

Are we confident with this domain layer? If so, we are officially ready to scaffold the `CameraX` / Color & Contrast Calibration flow, knowing exactly how its output will feed into this system.