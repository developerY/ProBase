# Redo GoSwift Application - Stitch UI Migration & MAD Upgrade

The goal of this implementation is to completely revamp the `applications/goswift` module (a caffeine/microdose tracker) based on the newly provided Stitch export. The design shifts to a high-precision "Warm Biometric Telemetry" design system.

Additionally, we are adopting the architectural patterns seen in `applications/kocolor`, upgrading the app to fully utilize Modern Android Development (MAD) tools, including Adaptive Layouts, Navigation3, Google Health Connect, and On-Device/Cloud AI (Gemini).

## User Review Required

> [!WARNING]
> The exported `DESIGN.md` dictates a dark-themed, glassmorphic UI. Following the "Precision Minimalism" specification, we will strictly enforce a Dark-themed palette across the app, replacing the current Light/Dark split. Please confirm.

> [!IMPORTANT]
> The features will be repurposed and heavily integrated with new capabilities. Please confirm if this mapping and capability assignment makes sense:
> - `live_fuel_dashboard` -> `features/home`
> - `smart_microdose_schedule` -> `features/shots` **(AI-Powered Scheduling using Gemini API / ML Kit)**
> - `biometric_insights` -> `features/nutrition` **(Repurposed for biometric readouts, pulling from Google Health Connect)**
> - `health_connect_sync` -> `features/settings`

## Proposed Changes

### 1. Architecture & Adaptive Navigation (`apps/mobile`)

Adopt the `NavigationSuiteScaffold` from `kocolor` to provide Adaptive Layouts (phones vs tablets/foldables).
- Update `GoSwiftMainScreen.kt` to use `NavigationSuiteScaffold` with `WindowSizeClass`.
- Retain Jetpack Navigation 3 but ensure backstack and entry decorators (`rememberSaveableStateHolderNavEntryDecorator`) follow the latest patterns.
- Ensure Edge-to-Edge is properly configured with system bar insets handled organically.

#### [MODIFY] [GoSwiftMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/src/main/java/com/zoewave/probase/goswift/mobile/ui/components/GoSwiftMainScreen.kt)

---

### 2. Design System & Theming (`apps/mobile/ui/theme`)

Implement the "Warm Biometric Telemetry" tokens into the Jetpack Compose Theme using the pattern from `kocolor`:
- **Colors:** Overhaul `GoSwiftTheme.kt` with the dark-centric palette (`surface: #101418`, `primary: #FF6F61` Living Coral, `secondary: #FDBA74` Pastel Peach, `tertiary: #FEF08A`).
- **Typography:** Add Space Grotesk (Headlines), Manrope (Body), and JetBrains Mono (Data & Telemetry Readouts).
- **Shapes & Components:** Build base components (Primary/Secondary Buttons, Biometric Chips, Glassmorphic Cards with `backdrop-blur`).

#### [MODIFY] [GoSwiftTheme.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/goswift/apps/mobile/src/main/java/com/zoewave/probase/goswift/mobile/ui/theme/GoSwiftTheme.kt)

---

### 3. Feature: Home / Live Fuel Dashboard (`features/home`)

Migrate the `live_fuel_dashboard` into the `home` feature module.
- Build the "Metabolic Fuel Ring" component (Multi-segmented progress arc).
- Integrate with Room/Hilt to read current metabolic burn/caffeine levels.

#### [MODIFY] Home UI Components

---

### 4. Feature: Shots / Smart Schedule (`features/shots`)

Migrate the `smart_microdose_schedule` into the `shots` feature module.
- Implement Gemini AI Logic (like `kocolor`) to recommend optimal microdose timings based on previous logs and circadian rhythms.
- Use horizontal swipe cards for telemetry timeline breakdowns.

#### [MODIFY] Shots UI Components

---

### 5. Feature: Insights / Health Connect (`features/nutrition`)

Migrate the `biometric_insights` into the `nutrition` feature module.
- Integrate **Google Health Connect** to pull sleep, heart rate (HRV), and activity data to display alongside caffeine logs.
- Create analytical charts (Sparkline Waveforms) using `#FDBA74`.

#### [MODIFY] Nutrition UI Components (Repurposed for Biometrics)

---

### 6. Feature: Settings / Sync (`features/settings`)

Migrate the `health_connect_sync` into the `settings` feature module.
- Create UI for toggling Health Connect sync permissions and showing sync status, utilizing standard Health Connect permission contracts.

#### [MODIFY] Settings UI Components

## Verification Plan

### Automated Tests
- Run existing UI tests on the features to ensure they still compile and launch properly.
- Write a basic unit test for the Gemini AI logic to verify prompt execution.

### Manual Verification
- Deploy the `GoSwift` mobile app to an emulator or physical device.
- Verify Adaptive Navigation switches from Bottom Bar to Navigation Rail on wider screens.
- Verify Health Connect permission flows work end-to-end.
- Check Edge-to-Edge compliance (no overlapping content with the system bars).