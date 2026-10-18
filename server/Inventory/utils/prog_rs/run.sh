#!/usr/bin/env bash
set -euo pipefail

export GEMINI_API_KEY="${GEMINI_API_KEY:?Please export GEMINI_API_KEY}"

MODEL_NAME=${1:-"gemini-3.8-flash"}

echo "Starting pipeline using model: ${MODEL_NAME}"

cargo run --release -- \
  --input-dir ../../package/input/KoColor \
  --output-dir ../../package/dist \
  --model "${MODEL_NAME}" \
  --resume
