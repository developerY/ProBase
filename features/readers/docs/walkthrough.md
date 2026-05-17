# Restructured Features to include Readers category

I have completed the restructuring of the `/features` directory by grouping `qrscanner` and `nfc` under a new `readers` category and adding a new `barcode` module.

## Summary of Changes

### Directory Restructuring
- Moved `features/qrscanner` to `features/readers/qrscanner`.
- Moved `features/nfc` to `features/readers/nfc`.
- Created a new module `features/readers/barcode` for general barcode scanning.

### Package and Namespace Updates
- Updated namespaces in `build.gradle.kts` files to include the `readers` sub-package.
- Restructured source directories to match the new package hierarchy: `com.zoewave.probase.features.readers.*`.
- Updated all Kotlin package declarations and imports to reflect the new paths.

### Project Configuration
- Updated `settings.gradle.kts` with the new module paths (`:features:readers:nfc`, etc.).
- Updated all project references in `app` and other modules (like `ashbike`) to point to the new reader locations.
- Fixed unresolved references in `FeatureInventoryEntryProvider.kt`.

## Verification Results

### Automated Tests
- Successfully ran `./gradlew :features:readers:nfc:assembleDebug`
- Successfully ran `./gradlew :features:readers:qrscanner:assembleDebug`
- Successfully ran `./gradlew :features:readers:barcode:assembleDebug`
- Successfully ran `./gradlew :app:assembleDebug` to confirm project-wide integrity.

### Manual Verification
- Verified directory structure and file existence using terminal commands.
- Performed a global search (`grep`) to ensure no remaining references to the old module paths exist in active code.
