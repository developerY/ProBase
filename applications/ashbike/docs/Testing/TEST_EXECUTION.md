# Test Execution Guide

To ensure clean execution of AshBike tests without interference from unrelated compilation errors in other monorepo modules (such as experimental XR or Vision modules), you must strictly target the AshBike namespaces when invoking tests.

## Running Tests via Command Line

Run this command from the root of the project to safely test the entire AshBike stack:

```bash
./gradlew \
    :applications:ashbike:database:testDebugUnitTest \
    :applications:ashbike:data:testDebugUnitTest \
    :applications:ashbike:model:testDebugUnitTest \
    :applications:ashbike:features:main:testDebugUnitTest \
    :applications:ashbike:apps:mobile:testDebugUnitTest \
    :applications:ashbike:apps:mobile:features:home:testDebugUnitTest \
    :applications:ashbike:apps:mobile:features:rides:testDebugUnitTest \
    :applications:ashbike:apps:mobile:features:settings:testDebugUnitTest \
    :applications:ashbike:apps:mobile:features:glass:testDebugUnitTest \
    :applications:ashbike:apps:wear:testDebugUnitTest \
    :applications:ashbike:apps:wear:features:home:testDebugUnitTest \
    :applications:ashbike:apps:wear:features:rides:testDebugUnitTest \
    :applications:ashbike:apps:wear:features:settings:testDebugUnitTest \
    :features:health:core:testDebugUnitTest \
    :features:compliance:testDebugUnitTest \
    --continue
```
*Note: The `--continue` flag ensures Gradle will execute all targeted tests even if one module's tests fail.*

## Running Tests via Android Studio

1. Open the **Project Tool Window** on the left side of Android Studio.
2. Select the **Project** view (not the Android view).
3. Expand `applications` and right-click on the `ashbike` folder.
4. Select **Run 'Tests in ashbike'**.
5. You can also run individual feature modules tests by right-clicking on specific folders like `features/health` or `features/compliance` and selecting run.

**Important**: Do not run the global `./gradlew test` command, as it will attempt to compile unrelated experimental modules across the ProBase monorepo which may fail due to ongoing independent development.
