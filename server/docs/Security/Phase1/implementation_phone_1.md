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
- **Cross-platform compatibility**: A single canonical data model shared across Android, Wear OS, XR, and future clients. Package ingestion and verification is performed strictly by the primary Android application.

## Protocol Invariants

- Every package MUST be immutable.
- Every package MUST be independently verifiable.
- Every package MUST be deterministic.
- Every package MUST be content-addressed.
- Every package MUST be platform-independent.

## Architectural Principles

> [!IMPORTANT]
> **Trust Bootstrap**: Before any package metadata is trusted, the application MUST verify the signature of `manifest.json` using the embedded root public key. Only after successful verification may package metadata (size, hash, signature, algorithms) be used. This completes the chain of trust from the app binary to the CDN artifacts.

> [!IMPORTANT]
> **Package Format Contract**: The `.kpkg` binary is the canonical distribution artifact. Client applications MUST NOT consume raw vendor JSON or unsigned payloads.

> [!IMPORTANT]
> **Canonical Signing Rule**: The compressed `.kpkg` byte stream is the canonical artifact. All hashes and digital signatures MUST be computed over the compressed bytes exactly as served by the CDN.

> [!IMPORTANT]
> **Verify-First Rule**: All binary payloads MUST be cryptographically verified before decompression. This protects against "zip bomb" attacks and ensures that the client only processes trusted data.
> - **SHA-256**: Detects accidental corruption or modification.
> - **Ed25519**: Proves publisher authenticity.

> [!IMPORTANT]
> **Zero-Trust Metadata**: Clients MUST NOT trust CDN metadata (headers, file names). All security decisions (size, hash, signature) MUST be derived exclusively from cryptographically verified manifest data.

> [!IMPORTANT]
> **Root of Trust**: Root public keys MUST be embedded within the application binary and MUST NOT be downloaded from the network.

## User Review Required

> [!CAUTION]
> **All package assets MUST be regenerated using the updated Rust Normalization Compiler before deployment. Legacy JSON payloads are not compatible with the new binary package format.**

## Phase 1.1: Zero-Heap Mobile Ingestion Pipeline

Optimize the `.kpkg` ingestion pipeline to protect the heap during large package downloads. This update implements incremental hashing and disk-spooling for the primary Android application, acting as a stable data hub for downstream clients (Wear OS, XR).

### [Ingestion Sequence]

The application MUST follow this exact order of operations to maintain the security contract:

1.  **Download Manifest**: Fetch `manifest.json` from the CDN.
2.  **Verify Manifest**: Validate manifest signature using the embedded root public key.
3.  **Download Package**: Stream the `.kpkg` binary from the CDN.
4.  **Verify Hash**: Calculate SHA-256 incrementally while streaming to disk; reject if mismatch.
5.  **Verify Signature**: Perform Ed25519 authenticity check on the spooled binary.
6.  **Validate Schema & Format**: Negotiate `package_format_version` and `schema_version`.
7.  **Decompress**: Invoke Zstd decompression ONLY after all cryptographic proofs pass.
8.  **Parse**: Deserialize minified JSON into DTO models.
9.  **Room Transaction**: Open an atomic database transaction.
10. **Commit**: Persist to local inventory and finalize the installation record.

---

### [Android Network Layer]

#### [MODIFY] [KocolorApiService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/remote/KocolorApiService.kt)
- Add `@Streaming` annotation to `downloadPackageBinary`. This prevents OkHttp from buffering the entire response in memory.

---

### [Android Security Layer]

#### [MODIFY] [SignatureVerifier.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/domain/security/SignatureVerifier.kt)
- Add an overloaded `verify` method that accepts an Okio `Source`.
- Implement chunked processing in `KoColorEd25519Verifier` using the `Ed25519Signer.update()` API.

---

### [Android Repository Layer]

#### [MODIFY] [StarterPackRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/StarterPackRepository.kt)
- Update `fetchVerifiedPackage` to implement the [Ingestion Sequence](#ingestion-sequence) using the Okio pipeline:
    1.  **Spool to Disk**: Use `HashingSink` to write the network stream to a temporary file while calculating SHA-256.
    2.  **Verify Size & Hash**: Perform early rejection if the spooled file doesn't match manifest metadata.
    3.  **Verify Signature**: Stream the temporary file into the updated `SignatureVerifier`.
    4.  **Streaming Decompression**: Decompress into the JSON parser.
    5.  **Cleanup**: Ensure temporary files are deleted in a `finally` block.

---

## Failure Handling

| Failure Point | Action |
| :--- | :--- |
| **Manifest Validation** | Reject catalog (Critical trust failure). |
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
- **Streaming Verification Test**: Unit test the `Source`-based signature verification.
- **Memory Safety Tests**: Verify buffer allocation logic using `uncompressed_size_bytes`.

### Manual Verification
1.  **Rust Build**: Generate v1 `.kpkg` artifacts and verify manifest timestamps/versioning.
2.  **CDN Performance**: Verify Cloudflare cache performance for hashed filenames.
3.  **End-to-End Integration**: Install a package on an Android phone, verify it passes the `.kpkg` security checks, and confirm that the synchronized Wear OS companion UI correctly reflects the updated inventory.
