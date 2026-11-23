#!/usr/bin/env bash
# AshBike Phone Release — Full P0/P1/P2 Ground-Truth Audit
# macOS (Bash 3.2+) and Linux compatible. Never prints raw Firebase API keys.
set -u

MOBILE_DIR="applications/ashbike/apps/mobile"
MOBILE_GRADLE=":applications:ashbike:apps:mobile"
MANIFEST_SRC="$MOBILE_DIR/src/main/AndroidManifest.xml"
PRIVACY_DOC="applications/ashbike/docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md"

RUN_GRADLE=0
for arg in "$@"; do
  case "$arg" in
    --gradle) RUN_GRADLE=1 ;;
    -h|--help)
      echo "Usage: $0 [--gradle]"
      echo "  --gradle   Run processReleaseMainManifest, testDebugUnitTest, and lintRelease first"
      exit 0
      ;;
  esac
done

# Auto-disable colors when piped to a file
if [[ -t 1 ]]; then
  RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[0;33m'; CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'
else
  RED=''; GREEN=''; YELLOW=''; CYAN=''; BOLD=''; NC=''
fi

PASS=0; FAIL=0; WARN=0; MANUAL=0

section() { printf "\n${BOLD}${CYAN}============================================================\n%s\n============================================================${NC}\n" "$1"; }
pass()    { PASS=$((PASS + 1));     printf "${GREEN}[PASS]${NC}   %s\n" "$1"; }
fail()    { FAIL=$((FAIL + 1));     printf "${RED}[FAIL]${NC}   %s\n" "$1"; }
warn()    { WARN=$((WARN + 1));     printf "${YELLOW}[WARN]${NC}   %s\n" "$1"; }
manual()  { MANUAL=$((MANUAL + 1)); printf "${BOLD}[MANUAL]${NC} %s\n" "$1"; }
info()    { printf "         %s\n" "$1"; }

# Unified search helper (uses rg if available, falls back to grep -rnE)
search() {
  local pattern="$1"; shift
  if command -v rg >/dev/null 2>&1; then
    rg -n --no-heading "$pattern" "$@" 2>/dev/null || true
  else
    grep -rnE --exclude-dir={build,.gradle,.git,node_modules,intermediates} "$pattern" "$@" 2>/dev/null || true
  fi
}

search_i() {
  local pattern="$1"; shift
  if command -v rg >/dev/null 2>&1; then
    rg -ni --no-heading "$pattern" "$@" 2>/dev/null || true
  else
    grep -rniE --exclude-dir={build,.gradle,.git,node_modules,intermediates} "$pattern" "$@" 2>/dev/null || true
  fi
}

if [[ ! -d "$MOBILE_DIR" ]]; then
  echo "ERROR: Run this script from the repository root (cannot find $MOBILE_DIR)."
  exit 1
fi

printf "${BOLD}AshBike Phone Release Audit${NC}\n"
printf "Timestamp : %s\n" "$(date '+%Y-%m-%d %H:%M:%S %Z')"
printf "Branch    : %s\n" "$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'unknown')"
printf "Commit    : %s\n" "$(git rev-parse --short HEAD 2>/dev/null || echo 'unknown')"
printf "Dirty Tree: %s modified/untracked files\n" "$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')"

# ------------------------------------------------------------------
# OPTIONAL GRADLE REFRESH
# ------------------------------------------------------------------
if [[ "$RUN_GRADLE" -eq 1 ]]; then
  section "GRADLE VERIFICATION (--gradle)"
  for task in \
    "${MOBILE_GRADLE}:processReleaseMainManifest" \
    "${MOBILE_GRADLE}:testDebugUnitTest" \
    "${MOBILE_GRADLE}:lintRelease"; do
    printf "Running ./gradlew %s ...\n" "$task"
    TMP_LOG=$(mktemp)
    if ./gradlew "$task" >"$TMP_LOG" 2>&1; then
      pass "Gradle task succeeded: $task"
    else
      fail "Gradle task FAILED: $task"
      tail -n 25 "$TMP_LOG" | sed 's/^/         | /'
    fi
    rm -f "$TMP_LOG"
  done
else
  warn "Skipped Gradle runs (pass --gradle to refresh merged manifest, tests, and lint)"
fi

# Locate merged release manifest (supports AGP 7/8/9 paths)
MERGED_MANIFEST=$(find "$MOBILE_DIR/build/intermediates/merged_manifest" -name "AndroidManifest.xml" 2>/dev/null | head -n 1 || true)
if [[ -n "$MERGED_MANIFEST" ]]; then
  info "Merged release manifest: $MERGED_MANIFEST"
else
  warn "Merged release manifest NOT FOUND — run with --gradle so manifest checks are accurate"
fi

# ==================================================================
# P0 — SHIP BLOCKERS
# ==================================================================
section "P0 — SHIP BLOCKERS (Items 1-6)"

# --- P0 #1: Firebase Crashlytics & Analytics ---
echo "-- P0 #1: Firebase Collection & Consent --"
FB_MANIFEST_TARGET="${MERGED_MANIFEST:-$MANIFEST_SRC}"
FB_META=$(search "firebase_.*collection|google_analytics_adid_collection_enabled" "$FB_MANIFEST_TARGET")
if [[ -n "$FB_META" ]]; then
  info "Firebase manifest meta-data found:"
  echo "$FB_META" | sed 's/^/         | /'
else
  warn "No Firebase collection meta-data flags found in $FB_MANIFEST_TARGET (Crashlytics & Analytics collect by default)"
fi

FB_RUNTIME=$(search "setCrashlyticsCollectionEnabled|isCrashlyticsCollectionEnabled|setAnalyticsCollectionEnabled" "$MOBILE_DIR/src")
if [[ -n "$FB_RUNTIME" ]]; then
  pass "Runtime Crashlytics/Analytics toggle code found:"
  echo "$FB_RUNTIME" | sed 's/^/         | /'
else
  fail "P0 #1: No runtime Crashlytics/Analytics toggle/consent code found in $MOBILE_DIR/src"
fi

# --- P0 #2: Privacy Policy & MainActivity URL ---
echo "-- P0 #2: Privacy Policy & URL Wiring --"
if [[ -f "$PRIVACY_DOC" ]]; then
  STALE_POLICY=$(search_i "no external crash reporting|encrypted|March 2026" "$PRIVACY_DOC")
  if [[ -n "$STALE_POLICY" ]]; then
    warn "P0 #2: Potential stale/contradictory lines in $PRIVACY_DOC:"
    echo "$STALE_POLICY" | sed 's/^/         | /'
  else
    pass "No stale 'no external crash reporting' / 'encrypted' / 'March 2026' lines in $PRIVACY_DOC"
  fi
else
  fail "P0 #2: Missing $PRIVACY_DOC"
fi

TODO_URL=$(search_i "ylabz\.com|actual privacy policy" applications/ashbike/ docs/)
if [[ -n "$TODO_URL" ]]; then
  fail "P0 #2: Stale placeholder privacy URL / TODO still present:"
  echo "$TODO_URL" | sed 's/^/         | /'
else
  pass "No ylabz.com or placeholder privacy URL TODOs found"
fi

HC_INTENTS=$(search "ACTION_SHOW_PERMISSIONS_RATIONALE|VIEW_PERMISSION_USAGE|START_VIEW_PERMISSION_USAGE|HEALTH_PERMISSIONS" "$FB_MANIFEST_TARGET")
if [[ -n "$HC_INTENTS" ]]; then
  pass "Health Connect privacy intent entries found:"
  echo "$HC_INTENTS" | sed 's/^/         | /'
else
  fail "P0 #2: Missing Health Connect privacy policy intent filters in manifest"
fi

# --- P0 #3: FGS Type for BLE ---
echo "-- P0 #3: Foreground Service Type & BLE Injection --"
FGS_LINES=$(search "foregroundServiceType|FOREGROUND_SERVICE_" "$FB_MANIFEST_TARGET")
echo "$FGS_LINES" | sed 's/^/         | /'

BLE_IN_SVC=$(search_i "ble|bluetooth|heartrate|MobileTrackingModule" "$MOBILE_DIR/src/main")
HAS_CONNECTED_DEV=$(echo "$FGS_LINES" | grep -c "connectedDevice" || true)
HAS_BLE_IN_SERVICE_FILE=$(search_i "ble|bluetooth|heart" $(find applications/ashbike -name "BikeForegroundService.kt" -o -name "MobileTrackingModule.kt" 2>/dev/null) || true)

if [[ -n "$HAS_BLE_IN_SERVICE_FILE" && "$HAS_CONNECTED_DEV" -eq 0 ]]; then
  fail "P0 #3: BLE references exist in BikeForegroundService/MobileTrackingModule, but connectedDevice FGS type is MISSING (Android 14+ SecurityException risk)"
  echo "$HAS_BLE_IN_SERVICE_FILE" | head -n 10 | sed 's/^/         | /'
elif [[ "$HAS_CONNECTED_DEV" -gt 0 ]]; then
  pass "P0 #3: connectedDevice FGS type is declared in manifest"
else
  pass "P0 #3: No BLE engine references found in BikeForegroundService/MobileTrackingModule"
fi

# --- P0 #4: Lint Baseline ---
echo "-- P0 #4: Lint Baseline Removal --"
if [[ -f "$MOBILE_DIR/lint-baseline.xml" ]]; then
  fail "P0 #4: $MOBILE_DIR/lint-baseline.xml STILL EXISTS"
else
  pass "P0 #4: $MOBILE_DIR/lint-baseline.xml is deleted"
fi
BASELINE_REF=$(search "lint-baseline" "$MOBILE_DIR/build.gradle" "$MOBILE_DIR/build.gradle.kts" 2>/dev/null)
if [[ -n "$BASELINE_REF" ]]; then
  fail "P0 #4: Gradle still references lint-baseline:"
  echo "$BASELINE_REF" | sed 's/^/         | /'
else
  pass "P0 #4: No lint-baseline reference in mobile build.gradle(.kts)"
fi

# --- P0 #5: R8 / ProGuard & Debug Symbols ---
echo "-- P0 #5: R8 / ProGuard & Debug Symbols --"
PROGUARD_FILES=$(find "$MOBILE_DIR" -name "proguard*.pro" 2>/dev/null || true)
if [[ -n "$PROGUARD_FILES" ]]; then
  info "ProGuard files: $PROGUARD_FILES"
  NAV_KEEP=$(search_i "AshBikeDestination|NavKey|serialization|SourceFile|LineNumberTable" $PROGUARD_FILES)
  if [[ -n "$NAV_KEEP" ]]; then
    pass "R8 keep rules for serialization/Nav3/LineNumberTable found:"
    echo "$NAV_KEEP" | sed 's/^/         | /'
  else
    warn "P0 #5: No explicit Nav3/AshBikeDestination/LineNumberTable keep rules found in $PROGUARD_FILES"
  fi
else
  warn "P0 #5: No proguard*.pro found in $MOBILE_DIR"
fi

NDK_SYM=$(search "debugSymbolLevel" "$MOBILE_DIR/build.gradle" "$MOBILE_DIR/build.gradle.kts" 2>/dev/null)
if [[ -n "$NDK_SYM" ]]; then
  pass "Native debug symbols configured: $NDK_SYM"
else
  warn "debugSymbolLevel not found in mobile build.gradle(.kts)"
fi

MAPPING_FILE=$(find "$MOBILE_DIR/build/outputs/mapping/release" -name "mapping.txt" 2>/dev/null | head -n 1 || true)
if [[ -n "$MAPPING_FILE" ]]; then
  pass "Release R8 mapping exists: $MAPPING_FILE"
else
  warn "No release R8 mapping.txt built yet (run bundleRelease/assembleRelease to verify R8)"
fi
manual "P0 #5: Install shrunk release AAB on physical phone and run full ride + Health Connect loop"

# --- P0 #6: Release Versioning ---
echo "-- P0 #6: Versioning (Mobile vs Wear collision check) --"
MOBILE_VER=$(search "versionCode|versionName" "$MOBILE_DIR/build.gradle" "$MOBILE_DIR/build.gradle.kts" 2>/dev/null)
info "Mobile version config:"
echo "$MOBILE_VER" | sed 's/^/         | /'
WEAR_GRADLE=$(find applications/ashbike/apps -path "*wear*" \( -name "build.gradle" -o -name "build.gradle.kts" \) 2>/dev/null | head -n 1 || true)
if [[ -n "$WEAR_GRADLE" ]]; then
  info "Wear version config ($WEAR_GRADLE):"
  search "versionCode|versionName" "$WEAR_GRADLE" | sed 's/^/         | /'
fi
VER_DOC=$(git ls-files "*VERSION*" "*version*" 2>/dev/null | grep -i "doc\|md" || true)
if [[ -n "$VER_DOC" ]]; then
  pass "Versioning doc found: $VER_DOC"
else
  warn "P0 #6: No dedicated versioning rule doc found in git"
fi

# ==================================================================
# P1 — PRE-RELEASE HARDENING
# ==================================================================
section "P1 — PRE-RELEASE HARDENING (Items 7-11)"

# --- P1 #7: Manifest <-> Policy Test ---
echo "-- P1 #7: Manifest <-> Policy Alignment Test --"
ATEST_FILES=$(find "$MOBILE_DIR/src/androidTest" -name "*Health*Test*.kt" 2>/dev/null || true)
JTEST_FILES=$(find "$MOBILE_DIR/src/test" -name "*Health*Manifest*Test*.kt" 2>/dev/null || true)
if [[ -n "$ATEST_FILES" ]]; then
  pass "P1 #7: Health manifest androidTest found: $ATEST_FILES"
  STRICT_EQ=$(search "assertEquals|containsExactly|==" $ATEST_FILES)
  if [[ -n "$STRICT_EQ" ]]; then
    info "Check equality assertion in androidTest (verify it checks BOTH directions, not just subset):"
    echo "$STRICT_EQ" | head -n 6 | sed 's/^/         | /'
  fi
else
  fail "P1 #7: No Health manifest alignment androidTest found under $MOBILE_DIR/src/androidTest"
fi
[[ -n "$JTEST_FILES" ]] && info "Companion JVM manifest test: $JTEST_FILES"

# --- P1 #8: Health Connect 3-Way Mismatch ---
echo "-- P1 #8: Health Connect 3-Way Alignment (Manifest vs Policy vs Code) --"
if [[ -n "$MERGED_MANIFEST" ]]; then
  HC_MERGED=$(grep -oE "android\.permission\.health\.[A-Z0-9_]+" "$MERGED_MANIFEST" | sort -u || true)
  HC_COUNT=$(echo "$HC_MERGED" | grep -c "android\.permission\.health\." || true)
  info "Merged release manifest has $HC_COUNT Health Connect permissions:"
  echo "$HC_MERGED" | sed 's/^/         | /'
  STRAY_HC=$(echo "$HC_MERGED" | grep -E "READ_SPEED|WRITE_SPEED|READ_ELEVATION_GAINED|WRITE_ELEVATION_GAINED|WRITE_ACTIVE_CALORIES_BURNED|READ_ACTIVE_CALORIES_BURNED" || true)
  if [[ -n "$STRAY_HC" ]]; then
    warn "P1 #8: Speed / Elevation / ActiveCalories permissions are STILL in merged release manifest:"
    echo "$STRAY_HC" | sed 's/^/         | /'
  else
    pass "P1 #8: No Speed / Elevation / ActiveCalories permissions in merged release manifest"
  fi
else
  warn "P1 #8: Cannot verify merged Health Connect permissions without merged release manifest"
fi

POLICY_FILES=$(find applications/ashbike -name "*HealthPermissionPolicy*.kt" 2>/dev/null || true)
if [[ -n "$POLICY_FILES" ]]; then
  info "Runtime HealthPermissionPolicy references ($POLICY_FILES):"
  search "HealthPermission|getReadPermission|getWritePermission|Record::class" $POLICY_FILES | sed 's/^/         | /'
fi

SPEED_CODE=$(search "SpeedRecord|ElevationGainedRecord|ActiveCaloriesBurnedRecord" applications/ashbike/ 2>/dev/null | grep -v "Test" || true)
if [[ -n "$SPEED_CODE" ]]; then
  info "Non-test code references to SpeedRecord/ElevationGainedRecord/ActiveCaloriesBurnedRecord:"
  echo "$SPEED_CODE" | head -n 12 | sed 's/^/         | /'
else
  info "Zero non-test code references to SpeedRecord / ElevationGainedRecord / ActiveCaloriesBurnedRecord"
fi

# --- P1 #9: Firebase Project Config ---
echo "-- P1 #9: Firebase Config Files --"
GS_FILES=$(find applications/ashbike -name "google-services*.json" 2>/dev/null || true)
GS_COUNT=$(echo "$GS_FILES" | grep -c "google-services" || true)
echo "$GS_FILES" | sed 's/^/         | /'
if echo "$GS_FILES" | grep -q "google-services-ylabz.json"; then
  fail "P1 #9: Stray google-services-ylabz.json still present"
elif [[ "$GS_COUNT" -eq 1 ]]; then
  pass "P1 #9: Single google-services.json present"
else
  warn "P1 #9: Found $GS_COUNT google-services*.json files (review above)"
fi
for f in $GS_FILES; do
  [[ -f "$f" ]] || continue
  info "Metadata for $f (API keys redacted):"
  grep -E '"project_id"|"package_name"' "$f" | sed 's/^/         | /'
done
manual "P1 #9: Confirm GCP API key restrictions in Google Cloud Console for android-zoewave"

# --- P1 #10 & #11: Play Console & Field Tests ---
echo "-- P1 #10 & #11: Release Docs & Human Checks --"
if [[ -d "docs/release" ]]; then
  info "docs/release/ files:"
  ls -1 docs/release/ | sed 's/^/         | /'
fi
manual "P1 #10: Confirm Health Connect declaration is APPROVED in Play Console for exact v1 list"
manual "P1 #11: Execute & sign off 1h GPS, 2h screen-off, BLE reconnect, and reinstall wipe tests"

# ==================================================================
# P2 — POLISH & HYGIENE
# ==================================================================
section "P2 — POLISH & HYGIENE (Items 12-17)"

# --- P2 #12: Backup & Data Extraction Rules ---
echo "-- P2 #12: Backup & Data Extraction Rules --"
BACKUP_ATTRS=$(search "allowBackup|fullBackupContent|dataExtractionRules" "$FB_MANIFEST_TARGET")
echo "$BACKUP_ATTRS" | sed 's/^/         | /'
if echo "$BACKUP_ATTRS" | grep -q 'fullBackupContent="true"'; then
  fail "P2 #12: fullBackupContent is still set to true"
elif [[ -f "$MOBILE_DIR/src/main/res/xml/backup_rules.xml" && -f "$MOBILE_DIR/src/main/res/xml/data_extraction_rules.xml" ]]; then
  pass "P2 #12: Both backup_rules.xml and data_extraction_rules.xml exist"
else
  fail "P2 #12: Missing backup_rules.xml or data_extraction_rules.xml"
fi

# --- P2 #13: POST_NOTIFICATIONS Timing ---
echo "-- P2 #13: POST_NOTIFICATIONS Timing --"
NOTIF_HITS=$(search "POST_NOTIFICATIONS" "$MOBILE_DIR/src/main")
if [[ -n "$NOTIF_HITS" ]]; then
  info "POST_NOTIFICATIONS sites in $MOBILE_DIR/src/main:"
  echo "$NOTIF_HITS" | sed 's/^/         | /'
else
  warn "P2 #13: No POST_NOTIFICATIONS runtime request found in $MOBILE_DIR/src/main"
fi

# --- P2 #14: Unused Permissions ---
echo "-- P2 #14: Unused Permissions (NFC / VIBRATE / BLUETOOTH_ADVERTISE / AD_ID / BG_LOC) --"
if [[ -n "$MERGED_MANIFEST" ]]; then
  UNUSED_PERMS=$(grep -E "android\.permission\.(NFC|VIBRATE|BLUETOOTH_ADVERTISE|ACCESS_BACKGROUND_LOCATION|ACCESS_ADSERVICES_AD_ID|ACCESS_ADSERVICES_ATTRIBUTION|AD_ID)" "$MERGED_MANIFEST" || true)
  if [[ -n "$UNUSED_PERMS" ]]; then
    warn "P2 #14: Potentially unused permissions still in merged release manifest:"
    echo "$UNUSED_PERMS" | sed 's/^/         | /'
  else
    pass "P2 #14: NFC, VIBRATE, BLUETOOTH_ADVERTISE, AD_ID, and ACCESS_BACKGROUND_LOCATION absent from merged release manifest"
  fi
fi

# --- P2 #15: ARCore Metadata ---
echo "-- P2 #15: ARCore Metadata --"
if [[ -n "$MERGED_MANIFEST" ]]; then
  ARCORE_HITS=$(search_i "ar\.core|arcore|camera\.ar|InstallActivity" "$MERGED_MANIFEST")
  if [[ -n "$ARCORE_HITS" ]]; then
    fail "P2 #15: ARCore references still in merged release manifest:"
    echo "$ARCORE_HITS" | sed 's/^/         | /'
  else
    pass "P2 #15: Zero ARCore references in merged release manifest"
  fi
fi

# --- P2 #16: Tree Hygiene ---
echo "-- P2 #16: Tree Hygiene --"
EXAMPLE_TESTS=$(git ls-files "*Example*Test*" 2>/dev/null || true)
EXAMPLE_COUNT=$(echo "$EXAMPLE_TESTS" | grep -c "Example" || true)
if [[ "$EXAMPLE_COUNT" -gt 0 ]]; then
  warn "P2 #16: $EXAMPLE_COUNT Example*Test* files still tracked in git:"
  echo "$EXAMPLE_TESTS" | head -n 15 | sed 's/^/         | /'
else
  pass "P2 #16: Zero Example*Test* files tracked in git"
fi

ASSET_PACK=$(git ls-files "*asset_pack_manifest*" 2>/dev/null || true)
if [[ -n "$ASSET_PACK" ]]; then
  fail "P2 #16: Committed build intermediate still tracked: $ASSET_PACK"
else
  pass "P2 #16: No asset_pack_manifest build intermediates tracked in git"
fi

if grep -q "intermediates" .gitignore 2>/dev/null; then
  pass "P2 #16: .gitignore contains an intermediates/ rule"
else
  warn "P2 #16: .gitignore has no explicit intermediates rule (verify build/ is ignored)"
fi

# --- P2 #17: Stale Trackers ---
echo "-- P2 #17: Trackers & Audit Docs in Repo --"
TRACKERS=$(git ls-files "*Steps*" "*AUDIT*" "*READINESS*" "*PUNCHLIST*" "*SNAPSHOT*" 2>/dev/null || true)
info "Tracked status/audit docs:"
echo "$TRACKERS" | sed 's/^/         | /'
if echo "$TRACKERS" | grep -qi "Steps\.md"; then
  warn "P2 #17: Steps.md is still tracked in git"
else
  pass "P2 #17: Steps.md is removed"
fi

# --- Full Permission & Feature Snapshot (for Play Console) ---
if [[ -n "$MERGED_MANIFEST" ]]; then
  section "MERGED RELEASE MANIFEST SNAPSHOT (Permissions & Features)"
  info "All <uses-permission>:"
  grep -oE 'uses-permission[^>]+android:name="[^"]+"' "$MERGED_MANIFEST" | sed 's/.*android:name="//;s/"//' | sort -u | sed 's/^/         | /'
  info "All <uses-feature>:"
  grep -E '<uses-feature' "$MERGED_MANIFEST" | sed 's/^[[:space:]]*/         | /'
fi

# ==================================================================
# SCOREBOARD
# ==================================================================
section "AUDIT SUMMARY"
printf "${GREEN}PASS${NC}   : %d\n" "$PASS"
printf "${RED}FAIL${NC}   : %d\n" "$FAIL"
printf "${YELLOW}WARN${NC}   : %d\n" "$WARN"
printf "${BOLD}MANUAL${NC} : %d (Play Console / GCP / physical device checks)\n" "$MANUAL"

if [[ "$FAIL" -gt 0 ]]; then
  exit 1
fi
exit 0