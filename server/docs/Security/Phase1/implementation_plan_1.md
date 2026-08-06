# Implementation Plan - Phase 1: The Static-First Compression Pipeline

Migrate the KoColor Secure Package Distribution Platform from plaintext JSON payloads to compressed, signed binary packages (`.kpkg`) using Zstandard (zstd) and Ed25519 signatures. This phase focuses on bandwidth optimization and zero-trust security.

> **Package Format Contract**: The `.kpkg` binary is the canonical distribution artifact. Client applications never consume raw vendor JSON or unsigned payloads.

## Architectural Principle

> [!IMPORTANT]
> **Verify-First Rule**: All binary payloads must be cryptographically verified before decompression. This protects against "zip bomb" attacks and ensures that the client only processes trusted data.
>
> *   **SHA-256**: Used for fast integrity checks, identification, CDN deduplication, and quick corruption detection.
> *   **Ed25519**: Used for authenticity and tamper resistance.

## User Review Required

> [!CAUTION]
> **All package assets must be regenerated using the updated Rust Normalization Compiler before deployment. Legacy JSON payloads are not compatible with the new binary package format.**

## Proposed Changes

### [Rust Backend (Compiler)]

#### [MODIFY] [Cargo.toml](file:///Users/developer/AndroidStudioProjects/ProBase/server/package/KoColor/Cargo.toml)
- Add `zstd = "0.13"` to the dependencies.

#### [MODIFY] [generate_payload.rs](file:///Users/developer/AndroidStudioProjects/ProBase/server/package/KoColor/src/bin/generate_payload.rs)
- Update `save_signed_payload` to implement the new binary transformation pipeline:
    1.  **Serialize**: Minified JSON serialization (`serde_json::to_vec`).
    2.  **Compress**: Zstd compression (level 3).
    3.  **Hash**: Calculate SHA-256 strictly on the **compressed bytes**.
    4.  **Sign**: Generate Ed25519 signature strictly on the **compressed bytes**.
    5.  **Output**: Save as `[id]-[hash].kpkg` for immutable CDN caching.
- Update manifest generation:
    - Point to `.kpkg` endpoints (using immutable hashed filenames).
    - Include the hashes/signatures of the compressed binaries.
    - **Optimization**: Include `uncompressed_size_bytes` and `compressed_size_bytes` for each pack to facilitate safe memory allocation and download progress UI.
    - **Future-proofing**: Include `compression_type` (e.g., `"zstd-v1"`).

---

### [Android Client]

#### [MODIFY] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/build.gradle.kts)
- Add `implementation("com.github.luben:zstd-jni:1.5.5-4")` for high-performance native decompression.

#### [MODIFY] [KocolorApiService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/KocolorApiService.kt)
- Add `downloadPackageBinary(url: String): ResponseBody` to fetch raw binary data using the full URL from the manifest.

#### [MODIFY] [StarterPackRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/StarterPackRepository.kt)
- Rename `getPackItems` to `fetchVerifiedPackage`.
- Implement the refined verification sequence:
    1.  **Download**: Stream binary bytes (`.kpkg`).
    2.  **Verify Manifest**: Ensure the manifest itself is signed and verified.
    3.  **Verify Package Hash**: Check SHA-256 against the manifest entry.
    4.  **Verify Signature**: Check Ed25519 authenticity.
    5.  **Verify Compression Header**: Perform basic sanity check on Zstd magic bytes before invoking JNI.
    6.  **Decompress**: Invoke `Zstd.decompress()` ONLY if all checks pass.
        - **Memory Safety**: Use `uncompressed_size_bytes` from the manifest to allocate the exact buffer size required.
    7.  **Ingest**: Parse the decompressed JSON and persist to Room in an atomic transaction.

---

### [Data Models]

#### [MODIFY] [PackManifest.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/model/PackManifest.kt)
- Add `uncompressed_size_bytes`, `compressed_size_bytes`, and `compression_type` fields to `PackInfo`.

#### [MODIFY] [Provenance.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/Provenance.kt)
- Add `contentHash` to track the exact immutable binary identity of the source package.

---

## Failure Handling

| Failure Point | Action |
| :--- | :--- |
| **Hash Mismatch** | Reject package immediately. |
| **Signature Failure** | Reject package immediately. |
| **Schema Mismatch** | Reject package (unsupported client). |
| **Decompression Failure** | Reject package (corrupt or malicious). |
| **Transaction Failure** | Rollback database changes. |

## Verification Plan

### Automated Tests
- **Decompression Unit Tests**: Verify `Zstd.decompress` restores minified JSON correctly.
- **Security Rejection Tests**: Verify rejection of tampered hashes, signatures, and malformed headers.

### Manual Verification
1.  **Run Compiler**: Confirm generation of `[id]-[hash].kpkg` and updated `manifest.json`.
2.  **Deploy**: Update CDN and verify Cloudflare cache performance for hashed filenames.
3.  **Import**: Confirm end-to-end import on a memory-constrained device (wearable).
4.  **Audit**: Verify provenance `contentHash` in the Room database matches the manifest.
