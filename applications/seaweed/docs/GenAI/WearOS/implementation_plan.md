# Add Wear OS version for Seaweed Application

This plan outlines the addition of a Wear OS module for the Seaweed budgeting application. The new module will reuse existing data, model, and database layers while providing a specialized UI for wearable devices using Wear Compose and Navigation 3.

## User Review Required

- **Scope of Wear App**: Initial version will include a summary of spending (Total balance, top categories) and a list of recent transactions.
- **Shared ViewModels**: We'll create Wear-specific ViewModels or use the existing ones if they are generic enough. The existing ones in `mobile` package seem to have `mobile` in their package name, so we'll likely create Wear-specific versions or refactor common logic.

## Proposed Changes

### [Project Configuration]

#### [settings.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/settings.gradle.kts)

- Include the new `:applications:seaweed:apps:wear` module.

---

### [Seaweed Wear Application]

#### [NEW] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/build.gradle.kts)

- Define the Wear OS application module.
- Add dependencies for Wear Compose Material3, Navigation3, Horologist, and existing Seaweed data/model modules.

#### [NEW] [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/src/main/AndroidManifest.xml)

- Define the application and activity with Wear OS features.

#### [NEW] [SeaweedWearApp.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/src/main/java/com/zoewave/probase/seaweed/wear/SeaweedWearApp.kt)

- Hilt application class.

#### [NEW] [MainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/src/main/java/com/zoewave/probase/seaweed/wear/MainActivity.kt)

- Main entry point for the Wear app.

#### [NEW] [SeaweedWearMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/src/main/java/com/zoewave/probase/seaweed/wear/ui/SeaweedWearMainScreen.kt)

- Root UI component using `SwipeToDismissBox` and `NavDisplay` for navigation.

#### [NEW] [SeaweedWearNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/src/main/java/com/zoewave/probase/seaweed/wear/ui/navigation/SeaweedWearNavEntryProvider.kt)

- Navigation logic for Wear OS using Navigation 3.

#### [NEW] [SeaweedWearTheme.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/src/main/java/com/zoewave/probase/seaweed/wear/ui/theme/SeaweedWearTheme.kt)

- Wear-specific Material3 theme.

---

### [Seaweed Wear Features]

#### [NEW] [HomeRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/src/main/java/com/zoewave/probase/seaweed/wear/features/home/HomeRoute.kt)

- Main summary screen for the watch.

#### [NEW] [TransactionListRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/src/main/java/com/zoewave/probase/seaweed/wear/features/transaction/TransactionListRoute.kt)

- Recent transactions list for the watch.

## Verification Plan

### Automated Tests

- **Build**: `./gradlew :applications:seaweed:apps:wear:assembleDebug` to verify the new module compiles.

### Manual Verification

- **Wear Emulator**: Run the app on a Wear OS emulator to verify:
    - Navigation between Home and Transactions works (swipe-to-dismiss).
    - Data from the shared database is correctly displayed.
    - Theme is applied correctly.
