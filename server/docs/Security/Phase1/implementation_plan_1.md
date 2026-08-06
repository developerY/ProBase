# Implementation Plan - Phase 1: The Static-First Compression Pipeline

Migrate the KoColor Secure Package Distribution Platform from plaintext JSON payloads to compressed, signed binary packages (`.kpkg`) using Zstandard (zstd) and Ed25519 signatures.

## Architectural Principle

> [!IMPORTANT]
> **Verify-First Rule**: All binary payloads must be cryptographically verified (SHA-256 integrity + Ed25519 authenticity) before decompression. This provides immunity against "zip bomb" attacks and ensures only trusted data is processed by the client.

## User Review Required

> [!CAUTION]
> This change introduces a breaking change in the package format. The Rust compiler must be run to generate the new `.kpkg` files before the updated Android client can successfully ingest data.

## Proposed Changes

### [Rust Backend (Compiler)]

#### [MODIFY] [Cargo.toml](file:///Users/developer/AndroidStudioProjects/ProBase/server/package/KoColor/Cargo.toml)
- Add `zstd = "0.13"` dependency.

#### [MODIFY] [generate_payload.rs](file:///Users/developer/AndroidStudioProjects/ProBase/server/package/KoColor/src/bin/generate_payload.rs)
- Update `save_signed_payload` to implement the new binary pipeline:
    1. Minified JSON serialization.
    2. Zstd compression (level 3).
    3. SHA-256 hashing of **compressed bytes**.
    4. Ed25519 signing of **compressed bytes**.
    5. Save as `[id].kpkg`.
- Update manifest generation to use `.kpkg` endpoints and include computed hashes/signatures.

---

### [Android Client]

#### [MODIFY] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/build.gradle.kts)
- Add `implementation("com.github.luben:zstd-jni:1.5.5-4")`.

#### [MODIFY] [KocolorApiService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/KocolorApiService.kt)
- Add `downloadPackageBinary(packId: String): ResponseBody` to fetch the new `.kpkg` artifacts.

#### [MODIFY] [StarterPackRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/StarterPackRepository.kt)
- Update `getPackItems` to implement the new verification and decompression pipeline:
    1. Download raw binary bytes.
    2. Verify SHA-256 integrity against the manifest.
    3. Verify Ed25519 authenticity using the existing `SignatureVerifier`.
    4. Decompress using `Zstd.decompress`.
    5. Parse JSON and persist to Room.

#### [MODIFY] [PackSyncRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/repository/PackSyncRepositoryImpl.kt)
- Align the legacy ingestion flow with the new `.kpkg` binary verification logic.

## Verification Plan

### Automated Tests
- Unit tests for the new `Zstd` decompression logic on Android.
- Verification that tampered `.kpkg` files (incorrect hash or signature) are rejected before decompression.

### Manual Verification
1. Run the updated Rust compiler to generate `.kpkg` and `manifest.json`.
2. Push the new files to the local/remote CDN.
3. Launch the KoColor Android app and navigate to the Sync Hub.
4. Verify that packs can be searched, previewed, and imported successfully.
5. Check logcat to confirm that verification occurs before decompression.
