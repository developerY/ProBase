#!/bin/bash
rm -rf /tmp/kocolor_16kb_check && mkdir -p /tmp/kocolor_16kb_check
AAB_PATH=$(find applications/kocolor/apps/mobile/build/outputs/bundle/release -name "*.aab" | head -n 1)
echo "Inspecting AAB: $AAB_PATH"
unzip -q "$AAB_PATH" "*/lib/*" -d /tmp/kocolor_16kb_check

SDK_ROOT="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
OBJDUMP=$(find "$SDK_ROOT/ndk" -name "llvm-objdump" 2>/dev/null | sort | tail -n 1)
[ -x "$OBJDUMP" ] || { echo "FATAL: llvm-objdump not found under $SDK_ROOT/ndk"; exit 1; }

echo "Using llvm-objdump: $OBJDUMP"
for so in $(find /tmp/kocolor_16kb_check -name "*.so" | sort); do
    echo "=== $so ==="
    "$OBJDUMP" -p "$so" | grep -E "LOAD"
done
