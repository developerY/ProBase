#!/bin/bash
SDK_ROOT="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ZIPALIGN=$(find "$SDK_ROOT/build-tools" -name "zipalign" 2>/dev/null | sort | tail -n 1)
[ -x "$ZIPALIGN" ] || { echo "FATAL: zipalign not found under $SDK_ROOT/build-tools"; exit 1; }

run_bundletool() {
    if command -v bundletool >/dev/null 2>&1; then
        bundletool "$@"
    else
        BT_JAR=$(find "$HOME" -name "bundletool*.jar" 2>/dev/null | head -n 1)
        [ -n "$BT_JAR" ] || { echo "FATAL: bundletool binary and bundletool*.jar both missing"; exit 1; }
        java -jar "$BT_JAR" "$@"
    fi
}

AAB_PATH=$(find applications/kocolor/apps/mobile/build/outputs/bundle/release -name "*.aab" | head -n 1)

run_bundletool dump config --bundle="$AAB_PATH" | grep -i -C 2 "alignment"

rm -rf /tmp/kocolor_16kb_check/apks_universal /tmp/kocolor_16kb_check/apks_splits
mkdir -p /tmp/kocolor_16kb_check/apks_universal /tmp/kocolor_16kb_check/apks_splits

run_bundletool build-apks --bundle="$AAB_PATH" --output=/tmp/kocolor_16kb_check/universal.apks --mode=universal
unzip -q /tmp/kocolor_16kb_check/universal.apks -d /tmp/kocolor_16kb_check/apks_universal
"$ZIPALIGN" -c -P 16 -v 4 /tmp/kocolor_16kb_check/apks_universal/universal.apk | tail -n 5

run_bundletool build-apks --bundle="$AAB_PATH" --output=/tmp/kocolor_16kb_check/splits.apks
unzip -q /tmp/kocolor_16kb_check/splits.apks -d /tmp/kocolor_16kb_check/apks_splits
for apk in $(find /tmp/kocolor_16kb_check/apks_splits -name "*.apk" | sort); do
    if unzip -l "$apk" | grep -q "\.so$"; then
        echo "=== Checking native split APK: $apk ==="
        "$ZIPALIGN" -c -P 16 -v 4 "$apk" | tail -n 5
    fi
done
