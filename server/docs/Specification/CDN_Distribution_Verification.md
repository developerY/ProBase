# CDN Distribution & Verification Guide

This document explains how CDN URLs are constructed by the Rust pipeline and provides a checklist for verifying the deployment using a browser, Postman, or `curl`.

---

## 🏗️ 1. URL Construction Logic

The KoColor pipeline uses a **Deterministic Flattening** strategy. All URLs are constructed relative to the `BASE_URL` defined in the Android client:
`https://cdn.kocolor.com/inventory/dist/`

### 🔄 The Transformation Rule
The `kc-distributor` takes the human-readable hierarchy in `input/` and projects it into a flat structure keyed by the product `id`.

| Source Path (Input) | CDN Path (Output) | Purpose |
|:---|:---|:---|
| `KoColor/PREP/Cleanser/kc-prep-01.png` | `assets/hero/kc-prep-01.webp` | 1024px High-Res Image |
| (Auto-generated from source) | `assets/thumb/kc-prep-01_thumb.webp` | 256px Placeholder |
| `package_configs/starter-kit.toml` | `starter-kit.kpkg` | Zstd Compressed Binary |
| (Compiler Output) | `manifest.json` | Signed Root of Trust |

---

## 🔍 2. Verification Checklist

### Step 1: The Root of Trust (Manifest)
The mobile app cannot ingest any data without a valid manifest.
*   **URL**: `https://cdn.kocolor.com/inventory/dist/manifest.json`
*   **Verification (Postman/Browser)**:
    1.  Ensure the response is a JSON object with a top-level `"signature"` field.
    2.  Verify the `"data"` block contains a list of `"packs"`.
    3.  Check that the `endpoint` for each pack matches the filename of the `.kpkg` on the server.

### Step 2: Global Search Index
Verify the pre-calculated search tokens are available for the Boutique Hub.
*   **URL**: `https://cdn.kocolor.com/inventory/dist/search_index.json`
*   **Verification**: Should return a flat map where keys are IDs (e.g., `"kc-prep-01"`) and values are arrays of lowercase tokens.

### Step 3: Scientific Payloads (.kpkg)
Verify the binary data stream is accessible.
*   **URL**: `https://cdn.kocolor.com/inventory/dist/kocolor-complete-collection.kpkg`
*   **Verification**: 
    *   **Browser**: Should prompt to download a file.
    *   **Postman**: Headers should show `Content-Type: application/octet-stream` (or similar binary type).

### Step 4: Visual Assets (WebP)
Verify the transcoding and directory flattening.
*   **Hero**: `https://cdn.kocolor.com/inventory/dist/assets/hero/{id}.webp`
*   **Thumb**: `https://cdn.kocolor.com/inventory/dist/assets/thumb/{id}_thumb.webp`
*   **Verification**: Open the URL in a browser tab. It must render the image correctly. If you get a 404, check if the ID in the URL matches the `id` field in the product's JSON.

---

## 🛠️ 3. Quick Verification Script (CLI)

You can run these commands from your terminal to verify headers and connectivity:

```bash
# Check Manifest Signature Presence
curl -s https://cdn.kocolor.com/inventory/dist/manifest.json | grep "signature"

# Check Hero Asset Content-Type
curl -I https://cdn.kocolor.com/inventory/dist/assets/hero/kc-prep-01.webp

# Check Package Size (Must match uncompressed_size_bytes in manifest)
curl -I https://cdn.kocolor.com/inventory/dist/kocolor-complete-collection.kpkg
```

---

## ⚠️ Troubleshooting Common 404s
1.  **Case Sensitivity**: The pipeline enforces lowercase extensions (`.webp`, `.json`). Ensure your URL isn't using `.WebP`.
2.  **ID Mismatch**: The filename on the CDN is derived from the `id` field *inside* the KPSS JSON, not the name of the folder it was in.
3.  **Path Depth**: Ensure `assets/hero/` and `assets/thumb/` are separate subdirectories. The distributor does not put all images in one folder.
