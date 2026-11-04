#!/usr/bin/env bash
set -euo pipefail

export GEMINI_API_KEY="${GEMINI_API_KEY:?Please export GEMINI_API_KEY}"
export KOCOLOR_SIGNING_SEED_HEX="${KOCOLOR_SIGNING_SEED_HEX:?Please export KOCOLOR_SIGNING_SEED_HEX (64 hex chars; see docs/Guides/Deployment_Pipeline_Manual.md)}"
# Optional: stamps key_id into manifest.json. Defaults to "atelier-dev-01" (dev).
# Production releases must set KOCOLOR_KEY_ID to the production key id (e.g. "atelier-prod-01").
export KOCOLOR_KEY_ID="${KOCOLOR_KEY_ID:-atelier-dev-01}"

MODEL_NAME=${1:-"gemini-3.8-flash"}

echo "Starting pipeline using model: ${MODEL_NAME} (key_id: ${KOCOLOR_KEY_ID})"

cargo run --release -- \
  --input-dir ../../package/input/KoColor \
  --output-dir ../../package/dist \
  --model "${MODEL_NAME}" \
  --resume
