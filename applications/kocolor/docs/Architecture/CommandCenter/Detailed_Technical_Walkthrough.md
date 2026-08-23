# Detailed Technical Walkthrough: Unified Style Command Center (V2 Funnel)

This document provides a comprehensive technical breakdown of the architectural layers implemented to create the **Unified Style Command Center**, the primary entry point for KoColor's V2 styling engine.

---

## 1. The Convergent Funnel (Presentation Layer)
The "Define your Intent" screen has been transformed into a high-fidelity **Command Center** that consolidates all user inputs into a single ergonomic surface before they are processed by the engine.

*   **Premium UI Architecture**: Updated **[`MessagingStep.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/components/list/MessagingStep.kt)** to follow the brand's off-white editorial aesthetic, utilizing `FontFamily.Serif` for headers and a minimalist layout.
*   **Visual Identity Integration**: The top card functions as an **Identity Anchor**. When a portrait is provided, local **Edge AI (ML Kit)** establishes the user's 12-season profile immediately (e.g., "True Winter Established"). This grounds the AI's advice in biometric DNA without sending raw images to the cloud.
*   **Dynamic Anchor Row**: A new section for **Hard Constraints** was added. As users lock garments from their V1 wardrobe, circular thumbnails appear in a horizontal row, providing immediate visual confirmation of "must-wear" items for the simulation.
*   **Dual-Path Action Branching**: Replaced the generic "Begin" button with two high-intent paths:
    *   **"Get Fashion Advice"**: Path A for a single, immediate look saved to history.
    *   **"Generate 7-Day Playlist"**: Path B for a full-week orchestrated forecast.

---

## 2. State Orchestration (ViewModel)
The **[`StyleSimulatorViewModel.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/StyleSimulatorViewModel.kt)** acts as the central controller for the Command Center.

*   **Branching Logic**: Handles the internal switch directing user intent. It manages the `GeneratePlaylist` event by invoking the domain UseCase and emitting a `NavigateToPlaylist` effect upon success.
*   **Context Consolidation**: Aggregates live weather, established seasonal profiles, and active garment anchors into a unified request package for the generation engine.
*   **Loading State Protection**: Implemented state-gating (`isAnalyzing`) to prevent double-clicks or multiple concurrent generation requests.

---

## 3. The Orchestration Engine (Domain Layer)
The core logic for the weekly forecast resides in the **[`GeneratePlaylistUseCase.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/GeneratePlaylistUseCase.kt)**.

*   **7-Iteration Simulation Loop**: Executes a dynamic loop rather than 7 static calls.
*   **State Forwarding**: Implemented a **"Projected State"** system. After Day N's outfit is selected, it is simulated as "worn" in a temporary `ProjectedRotationState`. When the engine generates Day N+1, it applies rotation penalties to Day N's items, ensuring a varied and fresh week.
*   **Anchor Scoping**: Verified that user-provided anchors apply **only to Day 1**. This prevents the "Cartoon Character" bug where the same locked item would be forced into every day of the week.
*   **Pure Domain Models**: Created **[`ProjectedUsage.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/model/src/main/java/com/zoewave/probase/kocolor/model/playlist/ProjectedRotationState.kt)** to keep simulation data completely decoupled from Room `@Entity` models.

---

## 4. Data Layer & The Master Invariant
The database layer protects the integrity of the physical wardrobe history through strict state separation.

*   **The Master Invariant**: Predictions (Projected State) are never recorded as historical facts. Only a user's explicit wear commitment updates the V1 ledger.
*   **Atomic Transactions**: Inside **[`KoColorDatabase.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/KoColorDatabase.kt)**, the `commitDailyStylePlan` function is a single, idempotent `@Transaction`.
*   **Idempotency**: Tapping "I'M WEARING THIS" is the only trigger for history. If double-tapped, the transaction detects the `COMMITTED` status and returns safely without double-counting the wear.

---

## 5. AI Provenance & Auditability
To ensure long-term stability as algorithms evolve, versioned metadata is included in the persistence layer.

*   **[`SelectionEvidence.kt`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/PlaylistEmbeddedModels.kt)**: Now includes a `scoringVersion` (e.g., `"rotation-v1.0"`). This records the exact algorithmic context behind every AI-picked outfit, ensuring that historical analytics remain consistent even if scoring rules change in future releases.

---

## 6. Verified Stability
A dedicated test suite confirms the system's architectural integrity:

*   **`ProjectedVsCommittedIsolationTest`**: Proved that generating a 7-day playlist has **zero impact** on the V1 ledger until an explicit user commit occurs.
*   **`GeneratePlaylistUseCaseTest`**: Confirmed that User Anchors stay correctly scoped to Day 1, while rotation penalties provide variety for the remainder of the week.

---

### Core Artifacts
- **Screens**: `MessagingStep.kt`, `StyleSimulatorViewModel.kt`, `StyleSimulatorScreen.kt`
- **Domain**: `GeneratePlaylistUseCase.kt`, `RotationScoringUseCase.kt`, `ProjectedRotationState.kt`
- **Data**: `KoColorDatabase.kt`, `PlaylistEmbeddedModels.kt`
