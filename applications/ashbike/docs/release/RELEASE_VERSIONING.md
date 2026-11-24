# 🔢 AshBike Versioning Strategy & Non-Collision Policy

**Package Identifier:** `com.zoewave.probase.ashbike` (Shared between Mobile & Wear OS)

## 1. Versioning Scheme

AshBike follows Semantic Versioning (`MAJOR.MINOR.PATCH`) alongside strict, monotonically increasing integer `versionCode` values.

| Form Factor | Package ID | Current `versionCode` | Current `versionName` |
|---|---|---|---|
| **Mobile App** | `com.zoewave.probase.ashbike` | **5** | **`0.0.5`** |
| **Wear OS App** | `com.zoewave.probase.ashbike` | **2** | **`0.0.1`** |

## 2. Play Console Non-Collision Rule

Because Google Play Store treats Mobile and Wear OS as multi-form-factor releases under the exact same package ID (`com.zoewave.probase.ashbike`), **no two uploaded `.aab` bundles can share the same `versionCode`**.

### Allocation Convention:
* **Mobile Releases:** Odd `versionCode` numbers (e.g., 1, 3, 5, 7, 9) or offset numbers starting at 1000+ (e.g., 10005).
* **Wear OS Releases:** Even `versionCode` numbers (e.g., 2, 4, 6, 8) or offset numbers starting at 2000+ (e.g., 20001).

Before releasing a new `.aab` bundle, developers must check both `applications/ashbike/apps/mobile/build.gradle.kts` and `applications/ashbike/apps/wear/build.gradle.kts` to guarantee `versionCode` uniqueness.
