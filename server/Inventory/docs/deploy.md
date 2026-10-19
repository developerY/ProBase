# KoColor Free-Forever CDN Deployment Runbook (Cloudflare Pages)

This runbook documents the Cloudflare Pages Direct Upload workflow for publishing `.kpkg` distribution bundles and signed `manifest.json` envelopes to Cloudflare's global edge network (`kocolor-cdn.pages.dev/inventory/dist/` or custom domain `cdn.kocolor.com/inventory/dist/`).

---

## Why Cloudflare Pages?

* **100% Free Forever:** Static hosting on Cloudflare Pages provides unmetered bandwidth and requests without R2 billing or credit card requirements.
* **Native Atomic Deployments:** `wrangler pages deploy` uploads all files into an immutable snapshot and flips the live production pointer atomically once all files are verified.
* **ETag Revalidation via `_headers`:** Both `manifest.json` and `*.kpkg` files use `Cache-Control: public, max-age=0, must-revalidate` so OkHttp on Android revalidates ETags and never serves cached old binary files after a re-deployment.

---

## Step 1: One-Time Project Creation

Run this once in your terminal to create your free Cloudflare Pages project:

```bash
npx wrangler pages project create kocolor-cdn --production-branch main
```

---

## Step 2: Atomic Deployment Execution

To deploy your catalog from your terminal:

```bash
cd server/Inventory/utils/prog_rs
./deploy_cdn.sh
```

*(Note: The script defaults `PATH_PREFIX` to `inventory/dist` matching `KocolorApiService.BASE_URL`).*

---

## What `deploy_cdn.sh` Does Automatically:

1. **Creates Isolated Staging (`mktemp -d`)**:
   Constructs a clean, isolated directory matching the `/inventory/dist/*` URL structure.
2. **Generates ETag-Safe `_headers` Rules**:
   ```http
   /inventory/dist/manifest.json
     Content-Type: application/json
     Cache-Control: public, max-age=0, must-revalidate

   /inventory/dist/*.kpkg
     Content-Type: application/octet-stream
     Cache-Control: public, max-age=0, must-revalidate
   ```
3. **Deploys Atomically**:
   Pushes the staged snapshot to Cloudflare Pages production:
   ```bash
   npx -y wrangler pages deploy "${STAGE_DIR}" \
     --project-name "kocolor-cdn" \
     --branch main \
     --commit-dirty=true
   ```

---

## Verification & Sanity Checks

1. **Test Live Manifest Endpoint**:
   ```bash
   curl -s https://kocolor-cdn.pages.dev/inventory/dist/manifest.json | jq .
   ```
   Confirm `manifest_version: 1`, `signature`, and valid pack hashes.

2. **Verify Headers & ETag Revalidation**:
   ```bash
   curl -I https://kocolor-cdn.pages.dev/inventory/dist/com.kocolor.pack.fashion.complete.kpkg
   ```
   Confirm `cache-control: public, max-age=0, must-revalidate`.

3. **Android Client Integration**:
   Your Android app fetches `https://kocolor-cdn.pages.dev/inventory/dist/manifest.json` (or `https://cdn.kocolor.com/inventory/dist/manifest.json`), validates the `ed25519` signature against `key_id: "atelier-dev-01"`, decompresses `zstd`, and imports items into Room DB seamlessly.
