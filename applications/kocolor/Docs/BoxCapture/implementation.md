# Isolated Box Capture Integration for KoColor

This plan describes how to safely bring the **“Box & Product Capture”** feature from the future branch into the KoColor-capture branch while maintaining strict architectural isolation and following the new core ritual model structure.

---

## User Review Required

> [!IMPORTANT]
> The feature will be placed strictly within
> `applications/kocolor/features/boxcapture`.
>
> No shared feature or other application will have access to this module, ensuring that any logic within it cannot “bleed” into other projects.

---

## Proposed Changes

### Module: `applications:kocolor:features:boxcapture` [NEW]

We will create this module by copying the code from the future branch and applying the following structural updates:

---

### [NEW] `build.gradle.kts`

* Configuration based on the future branch
* Updated dependencies:

    * Replace `:applications:kocolor:model`
    * Use:

        * `:core:model`
        * `:core:data`

---

### [NEW] `BoxCaptureViewModel.kt`

* Update imports:

    * From:

        * `com.zoewave.probase.kocolor.model.*`
    * To:

        * `com.zoewave.probase.core.model.ritual.*`

* Update:

    * `AiConfigurationSettings` import to:

        * `com.zoewave.probase.core.data.repository.AiConfigurationSettings`

* Use the **unnamed `AiConfigurationSettings`** (as refactored previously)

---

### [NEW] `LocalProductAnalyzer.kt`

* Update imports to use relocated **core models**
* Ensure no dependency on legacy `kocolor.model`

---

## Project Configuration

### [MODIFY] `settings.gradle.kts`

* Include new module:

```kotlin
include(":applications:kocolor:features:boxcapture")
```

---

## KoColor Application Integration

### [MODIFY] `KoColorRoute.kt`

* Add new route:

```kotlin
data class BoxCapture(val mode: String = "BOX") : KoColorRoute()
```

---

### [MODIFY] `KoColorNavEntryProvider.kt`

* Register new route mapping:

    * Add `BoxCaptureRoute` composable entry
    * Ensure navigation graph is updated cleanly

---

### [MODIFY] `VanityLandingScreen.kt`

* Add UI entry point:

    * “Capture Box” button (Top App Bar or Card)
* This triggers navigation into Box Capture flow

---

### [MODIFY] App `build.gradle.kts`

* Add dependency:

```kotlin
implementation(project(":applications:kocolor:features:boxcapture"))
```

---

## Verification Plan

### Automated Tests

Run the following builds:

```bash
./gradlew :applications:kocolor:features:boxcapture:assembleDebug
```

```bash
./gradlew :applications:kocolor:apps:mobile:assembleDebug
```

```bash
./gradlew :applications:ashbike:apps:mobile:assembleDebug
```

* Ensure no transitive dependency leaks across apps

---

## Manual Verification

* Verify “Capture Box” flow launches from Vanity screen
* Confirm navigation enters BoxCapture feature module correctly
* Validate Gemini Cloud extraction (if API key is set):

    * Correctly populates `CosmeticItem`
    * Produces structured professional metadata

---

## Summary

This integration keeps **Box Capture fully isolated**, ensuring:

* No shared model leakage
* Strict core-model dependency alignment
* Clean navigation boundary in KoColor
* Safe multi-app coexistence within the platform

---

If you want, I can also convert this into a **Jira epic + subtasks structure** or a **GitHub PR template with checkboxes and CI gates**.
