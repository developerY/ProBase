# KoColor CDN Atomic Deployment Runbook

This runbook outlines the atomic deployment process for pushing compiled `.kpkg` distribution bundles and signed `manifest.json` envelopes to Cloudflare R2 / Custom Domain CDN (`cdn.kocolor.com`).

---

## Critical Requirement: Atomic Deployment Order

To prevent Android clients from experiencing SHA-256 / Ed25519 signature verification crashes:
1. **`.kpkg` Packages MUST be uploaded first.**
2. **Edge Cache for `.kpkg` files MUST be invalidated.**
3. **`manifest.json` MUST be uploaded last.**

If `manifest.json` is updated before the new `.kpkg` binaries are live on the CDN edge, mobile clients will fetch new hashes from the manifest but download stale `.kpkg` binaries from the CDN, causing a security checksum mismatch.

---

## Step-by-Step Deployment Execution

### Step 1: Upload `.kpkg` Binary Packages First

Upload all compiled `.kpkg` binary bundles from `server/Inventory/package/dist/` to your Cloudflare R2 bucket:

```bash
# Upload fashion complete bundle
npx wrangler r2 object put kocolor-cdn/v1/com.kocolor.pack.fashion.complete.kpkg \
  --file server/Inventory/package/dist/com.kocolor.pack.fashion.complete.kpkg \
  --content-type "application/octet-stream"

# Upload cosmetics complete bundle
npx wrangler r2 object put kocolor-cdn/v1/com.kocolor.pack.cosmetics.complete.kpkg \
  --file server/Inventory/package/dist/com.kocolor.pack.cosmetics.complete.kpkg \
  --content-type "application/octet-stream"
```

---

### Step 2: Purge CDN Edge Cache for `.kpkg` Files

Invalidate any cached edge responses to ensure clients fetch the newly uploaded binaries immediately:

```bash
# Using Cloudflare API to purge specific URLs
curl -X POST "https://api.cloudflare.com/client/v4/zones/${CLOUDFLARE_ZONE_ID}/purge_cache" \
  -H "Authorization: Bearer ${CLOUDFLARE_API_TOKEN}" \
  -H "Content-Type: application/json" \
  --data '{
    "files": [
      "https://cdn.kocolor.com/v1/com.kocolor.pack.fashion.complete.kpkg",
      "https://cdn.kocolor.com/v1/com.kocolor.pack.cosmetics.complete.kpkg"
    ]
  }'
```

---

### Step 3: Upload Signed `manifest.json` Last

Once the binary packages are live and purged at the edge, upload the signed master manifest envelope:

```bash
npx wrangler r2 object put kocolor-cdn/v1/manifest.json \
  --file server/Inventory/package/dist/manifest.json \
  --content-type "application/json"
```

And purge the edge cache for `manifest.json`:

```bash
curl -X POST "https://api.cloudflare.com/client/v4/zones/${CLOUDFLARE_ZONE_ID}/purge_cache" \
  -H "Authorization: Bearer ${CLOUDFLARE_API_TOKEN}" \
  -H "Content-Type: application/json" \
  --data '{"files": ["https://cdn.kocolor.com/v1/manifest.json"]}'
```

---

## Verification & Sanity Checks

1. **Verify Manifest Payload & Signature:**
   ```bash
   curl -s https://cdn.kocolor.com/v1/manifest.json | jq .
   ```
   Confirm that `signature`, `key_id`, and `packs` array contain the updated SHA-256 hashes and compressed byte counts.

2. **Verify `.kpkg` Download Headers:**
   ```bash
   curl -I https://cdn.kocolor.com/v1/com.kocolor.pack.fashion.complete.kpkg
   ```
   Confirm `HTTP/2 200` and `Content-Type: application/octet-stream`.

3. **Android Client Cold-Start Verification:**
   - Launch the Android app (`KoColor`) on a test device or emulator.
   - Verify in Logcat that `fetchVerifiedPackage()` successfully validates the `ed25519` signature against `key_id: "atelier-dev-01"`, decompresses `zstd`, and imports catalog items into Room DB without error.
