#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Define target paths
TARGET_DIR="src/main/assets"
TARGET_FILE="$TARGET_DIR/model.litertlm"

echo "==========================================="
echo "📥 AI Model Downloader for Asset Pack"
echo "==========================================="

# Ensure the assets directory exists
mkdir -p "$TARGET_DIR"

if [ -z "$HF_TOKEN" ]; then
    echo "⚠️  No HF_TOKEN environment variable detected."
    echo "Downloading the ungated Qwen2.5-1.5B (Apache 2.0) model for immediate testing..."
    echo ""
    DOWNLOAD_URL="https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm?download=true"

    # Download directly without authorization header
    curl -L -C - -o "$TARGET_FILE" "$DOWNLOAD_URL"
else
    echo "✅ HF_TOKEN detected."
    echo "Downloading the official gated Google Gemma 2B model..."
    echo ""
    DOWNLOAD_URL="https://huggingface.co/google/gemma-2-2b-it-litert/resolve/main/gemma2-2b-it-gpu-int8.bin?download=true"

    # Download using the Hugging Face access token
    curl -L -C - -H "Authorization: Bearer $HF_TOKEN" -o "$TARGET_FILE" "$DOWNLOAD_URL"
fi

echo ""
echo "✅ Download successful!"
echo "Model safely placed at: features/ai/model_pack/$TARGET_FILE"
echo "File size: $(du -h $TARGET_FILE | cut -f1)"
echo "You can now build the App Bundle (.aab) to deploy via Play Asset Delivery."
