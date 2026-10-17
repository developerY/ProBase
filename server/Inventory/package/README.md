# KoColor Sovereign Distribution Pipeline

This directory is the centralized workspace for the Compute-at-Compile-Time (CCT) pipeline. It converts human-readable authoring files into cryptographically signed, GPU-optimized artifacts ready for edge network distribution.

## 🗂️ Directory Structure

* **`input/`**: The "Source of Truth". Contains everything needed to build a distribution.
    * **`KoColor/`**: Human-readable product hierarchy (Authoring assets: JSON & High-Res PNG).
    * **`package_configs/`**: TOML manifests defining kit compositions (e.g. `starter-kit.toml`).
* **`dist/`**: The "Output Vault". Contains intermediate build files and the final deployment ZIP.
* **`kc-optimizer/`**: Rust Crate for scientific enrichment, signing, and `.kpkg` generation.
* **`kc-distributor/`**: Rust Crate for WebP transcoding, structural flattening, and zipping.

## 🚀 Execution

To generate a new deployment payload, execute the pipeline script from this directory:

```bash
./runMe.sh
```

### 📦 Outputs

* **`dist/kocolor-v1-deploy.zip`**: The atomic, immutable deployment archive ready for CDN upload.
* **`dist/manifest.json`**: The signed root of trust for the mobile client.
* **`dist/search_index.json`**: The pre-calculated, zero-latency global search index.

## 🔍 Verification

For detailed instructions on constructing CDN URLs and verifying deployment via browser or Postman, see:
* [**CDN Distribution & Verification Guide**](./docs/CDN_Distribution_Verification.md)

## 🛠️ Workflow

1. Edit or add product JSONs and raw assets in `input/KoColor/`.
2. Define kit compositions in `input/package_configs/`.
3. Run `./runMe.sh`.
4. Upload `dist/kocolor-v1-deploy.zip` to your CDN and unpack it.
