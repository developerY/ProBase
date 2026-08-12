#!/usr/bin/env bash

# Fail immediately if any command exits with a non-zero status
set -e

echo "🚀 Starting KoColor V1 Build Pipeline..."
echo "========================================="

# 1. Clean the previous build
echo "🧹 Cleaning previous artifacts..."
rm -rf dist/
rm -f kocolor-v1-deploy.zip

# 2. Run the Optimizer (CCT, JSON compression, Ed25519 Signing)
echo "🧠 Phase 1: Executing kc-optimizer..."
cargo run --release --manifest-path kc-optimizer/Cargo.toml

# 3. Run the Distributor (WebP Transcoding, Flattening, Zipping)
echo "🖼️ Phase 2: Executing kc-distributor..."
cargo run --release --manifest-path kc-distributor/Cargo.toml

echo "========================================="
echo "✅ Pipeline Complete!"
echo "📁 Flattened CDN directory: ./dist/"
echo "📦 Deployment Archive: ./dist/kocolor-v1-deploy.zip"
