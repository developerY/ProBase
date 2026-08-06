# Implementation Plan - Phase 1: The Static-First Compression Pipeline

Migrate the KoColor Secure Package Distribution Platform from plaintext JSON payloads to compressed, signed binary packages (`.kpkg`) using Zstandard (zstd) and Ed25519 signatures. This phase focuses on bandwidth optimization and zero-trust security without requiring dynamic infrastructure.

## Architectural Principle

> [!IMPORTANT]
> **Verify-First Rule**: All binary payloads must be cryptographically verified (SHA-256 integrity + Ed25519 authenticity) before decompression. This protects against "zip bomb" attacks and ensures that the client only processes trusted, uncorrupted data.

## User Review Required

> [!CAUTION]
> This change introduces a breaking update to the package format. The Rust compiler must be executed to generate the new `.kpkg` files before the updated Android client can successfully ingest data.

## Proposed Changes

### [Rust Backend (Compiler)]

#### [MODIFY] [Cargo.toml](file:///Users/developer/AndroidStudioProjects/ProBase/server/package/KoColor/Cargo.toml)
- Add `zstd = "0.13"` to the dependencies.

#### [MODIFY] [generate_payload.rs](file:///Users/developer/AndroidStudioProjects/ProBase/server/package/KoColor/src/bin/generate_payload.rs)
- Update `save_signed_payload` to implement the new binary transformation pipeline:
    1. **Serialize**: Minified JSON serialization (`serde_json::to_vec`).
    2. **Compress**: Zstd compression (level 3).
    3. **Hash**: Calculate SHA-256 strictly on the **compressed bytes**.
    4. **Sign**: Generate Ed25519 signature strictly on the **compressed bytes**.
    5. **Output**: Save as `[id].kpkg`.
- Update manifest generation:
    - Point to `.kpkg` endpoints.
    - Include the hashes/signatures of the compressed binaries.
    - **Optimization**: Include `uncompressed_size_bytes` for each pack to facilitate safe memory allocation on memory-constrained devices (e.g., Pixel Watch).

---

### [Android Client]

#### [MODIFY] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/build.gradle.kts)
- Add `implementation("com.github.luben:zstd-jni:1.5.5-4")` for high-performance native decompression.

#### [MODIFY] [KocolorApiService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/KocolorApiService.kt)
- Add `downloadPackageBinary(packId: String): ResponseBody` to fetch raw binary data.

#### [MODIFY] [StarterPackRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/StarterPackRepository.kt)
- Update `getPackItems` to implement the verified decompression pipeline:
    1. **Fetch**: Download raw binary bytes (`.kpkg`).
    2. **Integrity**: Verify SHA-256 hash against the manifest.
    3. **Authenticity**: Verify Ed25519 signature via `SignatureVerifier`.
    4. **Decompress**: Invoke `Zstd.decompress()` ONLY if the above steps pass.
        - **Memory Safety**: Use `uncompressed_size_bytes` from the manifest to allocate the exact buffer size required, preventing `OutOfMemory` errors on wearables.
    5. **Ingest**: Parse the decompressed JSON and persist to Room in an atomic transaction.

#### [MODIFY] [PackSyncRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/repository/PackSyncRepositoryImpl.kt)
- Align the legacy ingestion logic with the new binary verification pipeline.

---

### [Data Models]

#### [MODIFY] [PackManifest.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/model/PackManifest.kt)
- Add `uncompressed_size_bytes` field to `PackInfo`.

#### [MODIFY] [Provenance.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/Provenance.kt)
- Ensure the `Provenance` object correctly reflects the verification status of the compressed payload.

## Verification Plan

### Automated Tests
- **Decompression Unit Tests**: Verify that `Zstd.decompress` correctly restores minified JSON on Android.
- **Security Rejection Tests**: Confirm that the client throws `PackException.SignatureException` or `PackException.ManifestException` if the binary hash or signature is tampered with, and that decompression is *never* called in these cases.

### Manual Verification
1. **Run Compiler**: Execute the Rust script and confirm `.kpkg` files are generated.
2. **Deploy**: Update the local/remote CDN with the new binaries and manifest.
3. **Android Sync**: Navigate to the Sync Hub in the app and verify search and preview still function.
4. **Import**: Confirm that importing a pack works end-to-end and data appears in the local inventory.
5. **Logcat Audit**: Monitor logs to ensure the verification sequence follows the **Integrity -> Authenticity -> Decompression** order.
