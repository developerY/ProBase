Here is the execution-grade prompt to feed directly into your GenAI agent. It specifies the integration of the `zstd` crate for your Rust backend and the `zstd-jni` library for your Android client to establish the secure `.kpkg` binary pipeline.

---

**Copy everything below this line:**

> **Role:** Expert Rust & Android Software Architect and Cryptography Specialist
> **Context:**
> We are implementing "Phase 1: The Compression Era" for the KoColor Secure Package Distribution Platform. We are migrating our individual payload files from raw `.json` files to compressed `.kpkg` binaries using Zstandard (zstd) to reduce CDN bandwidth and speed up mobile ingestion. The root `manifest.json` will remain plaintext JSON.
> **Current Architecture Overview:**
> 1. Rust normalizes external data into Canonical JSON, hashes the bytes (SHA-256), signs them (Ed25519), and saves as `.json`.
> 2. Android downloads the `.json` using Retrofit, intercepts the raw bytes, verifies the SHA-256 and Ed25519 signatures using BouncyCastle, and then deserializes to persist atomically in Room.
>
>
> **Task 1: Rust Compiler Setup (`kocolor-compiler`)**
> * Add the `zstd` crate to `Cargo.toml`.
> * Modify the Rust pipeline so that *after* serializing the DTOs to JSON bytes, it compresses those bytes using Zstandard.
> * Calculate the SHA-256 hash and Ed25519 signature on the *compressed* bytes (the zstd output), NOT the plaintext JSON.
> * Output the final signed payload as a `[package_id].kpkg` binary file instead of `.json`.
> * Update the `manifest.json` generator to reflect the hash of the compressed `.kpkg` payload.
>
>
> **Task 2: Android Setup (`:features:starterpack`)**
> * Add the Zstandard JNI dependency to the `build.gradle.kts`: `implementation("com.github.luben:zstd-jni:1.5.5-4")`
> * Modify the Retrofit `KocolorApiService` to download the `.kpkg` file as a raw `ResponseBody` or `ByteArray`.
>
>
> **Task 3: Android Verification & Decompression**
> * Update `SignatureVerifier` (and the `StarterPackRepositoryImpl` pipeline) to accept the compressed `.kpkg` byte array.
> * The integrity (SHA-256) and authenticity (Ed25519 via BouncyCastle) checks must run strictly against the *compressed* bytes.
> * If verification passes, use `Zstd.decompress` or `ZstdInputStream` to decompress the byte array back into the canonical JSON string.
> * Pass the decompressed JSON string to the deserializer and commit to the Room database inside an atomic `@Transaction`.
>
>
> **Strict Constraints (CRITICAL):**
> * **DO NOT** modify any Jetpack Compose UI files.
> * **DO NOT** break the existing BouncyCastle Ed25519 verifier logic.
> * **Security Rule:** Verification must happen *before* decompression. You must never decompress an untrusted stream.
> * **State Management:** Maintain the current atomic Room `@Transaction` for the final data insertion.
>
>
> **Execution Phase:**
> Please analyze my workspace, identify the `kocolor-compiler` logic and the `StarterPackRepositoryImpl`, and implement these tasks.