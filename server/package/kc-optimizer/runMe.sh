#!/bin/bash

# -----------------------------------------------------------------------------
# KoColor Asset Engineering Pipeline - Build & Sign Orchestrator
# -----------------------------------------------------------------------------

# Move to the directory where this script lives
cd "$(dirname "$0")" || exit 1

# 1. Environment & Security Check
if [ -f .env ]; then
    # Load keys (ignoring comments)
    # shellcheck disable=SC2046
    export "$(grep -v '^#' .env | xargs)"
    echo "✅ [Security] Loaded Ed25519 signing key from .env"
else
    echo "⚠️  [Warning] .env file not found."
    echo "   A temporary key will be generated for this run, but signatures will drift."
    echo "   Set CDN_PRIVATE_KEY_HEX in a .env file for deterministic builds."
fi

# 2. Performance Initialization
# We use --release to ensure Rayon image math and Zstd compression
# are executed with full CPU optimizations.
echo "⚙️  [Compiler] Running kocolor-asset-processor (CCT Mode)..."

cargo run --release

# 3. Finalization
BUILD_STATUS=$?

if [ $BUILD_STATUS -eq 0 ]; then
    echo "------------------------------------------------------------------"
    echo "🚀 [Success] Distribution artifacts ready in /dist"
    echo "✅ manifest.json     - Signed global registry"
    echo "✅ assets/*.webp    - Optimized hero & thumbnail images"
    echo "✅ *.kpkg           - Sealed Zstd Level 19 binaries"
    echo "------------------------------------------------------------------"
    echo "Next: Sync the /dist folder to your GitHub Pages CDN."
else
    echo "❌ [Failure] Pipeline execution failed. Check Rust logs above."
    exit $BUILD_STATUS
fi
