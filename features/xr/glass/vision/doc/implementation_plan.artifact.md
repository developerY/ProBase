# Add Glass Skills (AppFunctions)

Expose the vision and translation features of the AI Glasses as `AppFunctions` so they can be discovered and executed by system-level AI agents.

## User Review Required

> [!IMPORTANT]
> This requires updating the `targetSdk` to `36` as per the AppFunctions framework requirements.
> The implementation will use the Jetpack `androidx.appfunctions` library.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///Users/developer/AndroidStudioProjects/ProBase/gradle/libs.versions.toml)
- Update `android-targetSdk` to `36`.
- Add `appfunctions = "1.0.0-alpha09"` version.
- Add `androidx-appfunctions` and `androidx-appfunctions-compiler` libraries.

#### [MODIFY] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/build.gradle.kts)
- Add `appfunctions` dependencies and `ksp` configuration.

#### [MODIFY] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/translation/build.gradle.kts)
- Add `appfunctions` dependencies and `ksp` configuration.

### AppFunctions Implementation

#### [NEW] [VisionSkills.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/vision/src/main/java/com/zoewave/probase/features/glass/vision/data/VisionSkills.kt)
- Expose `describeScene` AppFunction.
- Integrate with `VisionViewModel` or a common repository/service.

#### [NEW] [TranslationSkills.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/xr/glass/translation/src/main/java/com/zoewave/probase/features/glass/translation/data/TranslationSkills.kt)
- Expose `translateSpeech` AppFunction.
- Integrate with `TranslationRepository`.

### System Configuration

#### [NEW] [app_metadata.xml](file:///Users/developer/AndroidStudioProjects/ProBase/app/src/main/res/xml/app_metadata.xml)
- Define app capabilities and operational patterns for the LLM.

#### [MODIFY] [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/app/src/main/AndroidManifest.xml)
- Link `app_metadata.xml`.

#### [MODIFY] [ProBaseApp.kt](file:///Users/developer/AndroidStudioProjects/ProBase/app/src/main/java/com/zoewave/probase/ProBaseApp.kt)
- Implement `AppFunctionConfiguration.Provider` to support Hilt-injected skill classes.

## Verification Plan

### Automated Tests
- `gradlew assembleDebug` to ensure KSP correctly generates the AppFunctions metadata.

### Manual Verification
- Use ADB commands (as per Step 4 of the AppFunctions skill) to list and invoke the new functions.
  - `adb shell cmd app_functions list-functions`
  - `adb shell cmd app_functions invoke-function ...`
