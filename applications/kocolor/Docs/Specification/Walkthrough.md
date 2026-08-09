# Walkthrough: KoColor Sovereign Distribution Platform (V1)

This document provides a comprehensive end-to-end journey of the KoColor inventory ecosystem. It tracks a single product from its data-driven origin in the Rust compiler to its high-fidelity manifestation in the mobile boutique.

---

## 🏗️ Phase 1: Data Authoring & Compilation

### 1. Zero-Touch Product Definition
You no longer modify code to add inventory. Products are defined in **KCPS v1** compliant JSON files located in `server/package/KoColor/input_packs/`.
*   **Schema Enforcement**: The compiler rejects any file missing required fields like `brand` or `macro_category`.
*   **Domain Integrity**: Cosmetic-specific fields (like `fda_data_verified`) are strictly prohibited in clothing arrays.

### 2. "Compute at Compile Time" Enrichment
When you run `./runMe.sh`, the **`kocolor-compiler`** performs expert-level science before the data reaches the user:
*   **Colorimetry**: Translates sRGB hex codes into the **CIELAB** ($L^*a^*b^*$) 3D color space (D65 Illuminant).
*   **Thermodynamics**: Maps chemical bases to professional phases (e.g., `SILICONE` → `HYDROPHOBIC_SILOXANE`) to pre-calculate pilling risks.

### 3. Distribution Security (Zero-Trust)
The compiler transforms the raw JSON into secure binary artifacts:
*   **Compression**: Uses Zstandard (Zstd) for high-ratio mobile-optimized shrinkage.
*   **Integrity**: Generates a **SHA-256** hash for content-addressed filenaming.
*   **Authenticity**: Signs every package and the master manifest using an **Ed25519** cryptographic private key.

---

## 📱 Phase 2: Mobile Discovery & Ingestion

### 4. Secure Manifest Sync
The Android Hub pings the CDN's `manifest.json`. 
*   **Signature Verification**: The app uses native Android security APIs to verify your developer signature. If a single byte was tampered with on the CDN, the app aborts the sync.
*   **Glow Sync Hub**: Users see a premium, list-based catalog of available "Atelier" kits (Core, Seasonal, Favorites).

### 5. The Boutique Selection Experience
Tapping a kit opens the **Select Items** screen—a high-fidelity "digital counter":
*   **Boutique Layout**: Items are grouped into **sticky category sections** (Prep, Complexion, etc.) with 100% opaque headers to prevent text bleedthrough.
*   **Granular Control**: Users can "Select All" in a section or pick individual items.
*   **Zero-Latency Info**: Tapping the "i" icon instantly reveals product details via the manifest's "Preview Projection" logic—no additional network calls required.

---

## 💾 Phase 3: Ingestion & Personalized Lineage

### 6. Transactional Database Sync
Once items are imported:
*   **Atomic Persistence**: All items and their **Provenance** (Source, Version, Publisher) are written to Room in a single transaction.
*   **Ancestry Tracking**: Every item knows exactly which pack it came from (`sourcePackId`).

### 7. "Make it Mine" (The Personal Archive)
The user views an imported item and taps the luxury **"MAKE IT MINE"** button.
*   **Clone & Detach**: The app clones the item into the user's personal archive.
*   **Safety Lock**: The `sourcePackId` is stripped from the clone. If the user later chooses to "Wipe Collection" to save space, their personalized item is **automatically protected** and stays in their archive.

---

## ✅ Summary of Achievements
*   **Scale**: Add infinite collections without changing Rust or Kotlin code.
*   **Speed**: Math and Science are pre-calculated at build time.
*   **Security**: Military-grade signatures protect the entire supply chain.
*   **UX**: A premium, boutique-inspired interface for high-fidelity discovery.

**Status**: 🚀 **V1 PRODUCTION BASELINE COMPLETE**
