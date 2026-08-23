# Implementation Plan: Unified Style Command Center (V2 Funnel)

This plan details the technical steps to transform the current "Define your Intent" screen into a convergent Command Center that orchestrates both immediate advice and 7-day style playlists based on the finalized V2 architecture.

## 1. UI Refinement (Presentation Layer)

### **[MODIFY] MessagingStep.kt**
- **Action Buttons**:
    - Replace the single "Begin" button with two distinct action buttons at the bottom.
    - **Button 1 (Secondary)**: "Get Fashion Advice" (Outlined). Triggers Path A.
    - **Button 2 (Primary)**: "Generate 7-Day Playlist" (Filled). Triggers Path B.
- **Intent Context**:
    - Update the `OutlinedTextField` to include a trailing `CalendarToday` icon.
    - Add a visual hint/placeholder: "Read from calendar" for future automated context enrichment.
- **Identity Feedback**:
    - Ensure the "Visual Identity Active" card clearly displays the **established season** (e.g., "True Winter Established") to confirm Edge AI completion.
- **Anchor Constraints**:
    - Add a horizontal row/section above the intent field displaying currently locked garments, along with an "Add Anchor" button (e.g., a plus icon) that opens the V1 wardrobe picker.

## 2. State Orchestration (ViewModel)

### **[MODIFY] StyleSimulatorViewModel.kt**
- **New Events**:
    - `SimulatorEvent.GeneratePlaylist`: Triggers the 7-day generation loop.
- **Branching Logic**:
    - **`runSimulation()`**: Remains the entry point for "Single Advice." Updates history and navigates to the Collection tab history section.
    - **`generatePlaylist()`**: Calls the `GeneratePlaylistUseCase` to build a full week using **State Forwarding** (simulated wear).
- **Navigation Effects**:
    - Add `SimulatorEffect.NavigateToPlaylist` to handle the transition after the 7-day loop completes.

## 3. Orchestration & Persistence (Domain/Data Layer)

### **[MODIFY] GeneratePlaylistUseCase.kt**
- **Hard Constraints**: Update the loop to explicitly pass the **User Anchors** into the **Day 1** engine call. Subsequent days in the 7-day forecast should rely purely on the `ProjectedRotationState` and weather/context to ensure a varied week, unconstrained by the Day 1 anchor.
- **State Forwarding**: Refine the `ProjectedRotationState` handling to ensure that "Monday's" simulated selection correctly penalizes items for "Tuesday's" engine call.

### **[VERIFY] KoColorDatabase.kt**
- **Idempotent Commit**: Confirm that `commitDailyStylePlan` correctly checks the `COMMITTED` status before writing to the V1 historical memory. This prevents double-counting if the user taps "I'M WEARING THIS" multiple times.

## 4. Navigation Integration

### **[MODIFY] StyleSimulatorScreen.kt**
- Handle the new `NavigateToPlaylist` effect to route the user to the Collection Tab upon successful generation.

---

## Verification Plan

### Automated Tests
- **UseCase Test**: Verify that the 7-day loop respects user anchors across all generated days.
- **Repository Test**: Verify that committing an outfit increments the `ClothingUsageEntity` count exactly once (Idempotency).

### Manual Verification
1. Open Style Simulator.
2. Provide a portrait and verify the "Established Profile" appears.
3. Lock a "Top" anchor.
4. Click **"Generate 7-Day Playlist"**.
5. Verify navigation lands on the Collection tab and the playlist summary is updated.
6. Verify clicking "I'M WEARING THIS" on the playlist detail screen correctly updates the wardrobe usage history.
