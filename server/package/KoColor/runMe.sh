#!/bin/bash

# Move to the directory where this script actually lives
cd "$(dirname "$0")"

# Load environment variables if .env exists
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
    echo "✅ Loaded environment variables from .env"
else
    echo "⚠️  Warning: .env file not found. Ensure CDN_PRIVATE_KEY_HEX is set in your shell."
fi

# Run the binary
cargo run --bin generate_payload