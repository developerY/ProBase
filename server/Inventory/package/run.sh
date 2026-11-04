#!/usr/bin/env bash
set -euo pipefail

# KoColor catalog compiler entry point. Delegates to the prog_rs pipeline runner.
# Required env (see docs/Guides/Deployment_Pipeline_Manual.md):
#   GEMINI_API_KEY, KOCOLOR_SIGNING_SEED_HEX (64 hex chars, from your secret manager)
# Optional: KOCOLOR_KEY_ID (defaults to "atelier-dev-01"; production must set e.g. "atelier-prod-01").
cd "$(dirname "$0")/../utils/prog_rs"
./run.sh "${1:-"gemini-3.8-flash"}"
