# Task - 10-Stage Scientific Morning Ritual Content Overhaul

Implementing all 10 stages of the scientifically optimized morning ritual with detailed scientific summaries and interactive info alerts.

## Status
- [ ] Update `RoutineDefaults.kt` with all 10 stages (Title, Subtitle, Body Text).
- [ ] Update `RoutinesViewModel.kt` to synchronize all 10 stages for existing users.
- [ ] Verify `RoutineDetailScreen.kt` info alert displays all 3 sections.
- [ ] Verify build and ritual order.

## Technical Details
- **IDs**: `m1` through `m10`.
- **Content**: 1: Wake Up, 2: Mouth Hygiene, 3: Hydrate, 4: Move, 5: Cleanse, 6: Skincare Hydrate, 7: SPF, 8: Makeup, 9: Prep, 10: Fuel.
- **Sync**: ViewModel checks `title` and `id` to trigger forced updates of descriptions and subtitles.
