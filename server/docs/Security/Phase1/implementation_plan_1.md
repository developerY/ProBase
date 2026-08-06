# Implementation Plan - Phase 1: The .kpkg Binary Distribution Protocol

Introduce the `.kpkg` binary package format as the canonical distribution artifact for the KoColor Secure Package Distribution Platform. This phase establishes a highly compressed, cryptographically signed pipeline using Zstandard (zstd) and Ed25519 signatures to optimize bandwidth and enforce zero-trust security.

## Design Goals

- **Zero dynamic infrastructure**: Strictly static CDN-based distribution.
- **Immutable package distribution**: Content-addressed artifacts for reliable caching.
- **Deterministic package generation**: Reproducible builds and stable cryptographic hashes.
- **Zero-trust verification**: All security decisions derived exclusively from verified manifest data.
- **CDN-first deployment**: Optimized for edge networks (Cloudflare).
- **Offline-first clients**: Local-first ingestion for high-performance mobile access.
- **Canonical data model**: Insulated from external vendor schema changes.
- **Cross-platform compatibility**: Unified protocol for Android, Wear OS, and XR.

## Architectural Principles

> [!IMPORTANT]
> **Package Format Contract**: The `.kpkg` binary is the canonical distribution artifact. Client applications never consume raw vendor JSON or unsigned payloads.

> [!IMPORTANT]
> **Canonical Signing Rule**: The compressed `.kpkg` byte stream is the canonical artifact. All hashes and digital signatures are computed over the compressed bytes exactly as served by the CDN.

> [!IMPORTANT]
> **Verify-First Rule**: All binary payloads must be cryptographically verified before decompression. This protects against "zip bomb" attacks and ensures that the client only processes trusted data.
> - **SHA-256**: Detects transmission corruption.
> - **Ed25519**: Proves publisher authenticity.

> [!IMPORTANT]
> **Zero-Trust Metadata**: Clients never trust CDN metadata (headers, file names). All security decisions (size, hash, signature) are derived exclusively from cryptographically verified manifest data.

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
    2.  **Compress**: Zstd compression at **Level 3** (optimized for mobile decompression speed).
    3.  **Hash**: Calculate SHA-256 strictly on the **compressed bytes**.
    4.  **Sign**: Generate Ed25519 signature strictly on the **compressed bytes**.
    5.  **Output**: Save as `<package-id>-<sha256>.kpkg` for immutable CDN caching.
- Update manifest generation:
    - Point to `.kpkg` endpoints (using immutable hashed filenames).
    - Include the hashes/signatures of the compressed binaries.
    - **Metadata Expansion**:
        - `manifest_version`: Initialized to `1`.
        - `key_id`: Identifier for the signing key (e.g., `"kocolor-root-v1"`) to support future key rotation.
        - `compressed_size_bytes`: Size of the `.kpkg` file for size validation.
        - `uncompressed_size_bytes`: Original size for deterministic memory allocation.
        - `compression_algorithm`: Default to `"zstd"`.
        - `hash_algorithm`: Default to `"sha256"`.
        - `signature_algorithm`: Default to `"ed25519"`.
        - `signature_encoding`: Default to `"hex"`.
        - `package_format_version`: Incremented to `1`.
        - `encryption`: Reserved field, set to `"none"`.

---

### [Android Client]

#### [MODIFY] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/build.gradle.kts)
- Add `implementation("com.github.luben:zstd-jni:1.5.5-4")` for native decompression.

#### [MODIFY] [KocolorApiService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/KocolorApiService.kt)
- Add `downloadPackageBinary(url: String): ResponseBody` for streaming `.kpkg` artifacts.

#### [MODIFY] [StarterPackRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/StarterPackRepository.kt)
- Update `fetchVerifiedPackage` to implement the refined verification and safety sequence:
    1.  **Stream**: Stream the binary package directly from the CDN.
    2.  **Size Validation**: Check `streamed_bytes == compressed_size_bytes` to catch truncated downloads.
    3.  **Integrity Check**: Verify SHA-256 hash.
    4.  **Authenticity Check**: Verify Ed25519 signature (decoded per `signature_encoding`).
    5.  **Version Negotiation**: Reject unknown `package_format_version` or `schema_version` exceeding supported maximums.
    6.  **Algorithm Validation**: Verify `compression_algorithm` and `signature_algorithm` are supported.
    7.  **Header Check**: Basic Zstd magic byte validation.
    8.  **Decompress**: Invoke `Zstd.decompress()` using `uncompressed_size_bytes` for exact buffer allocation.
    9.  **Parse & Validate**: Parse JSON into DTOs and perform structural validation against `schema_version`.
    10. **Map & Persist**: Map verified DTOs to domain models and commit via Room `@Transaction`.

---

### [Data Models]

#### [MODIFY] [PackManifest.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/model/PackManifest.kt)
- Reflect new fields: `manifest_version`, `key_id`, `uncompressed_size_bytes`, `compressed_size_bytes`, `compression_algorithm`, `hash_algorithm`, `signature_algorithm`, `signature_encoding`, `package_format_version`, `encryption`.

---

## Failure Handling

| Failure Point | Action |
| :--- | :--- |
| **Size Mismatch** | Reject (Truncated download). |
| **Hash Mismatch** | Reject (Corrupted payload). |
| **Signature Failure** | Reject (Unauthorized payload). |
| **Unknown Algorithm** | Reject (Unsupported client/format). |
| **Unknown Format Version** | Reject (Unsupported client). |
| **Schema Overflow** | Reject (Schema too new for client). |
| **Decompression Failure** | Reject (Malformed binary). |
| **Schema Mismatch** | Reject (DTO validation failed). |
| **Transaction Failure** | Rollback (Atomic consistency). |

## Verification Plan

### Automated Tests
- **Verification Pipeline Tests**: Unit tests for the sequential check logic (Size → Hash → Sig → Version).
- **Memory Safety Tests**: Verify buffer allocation logic using `uncompressed_size_bytes`.

### Manual Verification
1.  **Rust Build**: Generate v1 `.kpkg` artifacts using the `<id>-<hash>.kpkg` naming convention.
2.  **CDN Performance**: Verify Cloudflare serves hashed filenames with `Cache-Control: immutable`.
3.  **End-to-End**: Install a package on a Pixel Watch and monitor for `OutOfMemory` or CPU spikes during decompression.
