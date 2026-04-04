# Bring Seaweed Mobile & Wear Apps to Gold Standard

Align `seaweed` mobile and wear applications with the "Gold Standard" architecture and patterns established in the `photodo` app. This includes refactoring the mobile app, creating a mobile `core` module, and adding `home` and `transactions` feature modules for WearOS.

## User Review Required

- [ ] **Mobile Core Module**: I'm creating a new `core` module for `seaweed` mobile to host theme and shared UI components, matching the `photodo` structure.
- [ ] **WearOS Features**: I'm adding `home` and `transactions` feature modules under `seaweed/apps/wear/features`.

## Proposed Changes

### [New] Seaweed Mobile Core Module
Create a dedicated `core` module for seaweed mobile app to host shared theme and UI components.

#### [NEW] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/core/build.gradle.kts)
- Define a basic library module with Compose and Hilt support.

#### [NEW] [SeaweedTheme.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/core/src/main/java/com/zoewave/probase/seaweed/mobile/core/ui/theme/SeaweedTheme.kt)
- Host the refactored, versioned theme for mobile.

---

### [Refactor] Seaweed Mobile App & Features
Update the main mobile app and its features to use the new `core` module and adopt "Gold Standard" patterns.

#### [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/build.gradle.kts)
- Update dependencies to include the new `core` module.
- Align with `photodo`'s plugin configuration.

#### [SeaweedMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/src/main/java/com/zoewave/probase/seaweed/mobile/ui/components/SeaweedMainScreen.kt)
- Refactor to use adaptive layout patterns and a dedicated bottom bar.

---

### [New] Seaweed WearOS Features
Create feature modules for WearOS to enable modular development.

#### [NEW] [home/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/features/home/build.gradle.kts)
- Define the home feature module for WearOS.

#### [NEW] [transactions/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/features/transactions/build.gradle.kts)
- Define the transactions feature module for WearOS.

---

### Project Configuration
Register new modules in the root project.

#### [settings.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/settings.gradle.kts)
- Include `:applications:seaweed:apps:mobile:core`.
- Include `:applications:seaweed:apps:wear:features:home`.
- Include `:applications:seaweed:apps:wear:features:transactions`.

## Verification Plan

### Automated Tests
- Run Gradle sync to verify project structure.
- `gradlew applications:seaweed:apps:mobile:assembleDebug`
- `gradlew applications:seaweed:apps:wear:assembleDebug`

### Manual Verification
- Verify the mobile app launches correctly on a phone emulator.
- Verify the wear app launches correctly on a wear emulator.
- Check navigation and theme consistency across both platforms.
