# Add Time Budgeting Capabilities to Database

The goal is to expand the "dark code" time tracking infrastructure to include budgeting. This involves adding estimation fields to tasks and creating a mechanism for category-level time targets.

## Proposed Changes

### Database Module

#### [TaskEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/entity/TaskEntity.kt)

- Add `estimatedTimeMillis: Long? = null` to support per-task time budgeting.

#### [NEW] [TimeBudgetEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/entity/time/TimeBudgetEntity.kt)

- Create an `internal` entity to define time goals at the Category level (e.g., "Spend 10 hours a week on Development").

```kotlin
@Entity(
    tableName = "time_budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
internal data class TimeBudgetEntity(
    @PrimaryKey(autoGenerate = true) val budgetId: Long = 0,
    val categoryId: Long,
    val targetTimeMillis: Long,
    val period: String = "WEEKLY", // e.g., DAILY, WEEKLY, MONTHLY
    val lastModified: Long = System.currentTimeMillis()
)
```

#### [TimeTrackingDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/TimeTrackingDao.kt)

- Add methods to manage `TimeBudgetEntity`.

#### [PhotoDoDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/PhotoDoDatabase.kt)

- Increment `version` to 3.
- Add `TimeBudgetEntity::class` to `entities`.
- Add `AutoMigration(from = 2, to = 3)`.

## Verification Plan

### Automated Tests
- Run `:applications:photodo:db:assembleDebug` to generate version 3 schema.
- Verify that `3.json` is created.

### Manual Verification
- Code review to ensure `internal` visibility is maintained for new budgeting features.
