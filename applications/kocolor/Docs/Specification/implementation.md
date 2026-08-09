# Technical Implementation: KoColor V1 Sovereign Distribution Platform

This document serves as the authoritative technical summary of the KoColor Version 1 infrastructure. It details the integration of the Rust compiler, the secure delivery protocol, and the high-fidelity Boutique UI.

---

## 🏗️ 1. System Architecture

The ecosystem operates on a **"Compute at Compile Time"** model, shifting mathematical and chemical evaluation to the build phase.

*   **Platform Hub**: Android Phone (Centralized ingestion & verification).
*   **Downstream Spokes**: Wear OS & XR (Consumer-only devices).
*   **Compiler**: Data-driven Rust CLI binary.
*   **Storage**: Offline-first Room DB with transactional integrity.

---

## 🔐 2. Security & Integrity (Ed25519)

We have implemented a **Zero-Trust Distribution Model**.

1.  **Enveloped Data**: Every JSON (manifest or pack) is wrapped in a `SignedPayloadEnvelope`.
2.  **Cryptographic Signing**: The Rust compiler signs every artifact with an **Ed25519** private key.
3.  **Native Verification**: The Android Hub uses `java.security.Signature` to verify the payload before decompression or ingestion.
4.  **Content Addressing**: Every `.kpkg` filename includes its **SHA-256** hash for immutable integrity.

---

## ⚙️ 3. The Normalization Engine (Rust)

The compiler is no longer an inventory database; it is a **Transformation Pipeline**.

*   **Dynamic Discovery**: Scans `input_packs/` for partner JSON files.
*   **Deterministic Serialization**: Serializes structs to canonical bytes to ensure stable hashes.
*   **Physics Enrichment**:
    *   **Chemistry**: Maps bases to thermodynamic phases (e.g., `HYDROPHILIC_AQUEOUS`).
    *   **Colorimetry**: Converts sRGB Hex to **CIELAB** coordinates and calculates the **Hue Angle ($h_{ab}$)** for D65 illuminant.

---

## 🎨 4. High-Fidelity Boutique UI (Android)

The UI layer is designed for **Zero Latency** and **Professional Accuracy**.

### Key Components:
*   **Glow Sync Hub**: Spotlights the "Core Collection" with Ed25519 verification badges.
*   **Boutique Selector**:
    *   **Categorized Grouping**: Sticky, opaque headers with category-level bulk actions.
    *   **Projection Previews**: Instant info popups using manifest-provided metadata.
    *   **Safe Handling**: Intelligent brand/shade formatting to prevent "null" string bugs.
*   **"Make it Mine" Engine**: Transactional cloning logic that detaches curated items into personal, safe-to-keep archive records.

---

## 🛠️ 5. Deployment Workflow

1.  **Authoring**: Create KCPS v1 JSON in `input_packs/`.
2.  **Compilation**: Run `./runMe.sh` to generate signed artifacts in `dist/`.
3.  **Sync**: The mobile app pings `manifest.json` on the CDN.
4.  **Ingestion**: User selects items and imports them via verified binary streams.

---
**Status**: ✅ **V1 PLATFORM ACTIVE**
**Data Contract**: KCPS v1 (Strict)
**Security Curve**: SECP256R1 (Android Signature API)
