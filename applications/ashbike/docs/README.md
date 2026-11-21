# AshBike Documentation Directory

Welcome to the AshBike documentation hub. All documentation for the mobile app, wear app, glass feature, and backend architecture has been consolidated here.

## 🗂 Directory Structure

*   **`GETTING_STARTED.md`** - Developer onboarding, build instructions, and simulator setup.
*   **`architecture/`**: High-level system design.
    *   `ARCHITECTURE.md` - Core system boundaries and app layers.
    *   `ArchReview.md` - Past architectural reviews and decisions.
    *   `DeSignDoc.md` - Main design document and UI structure.
    *   `ARCHITECTURE_OVERVIEW.md` - High-level system design, DI, and Testability.

    *   `FileStructure.md` - Project file hierarchy logic.
    *   `STATE_MANAGEMENT.md` - MVI and Flow architectures.
    *   `BLUETOOTH_INTEGRATION.md` - Phone/Wear BLE telemetry handoff logic.
    *   `Bike_Calculations.md` - Mathematical logic for speed, distance, elevation, and calories.
    *   `SharedForegroundService.md` - FGS location and Bluetooth handling.
    *   `database/` - Room schema and Health Connect mapping logic.
*   **`apps/`**: Platform-specific implementation details.
    *   `mobile/` - Phone-specific docs (architecture, structures, testing).
    *   `wear/` - WearOS-specific docs (features, services, testing).
    *   `glass/` - Projected XR / Smart Glasses interface docs (navigation, voice).
*   **`product/`**: Product definitions and user experience.
    *   `Features.md` - Detailed list of AshBike features.
    *   `UserFlow.md` - App navigation diagrams and states.
    *   `Roadmap.md` - Future development goals.
*   **`release/`**: Google Play Store and shipping.
    *   `PRODUCTION_READINESS.md` - P0/P1/P2 release gates checklist.
    *   `DEPLOYMENT_PLAY_STORE.md` - Deployment runbook.
    *   `Info.md` & `video.md` - Store listing copy and promo scripts.
*   **`testing/`**: Quality assurance.
    *   `TESTING_STRATEGY.md` - Testing architecture, tools, and layers.
    *   `TEST_EXECUTION.md` - Commands for executing tests without monorepo bleed.
    *   `TestPlan.md` - Unit, Integration, and Field test matrices.
*   **`data_and_privacy/`**: Legal and compliance.
    *   `Data_Residency&Privacy.md` - Local-first data retention.
    *   `HEALTH_PERMISSIONS.md` - Per-app Health Connect policy system (AshBike & KoColor).
    *   `PRIVACY_POLICY_PUBLIC.md` - Plain-text privacy policy for the Play Store.
*   **`operations/`**: Background services and debugging.
    *   `SYNC.md` - Health Connect syncing workflows.
    *   `SIMULATION.md` - How to run GPX emulator rides.
*   **`actions/`**: Automation and testing assets.
    *   Contains `.gpx` files for ride simulation and sensor development notes.
