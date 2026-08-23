# Implementation Plan: Unified Style Command Center (V2 Funnel)

**Core Invariant:** *Playlist generation may mutate Projected Rotation State, but it must never mutate Committed Rotation State. Only an explicit user wear commitment may update V1 ClothingUsageEntity.*

This plan details the technical steps to transform the current "Define your Intent" screen into a convergent Command Center that orchestrates both immediate advice and 7-day style playlists based on the finalized V2 architecture.

## 1. UI Refinement (Presentation Layer)

### **[MODIFY] MessagingStep.kt**

* **Action Buttons**:
    - Replace the single "Begin" button with two distinct action buttons at the bottom.
    - **Button 1 (Secondary)**: "Get Fashion Advice" (Outlined). Triggers Path A.
    - **Button 2 (Primary)**: "Generate 7-Day Playlist" (Filled). Triggers Path B.

* **Intent Context**:
    - Update the `OutlinedTextField` to include a trailing `CalendarToday` icon.
    - **Semantics**: The calendar serves as a context provider (enrichment) that the user can manually override with custom text, rather than a total replacement for manual intent.

* **Identity Feedback**:
    - Ensure the "Visual Identity Active" card clearly displays the **established season** (e.g., "True Winter Established").

* **Anchor Constraints**:
    - Add a horizontal row displaying currently locked garments, along with an "Add Anchor" button.

## 2. State Orchestration (ViewModel)

### **[MODIFY] StyleSimulatorViewModel.kt**

* **New Events**:
    - `SimulatorEvent.GeneratePlaylist`: Triggers the 7-day generation loop.

* **Branching Logic**:
    - **`runSimulation()`**: Remains the entry point for "Single Advice." Updates history and navigates to the Collection tab history section.
    - **`generatePlaylist()`**: Calls the `GeneratePlaylistUseCase` to build a full week using **State Forwarding**.

* **Navigation Effects**:
    - Add `SimulatorEffect.NavigateToPlaylist` to route to the Collection Tab upon successful generation.

## 3. Orchestration & Persistence (Domain/Data Layer)

### **[MODIFY] Domain Slice: ProjectedRotationState + GeneratePlaylistUseCase**

* **Pure Domain Projection**: Create a new domain model `ProjectedUsage(val useCount: Int, val lastUsedTimestamp: Long?)`. **CRITICAL**: Do NOT use Room's `ClothingUsageEntity` inside the simulation engine or the `ProjectedRotationState`. This ensures the AI logic remains decoupled from the infrastructure layer.
* **Scoped Hard Constraints**: Apply anchors according to their declared scope. Day-scoped anchors constrain only that specific day (e.g., "Must wear this blazer Monday"); playlist-scoped anchors remain available as constraints throughout the 7-day generation.
* **Complete State Forwarding**: After a daily outfit is generated, simulate **EVERY** worn garment from that outfit into the `ProjectedRotationState` before generating the next day.
* **Versioned Selection Provenance**: Persist deterministic `SelectionEvidence` alongside the generated playlist plan as immutable generation provenance. 
    - **Include `scoringVersion`** (e.g., "rotation-v1.0") to ensure reproducibility and auditability of AI decisions over time. 
    - This evidence must never be recalculated once generated.

### **[VERIFY] KoColorDatabase.kt (Data Layer Idempotency)**

* **Semantic Separation**: Explicitly verify that generating, previewing, or locking a playlist **DOES NOT** update physical wear counts. 
* **Atomic Commit**: Ensure `commitDailyStylePlan` is the only path to the V1 ledger. It must be wrapped in a strict database `@Transaction`. The operation must conceptually be:
    1. BEGIN TRANSACTION.
    2. Check daily plan execution state.
    3. If already `COMMITTED` → no-op (return).
    4. Otherwise: Commit V1 usage (`useCount + 1`) AND mark daily plan `COMMITTED`.
    5. COMMIT TRANSACTION.

## 4. Implementation Sequence

Execute the implementation in this exact order to ensure dependencies resolve correctly:

1. `MessagingStep` (UI updates)
2. `StyleSimulatorViewModel` (Events/Effects)
3. **Domain Slice:** `ProjectedRotationState` + `GeneratePlaylistUseCase` (State-forwarding contract & consumer)
4. Playlist Room persistence layer
5. Daily-plan commit/idempotency (The Transaction)
6. Collection navigation routing
7. Automated tests
8. Manual end-to-end test

---

## Verification Plan

### Automated Tests

* **UseCase Test**: Verify that the 7-day loop applies user anchors strictly according to their declared scope and generates valid `SelectionEvidence` with a correct `scoringVersion`.
* **Projected/Committed Isolation Test**:
    1. Generate a 7-day playlist.
    2. Simulate seven days.
    3. **Assert** `ClothingUsageEntity` is completely unchanged.
    4. Explicitly commit Day 1.
    5. **Assert** `ClothingUsageEntity` use-count is incremented by exactly 1.
    6. Fire the commit again (double-tap simulation) and **Assert** the count remains 1.

### Manual Verification

1. Open Style Simulator.
2. Provide a portrait and verify the "Established Profile" appears.
3. Lock a "Top" anchor.
4. Click **"Generate 7-Day Playlist"**.
5. Verify navigation lands on the Collection tab and the playlist summary is updated.
6. Verify tapping "I'M WEARING THIS" increments the physical wardrobe count exactly once, even if double-tapped.

---
**Status:** V2-Locked. Optimized for V1 schema with zero RoomDB migration requirements.
