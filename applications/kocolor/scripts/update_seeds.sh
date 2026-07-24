#!/bin/bash

# Exit immediately if any command fails
set -e

echo "💄 Preparing KoColor Seed Data..."

# 1. Get the absolute path to the directory where THIS script lives
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# 2. Calculate the ProBase repository root (3 levels up from applications/kocolor/scripts)
PROBASE_ROOT="$( cd "$SCRIPT_DIR/../../.." >/dev/null 2>&1 && pwd )"

# 3. Navigate to the root directory so the Python relative paths align perfectly
cd "$PROBASE_ROOT"

# 4. Verify Python 3 is installed on this Mac
if ! command -v python3 &> /dev/null; then
    echo "❌ Error: python3 is not installed or not in your PATH."
    exit 1
fi

# 5. Execute the Python generator
echo "📂 Running from project root: $PROBASE_ROOT"
python3 applications/kocolor/scripts/generate_seeds.py

echo "✨ Seed Data Successfully Updated!"