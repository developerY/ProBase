# Implementation Plan - Add Microphone Feature Module

Add a new isolated feature module `:features:microphone` that uses Android's `SpeechRecognizer` to transcribe speech to text and displays it in a Compose UI. This feature will be integrated into the main application's feature inventory.

## Proposed Changes![img.png](img.png)

### [Microphone Feature Module]

#### [NEW] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/features/microphone/build.gradle.kts)
Create the build configuration for the new module using the project's convention plugins.

#### [NEW] [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/features/microphone/src/main/AndroidManifest.xml)
Define the module's manifest and request the `RECORD_AUDIO` permission.

#### [NEW] [SpeechEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/microphone/src/main/java/com/zoewave/probase/features/microphone/SpeechEngine.kt)
Implement the core logic for speech recognition using `SpeechRecognizer`.

#### [NEW] [SpeechTestScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/microphone/src/main/java/com/zoewave/probase/features/microphone/SpeechTestScreen.kt)
Implement the Compose UI for the microphone test, handling permissions and displaying transcribed text.

---

### [Project Configuration]

#### [MODIFY] [settings.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/settings.gradle.kts)
Register the new `:features:microphone` module.

#### [MODIFY] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/app/build.gradle.kts)
Add `:features:microphone` as a dependency to the main app module.

---

### [Navigation & UI Integration]

#### [MODIFY] [FeatureInventory.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/nav3/src/main/java/com/zoewave/probase/features/nav3/ui/inventory/FeatureInventory.kt)
Add `Microphone` to the `FeatureInventory` sealed interface.

#### [MODIFY] [FeatureInventoryScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/nav3/src/main/java/com/zoewave/probase/features/nav3/ui/inventory/FeatureInventoryScreen.kt)
Add a callback and a `FeatureCard` for the Microphone feature in the inventory screen.

#### [MODIFY] [FeatureInventoryEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/app/src/main/java/com/zoewave/probase/ui/components/FeatureInventoryEntryProvider.kt)
Update the entry provider to handle navigation to the `SpeechTestScreen`.

## Verification Plan

### Manual Verification
1.  Build the project to ensure everything compiles correctly.
2.  Run the app on a device or emulator with microphone support.
3.  Navigate to the "System Features Inventory".
4.  Find and tap the "Microphone" feature.
5.  Grant microphone permission when prompted.
6.  Tap "Start" and speak to verify that transcribed text appears on the screen.
7.  Tap "Stop" to end the session.
