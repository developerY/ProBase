# User Manual: KoColor Asset Engineering Platform (v1.0)

Welcome to the Sovereign Distribution Platform. This manual explains how to use the **`kc-optimizer`** toolchain to manage, secure, and distribute product collections to the KoColor mobile ecosystem.

---

## 📖 1. The Core Philosophy: "Drop JSON & Run"

The platform is designed to be **Zero-Maintenance**. 
*   **Data is Data**: Product definitions are authored in JSON.
*   **Code is Logic**: The Rust compiler logic never changes.
*   **Zero-Latency**: Heavy math (Colorimetry, Chemistry, Visuals) is computed at build-time, not on the phone.

---

## 🛠️ 2. Authoring New Products (KPSS v1)

To add a new product to the global registry, you must provide a **KPSS (Product Source)** JSON file and a high-resolution image.

### File Requirements:
1.  **Directory**: Place files anywhere in `server/package/kc-optimizer/raw_assets/`.
2.  **Image**: A 1:1 square, high-resolution `.png`.
3.  **JSON**: A file containing the authoritative semantic identity.

**Example Authoring JSON (`raw_assets/glow_serum.json`):**
```json
{
  "schema_version": 1,
  "id": "kc-prep-02",
  "brand": "KoColor",
  "macro_category": "PREP",
  "micro_category": "SERUM",
  "shade_name": "Luminous Glow",
  "color_hex": "#FFF8E7",
  "raw_image_input": "./glow_serum.png",
  "price": 28.0,
  "volume": "30ml",
  "ingredients": ["Water", "Vitamin C", "Glycerin"],
  "Contains_Fragrance": false,
  "fda_data_verified": true
}
```

---

## 🎛️ 3. Composing Collections (TOML)

Products exist in a global pool. To group them into a downloadable "Pack" for the mobile app, create or edit a configuration in `server/package/kc-optimizer/package_configs/`.

**Example Collection (`package_configs/starter_kit.toml`):**
```toml
[package_metadata]
id = "starter-kit"
name = "Essentials Starter Kit"
description = "The foundational kit for professional styling."

[assortment]
includes = [
    "kc-prep-02",        # Resolved from your dropped JSON
    "kc-lips-crimson",   # Resolved from another source
    "as-primer-velvet"   # Resolved from another source
]
```

---

## 🚀 4. Running the Build Pipeline

Once your JSON and TOML files are ready, execute the compiler to generate secure artifacts.

1.  Open your terminal in `server/package/kc-optimizer/`.
2.  Run the release build:
    ```bash
    cargo run --release
    ```

### What the compiler does for you:
*   **Enrichment**: Calculates CIELAB color math and Chemistry phases.
*   **Visuals**: Concurrently generates WebP Hero/Thumbnail images and **BlurHash** placeholders.
*   **Security**: Signs every package with **Ed25519** and hashes them with **SHA-256**.
*   **Registry**: Automatically updates the global `manifest.json`.

---

## 📦 5. Verification & Deployment

After the run completes, navigate to the `dist/` directory.

### Build Artifacts:
*   **`assets/*.webp`**: Your optimized, mobile-ready images.
*   **`*.kpkg`**: Signed, Zstd-compressed binary packages ready for the CDN.
*   **`manifest.json`**: The authoritative root of trust for the Android app.

**Final Step**: Upload the contents of the `dist/` folder to your CDN (GitHub Pages). The mobile app will instantly detect the new collection upon its next sync.

---

## 🚨 Troubleshooting
*   **"Duplicate ID" Error**: You have two JSON files with the same `id`. Fix the duplicate and re-run.
*   **"Missing Asset" Error**: The `raw_image_input` path in your JSON doesn't point to a real file.
*   **"Schema Mismatch"**: Your JSON uses `schema_version` other than 1. Only V1 is currently supported.

---
**Platform Status**: 🚀 **V1 PRODUCTION ACTIVE**
**Compiler**: `kocolor-asset-processor`
