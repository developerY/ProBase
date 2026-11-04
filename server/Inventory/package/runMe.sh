#!/usr/bin/env bash

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Fail fast BEFORE wiping dist/: the optimizer signs every bundle and refuses to
# run without a seed. Get the seed from your secret manager, never from the repo.
# See docs/Guides/Deployment_Pipeline_Manual.md for the rotation procedure.
export KOCOLOR_SIGNING_SEED_HEX="${KOCOLOR_SIGNING_SEED_HEX:?Please export KOCOLOR_SIGNING_SEED_HEX (64 hex chars)}"
# Optional: stamps key_id into manifest.json. Production must set e.g. "atelier-prod-01".
export KOCOLOR_KEY_ID="${KOCOLOR_KEY_ID:-atelier-dev-01}"

echo "========================================"
echo " KoColor Sovereign Distribution Pipeline"
echo "========================================"

# 1. Clean the previous build
echo
echo "🧹 Cleaning previous artifacts..."
rm -rf "$ROOT/dist/"
rm -f "$ROOT/dist/kocolor-v1-deploy.zip"

echo
echo "[1/2] Running KC Optimizer..."
cargo run \
    --manifest-path "$ROOT/kc-optimizer/Cargo.toml" \
    --release

echo
echo "[2/2] Running KC Distributor..."
cargo run \
    --manifest-path "$ROOT/kc-distributor/Cargo.toml" \
    --release

echo
echo "========================================"
echo " Deployment Ready"
echo "========================================"
echo
echo "dist/kocolor-v1-deploy.zip"
