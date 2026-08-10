# KoColor Sovereign Distribution Platform

This directory contains the core infrastructure for the **KoColor Sovereign Distribution Platform**, a high-performance system for generating, securing, and distributing professional cosmetic and wardrobe intelligence.

## 🏗️ System Architecture: "Compute at Compile Time"

The platform operates on a **Static-First Hub & Spoke** model, offloading heavy mathematical and chemical calculations from the mobile device to the build-time compiler.

1.  **Normalization Compiler (`kc-optimizer`)**: The Rust-based engine that transforms raw authoring data into optimized, signed payloads.
2.  **CDN (Static Distribution)**: Hosts the cryptographically signed `manifest.json` and highly compressed `.kpkg` binaries (GitHub Pages).
3.  **Platform Hub (Android Phone)**: The central verification and ingestion engine.
4.  **Downstream Spokes (Wear OS / XR)**: Consumer devices that mirror data from the verified Phone Hub.

---

## 🔐 The Multi-Layer Trust Framework

We enforce a strict **Verify-Before-Execute** policy to ensure a zero-trust distribution environment.

*   **Trust Bootstrap**: Every manifest is signed with an **Ed25519** private key. The client verifies the signature before reading metadata.
*   **Integrity (SHA-256)**: Every binary package is content-addressed using its SHA-256 hash.
*   **Authenticity**: Packages are signed immediately after compression. The client verifies this signature *before* decompression.
*   **Safety**: All ingestion is protected by **JSON Bomb Prevention** logic and hardcoded safety caps (32MB).

---

## ⚙️ Core Toolchain: `kc-optimizer`

The `kc-optimizer` project is a data-driven compiler that replaces hardcoded registries with a flexible, directory-based workflow.

### Key Capabilities:
*   **Colorimetry Engine**: Converts sRGB hex to **CIELAB** ($L^*a^*b^*$) and Hue Angle ($h_{ab}$) for D65 illuminants.
*   **Enrichment Engine**: Maps chemical bases to thermodynamic phases and tokenizes ingredients for "Clean Beauty" filtering.
*   **Visual Pipeline**: Generates **BlurHash** placeholders and optimized **WebP** assets concurrently via `rayon`.
*   **Deterministic Sealing**: Produces byte-identical `.kpkg` binaries using high-ratio **Zstd** (Level 19) compression.

---

## 🚀 Team Workflow

Adding new inventory to the KoColor ecosystem requires **zero source-code changes**.

### 1. The "Drop"
Place raw images (.png) and authoring metadata (**KPSS v1** JSON) into the `kc-optimizer/raw_assets/` directory.

### 2. The "Mix"
Define package assortments in the `kc-optimizer/package_configs/` directory using TOML.

### 3. The "Run"
Execute the authoritative distribution pipeline:
```bash
cd server/package/kc-optimizer
./runMe.sh
```

---

## 📚 Technical Documentation Hub

*   **[Sovereign Architecture Guide](./docs/Specification/Anchor.md)**: The foundational "Data as Code" manifesto.
*   **[KCPS v1 Specification](./docs/Specification/Schema/KCPS_v1_SPEC.md)**: The authoritative distribution data contract.
*   **[Product Authoring Guide](./docs/Specification/Schema/KCPS_Product_Authoring_Guide.md)**: How to define KPSS-compliant source data.
*   **[User Manual](./docs/Specification/ProductGen/USER_MANUAL.md)**: Operational guide for the Asset Engineering Platform.
*   **[Platform Walkthrough](./docs/Specification/Walkthrough.md)**: End-to-end journey from authoring to mobile ingestion.

---
**Status**: 🚀 **V1 PRODUCTION ACTIVE**
**Compiler**: `kocolor-asset-processor`
**Security Standard**: Ed25519 + SHA-256 + Zstd
