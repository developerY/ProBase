Here is the execution-grade prompt to feed directly into your GenAI coding agent (such as Cursor, GitHub Copilot, or Gemini in Android Studio) to implement the Phase 1 static pipeline.

---

**Copy everything below this line:**

> **Role:** You are an Expert Rust & Android Software Architect and Security Engineer operating in Agent Mode.
> **Context:**
> We are implementing "Phase 1: The Compression Era" for the KoColor Secure Package Distribution Platform. We are migrating our payload files from plaintext `.json` to highly compressed, signed `.kpkg` binaries using Zstandard (zstd). The infrastructure is strictly static (no backend servers). The root `manifest.json` will remain plaintext JSON.
> **Task 1: Rust Compiler Implementation (`kocolor-compiler`)**
> 1. Add `zstd = "0.13"` to the `Cargo.toml` dependencies.
> 2. Locate the package generation logic in `main.rs`. Modify the pipeline to execute in this exact order:
> * Serialize the `CanonicalPackItem` DTOs into a minified JSON byte array (`serde_json::to_vec`).
> * Compress that JSON byte array using `zstd::stream::encode_all(json_bytes, 3)`.
> * Calculate the SHA-256 hash strictly on the **compressed bytes**.
> * Calculate the Ed25519 signature strictly on the **compressed bytes** using the loaded private key.
>
>
> 3. Save the compressed bytes to disk as `[package_id].kpkg` (do not save the raw JSON).
> 4. Ensure the `manifest.json` output correctly reflects the SHA-256 hash and signature of the new `.kpkg` binary, and includes `thumbnail_url` and `search_tags` arrays for UI cataloging.
>
>
> **Task 2: Android Network Layer (`:features:starterpack`)**
> 1. Add `implementation("com.github.luben:zstd-jni:1.5.5-4")` to the module's `build.gradle.kts`.
> 2. Update `KocolorApiService.kt` (Retrofit) to fetch the binary. Change the endpoint method to:
     > `@GET("packs/{packId}.kpkg") suspend fun downloadPackageBinary(@Path("packId") packId: String): ResponseBody`
>
>
> **Task 3: Android Verification & Decompression Pipeline**
> 1. Locate the package ingestion logic in `StarterPackRepositoryImpl.kt`.
> 2. Fetch the `manifest.json` to get the expected hash and signature.
> 3. Download the package binary and extract the raw bytes: `val rawBytes = apiService.downloadPackageBinary(packId).bytes()`.
> 4. **Enforce Security Pipeline (Order matters!):**
> * *Check 1 (Integrity):* Calculate the SHA-256 hash of `rawBytes` and compare it to the manifest. If they don't match, throw `PackException.IntegrityException`.
> * *Check 2 (Authenticity):* Pass `rawBytes` and the expected signature to our existing `SignatureVerifier` (BouncyCastle Ed25519). If it returns false, throw `PackException.SignatureException`.
> * *Check 3 (Decompression):* ONLY if both checks pass, decompress the bytes using `Zstd.decompress()`. Note: You may need to allocate an appropriately sized buffer or use `ZstdInputStream`.
>
>
> 5. Convert the decompressed bytes into a UTF-8 String, deserialize it into `List<CanonicalPackItem>`, and insert them into the Room Database inside an atomic `@Transaction`.
>
>
> **Strict Constraints (CRITICAL):**
> * **DO NOT** modify any Jetpack Compose UI files.
> * **DO NOT** modify the underlying `SignatureVerifier` Ed25519 logic, just pass it the correct compressed byte array.
> * **Security Rule:** Never decompress a byte array that has not been successfully cryptographically authenticated. This prevents zip-bomb memory attacks.
>
>
> **Execution Phase:**
> Please analyze my workspace, identify the `kocolor-compiler` pipeline and `StarterPackRepositoryImpl`, and implement these exact steps.