# Walkthrough - Firebase Crashlytics and Analytics Integration

I have successfully added Google Firebase Crashlytics and Google Analytics for Firebase to all application projects in the ProBase monorepo.

## Changes Made

### Configuration
- **Seaweed & GoSwift**: Added the `google-services` and `firebase-crashlytics` plugins to their respective `build.gradle.kts` files. Integrated the Firebase BOM along with Crashlytics and Analytics dependencies.
- **PhotoDo**: Added the missing `google-services` plugin. Firebase dependencies and the Crashlytics plugin were already present.
- **AshBike**: Verified that both plugins and dependencies were already correctly configured.

### Build Verification
I attempted to run an assemble build for all projects. The builds correctly identified the new plugins but halted because the `google-services.json` configuration files are missing for the new integrations.

> [!IMPORTANT]
> To complete the integration, you must now:
> 1. Download the `google-services.json` file from the Firebase Console for each project (**PhotoDo**, **Seaweed**, and **GoSwift**).
> 2. Place them in their respective application modules (e.g., `applications/seaweed/apps/mobile/google-services.json`).

## Verification Summary
- **Gradle Sync**: Completed successfully for all projects after adding the new configurations.
- **Plugin Validation**: The build process for `photodo`, `seaweed`, and `goswift` successfully triggered the `processDebugGoogleServices` task, confirming that the plugins are active and correctly searching for their configuration files.
