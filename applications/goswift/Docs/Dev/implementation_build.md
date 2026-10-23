# Redo GoSwift Application - Stitch UI Migration & MAD Upgrade

The goal of this implementation is to completely revamp the `applications/goswift` module (a caffeine tracker) based on the newly provided Stitch export. The design shifts to a high-precision "Warm Biometric Telemetry" design system (Dark Theme). We are adopting the architectural patterns seen in `applications/kocolor`, upgrading the app to fully utilize Modern Android Development (MAD) tools, including Adaptive Layouts, Navigation3, Google Health Connect, and bounded On-Device/Cloud AI.

Crucially, this plan prioritizes responsible health data handling: AI will be advisory (not a dosing authority), health records will be strictly permissioned via Health Connect, and local calculations for caffeine clearance will be deterministic and transparently estimated.

## Proposed Changes

### 1. Architecture & Adaptive Navigation (`apps/mobile`)

Adopt the `NavigationSuiteScaffold` from `kocolor` to provide Adaptive Layouts (phones vs tablets/foldables).
- Update `GoSwiftMainScreen.kt` to use `NavigationSuiteScaffold` with adaptive layouts.
- Use `currentWindowAdaptiveInfoV2()` (where supported by the chosen Compose Material 3 Adaptive version) for window-size and posture information.
- Retain Jetpack Navigation 3, ensuring entry decorators are ordered correctly: `rememberSaveableStateHolderNavEntryDecorator()` followed by `rememberViewModelStoreNavEntryDecorator()` for entry-scoped ViewModels.
- Ensure Edge-to-Edge is properly configured with system bar insets handled organically.

#### [MODIFY] [GoSwiftMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/src/main/java/com/zoewave/probase/goswift/mobile/ui/components/GoSwiftMainScreen.kt)

---

### 2. Design System & Theming (`apps/mobile/ui/theme`)

Implement the "Warm Biometric Telemetry" tokens into the Jetpack Compose Theme, reconciling the Stitch `DESIGN.md` inconsistencies:
- **Colors:** Use a consistent dark-centric Material 3 `ColorScheme`. Base surface: `#101418`. Primary: `#ffb4aa` (with `#FF6F61` for primary-container/accents). Secondary: `#FDBA74`. Tertiary: `#FEF08A`.
- **Typography:** Add Space Grotesk (Headlines), Manrope (Body), and JetBrains Mono (Data & Telemetry Readouts). Map the Stitch type scale to scalable Compose `sp` values, ensuring minimum tap target and legibility standards.
- **Shapes & Components:** Build base components (Primary/Secondary Buttons, Biometric Chips). Use tonal surfaces, translucent layers, and subtle borders instead of complex `backdrop-blur` where performance dictates. Fix the broken logo header from the mockups with a vector asset or typography.

#### [MODIFY] [GoSwiftTheme.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/src/main/java/com/zoewave/probase/goswift/mobile/ui/theme/GoSwiftTheme.kt)

---

### 3. Feature: Home / Live Fuel Dashboard (`features/home`)

- Build the "Metabolic Fuel Ring" component (Multi-segmented progress arc).
- **Domain Correction:** Display explicit estimates labeled as "Estimated caffeine remaining" and "Your configured daily limit". Remove unsafe mockup claims like serum caffeine in mg/dL.
- Integrate with Room/Hilt to read current caffeine levels using a documented calculation model with configurable assumptions and timestamped intake records.

#### [MODIFY] Home UI Components

---

### 4. Feature: Schedule (`features/schedule`)

- **Module Restructure:** Inspect the existing Gradle project and dependency graph before restructuring/renaming to preserve working code. Transition to `features/schedule` (handling Reminders, planned intake, user-configured limits).
- Use horizontal swipe cards for telemetry timeline breakdowns.
- **Bounded AI:** AI (Gemini) is strictly optional, placed behind an interface and a feature flag. A deterministic local engine handles intake totals and clearance estimates. AI summarizes logged trends and explains observations, with graceful fallbacks for unsupported devices, model errors, quotas, and offline use.

#### [NEW/MODIFY] Schedule UI Components

---

### 5. Shared Health Data Repository (`data/health`)

- Extract Health Connect queries into a shared repository within a data or health module.
- Handle required permissions, explicitly accounting for the additional historical-read permission for data older than 30 days and background read requirements.

#### [NEW] Health Repository

---

### 6. Feature: Health Sync (`features/healthsync`)

- **Module Restructure:** Create/rename to `features/healthsync`.
- Owns the UI for toggling Health Connect sync permissions, defining data sources, and showing sync status utilizing standard Health Connect permission contracts. It will consume the shared Health Repository.

#### [NEW/MODIFY] Health Sync UI Components

---

### 7. Feature: Insights (`features/insights`)

- **Module Restructure:** Create/rename to `features/insights`.
- Consumes the shared Health Repository to display available, permissioned sleep, heart rate (HRV), and activity data alongside caffeine logs. Health Connect access logic is kept strictly out of the UI layer.
- Create analytical charts (Sparkline Waveforms).

#### [NEW/MODIFY] Insights UI Components

---

### 8. Feature: Settings (`features/settings`)

- Manage Appearance, privacy, AI preferences, and app configuration.

#### [MODIFY] Settings UI Components

## Verification Plan

### Automated Tests
- **Calculations:** Unit tests for the documented caffeine clearance model, unit conversions, timezone changes, daylight-saving changes, and repeated entries.
- **Health Connect:** Tests for denied/revoked permissions, missing metrics, and stale data.
- **AI/Network:** Test provider routing and fallback when the on-device model is unavailable, cloud quota is exceeded, or the app is offline.
- **Persistence:** Verify Room persistence and ensure privacy boundaries (health records are not silently sent to cloud prompts).

### Manual Verification
- Deploy the `GoSwift` mobile app to an emulator or physical device.
- Verify Adaptive Navigation switches from Bottom Bar to Navigation Rail on wider screens utilizing `currentWindowAdaptiveInfoV2()`.
- Verify Health Connect permission flows work end-to-end, including >30 days history permissions if applicable.
- Check Edge-to-Edge compliance (no overlapping content with the system bars).
- Ensure large text/font scaling respects bounds and doesn't break the layout.