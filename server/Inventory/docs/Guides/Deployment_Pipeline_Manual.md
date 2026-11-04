# KoColor Catalog Pipeline Manual (Build-Time Generator)

You are not running a "live" server. The Rust crates in `server/Inventory` are
**build-time generators**: they compile human-readable product inputs into
cryptographically signed `.kpkg` bundles plus a signed `manifest.json`, which
are then uploaded to the CDN (Cloudflare Pages). The Android client verifies
every bundle against its pinned root public key before importing anything.

## Pipelines

There are two compiler entry points. Both produce signed `manifest.json` +
`.kpkg` output and both enforce the same signing rules below.

| Entry point | What it runs | Output |
|---|---|---|
| `server/Inventory/package/runMe.sh` | `kc-optimizer` (enrichment, signing, `.kpkg`) + `kc-distributor` (WebP, zip) | `package/dist/staging/inventory/dist/` (`manifest.json`, `*.kpkg`, `search_index.json`, `assets/`) plus `package/dist/kocolor-v1-deploy.zip` |
| `server/Inventory/package/run.sh [model]` | `utils/prog_rs` tagging pipeline (delegated; default model `gemini-3.8-flash`) | `package/dist/` (`manifest.json`, `*.kpkg`, `quality_report.json`) |

Raw inputs live in `package/input/KoColor/` (tracked in git); compiled outputs
in `package/dist/` are git-ignored build artifacts — never commit them.

## Required environment

| Variable | Required by | Purpose |
|---|---|---|
| `KOCOLOR_SIGNING_SEED_HEX` | both pipelines | 64-hex-char (32-byte) Ed25519 seed. **Both pipelines exit without signing if this is missing or invalid.** There is no fallback key. |
| `KOCOLOR_KEY_ID` | both pipelines | `key_id` stamped into `manifest.json` (e.g. `atelier-prod-01`). Defaults to `atelier-dev-01` with a warning; production releases must set the production id. |
| `GEMINI_API_KEY` | `prog_rs` only | Gemini structured-extraction calls. |

The wrapper scripts (`run.sh`, `runMe.sh`, `utils/prog_rs/run.sh`) fail fast
when required variables are missing, before any build output is touched.

## Key management rules

1. **Generate once per environment:** `openssl rand -hex 32`.
2. **Store the seed in a secret manager** (or equivalent vault). Never commit
   it, never paste it into docs/chat, and never rely on a `.env` file that
   could be accidentally added to git.
3. **The client pins the public key.** After (re)generating a seed, run the
   pipeline once — it prints the public key — and update
   `KOCOLOR_ROOT_PUBLIC_KEY` in
   `applications/kocolor/features/starterpack/.../data/SecurityConstants.kt`.
   Rebuild, re-sign, and redeploy the full catalog afterwards.
4. **The old `atelier-dev-01` seed is burned.** It was previously committed to
   this repo, so treat it as public. It must never sign a production catalog.

## Rotation procedure

Do this before release while there is no installed base. After release, old
app versions pin the old public key and will reject bundles signed by the new
key, so rotation needs a client-update overlap window.

1. Generate a new seed and store it in the secret manager.
2. Export `KOCOLOR_SIGNING_SEED_HEX` and `KOCOLOR_KEY_ID` (new production id).
3. Run the pipeline; confirm the printed public key.
4. Update `SecurityConstants.kt` with the new public key.
5. Rebuild the full catalog and bump the app version.
6. Deploy: upload `.kpkg` files first, then `manifest.json`
   (`deploy_cdn.sh` stages both atomically — see `docs/deploy.md`).
7. Verify: `curl` the live `manifest.json`, confirm `key_id`, `signature`,
   and pack hashes; confirm the release client syncs successfully.

## End-to-end flow

1. Author or edit products in `package/input/KoColor/` and kit
   compositions in `package/input/package_configs/`.
2. Export the environment (seed from secret manager).
3. Run `./runMe.sh` or `./run.sh` from `server/Inventory/package/`.
4. Deploy (see `docs/deploy.md` for the runbook and verification checks):
   - `prog_rs` output: `./deploy_cdn.sh` from `server/Inventory/package/`
     (expects loose `manifest.json` + `*.kpkg` in `package/dist/`).
   - `runMe.sh` output: upload `dist/kocolor-v1-deploy.zip` and unpack it at
     the CDN origin, or point `utils/prog_rs/deploy_cdn.sh` at
     `package/dist/staging/inventory/dist/`.
