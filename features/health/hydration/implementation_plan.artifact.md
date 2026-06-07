# Implementation Plan - 10-Stage Scientific Morning Ritual Content Overhaul

I will fully implement the 10-stage scientifically optimized morning ritual protocol. This involves updating all ritual content in the database and ensuring the interactive "Info" alerts accurately display the provided Title, Subtitle, and Body Text.

## 1. Research & Content Mapping
- **Stages**: 10 distinct biological stages (Wake Up, Mouth Hygiene, Hydrate, Move, Cleanse, Skincare Hydrate, SPF, Makeup, Prep, Fuel).
- **Fields**: Each stage will map to:
    - `title`: Stage [X]: [Name]
    - `subtitle`: [Scientific/Psychological Subtitle]
    - `description`: [Scientific Body Text]
- **Data Source**: `RoutineDefaults.kt` is the source of truth for new and synced routines.

## 2. Technical Steps

### Content Overhaul (`RoutineDefaults.kt`)
- [ ] Update `getMorningRoutine()` to include all 10 stages with the exact text provided for Title, Subtitle, and Body Text.
- [ ] Standardize IDs (`m1` through `m10`) for reliable synchronization.

### Migration & Sync Logic (`RoutinesViewModel.kt`)
- [ ] Refine the **Auto-Sync Engine**:
    - Iterate through the user's current morning ritual.
    - Match steps by ID (`m1`-`m10`).
    - If a step exists, update its `title`, `subtitle`, and `description` to the new scientific content.
    - Ensure new steps (like "Mouth Hygiene") are added if missing from legacy 10-step routines.

### UI Synchronization (`RoutineDetailScreen.kt`)
- [ ] Verify the `RitualKnowledgeDialog` (AlertDialog):
    - Ensure it displays `selectedInfoStep.subtitle` (Uppercase).
    - Ensure it displays `selectedInfoStep.title` (Serif Bold).
    - Ensure it displays `selectedInfoStep.description` (Body text with proper line height).

## 3. Visual & Aesthetic Standards
- **Typography**: Editorial Serif for all dialog content to maintain the "Atelier" design language.
- **Consistency**: All stages must follow the exact same visual pattern in the info alerts.

## 4. Verification
- [ ] **Data Integrity**: Verify all 10 stages are present and correctly ordered.
- [ ] **Interaction**: Test the info button for every single stage to ensure no text is truncated or missing.
- [ ] **Persistence**: Verify that clearing app data or starting fresh correctly loads the new 10-stage protocol.

---
<!-- feedback_request -->
I've prepared the content for all 10 stages. I will now proceed to bake this scientific text into the ritual database and ensure the info alerts show all three sections as requested.

**Should I proceed with the full 10-stage content update?**
