# KoColor Secure Package Distribution Platform

This directory contains the **Rust Normalization Compiler**, the engine responsible for transforming raw vendor data into secure, highly compressed, and cryptographically signed KoColor Packages (`.kpkg`).

## 🏗 System Architecture

The platform operates on a **Static-First Hub & Spoke** model:
1.  **Compiler (Rust)**: Normalizes data, applies Zstandard compression, and signs payloads with Ed25519.
2.  **CDN (Static)**: Hosts the `manifest.json` and `.kpkg` binaries (Cloudflare / GitHub Pages).
3.  **Hub (Android Phone)**: Streams, verifies, decompresses, and persists packages into a local Room database.
4.  **Spoke (Wear OS / XR)**: Mirrors the verified data from the Phone Hub via local synchronization.

---

## 🔐 Security Protocol (Phase 1.1)

All distribution artifacts follow the **Verify-First Rule**:
*   **Trust Bootstrap**: `manifest.json` is signed. The client verifies this signature using an embedded Root Public Key.
*   **Integrity (SHA-256)**: Every `.kpkg` has a unique hash in the manifest to detect corruption.
*   **Authenticity (Ed25519)**: Every `.kpkg` is signed. The client verifies the signature *before* decompression.
*   **Immutability**: Packages are content-addressed using the convention `<package-id>-<sha256>.kpkg`.

---

## 🚀 Operational Guide

### 1. Requirements
*   **Rust Toolchain**: 1.75+
*   **Private Key**: A 32-byte Ed25519 private key (64 hex characters).

### 2. Setup Environment
Create a `.env` file in `server/package/KoColor/`:
```env
CDN_PRIVATE_KEY_HEX=your_64_character_hex_private_key
```
*Note: If you need to generate a new keypair, run the keygen utility:*
```bash
cd server/gen/Key
cargo run --bin keygen
```

### 3. Run the Compiler
The compiler aggregates all inventory definitions, generates the search index, and builds the signed binary packages.

```bash
# Navigate to the compiler crate
cd server/package/KoColor

# Execute the normalization pipeline
cargo run --bin generate_payload
```

### 4. Output Artifacts
The compiler generates the following files in `server/package/KoColor/`:
*   `manifest.json`: The signed root index containing all package metadata.
*   `search_index.json`: Global discovery index for the Android Sync Hub.
*   `*.kpkg`: The compressed, signed binary data payloads.

---

## 🌐 Deployment (CDN)

Since the architecture is static, "deployment" simply means syncing the output artifacts to your web host.

### Folder Structure for CDN
```text
/inventory/
├── manifest.json
├── search_index.json
├── com.kocolor.pack.core-a3b1c3...kpkg
└── com.kocolor.pack.winter2026-483e68...kpkg
```

### GitHub / Cloudflare Sync
1.  Copy the generated files to your `kc-cdn` repository.
2.  **Commit and Push**.
3.  Ensure the Android `BASE_URL` in `KocolorApiService.kt` matches your raw CDN path.

---

## 📋 Maintenance Checklist
*   [ ] **Version Bumping**: Increment the `version` in `generate_payload.rs` when data changes.
*   [ ] **Schema Changes**: Increment `schema_version` to `3` if adding mandatory fields to items.
*   [ ] **Immutable Purge**: Periodically remove old `.kpkg` files from the CDN that are no longer referenced in the current `manifest.json`.
