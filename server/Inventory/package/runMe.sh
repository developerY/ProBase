#!/usr/bin/env bash

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

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
