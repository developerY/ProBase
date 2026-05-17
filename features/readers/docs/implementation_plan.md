# Restructure Features to include Readers category

Group `qrscanner` and `nfc` features under a new `readers` category and add a new `barcode` reader feature.

## Proposed Changes

### Build Configuration

#### [settings.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/settings.gradle.kts)

- Remove `:features:nfc` and `:features:qrscanner`.
- Add `:features:readers:nfc`, `:features:readers:qrscanner`, and `:features:readers:barcode`.

### Feature Modules

#### [qrscanner](file:///Users/developer/AndroidStudioProjects/ProBase/features/qrscanner) -> [readers/qrscanner](file:///Users/developer/AndroidStudioProjects/ProBase/features/readers/qrscanner)

- Move directory `features/qrscanner` to `features/readers/qrscanner`.
- Update package names to `com.zoewave.probase.features.readers.qrscanner`.

#### [nfc](file:///Users/developer/AndroidStudioProjects/ProBase/features/nfc) -> [readers/nfc](file:///Users/developer/AndroidStudioProjects/ProBase/features/readers/nfc)

- Move directory `features/nfc` to `features/readers/nfc`.
- Update package names to `com.zoewave.probase.features.readers.nfc`.

#### [NEW] [barcode](file:///Users/developer/AndroidStudioProjects/ProBase/features/readers/barcode)

- Create a new module for general barcode scanning.
- Basic implementation using Google Code Scanner (ML Kit).

---

### External References

#### [app/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/app/build.gradle.kts)
#### [applications/ashbike/apps/mobile/features/settings/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/mobile/features/settings/build.gradle.kts)
#### [applications/ashbike/apps/wear/features/settings/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/ashbike/apps/wear/features/settings/build.gradle.kts)

- Update project references from `:features:nfc` to `:features:readers:nfc`.
- Update project references from `:features:qrscanner` to `:features:readers:qrscanner`.

## Verification Plan

### Automated Tests
- Run `./gradlew :features:readers:nfc:assembleDebug`
- Run `./gradlew :features:readers:qrscanner:assembleDebug`
- Run `./gradlew :features:readers:barcode:assembleDebug`
- Run `./gradlew :app:assembleDebug` to ensure no broken dependencies.

### Manual Verification
- Check IDE for any unresolved references.
- Verify directory structure in terminal.
