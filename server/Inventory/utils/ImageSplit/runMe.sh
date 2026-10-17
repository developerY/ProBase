#!/usr/bin/env bash
set -e

echo "🚀 Starting KoColor Batch Image Slicer..."

# 1. Scaffolding
mkdir -p input_images
mkdir -p output_slices

# 2. Check for input files
if [ -z "$(ls -A input_images)" ]; then
    echo "⚠️  The 'input_images/' directory is currently empty."
    echo "Please drop your 3x3 grid image files into 'input_images/' and run this script again."
    exit 1
fi

# 3. Compile and Run
echo "⚙️  Compiling kc-slicer in release mode..."
cargo build --release

echo "🔪 Executing batch slicer..."
cargo run --release

echo "🎉 Done! Check the output_slices/ directory for your newly cropped assets."
