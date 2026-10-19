#!/usr/bin/env bash
set -euo pipefail

# Delegate to prog_rs Rust pipeline runner
cd "$(dirname "$0")/../utils/prog_rs"
./run.sh "${1:-"gemini-3.8-flash"}"
