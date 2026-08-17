# Team Workflow: The "Drop JSON & Run" Pipeline

Welcome to the **KoColor Asset Engineering Pipeline (v1.0)**.

We operate on a strict **Compute-at-Compile-Time (CCT)** philosophy. This means all the heavy lifting—image resizing, BlurHash generation, payload normalization, compression, and cryptographic signing—is handled automatically by our Rust compiler before anything reaches the mobile app.

**Golden Rule:** Adding new products, brands, images, or packages requires **zero changes to the Rust source code.** You only need to drop your data and run the engine.

Here is the step-by-step workflow.

---

## 📦 Step 1: Add Raw Assets (The "Drop")

All product data lives in the `raw_assets/` directory. You can organize folders however you want (e.g., `AuraSkin/Skincare/Cleansers/`)—the folder structure is completely ignored by the compiler.

For every new product, drop exactly two files into the directory:

**1. The Source Image (PNG)**

* Must be a high-resolution, 1:1 square, lossless `.png`.
* Example: `kc-prep-gel.png`

**2. The Authoring Data (KPSS v1 JSON)**

* Create a minimal JSON file containing the authoritative product identity.
* Ensure `raw_image_input` exactly matches the name of your PNG.

```json
{
  "schema_version": 1,
  "id": "kc-prep-01",
  "brand": "KoColor",
  "macro_category": "PREP",
  "micro_category": "CLEANSER",
  "shade_name": "Clear Crystal",
  "color_hex": "#F4F6F0",
  "raw_image_input": "./kc-prep-gel.png",
  "price": 18.0,
  "volume": "150ml",
  "ingredients": ["Water", "Glycerin", "Niacinamide"],
  "Contains_Fragrance": false,
  "fda_data_verified": true
}
```

---

## 🎛️ Step 2: Define Assortments (The "Mix")

Products exist independently of how they are packaged. To bundle your new product into a kit that users can download, edit or create a TOML file in the `package_configs/` directory.

Add your new product's `id` to the `includes` array.

**Example: `package_configs/starter_prep.toml`**

```toml
[package_metadata]
id = "starter-prep-kit"
name = "Initial Prep Routine"
description = "The ultimate daily reset for your skin."

[assortment]
includes = [
    "kc-prep-01",       # Your newly dropped cleanser
    "as-vit-c-serum",   # An existing product
    "kc-spf-invisible"  # An existing product
]
```

---

## 🚀 Step 3: Execute the Compiler (The "Run")

With your data dropped and your assortment defined, run the compiler. Open your terminal at the root of the `kc-optimizer` project and execute:

```bash
cargo run --release
```

*(Note: Always use `--release` so the compiler applies maximum CPU optimizations for image math and Zstd compression.)*

**What happens in the background?**

1. **Indexing:** Validates your JSON against the strict KPSS schema.
2. **Optimization:** Concurrently shrinks your PNGs into optimized WebP formats (Hero and Thumbnail) and generates Base83 BlurHashes.
3. **Composition:** Assembles your TOML assortments and purges internal authoring data.
4. **Sealing:** Compresses the payloads with Zstd (Level 19) and signs them with Ed25519 cryptography.

---

## ✅ Step 4: Verify the Output

Check the generated `dist/` directory. You will find:

1. **Optimized Images:** `kc-prep-gel.webp` and `kc-prep-gel_thumb.webp`.
2. **Sealed Payloads:** `starter-prep-kit.kpkg` (Your highly compressed, mobile-ready binary).
3. **The Master Manifest:** `manifest.json`.

The `manifest.json` contains the exact SHA-256 hashes and cryptographic signatures for the mobile Hub to securely ping and download.

**You are done. Commit your `raw_assets/` and `package_configs/` to version control, and the CI/CD pipeline will handle the rest.**

---

### 🚨 Troubleshooting

* **Compiler Panics with "Duplicate ID":** Two JSON files share the same `"id"`. IDs must be globally unique.
* **Compiler Panics with "Missing Optimized Assets":** Check that the `raw_image_input` path in your JSON is spelled correctly and points to a valid PNG.
* **Compiler Panics during Composition:** Your TOML file is requesting an ID that does not exist in `raw_assets/`. Check your spelling.
