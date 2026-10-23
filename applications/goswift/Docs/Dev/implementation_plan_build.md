# Redo GoSwift Application - Stitch UI Migration & MAD Upgrade

The goal of this implementation is to completely revamp the `applications/goswift` module (a caffeine tracker) based on the newly provided Stitch export. The design shifts to a high-precision "Warm Biometric Telemetry" design system (Dark Theme). We are adopting the architectural patterns seen in `applications/kocolor`, upgrading the app to fully utilize Modern Android Development (MAD) tools, including Adaptive Layouts, Navigation3, Google Health Connect, and bounded On-Device/Cloud AI.

Crucially, this plan prioritizes responsible health data handling: AI will be advisory (not a dosing authority), health records will be strictly permissioned via Health Connect, and local calculations for caffeine clearance will be deterministic and transparently estimated.

## Proposed Changes

### 1. Architecture & Adaptive Navigation (`apps/mobile`)

Adopt the `NavigationSuiteScaffold` from `kocolor` to provide Adaptive Layouts (phones vs tablets/foldables).
- Update `GoSwiftMainScreen.kt` to use `NavigationSuiteScaffold` with `WindowSizeClass`.
- Retain Jetpack Navigation 3 but ensure backstack and entry decorators (`rememberSaveableStateHolderNavEntryDecorator`) follow the latest patterns.
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
- **Domain Correction:** Display *estimated* caffeine remaining (mg) and logged intake. Remove toxic/lethal "Serum Level (mg/dL)" mockup claims. Use explicit labels like "Estimated caffeine remaining" and "Personal daily limit" instead of "Safe Ceiling".
- Integrate with Room/Hilt to read current metabolic burn/caffeine levels deterministically.

#### [MODIFY] Home UI Components

---

### 4. Feature: Schedule (`features/schedule`)

- **Module Restructure:** Create/rename to `features/schedule` (handling Reminders, planned intake, user-configured limits).
- Use horizontal swipe cards for telemetry timeline breakdowns.
- **Bounded AI:** Use a deterministic local engine for intake totals and clearance estimates. AI (Gemini) is strictly advisory, summarizing logged trends and explaining observations, falling back gracefully if offline or unsupported.

#### [NEW/MODIFY] Schedule UI Components

---

### 5. Feature: Health Sync (`features/healthsync`)

- **Module Restructure:** Create/rename to `features/healthsync`.
- Create UI for toggling Health Connect sync permissions, defining data sources, and showing sync status utilizing standard Health Connect permission contracts.

#### [NEW/MODIFY] Health Sync UI Components

---

### 6. Feature: Insights (`features/insights`)

- **Module Restructure:** Create/rename to `features/insights`.
- Integrate Google Health Connect to pull available, permissioned sleep, heart rate (HRV), and activity data to display alongside caffeine logs.
- Create analytical charts (Sparkline Waveforms).

#### [NEW/MODIFY] Insights UI Components

---

### 7. Feature: Settings (`features/settings`)

- Manage Appearance, privacy, AI preferences, and app configuration.

#### [MODIFY] Settings UI Components

## Verification Plan

### Automated Tests
- **Calculations:** Unit tests for caffeine clearance math, unit conversions, timezone changes, and repeated entries.
- **Health Connect:** Tests for denied/revoked permissions, missing metrics, and stale data.
- **AI/Network:** Tests using a fake provider for deterministic local AI unavailability, cloud quota exceeded, and offline operation fallbacks.
- **Persistence:** Verify Room persistence and ensure privacy boundaries (health records are not silently sent to cloud prompts).

### Manual Verification
- Deploy the `GoSwift` mobile app to an emulator or physical device.
- Verify Adaptive Navigation switches from Bottom Bar to Navigation Rail on wider screens.
- Verify Health Connect permission flows work end-to-end.
- Check Edge-to-Edge compliance (no overlapping content with the system bars).
- Ensure large text/font scaling respects bounds and doesn't break the layout.