# Walkthrough: The Rust Distribution & Deployment Pipeline

This document provides a technical walkthrough of the finalized Rust-native distribution pipeline. We have established a strictly deterministic "Author -> Compile -> Transcode -> Deploy" workflow that eliminates script dependency and enforces zero-trust data integrity.

---

## 🏗️ 1. The Authoring Source (`raw_assets/`)

The pipeline begins with the human-readable authoring hierarchy. 

*   **Hierarchical Organization**: Authors organize products by category (e.g., `COMPLEXION/Concealer/`).
*   **The KPSS Triad**: Every product is defined by a mandatory **JSON** authoring file, a **PNG** high-res image, and an optional **.notes.json** editorial file.
*   **Decoupled Paths**: The compiler resolves these files dynamically, allowing authors to move or rename folders without breaking the build logic.

---

## ⚙️ 2. The Compilation Phase (`kc-optimizer`)

Running `cargo run --release` in the optimizer crate triggers the **Compute-at-Compile-Time (CCT)** engine.

*   **Scientific Enrichment**: The compiler calculates CIELAB color coordinates, identifies thermodynamic phases, and generates typo-tolerant search tokens.
*   **Cryptographic Sealing**: 
    *   **Zstd Compression**: The JSON data is compressed into binary `.kpkg` files.
    *   **Ed25519 Signing**: Every package is signed with the developer's private key.
    *   **Signed Size Contract**: The exact uncompressed byte size is recorded in the manifest to prevent memory-exhaustion (JSON bomb) attacks on the mobile client.
*   **Output**: Produces the signed `manifest.json` and `.kpkg` binaries in the `dist/` root.

---

## 🖼️ 3. The Distribution Phase (`kc-distributor`)

The dedicated distributor tool handles the "Heavy Lifting" of visual processing and structural flattening.

### 🔄 structural Flattening
The distributor walks the human-readable tree and projects it into a machine-centric, flat CDN structure:
*   **`assets/hero/`**: Keyed by `{id}.webp`.
*   **`assets/thumb/`**: Keyed by `{id}_thumb.webp`.
*   **`notes/`**: Keyed by `{id}.notes.json`.

### ⚡ Visual Transcoding
Raw assets are converted into GPU-optimized WebP formats:
*   **Hero (1024x1024)**: High-fidelity detail via Lanczos3 filtering.
*   **Thumb (256x256)**: Rapid-load placeholders via efficient downscaling.

---

## 📦 4. The Deployment Archive (`.zip`)

The final output is an immutable **`kocolor-v1-deploy.zip`**.

*   **Self-Contained**: Contains the master manifest, scientific packages, and optimized visual assets.
*   **Atomic Deployment**: This single file ensures that the CDN state is always consistent. You never end up with a high-res image that doesn't match its JSON metadata.
*   **Cloud Ready**: Ready for direct upload to GitHub Pages or AWS S3.

---

## ✅ Summary of Pipeline Integrity
*   **Deterministic**: Same input always produces the same cryptographic hash.
*   **Performance**: WebP and Zstd minimize mobile data consumption.
*   **Security**: Every byte is hashed and signed before it leaves your machine.

**Status**: 🚀 **V1 DISTRIBUTION PIPELINE READY FOR PRODUCTION**
