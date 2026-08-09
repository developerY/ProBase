#!/bin/bash

# -----------------------------------------------------------------------------
# KoColor Package Distribution & Signing Pipeline
# -----------------------------------------------------------------------------

# Move to the directory where this script actually lives
cd "$(dirname "$0")"

# 1. Environment & Security Setup
if [ -f .env ]; then
    # Filter out comments and export variables
    export $(grep -v '^#' .env | xargs)
    echo "✅ [Security] Loaded distribution keys from .env"
else
    echo "❌ [Error] .env file not found. Create one with CDN_PRIVATE_KEY_HEX."
    echo "   See .env.example for a template."
    exit 1
fi

# 2. Workspace Cleanup
echo "🧹 [Cleanup] Clearing previous distribution artifacts..."
rm -rf dist/
mkdir -p dist/

# 3. Main Generation & Compilation
echo "⚙️  [Compiler] Running authoritative payload generator (Version 1)..."

# Run the distribution generator which compiles Core, Seasonal, and the Manifest
cargo run --bin kocolor-compiler

# Capture exit status
GEN_STATUS=$?

if [ $GEN_STATUS -eq 0 ]; then
    echo "------------------------------------------------------------------"
    echo "🚀 [Success] Distribution assets generated in /dist"
    echo "✅ manifest.json      - Signed master index"
    echo "✅ search_index.json  - Global discovery index"
    echo "✅ *.kpkg             - Compressed & Signed binary packages"
    echo "------------------------------------------------------------------"
    echo "Next: Upload the contents of /dist to your GitHub Pages CDN."
else
    echo "❌ [Failure] Compilation failed. Check Rust logs above."
    exit $GEN_STATUS
fi
