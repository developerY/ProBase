# Architecture Manifesto: The Sovereign Distribution Platform

This document codifies the architectural principles and technical foundation of the KoColor ecosystem. It establishes the "Data as Code" paradigm, where the Rust codebase operates as a normalization compiler rather than a static database.

---

## 🏗️ 1. Core Architectural Model: Hub & Spoke

The KoColor system is designed for high-performance data delivery across a distributed device mesh.

*   **The Compiler (Rust)**: The sole authority for data normalization, visual optimization, and cryptographic security.
*   **The Hub (Android Phone)**: The primary trust gateway. It performs all cryptographic verification and binary decompression before persisting data.
*   **The Spokes (Wear OS / XR)**: Lightweight consumer devices that mirror data from the verified Phone Hub.

---

## ⚙️ 2. The Normalization Engine (`kc-optimizer`)

The Rust engine is a **Data-Driven Compiler**, not an inventory registry. 

### The CCT Philosophy (Compute-at-Compile-Time)
To protect mobile CPU performance and ensure zero-latency rendering, all heavy lifting is offloaded to the build phase:
*   **Colorimetry**: Translating sRGB hex into mathematically accurate CIELAB space ($L^*a^*b^*$).
*   **Thermodynamics**: Resolving chemical bases into physical phases to pre-calculate pilling risks.
*   **Visuals**: Generating 4x4 Base83 BlurHash placeholders and downscaling hero assets to optimized WebP.
*   **Intelligence**: Scanning technical INCI lists to extract "Hero" actives and safety flags.

### Data Flow Pipeline
```text
                   PRODUCT DATA
                       │
                       ▼
              ┌─────────────────┐
              │   KPSS v1 JSON  │
              │   / Raw PNGs    │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │ Rust Normalizer │
              │ (kc-optimizer)  │
              └────────┬────────┘
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
       Validate     Normalize    Derive
          │            │            │
          └────────────┼────────────┘
                       ▼
                Canonical JSON
                       │
                       ▼
                  Zstandard
                       │
                       ▼
                  SHA-256
                       │
                       ▼
                  Ed25519
                       │
                       ▼
                  `.kpkg`
                       │
                       ▼
                     CDN
                       │
                       ▼
                Android Hub
                       │
              ┌────────┴────────┐
              ▼                 ▼
           Wear OS              XR
```

---

## 🔐 3. Security & Integrity Strategy

We operate on a **Zero-Trust Supply Chain**.

*   **Verification-on-Arrival**: Every manifest and package is cryptographically signed using **Ed25519**. The Android Hub verifies the signature *before* decompression.
*   **Content Addressing**: Every `.kpkg` is addressed by its **SHA-256** hash, making it mathematically impossible for corrupt or tampered data to enter the archive.
*   **Memory Safety**: Ingestion is protected by **JSON Bomb Prevention** logic and hardcoded 32MB safety caps.

---

## 📋 4. Authoring Principle

**Product data is data, not source code.**

*   New products, brands, and assortments MUST be introduced via **KPSS v1** JSON files in the `raw_assets/` directory.
*   The Rust compiler logic is frozen and MUST NOT require source-code changes to add new inventory.
*   **Exclusivity**: The schema strictly enforces domain boundaries (e.g., cosmetic-specific fields like `fda_data_verified` are prohibited in clothing arrays).

---
**Status**: 🚀 **V1 ARCHITECTURE SEALED**
**Schema**: KPSS v1 (Source) | KCPS v1 (Wire)
**Engine**: `kocolor-asset-processor`
