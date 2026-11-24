#!/usr/bin/env bash
# ==============================================================================
# AshBike Phone v1 — Full End-to-End Verification & BLE Hub Readiness Suite
# ==============================================================================
# Covers:
#   0. Tooling & Auto-ADB Resolution (reads local.properties sdk.dir)
#   1. Git Scope, Tree Hygiene & Secret Leak Guard (never prints raw API keys)
#   2. Full Gradle Build, Unit Tests (Phone + Shared + BLE + Core Data), Lint & R8
#   3. Test Quality & JUnit Tally (auto-clears stale XMLs; excludes Wear/Glass)
#   4. Deep Merged Release Manifest XML Audit (Permissions, FGS, HC, Firebase)
#   5. Complete BLE Hub Code, UUID, API-34+, DI Wiring, Fusion & Unit-Test Audit
#      (HRS 0x180D, CPS 0x1818, CSC 0x1816, Read-Only FTMS 0x1826, BAS 0x180F,
#       OpenBikeControl, BleSensorHub, RideSensorFusionEngine, ObcRideActionMapper)
#   6. Privacy Policy Content & Live Main-Branch Parity Check
#   7. Lint Baseline & Release Lint XML Audit
#   8. Release AAB Binary Inspection & Specific R8 Nav3 / BLE Survival Check
#   9. On-Device Instrumented Tests + Cold-Launch & Rationale Intent Smoke Test
#  10. Play Console Manifest Reference Snapshot & Scoreboard
#
# Compatible with macOS (Bash 3.2+ / Zsh / Python 3.8+) and Linux.
# ==============================================================================
set -u

MOBILE_DIR="applications/ashbike/apps/mobile"
MOBILE_GRADLE=":applications:ashbike:apps:mobile"
WEAR_APP_GRADLE="applications/ashbike/apps/wear/build.gradle.kts"
APP_ID="com.zoewave.probase.ashbike"

RUN_GRADLE=1
RUN_DEVICE=0
WITH_BLE_HUB=1
INCLUDE_GLASS=0
SKIP_LIVE_NET=0

for arg in "$@"; do
  case "$arg" in
    --fast)          RUN_GRADLE=0 ;;
    --device)        RUN_DEVICE=1 ;;
    --with-ble-hub)  WITH_BLE_HUB=1 ;;
    --no-ble-hub|--v1-no-ble) WITH_BLE_HUB=0 ;;
    --include-glass) INCLUDE_GLASS=1 ;;
    --offline)       SKIP_LIVE_NET=1 ;;
    -h|--help)
      cat <<'EOF'
Usage: ./scripts/test_ashbike_e2e.sh [OPTIONS]

Options:
  (default)        Full Gradle build, unit tests across Phone + Shared + BLE +
                   core:data, lintRelease, bundleRelease (R8), AAB inspection,
                   manifest XML audit, strict BLE Hub code/UUID/DI/test checks,
                   and live Privacy Policy parity check.
  --fast           Skip Gradle build/test execution; audit existing outputs only.
  --device         Auto-resolve adb (including local.properties), run
                   connectedDebugAndroidTest, and execute a live cold-launch +
                   Health Connect rationale intent crash check on the phone.
  --with-ble-hub   Enforce strict BLE Hub verification (enabled by default).
  --no-ble-hub     Skip strict BLE Hub code/test assertions (alias: --v1-no-ble).
  --include-glass  Include :applications:ashbike:apps:mobile:features:glass
                   in unit test runs and JUnit totals (excluded by default).
  --offline        Skip live curl HTTP check against the hosted Privacy Policy.
EOF
      exit 0
      ;;
    *)
      echo "Unknown option: $arg (use --help for usage)"
      exit 2
      ;;
  esac
done

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

# Always exclude build/, .gradle/, intermediates/, and .git/ so generated files and API keys never leak
src_search() {
  local pattern="$1"; shift
  if command -v rg >/dev/null 2>&1; then
    rg -n --no-heading --glob '!**/build/**' --glob '!**/.gradle/**' --glob '!**/.git/**' --glob '!**/intermediates/**' "$pattern" "$@" 2>/dev/null || true
  else
    grep -rnE --exclude-dir=build --exclude-dir=.gradle --exclude-dir=.git --exclude-dir=intermediates "$pattern" "$@" 2>/dev/null || true
  fi
}

src_search_i() {
  local pattern="$1"; shift
  if command -v rg >/dev/null 2>&1; then
    rg -ni --no-heading --glob '!**/build/**' --glob '!**/.gradle/**' --glob '!**/.git/**' --glob '!**/intermediates/**' "$pattern" "$@" 2>/dev/null || true
  else
    grep -rniE --exclude-dir=build --exclude-dir=.gradle --exclude-dir=.git --exclude-dir=intermediates "$pattern" "$@" 2>/dev/null || true
  fi
}

# Resolve adb automatically from local.properties, ANDROID_HOME, or PATH
resolve_adb() {
  local sdk_dir=""
  if [[ -f "local.properties" ]]; then
    sdk_dir=$(grep '^sdk\.dir=' local.properties 2>/dev/null | head -n 1 | cut -d'=' -f2- | sed 's/\\:/:/g;s/\\\\/\\/g' || true)
    if [[ -n "$sdk_dir" && -x "$sdk_dir/platform-tools/adb" ]]; then
      echo "$sdk_dir/platform-tools/adb"
      return 0
    fi
  fi
  for candidate in \
    "${ANDROID_HOME:-}/platform-tools/adb" \
    "${ANDROID_SDK_ROOT:-}/platform-tools/adb" \
    "$HOME/Library/Android/sdk/platform-tools/adb" \
    "$HOME/Android/Sdk/platform-tools/adb"; do
    if [[ -n "$candidate" && -x "$candidate" ]]; then
      echo "$candidate"
      return 0
    fi
  done
  if command -v adb >/dev/null 2>&1; then
    command -v adb
    return 0
  fi
  return 1
}

if [[ ! -d "$MOBILE_DIR" ]]; then
  echo "ERROR: Run this script from the repository root (cannot find $MOBILE_DIR)."
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "ERROR: python3 is required to parse AndroidManifest.xml, JUnit XMLs, and AAB archives."
  exit 1
fi

printf "${BOLD}AshBike Phone v1 — Full End-to-End Verification Suite${NC}\n"
printf "Timestamp    : %s\n" "$(date '+%Y-%m-%d %H:%M:%S %Z')"
printf "Branch       : %s\n" "$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'unknown')"
printf "Commit       : %s\n" "$(git rev-parse --short HEAD 2>/dev/null || echo 'unknown')"
printf "Dirty Tree   : %s modified/untracked files\n" "$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')"
printf "Mode         : gradle=%s device=%s ble_hub=%s include_glass=%s\n" "$RUN_GRADLE" "$RUN_DEVICE" "$WITH_BLE_HUB" "$INCLUDE_GLASS"

# ==============================================================================
# 1. GIT SCOPE, TREE HYGIENE & SECRET LEAK GUARD
# ==============================================================================
section "1. GIT SCOPE, TREE HYGIENE & SECRET LEAK GUARD"

DIRTY_FILES=$(git status --porcelain 2>/dev/null | awk '{print $NF}' || true)
OUT_OF_SCOPE=$(echo "$DIRTY_FILES" | grep -E '^(applications/(kocolor|seaweed|photodo|goswift|gotmind)|applications/ashbike/apps/wear/|features/xr/)' || true)
if [[ -n "$OUT_OF_SCOPE" ]]; then
  warn "Uncommitted edits detected outside Phone scope (review before committing):"
  echo "$OUT_OF_SCOPE" | sed 's/^/         | /'
else
  pass "No uncommitted edits in Wear OS, XR/Glass, or sibling apps"
fi

if git ls-files --error-unmatch ashbike_audit_report.txt >/dev/null 2>&1; then
  fail "ashbike_audit_report.txt is tracked in git — untrack with: git rm --cached ashbike_audit_report.txt"
else
  pass "ashbike_audit_report.txt is not tracked in git"
fi

if grep -q "ashbike_audit_report\.txt" .gitignore 2>/dev/null; then
  pass "ashbike_audit_report.txt is listed in .gitignore"
else
  warn "ashbike_audit_report.txt not listed in .gitignore"
fi

# Check for accidentally tracked raw Google API keys in AshBike docs/source (excluding google-services.json)
LEAKED_KEY_FILES=$(git ls-files "applications/ashbike/" "docs/" "scripts/" 2>/dev/null | grep -v "google-services\.json$" | while IFS= read -r f; do
  [[ -f "$f" ]] || continue
  if grep -qE 'AIzaSy[0-9A-Za-z_-]{20,}' "$f" 2>/dev/null; then
    echo "$f"
  fi
done || true)
if [[ -n "$LEAKED_KEY_FILES" ]]; then
  fail "Tracked file(s) appear to contain a raw Google API key (keys redacted):"
  echo "$LEAKED_KEY_FILES" | sed 's/^/         | /'
else
  pass "No raw Google API keys found in tracked AshBike source, docs, or scripts"
fi

# Check asset_pack_manifest separately for AshBike vs. rest of monorepo
ASHBIKE_APM=$(git ls-files "applications/ashbike/*asset_pack_manifest*" 2>/dev/null || true)
OTHER_APM=$(git ls-files "*asset_pack_manifest*" 2>/dev/null | grep -v "^applications/ashbike/" || true)
if [[ -n "$ASHBIKE_APM" ]]; then
  fail "AshBike build intermediate tracked in git:"
  echo "$ASHBIKE_APM" | sed 's/^/         | /'
else
  pass "Zero asset_pack_manifest build intermediates tracked under applications/ashbike/"
fi
if [[ -n "$OTHER_APM" ]]; then
  warn "Tracked asset_pack_manifest exists in sibling app outside AshBike (non-blocking for AshBike):"
  echo "$OTHER_APM" | head -n 5 | sed 's/^/         | /'
fi

# Check placeholder Example*Test* files inside AshBike mobile & shared
ASHBIKE_EXAMPLES=$(git ls-files \
  "applications/ashbike/apps/mobile/*Example*Test*" \
  "applications/ashbike/model/*Example*Test*" \
  "applications/ashbike/data/*Example*Test*" \
  "applications/ashbike/database/*Example*Test*" \
  "applications/ashbike/features/*Example*Test*" 2>/dev/null || true)
if [[ -n "$ASHBIKE_EXAMPLES" ]]; then
  warn "Placeholder Example*Test* files still tracked in AshBike mobile/shared:"
  echo "$ASHBIKE_EXAMPLES" | sed 's/^/         | /'
else
  pass "Zero Example*Test* placeholder files in AshBike mobile/shared"
fi

# Firebase config hygiene
if find "$MOBILE_DIR" -name "google-services-ylabz.json" 2>/dev/null | grep -q .; then
  fail "Stray google-services-ylabz.json still present under $MOBILE_DIR"
elif [[ -f "$MOBILE_DIR/google-services.json" ]]; then
  pass "Single mobile google-services.json present"
  GS_META=$(python3 - "$MOBILE_DIR/google-services.json" <<'PY'
import sys, json
d = json.load(open(sys.argv[1], "r", encoding="utf-8"))
pid = d.get("project_info", {}).get("project_id", "")
pkgs = [c.get("client_info", {}).get("android_client_info", {}).get("package_name", "") for c in d.get("client", [])]
print("{}|{}".format(pid, ",".join(pkgs)))
PY
)
  info "google-services.json project_id & package(s): $GS_META"
else
  fail "Missing $MOBILE_DIR/google-services.json"
fi

# ==============================================================================
# 2. FULL GRADLE BUILD, UNIT TESTS, LINT & R8 BUNDLE
# ==============================================================================
section "2. GRADLE BUILD, UNIT TESTS, LINT & R8 BUNDLE"

MODULE_DIRS=(
  "applications/ashbike/apps/mobile/features/home"
  "applications/ashbike/apps/mobile/features/rides"
  "applications/ashbike/apps/mobile/features/settings"
  "applications/ashbike/apps/mobile/data"
  "applications/ashbike/features/main"
  "applications/ashbike/features/places"
  "applications/ashbike/features/ble"
  "applications/ashbike/data"
  "applications/ashbike/database"
  "applications/ashbike/model"
  "features/health/core"
  "features/ble"
)
if [[ "$INCLUDE_GLASS" -eq 1 ]]; then
  MODULE_DIRS+=("applications/ashbike/apps/mobile/features/glass")
fi
if [[ "$WITH_BLE_HUB" -eq 1 ]]; then
  MODULE_DIRS+=("core/data")
fi

CORE_TASKS=(
  "${MOBILE_GRADLE}:processReleaseMainManifest"
  "${MOBILE_GRADLE}:testDebugUnitTest"
  "${MOBILE_GRADLE}:assembleDebugAndroidTest"
  "${MOBILE_GRADLE}:lintRelease"
  "${MOBILE_GRADLE}:bundleRelease"
)

MODULE_TEST_TASKS=()
for mod_dir in "${MODULE_DIRS[@]}"; do
  if [[ -f "$mod_dir/build.gradle.kts" || -f "$mod_dir/build.gradle" ]]; then
    gradle_path=":$(echo "$mod_dir" | tr '/' ':')"
    MODULE_TEST_TASKS+=("${gradle_path}:testDebugUnitTest")
  fi
done

if [[ "$RUN_GRADLE" -eq 1 ]]; then
  if [[ ! -x "./gradlew" ]]; then
    fail "./gradlew not found or not executable in repo root"
    exit 1
  fi

  info "Clearing stale JUnit XML reports in phone/shared/BLE scope before executing tests..."
  rm -rf "$MOBILE_DIR/build/test-results" 2>/dev/null || true
  for mod_dir in "${MODULE_DIRS[@]}"; do
    rm -rf "$mod_dir/build/test-results" 2>/dev/null || true
  done

  for task in "${CORE_TASKS[@]}" "${MODULE_TEST_TASKS[@]}"; do
    printf "Running ./gradlew %s ...\n" "$task"
    TMP_LOG=$(mktemp)
    if ./gradlew "$task" >"$TMP_LOG" 2>&1; then
      pass "Gradle PASSED: $task"
    else
      if [[ "$task" == *":testDebugUnitTest" ]] && grep -q "not found in project" "$TMP_LOG"; then
        alt_task="${task%:testDebugUnitTest}:test"
        if ./gradlew "$alt_task" >"$TMP_LOG" 2>&1; then
          pass "Gradle PASSED: $alt_task"
        else
          fail "Gradle FAILED: $alt_task"
          tail -n 25 "$TMP_LOG" | sed 's/^/         | /'
        fi
      else
        fail "Gradle FAILED: $task"
        tail -n 30 "$TMP_LOG" | sed 's/^/         | /'
      fi
    fi
    rm -f "$TMP_LOG"
  done
else
  warn "Skipped Gradle execution (--fast); auditing existing build artifacts only"
fi

# ==============================================================================
# 3. JUNIT SCOREBOARD & TEST QUALITY AUDIT
# ==============================================================================
section "3. JUNIT SCOREBOARD & TEST QUALITY AUDIT"

JUNIT_REPORT=$(python3 - "$INCLUDE_GLASS" "$WITH_BLE_HUB" <<'PY'
import sys, os, glob, xml.etree.ElementTree as ET
include_glass = sys.argv[1] == "1"
with_ble_hub = sys.argv[2] == "1"

roots = [
    "applications/ashbike/apps/mobile",
    "applications/ashbike/features",
    "applications/ashbike/data",
    "applications/ashbike/database",
    "applications/ashbike/model",
    "features/health/core",
    "features/ble",
]
if with_ble_hub:
    roots.append("core/data")

seen = set()
per_mod = {}
suite_names = {}
failing_files = []
tot_suites = tot_tests = tot_fail = tot_err = tot_skip = 0

for r in roots:
    if not os.path.isdir(r):
        continue
    for f in glob.glob(os.path.join(r, "**/build/test-results/**/TEST-*.xml"), recursive=True):
        norm = os.path.normpath(f)
        if norm in seen:
            continue
        if "/apps/wear/" in norm:
            continue
        if not include_glass and "/features/glass/" in norm:
            continue
        seen.add(norm)
        try:
            xml_root = ET.parse(norm).getroot()
            if xml_root.tag != "testsuite":
                continue
            t = int(xml_root.attrib.get("tests", 0))
            fl = int(xml_root.attrib.get("failures", 0))
            er = int(xml_root.attrib.get("errors", 0))
            sk = int(xml_root.attrib.get("skipped", 0))
            sname = xml_root.attrib.get("name", os.path.basename(norm))
            tot_suites += 1
            tot_tests += t
            tot_fail += fl
            tot_err += er
            tot_skip += sk
            mod = norm.split("/build/test-results/")[0]
            s = per_mod.setdefault(mod, [0, 0, 0, 0])
            s[0] += t; s[1] += fl; s[2] += er; s[3] += sk
            suite_names[sname] = (t, fl, er, sk)
            if fl > 0 or er > 0:
                failing_files.append("{} (failures={}, errors={})".format(norm, fl, er))
        except Exception:
            pass

print("TOTALS|{}|{}|{}|{}|{}".format(tot_suites, tot_tests, tot_fail, tot_err, tot_skip))
for mod in sorted(per_mod):
    t, fl, er, sk = per_mod[mod]
    print("MOD|{}|{}|{}|{}|{}".format(mod, t, fl, er, sk))
for sname in sorted(suite_names):
    t, fl, er, sk = suite_names[sname]
    print("SUITE|{}|{}|{}|{}|{}".format(sname, t, fl, er, sk))
for ff in failing_files:
    print("FAILFILE|{}".format(ff))
PY
)

TOT_LINE=$(echo "$JUNIT_REPORT" | grep "^TOTALS|" | head -n 1 || true)
IFS='|' read -r _ SUITES TOTAL_T FAIL_T ERR_T SKIP_T <<< "$TOT_LINE"
echo "$JUNIT_REPORT" | grep "^MOD|" | while IFS='|' read -r _ mod t fl er sk; do
  info "$(printf '%-52s tests=%-3s fail=%-2s err=%-2s skip=%-2s' "$mod" "$t" "$fl" "$er" "$sk")"
done

if [[ "${TOTAL_T:-0}" -gt 0 && "${FAIL_T:-1}" -eq 0 && "${ERR_T:-1}" -eq 0 && "${SKIP_T:-1}" -eq 0 ]]; then
  pass "All $TOTAL_T executed unit tests across $SUITES suites PASSED (0 failures, 0 errors, 0 skipped)"
elif [[ "${SKIP_T:-0}" -gt 0 ]]; then
  warn "$SKIP_T unit test(s) were skipped/ignored (tests=$TOTAL_T, failures=$FAIL_T, errors=$ERR_T)"
else
  fail "Unit test issue: suites=${SUITES:-0}, tests=${TOTAL_T:-0}, failures=${FAIL_T:-0}, errors=${ERR_T:-0}"
  echo "$JUNIT_REPORT" | grep "^FAILFILE|" | sed 's/^FAILFILE|/         | /'
fi

IGNORED_TESTS=$(src_search "@Ignore" \
  "$MOBILE_DIR/src/test" \
  "$MOBILE_DIR/src/androidTest" \
  "$MOBILE_DIR/features/home/src/test" \
  "$MOBILE_DIR/features/rides/src/test" \
  "$MOBILE_DIR/features/settings/src/test" \
  "applications/ashbike/features/main/src/test" \
  "applications/ashbike/features/ble/src/test" \
  "features/ble/src/test")
if [[ -n "$IGNORED_TESTS" ]]; then
  warn "@Ignore annotation(s) found in test sources:"
  echo "$IGNORED_TESTS" | sed 's/^/         | /'
else
  pass "Zero @Ignore annotations in phone/shared/BLE unit test sources"
fi

JVM_MANIFEST_TEST=$(find "$MOBILE_DIR/src/test" -name "*Health*Manifest*Test*.kt" 2>/dev/null | head -n 1 || true)
if [[ -n "$JVM_MANIFEST_TEST" && -f "$JVM_MANIFEST_TEST" ]]; then
  if grep -qE 'if\s*\(\s*!.*exists\(\)\s*\)\s*return' "$JVM_MANIFEST_TEST" 2>/dev/null; then
    warn "JVM manifest guard test ($JVM_MANIFEST_TEST) has an early 'return' if manifest is absent — consider fail-closed assertTrue(exists())"
  else
    pass "JVM manifest guard test is fail-closed ($JVM_MANIFEST_TEST)"
  fi
fi

# ==============================================================================
# 4. DEEP MERGED RELEASE MANIFEST AUDIT
# ==============================================================================
section "4. MERGED RELEASE MANIFEST AUDIT"

MERGED_MANIFEST=$(find "$MOBILE_DIR/build/intermediates/merged_manifests/release" -name "AndroidManifest.xml" 2>/dev/null | head -n 1 || true)
if [[ -z "$MERGED_MANIFEST" ]]; then
  MERGED_MANIFEST=$(find "$MOBILE_DIR/build/intermediates/merged_manifest/release" -name "AndroidManifest.xml" 2>/dev/null | head -n 1 || true)
fi

if [[ -z "$MERGED_MANIFEST" ]]; then
  fail "Merged release AndroidManifest.xml NOT FOUND — run without --fast!"
  exit 1
fi
info "Merged release manifest: $MERGED_MANIFEST"

MANIFEST_JSON=$(python3 - "$MERGED_MANIFEST" <<'PY'
import sys, json, xml.etree.ElementTree as ET
NS = "{http://schemas.android.com/apk/res/android}"
root = ET.parse(sys.argv[1]).getroot()

uses_sdk = root.find("uses-sdk")
data = {
    "package": root.attrib.get("package", ""),
    "versionCode": root.attrib.get(NS + "versionCode", ""),
    "versionName": root.attrib.get(NS + "versionName", ""),
    "minSdk": uses_sdk.attrib.get(NS + "minSdkVersion", "") if uses_sdk is not None else "",
    "targetSdk": uses_sdk.attrib.get(NS + "targetSdkVersion", "") if uses_sdk is not None else "",
    "permissions": [],
    "features": [],
    "queries_packages": [],
    "meta_data": {},
    "services": [],
    "app_attrs": {},
    "hc_intents": []
}

for p in root.findall("uses-permission"):
    data["permissions"].append({
        "name": p.attrib.get(NS + "name", ""),
        "flags": p.attrib.get(NS + "usesPermissionFlags", "")
    })

for f in root.findall("uses-feature"):
    name = f.attrib.get(NS + "name", "")
    gles = f.attrib.get(NS + "glEsVersion", "")
    label = name if name else ("glEsVersion=" + gles if gles else "(unnamed)")
    data["features"].append({
        "name": name,
        "label": label,
        "required": f.attrib.get(NS + "required", "true (implicit)")
    })

for q in root.findall("queries"):
    for pkg in q.findall("package"):
        data["queries_packages"].append(pkg.attrib.get(NS + "name", ""))

app = root.find("application")
if app is not None:
    for k in ("allowBackup", "fullBackupContent", "dataExtractionRules", "usesCleartextTraffic", "debuggable"):
        data["app_attrs"][k] = app.attrib.get(NS + k, "")
    for m in app.findall("meta-data"):
        name = m.attrib.get(NS + "name", "")
        val = m.attrib.get(NS + "value", m.attrib.get(NS + "resource", ""))
        data["meta_data"][name] = val
    for s in app.findall("service"):
        fgs = s.attrib.get(NS + "foregroundServiceType", "")
        sname = s.attrib.get(NS + "name", "")
        if fgs or "BikeForegroundService" in sname:
            data["services"].append({
                "name": sname,
                "fgsType": fgs,
                "exported": s.attrib.get(NS + "exported", "")
            })
    for tag in ("activity", "activity-alias"):
        for act in app.findall(tag):
            perm = act.attrib.get(NS + "permission", "")
            exported = act.attrib.get(NS + "exported", "")
            for inf in act.findall("intent-filter"):
                actions = [a.attrib.get(NS + "name", "") for a in inf.findall("action")]
                cats = [c.attrib.get(NS + "name", "") for c in inf.findall("category")]
                if any("HEALTH" in x or "PERMISSIONS_RATIONALE" in x or "VIEW_PERMISSION_USAGE" in x for x in actions + cats):
                    data["hc_intents"].append({
                        "tag": tag,
                        "name": act.attrib.get(NS + "name", ""),
                        "exported": exported,
                        "permission": perm,
                        "actions": actions,
                        "categories": cats
                    })

print(json.dumps(data))
PY
)

py_get() {
  python3 -c "import sys, json; d=json.loads(sys.stdin.read()); $1" <<< "$MANIFEST_JSON"
}

M_PKG=$(py_get 'print(d["package"])')
M_VC=$(py_get 'print(d["versionCode"])')
M_VN=$(py_get 'print(d["versionName"])')
M_MIN=$(py_get 'print(d["minSdk"])')
M_TGT=$(py_get 'print(d["targetSdk"])')
M_DBG=$(py_get 'print(d["app_attrs"].get("debuggable", ""))')
M_CLEAR=$(py_get 'print(d["app_attrs"].get("usesCleartextTraffic", ""))')

info "Package=$M_PKG | versionCode=$M_VC | versionName=$M_VN | minSdk=$M_MIN | targetSdk=$M_TGT"
[[ "$M_PKG" == "$APP_ID" ]] && pass "Package name matches $APP_ID" || fail "Unexpected package name: '$M_PKG'"
[[ -n "$M_TGT" && "$M_TGT" -ge 35 ]] && pass "targetSdk=$M_TGT satisfies Google Play API 35+ requirement" || fail "targetSdk='$M_TGT' is below 35"
[[ "$M_DBG" != "true" ]] && pass "Release manifest is not debuggable" || fail "Release manifest has android:debuggable=true!"
[[ "$M_CLEAR" != "true" ]] && pass "Cleartext HTTP traffic is not enabled in release manifest" || warn "usesCleartextTraffic=true found in release manifest"

if [[ -f "$WEAR_APP_GRADLE" ]]; then
  W_VC=$(grep -E 'versionCode\s*=' "$WEAR_APP_GRADLE" | grep -oE '[0-9]+' | head -n 1 || true)
  if [[ -n "$W_VC" && "$M_VC" == "$W_VC" ]]; then
    fail "VERSION CODE COLLISION: Mobile ($M_VC) == Wear ($W_VC)"
  else
    pass "Mobile versionCode ($M_VC) does not collide with Wear (${W_VC:-none})"
  fi
fi

# Firebase telemetry checks
ADID_FLAG=$(py_get 'print(d["meta_data"].get("google_analytics_adid_collection_enabled", ""))')
CRASH_FLAG=$(py_get 'print(d["meta_data"].get("firebase_crashlytics_collection_enabled", ""))')
ANAL_FLAG=$(py_get 'print(d["meta_data"].get("firebase_analytics_collection_enabled", ""))')
DEACT_FLAG=$(py_get 'print(d["meta_data"].get("firebase_analytics_collection_deactivated", ""))')

[[ "$ADID_FLAG" == "false" ]] && pass "Analytics Advertising ID collection explicitly disabled (false)" || fail "google_analytics_adid_collection_enabled != false (got '$ADID_FLAG')"
if [[ "$CRASH_FLAG" == "false" || "$ANAL_FLAG" == "false" || "$DEACT_FLAG" == "true" ]]; then
  fail "Crashlytics or Analytics unexpectedly disabled in manifest (crash='$CRASH_FLAG', anal='$ANAL_FLAG', deact='$DEACT_FLAG')"
else
  pass "Crashlytics & Analytics enabled by default in merged manifest"
fi

FB_TOGGLES=$(src_search "setCrashlyticsCollectionEnabled|setAnalyticsCollectionEnabled" "$MOBILE_DIR")
if [[ -n "$FB_TOGGLES" ]]; then
  pass "Runtime Crashlytics & Analytics opt-out toggles present"
else
  fail "Missing runtime Crashlytics/Analytics opt-out controller in $MOBILE_DIR"
fi

# Backup & Leave-No-Trace XML verification
BACKUP_RULE=$(py_get 'print(d["app_attrs"].get("fullBackupContent", ""))')
EXTRACT_RULE=$(py_get 'print(d["app_attrs"].get("dataExtractionRules", ""))')
if [[ "$BACKUP_RULE" == "@xml/backup_rules" && "$EXTRACT_RULE" == "@xml/data_extraction_rules" ]]; then
  pass "Explicit backup & data extraction XML rules wired"
else
  fail "Backup rules mismatch (fullBackupContent='$BACKUP_RULE', dataExtractionRules='$EXTRACT_RULE')"
fi
if [[ -f "$MOBILE_DIR/src/main/res/xml/backup_rules.xml" && -f "$MOBILE_DIR/src/main/res/xml/data_extraction_rules.xml" ]]; then
  if grep -q "<exclude" "$MOBILE_DIR/src/main/res/xml/backup_rules.xml" && grep -q "<exclude" "$MOBILE_DIR/src/main/res/xml/data_extraction_rules.xml"; then
    pass "backup_rules.xml and data_extraction_rules.xml actively exclude local data (Leave No Trace)"
  else
    warn "Verify backup_rules.xml / data_extraction_rules.xml contain <exclude> directives"
  fi
fi

# Foreground service checks
info "Foreground services in merged release manifest:"
py_get '
for s in d["services"]:
    print("         | " + s["name"] + "  ->  foregroundServiceType=\"" + s["fgsType"] + "\" (exported=" + s["exported"] + ")")
'
DATASYNC_SVCS=$(py_get 'print("\n".join(s["name"] for s in d["services"] if "dataSync" in s["fgsType"]))')
if [[ -n "$DATASYNC_SVCS" ]]; then
  fail "Stray dataSync foreground service(s) in merged manifest:"
  echo "$DATASYNC_SVCS" | sed 's/^/         | /'
else
  pass "Zero stray dataSync foreground services in merged manifest"
fi

BIKE_FGS=$(py_get 'print(",".join(s["fgsType"] for s in d["services"] if "BikeForegroundService" in s["name"]))')
HAS_FGS_LOC=$(py_get 'print(any(p["name"]=="android.permission.FOREGROUND_SERVICE_LOCATION" for p in d["permissions"]))')
HAS_FGS_CONN=$(py_get 'print(any(p["name"]=="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" for p in d["permissions"]))')
if [[ "$BIKE_FGS" == "location" && "$HAS_FGS_LOC" == "True" && "$HAS_FGS_CONN" == "False" ]]; then
  pass "BikeForegroundService is strictly location-only with FOREGROUND_SERVICE_LOCATION"
else
  warn "BikeForegroundService config: type='$BIKE_FGS', FGS_LOC=$HAS_FGS_LOC, FGS_CONN=$HAS_FGS_CONN"
fi

# Health Connect exact 8 permissions & intent filters
EXPECTED_HC="android.permission.health.READ_DISTANCE
android.permission.health.READ_EXERCISE
android.permission.health.READ_HEART_RATE
android.permission.health.READ_TOTAL_CALORIES_BURNED
android.permission.health.WRITE_DISTANCE
android.permission.health.WRITE_EXERCISE
android.permission.health.WRITE_HEART_RATE
android.permission.health.WRITE_TOTAL_CALORIES_BURNED"

ACTUAL_HC=$(py_get 'print("\n".join(sorted(p["name"] for p in d["permissions"] if p["name"].startswith("android.permission.health."))))')
if [[ "$ACTUAL_HC" == "$EXPECTED_HC" ]]; then
  pass "Health Connect merged manifest matches exact 8-permission v1 set (1:1 parity)"
else
  fail "Health Connect permission mismatch! Actual merged permissions:"
  echo "$ACTUAL_HC" | sed 's/^/         | /'
fi

HC_INTENT_OK=$(py_get '
acts = [a for i in d["hc_intents"] for a in i["actions"]]
cats = [c for i in d["hc_intents"] for c in i["categories"]]
perms = [i["permission"] for i in d["hc_intents"]]
ok = ("androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" in acts and
      "android.intent.action.VIEW_PERMISSION_USAGE" in acts and
      "android.intent.category.HEALTH_PERMISSIONS" in cats and
      "android.permission.START_VIEW_PERMISSION_USAGE" in perms)
print("OK" if ok else "FAIL")
')
[[ "$HC_INTENT_OK" == "OK" ]] && pass "Both Android <=13 and Android 14+ Health Connect privacy intent filters verified" || fail "Health Connect privacy intent filter configuration incomplete"

# Hardware & Play-policy stripped permissions
HAS_CAMERA=$(py_get 'print(any(p["name"]=="android.permission.CAMERA" for p in d["permissions"]))')
CAM_FEAT=$(py_get 'print(",".join(f["required"] for f in d["features"] if f["name"]=="android.hardware.camera"))')
[[ "$HAS_CAMERA" == "True" ]] && pass "CAMERA permission present (QR code reader)" || fail "CAMERA permission missing"
[[ "$CAM_FEAT" == "false" ]] && pass "android.hardware.camera is required=false" || warn "android.hardware.camera required='$CAM_FEAT'"

ARCORE_LEAK=$(grep -niE "ar\.core|arcore|camera\.ar|InstallActivity" "$MERGED_MANIFEST" || true)
[[ -z "$ARCORE_LEAK" ]] && pass "Zero ARCore references in merged release manifest (including <queries>)" || fail "ARCore leak detected in merged manifest"

BANNED_PERMS=$(py_get '
banned = {
    "android.permission.RECORD_AUDIO",
    "android.permission.NFC",
    "android.permission.VIBRATE",
    "android.permission.BLUETOOTH_ADVERTISE",
    "android.permission.ACCESS_BACKGROUND_LOCATION",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "com.google.android.gms.permission.AD_ID",
    "com.google.android.apps.aicore.service.BIND_SERVICE"
}
print("\n".join(sorted(p["name"] for p in d["permissions"] if p["name"] in banned)))
')
if [[ -n "$BANNED_PERMS" ]]; then
  fail "Stripped/banned permissions leaked back into merged manifest:"
  echo "$BANNED_PERMS" | sed 's/^/         | /'
else
  pass "RECORD_AUDIO, NFC, VIBRATE, BLUETOOTH_ADVERTISE, BG_LOCATION, AD_ID & AICore all absent"
fi

# ==============================================================================
# 5. COMPLETE BLE HUB CODE, UUID, API-34+, DI WIRING & TEST COVERAGE AUDIT
# ==============================================================================
section "5. BLE HUB CODE, UUIDS, API-34+, DI WIRING & TEST AUDIT"

HAS_BLE_SCAN=$(py_get 'print(any(p["name"]=="android.permission.BLUETOOTH_SCAN" for p in d["permissions"]))')
HAS_BLE_CONN=$(py_get 'print(any(p["name"]=="android.permission.BLUETOOTH_CONNECT" for p in d["permissions"]))')
BLE_SCAN_FLAGS=$(py_get 'print(",".join(p["flags"] for p in d["permissions"] if p["name"]=="android.permission.BLUETOOTH_SCAN"))')
BLE_FEAT=$(py_get 'print(",".join(f["required"] for f in d["features"] if f["name"]=="android.hardware.bluetooth_le"))')

BLE_MODULE_ROOTS=("applications/ashbike/features/ble" "features/ble")
BLE_SEARCH_ROOTS=(
  "applications/ashbike/features/ble"
  "features/ble"
  "applications/ashbike/apps/mobile"
  "applications/ashbike/features/main"
  "applications/ashbike/data"
  "applications/ashbike/model"
)

if [[ "$WITH_BLE_HUB" -eq 1 ]]; then
  # 5A: Manifest BLE permissions & hardware feature
  [[ "$HAS_BLE_SCAN" == "True" ]] && pass "BLE Permission: BLUETOOTH_SCAN present (flags='${BLE_SCAN_FLAGS:-none}')" || fail "BLE Permission: BLUETOOTH_SCAN missing"
  [[ "$BLE_SCAN_FLAGS" == *"neverForLocation"* ]] && pass "BLE Permission: BLUETOOTH_SCAN declares neverForLocation" || fail "BLE Permission: BLUETOOTH_SCAN missing neverForLocation flag"
  [[ "$HAS_BLE_CONN" == "True" ]] && pass "BLE Permission: BLUETOOTH_CONNECT present" || fail "BLE Permission: BLUETOOTH_CONNECT missing"
  [[ "$BLE_FEAT" == "false" ]] && pass "BLE Feature: android.hardware.bluetooth_le declared required=false" || fail "BLE Feature: android.hardware.bluetooth_le required='$BLE_FEAT' (expected 'false')"

  # 5B: Required Source Components
  info "--- 5B. Required BLE Hub Source Files ---"
  for spec in \
    "CyclingBleUuids|CscUuids|BLE Protocol UUID Registry" \
    "HeartRateDecoder|HeartRateParser|HRS Decoder (0x180D / 0x2A37)" \
    "CyclingPowerDecoder|CyclingPowerParser|CPS Decoder (0x1818 / 0x2A63)" \
    "CscDecoder|CscMeasurementParser|CSC Decoder (0x1816 / 0x2A5B)" \
    "FtmsIndoorBikeDecoder|FtmsDecoder|Read-Only FTMS Decoder (0x1826 / 0x2AD2)" \
    "WheelCrankKinematicsCalculator|CscTelemetryCalculator|Rollover-Safe Kinematics Calculator" \
    "BatteryDecoder|BatteryLevelDecoder|BAS Battery Decoder (0x180F / 0x2A19)" \
    "OpenBikeControlCodec|OpenBikeControlParser|OpenBikeControl BLE Codec" \
    "BleSensorHub|BleHub|Concurrent Multi-Sensor BLE Hub" \
    "RideSensorFusionEngine|SensorFusion|GPS + BLE Sensor Fusion Engine" \
    "ObcRideActionMapper|OpenBikeControlMapper|OpenBikeControl Action Mapper"; do
    IFS='|' read -r p1 p2 label <<< "$spec"
    hit=$(find "${BLE_SEARCH_ROOTS[@]}" -type f \( -name "*${p1}*.kt" -o -name "*${p2}*.kt" \) ! -name "*Test*.kt" 2>/dev/null | head -n 1 || true)
    if [[ -n "$hit" ]]; then
      pass "Code present: $label -> $hit"
    else
      fail "MISSING BLE CODE: $label (expected *${p1}*.kt or *${p2}*.kt)"
    fi
  done

  # 5C: Required Standard & OpenBikeControl UUID Verification
  info "--- 5C. Protocol UUID Verification ---"
  for uuid_spec in \
    "0000180d-0000-1000-8000-00805f9b34fb|HRS Service (0x180D)" \
    "00002a37-0000-1000-8000-00805f9b34fb|HRS Measurement (0x2A37)" \
    "00001818-0000-1000-8000-00805f9b34fb|CPS Service (0x1818)" \
    "00002a63-0000-1000-8000-00805f9b34fb|CPS Measurement (0x2A63)" \
    "00001816-0000-1000-8000-00805f9b34fb|CSC Service (0x1816)" \
    "00002a5b-0000-1000-8000-00805f9b34fb|CSC Measurement (0x2A5B)" \
    "00001826-0000-1000-8000-00805f9b34fb|FTMS Service (0x1826)" \
    "00002ad2-0000-1000-8000-00805f9b34fb|FTMS Indoor Bike Data (0x2AD2)" \
    "0000180f-0000-1000-8000-00805f9b34fb|BAS Battery Service (0x180F)" \
    "00002a19-0000-1000-8000-00805f9b34fb|BAS Battery Level (0x2A19)" \
    "00002902-0000-1000-8000-00805f9b34fb|CCCD Descriptor (0x2902)" \
    "d273f680-d548-419d-b9d1-fa0472345229|OpenBikeControl Service" \
    "d273f681-d548-419d-b9d1-fa0472345229|OpenBikeControl Button State" \
    "d273f682-d548-419d-b9d1-fa0472345229|OpenBikeControl Haptic Feedback" \
    "d273f683-d548-419d-b9d1-fa0472345229|OpenBikeControl App Info"; do
    IFS='|' read -r uuid label <<< "$uuid_spec"
    short_hex=$(echo "$uuid" | cut -c5-8)
    if [[ "$uuid" == 0000*-0000-1000-8000-00805f9b34fb ]]; then
      hit=$(src_search_i "${uuid}|0x${short_hex}\b" "${BLE_MODULE_ROOTS[@]}" | grep -v "Test\.kt:" | head -n 1 || true)
    else
      hit=$(src_search_i "${uuid}" "${BLE_MODULE_ROOTS[@]}" | grep -v "Test\.kt:" | head -n 1 || true)
    fi
    if [[ -n "$hit" ]]; then
      pass "UUID wired: $label"
    else
      fail "UUID MISSING in BLE modules: $label ($uuid)"
    fi
  done

  # Verify FTMS remains strictly read-only (no 0x2AD9 Control Point writes)
  FTMS_CP=$(src_search_i "00002ad9|0x2ad9\b" "${BLE_SEARCH_ROOTS[@]}" | grep -v "Test\.kt:" || true)
  if [[ -n "$FTMS_CP" ]]; then
    fail "FTMS Control Point (0x2AD9) found in source — v1 FTMS must be read-only:"
    echo "$FTMS_CP" | head -n 5 | sed 's/^/         | /'
  else
    pass "FTMS is strictly read-only (zero 0x2AD9 Control Point writes)"
  fi

  # Verify CPS 1/2048s wheel timer constant exists specifically inside BLE modules (not random mobile constants)
  if src_search "2048" "${BLE_MODULE_ROOTS[@]}" | grep -v "Test\.kt:" | grep -q .; then
    pass "CPS 1/2048s wheel-event time resolution constant (2048) verified in BLE module source"
  else
    fail "Missing 2048 constant in BLE module source — verify CPS wheel event time uses 1/2048s (not 1/1024s)!"
  fi

  # 5D: Android 14+ (minSdk 34) Non-Deprecated BLE API Compliance & DI Wiring
  info "--- 5D. Android 14+ BLE API & DI Wiring Audit ---"
  DEP_BLE=$(src_search "descriptor\.value\s*=|characteristic\.value\s*=|override fun onCharacteristicChanged\([^,]+,[^,)]+\)|override fun onCharacteristicRead\([^,]+,[^,]+,[^,)]+\)" \
    "applications/ashbike/features/ble" "features/ble" "applications/ashbike/apps/mobile" | grep -v "Test\.kt:" || true)
  if [[ -n "$DEP_BLE" ]]; then
    fail "Deprecated pre-API-33 BluetoothGatt API(s) detected in BLE code:"
    echo "$DEP_BLE" | head -n 8 | sed 's/^/         | /'
  else
    pass "Zero deprecated pre-API-33 BluetoothGatt callbacks or .value assignments in BLE code"
  fi

  # Verify real BLE wiring in MobileSensorModule
  SENSOR_MOD=$(find applications/ashbike/apps/mobile -name "*MobileSensorModule*.kt" 2>/dev/null | head -n 1 || true)
  if [[ -n "$SENSOR_MOD" && -f "$SENSOR_MOD" ]]; then
    if src_search "BleSensorHub|RideSensorFusionEngine|BluetoothLeRepo" "$SENSOR_MOD" | grep -q .; then
      pass "DI Wiring: $SENSOR_MOD references real BLE Hub / Fusion Engine"
    else
      fail "DI Wiring: $SENSOR_MOD does NOT reference BleSensorHub / RideSensorFusionEngine (may still be stubbed!)"
    fi
  else
    warn "Could not locate MobileSensorModule.kt — verify Hilt DI binds BleSensorHub manually"
  fi

  # Verify wheel circumference setting & saved sensor persistence exist
  if src_search_i "wheelCircumference" applications/ashbike features/ble | grep -v "Test\.kt:" | grep -q .; then
    pass "Wheel circumference configuration present in source"
  else
    fail "Missing wheelCircumference setting/persistence in AshBike/BLE source"
  fi

  # Verify DemoModeSimulator is isolated (not hardcoded enabled=true by default)
  DEMO_DEFAULT_ON=$(src_search "demoModeEnabled.*=\s*true|simulate\s*=\s*true" \
    applications/ashbike/apps/mobile applications/ashbike/features/main applications/ashbike/features/ble | grep -v "Test\.kt:" || true)
  if [[ -n "$DEMO_DEFAULT_ON" ]]; then
    fail "Demo/simulator flag appears hardcoded ON in live code:"
    echo "$DEMO_DEFAULT_ON" | head -n 5 | sed 's/^/         | /'
  else
    pass "DemoModeSimulator is not hardcoded ON on the live phone path"
  fi

  # Verify stubbed BikeRepositoryImpl shift/suspension TODOs were cleaned up or replaced
  STUB_TODOS=$(src_search_i "TODO.*bluetooth|TODO.*gatt" \
    applications/ashbike/features/ble applications/ashbike/apps/mobile applications/ashbike/data applications/ashbike/features/main || true)
  if [[ -n "$STUB_TODOS" ]]; then
    warn "Remaining Bluetooth/GATT TODO comment(s) in AshBike source:"
    echo "$STUB_TODOS" | head -n 8 | sed 's/^/         | /'
  else
    pass "Zero leftover Bluetooth/GATT TODO stubs in AshBike BLE/mobile/data/main"
  fi

  # 5E: Mandatory BLE Unit Tests Present AND Actually Executed in JUnit XML
  info "--- 5E. BLE Unit Test Existence & Execution Audit ---"
  ASHBIKE_BLE_COUNT=$(echo "$JUNIT_REPORT" | grep -F "MOD|applications/ashbike/features/ble|" | awk -F'|' '{print $3}' | head -n 1 || true)
  SHARED_BLE_COUNT=$(echo "$JUNIT_REPORT" | grep -F "MOD|features/ble|" | awk -F'|' '{print $3}' | head -n 1 || true)
  TOTAL_BLE_TESTS=$(( ${ASHBIKE_BLE_COUNT:-0} + ${SHARED_BLE_COUNT:-0} ))
  if [[ "$TOTAL_BLE_TESTS" -gt 0 ]]; then
    pass "BLE modules executed $TOTAL_BLE_TESTS unit test(s) (ashbike/features/ble=${ASHBIKE_BLE_COUNT:-0}, features/ble=${SHARED_BLE_COUNT:-0})"
  else
    fail "BLE modules executed 0 unit tests (JUnit XML missing or empty)"
  fi

  for test_spec in \
    "HeartRateDecoderTest|HRS Decoder Unit Tests" \
    "CyclingPowerDecoderTest|CPS Decoder Unit Tests" \
    "CscDecoderTest|CscMeasurementParserTest|CSC Decoder Unit Tests" \
    "FtmsIndoorBikeDecoderTest|FTMS Read-Only Decoder Unit Tests" \
    "WheelCrankKinematicsCalculatorTest|CscTelemetryCalculatorTest|Kinematics Rollover/Coasting Tests" \
    "OpenBikeControlCodecTest|OpenBikeControl Codec Tests" \
    "BleSensorHubTest|Multi-Sensor BLE Hub Session Tests" \
    "RideSensorFusionEngineTest|Sensor Fusion Priority Tests" \
    "ObcRideActionMapperTest|OpenBikeControl Action Mapper Tests"; do
    IFS='|' read -r f1 f2 f3 <<< "$test_spec"
    if [[ -n "$f3" ]]; then
      pat="$f1|$f2"; label="$f3"
    else
      pat="$f1"; label="$f2"
    fi
    suite_hit=$(echo "$JUNIT_REPORT" | grep -E "^SUITE\|.*($pat)\|" | head -n 1 || true)
    if [[ -n "$suite_hit" ]]; then
      IFS='|' read -r _ sname st sf se ss <<< "$suite_hit"
      if [[ "$st" -gt 0 && "$sf" -eq 0 && "$se" -eq 0 && "$ss" -eq 0 ]]; then
        pass "Executed BLE Test Suite: $label ($st test(s) passed)"
      else
        fail "BLE Test Suite issue: $label (tests=$st, fail=$sf, err=$se, skip=$ss)"
      fi
    else
      file_hit=$(find features/ble applications/ashbike core/data -type f -name "*.kt" 2>/dev/null | grep -E "(${pat})\.kt$" | head -n 1 || true)
      if [[ -n "$file_hit" ]]; then
        fail "BLE Test file exists ($file_hit) but did NOT execute in JUnit results!"
      else
        fail "MISSING BLE TEST SUITE: $label (expected ${pat}.kt)"
      fi
    fi
  done

  # 5F: Hardware test protocol doc
  if [[ -f "applications/ashbike/docs/release/BLE_HUB_HARDWARE_TEST.md" ]]; then
    pass "Hardware test protocol doc exists: applications/ashbike/docs/release/BLE_HUB_HARDWARE_TEST.md"
  else
    warn "Missing applications/ashbike/docs/release/BLE_HUB_HARDWARE_TEST.md"
  fi
else
  info "No-BLE mode (--no-ble-hub): skipping strict BLE Hub code assertions."
  info "BLE permissions in manifest: BLUETOOTH_SCAN=$HAS_BLE_SCAN (flags='$BLE_SCAN_FLAGS'), BLUETOOTH_CONNECT=$HAS_BLE_CONN, ble_feature=$BLE_FEAT"
  if [[ "$HAS_BLE_SCAN" == "True" && "$HAS_BLE_CONN" == "True" ]]; then
    pass "BLE scanner permissions configured cleanly"
  elif [[ "$HAS_BLE_SCAN" == "False" && "$HAS_BLE_CONN" == "False" ]]; then
    pass "Unused BLE permissions cleanly stripped"
  else
    warn "Partial BLE permission pair (SCAN=$HAS_BLE_SCAN, CONNECT=$HAS_BLE_CONN)"
  fi
fi

# ==============================================================================
# 6. PRIVACY POLICY & LIVE MAIN-BRANCH PARITY CHECK
# ==============================================================================
section "6. PRIVACY POLICY & LIVE URL PARITY"

ASHBIKE_POLICY="applications/ashbike/docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md"
if [[ ! -f "$ASHBIKE_POLICY" ]]; then
  ASHBIKE_POLICY=$(find . -iname "*PRIVACY_POLICY*.md" -not -path "*/build/*" -not -path "*/.git/*" 2>/dev/null | grep -i "ashbike" | head -n 1 || true)
fi

if [[ -n "$ASHBIKE_POLICY" && -f "$ASHBIKE_POLICY" ]]; then
  pass "Found privacy policy doc: $ASHBIKE_POLICY"
  BAD_CLAIMS=$(src_search_i "no external crash reporting|encrypted room|March 2026" "$ASHBIKE_POLICY")
  if [[ -n "$BAD_CLAIMS" ]]; then
    fail "Contradictory/stale claims found in $ASHBIKE_POLICY:"
    echo "$BAD_CLAIMS" | sed 's/^/         | /'
  else
    pass "Zero stale 'no external crash reporting' / 'encrypted Room' / 'March 2026' claims"
  fi
  if src_search_i "Crashlytics" "$ASHBIKE_POLICY" | grep -q . && src_search_i "Analytics" "$ASHBIKE_POLICY" | grep -q .; then
    pass "Privacy policy discloses both Firebase Crashlytics and Firebase Analytics"
  else
    fail "Privacy policy missing Crashlytics or Analytics disclosure"
  fi
else
  fail "PRIVACY_POLICY_PUBLIC.md not found"
fi

MAIN_ACT="$MOBILE_DIR/src/main/java/com/zoewave/probase/ashbike/mobile/MainActivity.kt"
if [[ -f "$MAIN_ACT" ]]; then
  if src_search_i "ylabz\.com|actual privacy policy" "$MAIN_ACT" | grep -q .; then
    fail "MainActivity.kt still contains ylabz.com or placeholder TODO"
  else
    pass "MainActivity.kt has no ylabz.com or placeholder privacy URL TODO"
  fi
fi

PRIV_URL=$(src_search "https://[^\"'[:space:])]+" "$MOBILE_DIR/src/main" | grep -i "privacy" | grep -oE 'https://[^"'\''[:space:])]+' | head -n 1 || true)
if [[ -n "$PRIV_URL" ]]; then
  info "Wired Privacy Policy URL: $PRIV_URL"
  if [[ "$SKIP_LIVE_NET" -eq 0 ]] && command -v curl >/dev/null 2>&1; then
    HTTP_CODE=$(curl -sIL -o /dev/null -w "%{http_code}" --max-time 10 "$PRIV_URL" 2>/dev/null || echo "000")
    if [[ "$HTTP_CODE" == "200" ]]; then
      pass "Live Privacy Policy URL returned HTTP 200"
      if [[ "$PRIV_URL" == https://github.com/*/blob/* && -n "$ASHBIKE_POLICY" && -f "$ASHBIKE_POLICY" ]]; then
        RAW_URL=$(echo "$PRIV_URL" | sed 's#https://github.com/#https://raw.githubusercontent.com/#; s#/blob/#/#')
        TMP_LIVE=$(mktemp)
        if curl -sL --max-time 10 "$RAW_URL" -o "$TMP_LIVE" 2>/dev/null; then
          if cmp -s "$ASHBIKE_POLICY" "$TMP_LIVE"; then
            pass "Live hosted policy on GitHub matches local $ASHBIKE_POLICY byte-for-byte!"
          else
            warn "LIVE POLICY DRIFT: Hosted URL ($RAW_URL) differs from local $ASHBIKE_POLICY — merge/push updated policy to main before Play submission!"
          fi
        fi
        rm -f "$TMP_LIVE"
      fi
    else
      fail "Live Privacy Policy URL returned HTTP $HTTP_CODE (must be HTTP 200 before submission)"
    fi
  else
    info "Skipped live HTTP check (--offline or curl unavailable)"
  fi
else
  warn "Could not auto-extract literal HTTPS privacy policy URL from $MOBILE_DIR/src/main"
fi

# ==============================================================================
# 7. LINT BASELINE & RELEASE LINT XML AUDIT
# ==============================================================================
section "7. LINT BASELINE & RELEASE LINT REPORT"

[[ ! -f "$MOBILE_DIR/lint-baseline.xml" ]] && pass "lint-baseline.xml is deleted" || fail "lint-baseline.xml STILL EXISTS"
if src_search "lint-baseline|abortOnError\s*=\s*false" "$MOBILE_DIR/build.gradle.kts" | grep -q .; then
  fail "build.gradle.kts still references lint-baseline or abortOnError=false"
else
  pass "build.gradle.kts has no lint-baseline or abortOnError=false"
fi

LINT_XML="$MOBILE_DIR/build/reports/lint-results-release.xml"
if [[ -f "$LINT_XML" ]]; then
  LINT_STATS=$(python3 - "$LINT_XML" <<'PY'
import sys, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
fatals = errors = warns = 0
for i in root.findall("issue"):
    sev = i.attrib.get("severity", "")
    if sev == "Fatal": fatals += 1
    elif sev == "Error": errors += 1
    elif sev == "Warning": warns += 1
print("{}|{}|{}".format(fatals, errors, warns))
PY
)
  IFS='|' read -r L_FATAL L_ERR L_WARN <<< "$LINT_STATS"
  if [[ "$L_FATAL" -eq 0 && "$L_ERR" -eq 0 ]]; then
    pass "lint-results-release.xml verified: 0 Fatal, 0 Error ($L_WARN warnings)"
  else
    fail "lint-results-release.xml has Fatal=$L_FATAL, Error=$L_ERR"
  fi
fi

# ==============================================================================
# 8. RELEASE AAB BINARY INSPECTION & R8 NAV3 / BLE SURVIVAL
# ==============================================================================
section "8. RELEASE AAB BINARY INSPECTION & R8 SURVIVAL"

AAB_FILE=$(find "$MOBILE_DIR/build/outputs/bundle/release" -name "*.aab" 2>/dev/null | head -n 1 || true)
MAPPING_FILE="$MOBILE_DIR/build/outputs/mapping/release/mapping.txt"
SEEDS_FILE="$MOBILE_DIR/build/outputs/mapping/release/seeds.txt"

if [[ -n "$AAB_FILE" && -f "$AAB_FILE" ]]; then
  pass "Release AAB present: $AAB_FILE ($(du -h "$AAB_FILE" | awk '{print $1}'))"
  AAB_CHECK=$(python3 - "$AAB_FILE" <<'PY'
import sys, zipfile
with zipfile.ZipFile(sys.argv[1], "r") as z:
    names = z.namelist()
    has_manifest = "base/manifest/AndroidManifest.xml" in names
    dex_files = [n for n in names if n.startswith("base/dex/") and n.endswith(".dex")]
    has_map = any("proguard.map" in n for n in names)
    so_files = [n for n in names if n.endswith(".so")]
    sym_files = [n for n in names if "debugsymbols" in n.lower()]
    print("{}|{}|{}|{}|{}".format(has_manifest, len(dex_files), has_map, len(so_files), len(sym_files)))
PY
)
  IFS='|' read -r A_MAN A_DEX A_MAP A_SO A_SYM <<< "$AAB_CHECK"
  info "AAB archive inspection: manifest=$A_MAN, dex_count=$A_DEX, proguard_map_bundled=$A_MAP, native_so=$A_SO, debug_symbols=$A_SYM"
  [[ "$A_MAN" == "True" && "$A_DEX" -ge 1 ]] && pass "AAB archive is structurally valid ($A_DEX dex file(s))" || fail "AAB archive missing manifest or DEX!"
  [[ "$A_MAP" == "True" ]] && pass "R8 ProGuard mapping is bundled inside AAB metadata" || warn "ProGuard mapping not found in AAB BUNDLE-METADATA"
  if [[ "$A_SO" -gt 0 && "$A_SYM" -eq 0 ]]; then
    warn "AAB contains $A_SO native .so files but 0 bundled debug symbol files — verify ndk.debugSymbolLevel"
  elif [[ "$A_SO" -gt 0 ]]; then
    pass "Native debug symbols bundled inside AAB ($A_SYM entries for $A_SO .so libs)"
  fi
else
  fail "Release AAB not found under $MOBILE_DIR/build/outputs/bundle/release/"
fi

if [[ -f "$MAPPING_FILE" ]]; then
  pass "R8 mapping.txt generated ($(wc -l < "$MAPPING_FILE" | tr -d ' ') lines)"
else
  fail "R8 mapping.txt missing"
fi

NAV_HITS=$(grep -E "AshBikeDestination|ashbike.*Destination|ashbike.*Route|ashbike.*NavKey" "$SEEDS_FILE" "$MAPPING_FILE" 2>/dev/null | head -n 10 || true)
if [[ -n "$NAV_HITS" ]]; then
  pass "AshBike Nav3 destination symbols verified present in R8 outputs:"
  echo "$NAV_HITS" | sed 's/^/         | /'
else
  warn "Could not find 'AshBikeDestination' in R8 seeds.txt/mapping.txt — inspect ProGuard keep rules manually"
fi

if [[ "$WITH_BLE_HUB" -eq 1 && -f "$MAPPING_FILE" ]]; then
  BLE_R8_HITS=$(grep -E "BleSensorHub|RideSensorFusionEngine|FtmsIndoorBikeDecoder|OpenBikeControlCodec|CyclingPowerDecoder|CscDecoder" "$MAPPING_FILE" 2>/dev/null | head -n 8 || true)
  if [[ -n "$BLE_R8_HITS" ]]; then
    pass "BLE Hub & Protocol Decoder classes compiled into release AAB (verified in R8 mapping.txt)"
  else
    fail "BLE Hub / Decoder classes NOT found in release R8 mapping.txt — code may not be wired into the mobile APK!"
  fi
fi

if src_search "debugSymbolLevel" "$MOBILE_DIR/build.gradle.kts" | grep -qE "FULL|SYMBOL_TABLE"; then
  pass "debugSymbolLevel configured in $MOBILE_DIR/build.gradle.kts"
else
  fail "debugSymbolLevel missing in $MOBILE_DIR/build.gradle.kts"
fi

if [[ -f "applications/ashbike/docs/release/RELEASE_VERSIONING.md" ]]; then
  pass "RELEASE_VERSIONING.md present"
else
  warn "applications/ashbike/docs/release/RELEASE_VERSIONING.md missing"
fi

# ==============================================================================
# 9. ON-DEVICE INSTRUMENTED TESTS & LIVE SMOKE TEST (--device)
# ==============================================================================
if [[ "$RUN_DEVICE" -eq 1 ]]; then
  section "9. ON-DEVICE INSTRUMENTED & LIVE SMOKE TESTS (--device)"
  ADB_BIN=$(resolve_adb || true)
  if [[ -z "$ADB_BIN" ]]; then
    fail "--device requested, but could not locate adb in local.properties, ANDROID_HOME, or PATH"
  else
    info "Resolved adb: $ADB_BIN"
    DEVICES=$("$ADB_BIN" devices 2>/dev/null | awk 'NR>1 && $2=="device" {print $1}' || true)
    if [[ -z "$DEVICES" ]]; then
      fail "No authorized adb device connected (run '$ADB_BIN devices' to check unauthorized/offline status)"
    else
      FIRST_DEV=$(echo "$DEVICES" | head -n 1)
      DEV_MODEL=$("$ADB_BIN" -s "$FIRST_DEV" shell getprop ro.product.model 2>/dev/null | tr -d '\r' || echo "unknown")
      DEV_API=$("$ADB_BIN" -s "$FIRST_DEV" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r' || echo "?")
      info "Testing on device: $FIRST_DEV ($DEV_MODEL, API $DEV_API)"

      TMP_LOG=$(mktemp)
      if ./gradlew "${MOBILE_GRADLE}:connectedDebugAndroidTest" >"$TMP_LOG" 2>&1; then
        pass "connectedDebugAndroidTest PASSED on $DEV_MODEL ($FIRST_DEV)"
      else
        fail "connectedDebugAndroidTest FAILED on $DEV_MODEL"
        tail -n 30 "$TMP_LOG" | sed 's/^/         | /'
      fi
      rm -f "$TMP_LOG"

      if "$ADB_BIN" -s "$FIRST_DEV" shell pm path "$APP_ID" >/dev/null 2>&1; then
        "$ADB_BIN" -s "$FIRST_DEV" shell am force-stop "$APP_ID" 2>/dev/null || true
        "$ADB_BIN" -s "$FIRST_DEV" logcat -c 2>/dev/null || true
        "$ADB_BIN" -s "$FIRST_DEV" shell monkey -p "$APP_ID" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
        sleep 4
        PID=$("$ADB_BIN" -s "$FIRST_DEV" shell pidof "$APP_ID" 2>/dev/null | tr -d '\r' || true)
        CRASH_LOG=$("$ADB_BIN" -s "$FIRST_DEV" logcat -d -b crash 2>/dev/null | grep -A 8 "$APP_ID" || true)
        if [[ -n "$PID" && -z "$CRASH_LOG" ]]; then
          pass "Cold-launch smoke test PASSED: $APP_ID running (PID $PID) with 0 crash-buffer entries"
        else
          fail "Cold-launch smoke test FAILED (PID='${PID:-dead}')"
          [[ -n "$CRASH_LOG" ]] && echo "$CRASH_LOG" | head -n 15 | sed 's/^/         | /'
        fi

        HC_RES=$("$ADB_BIN" -s "$FIRST_DEV" shell cmd package resolve-activity --brief -a androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE "$APP_ID" 2>/dev/null | tr -d '\r' || true)
        if echo "$HC_RES" | grep -q "$APP_ID"; then
          pass "On-device PackageManager resolves ACTION_SHOW_PERMISSIONS_RATIONALE -> $(echo "$HC_RES" | tail -n 1)"
        else
          fail "On-device PackageManager did NOT resolve ACTION_SHOW_PERMISSIONS_RATIONALE for $APP_ID"
        fi
      else
        warn "$APP_ID is not installed on $FIRST_DEV; skipping live launch smoke test"
      fi
    fi
  fi
else
  manual "Run with --device while your phone is connected to execute connectedDebugAndroidTest + live cold-launch & Health Connect intent smoke tests"
fi

# ==============================================================================
# 10. PLAY CONSOLE REFERENCE SNAPSHOT
# ==============================================================================
section "10. PLAY CONSOLE MANIFEST REFERENCE SNAPSHOT"

info "All <uses-permission> ($(py_get 'print(len(d["permissions"]))')):"
py_get '
for p in sorted(d["permissions"], key=lambda x: x["name"]):
    flg = "  [flags=" + p["flags"] + "]" if p["flags"] else ""
    print("         | " + p["name"] + flg)
'

info "All <uses-feature> ($(py_get 'print(len(d["features"]))')):"
py_get '
for f in sorted(d["features"], key=lambda x: x["label"]):
    print("         | " + f["label"] + "  (required=" + f["required"] + ")")
'

# ==============================================================================
# 11. HUMAN-ONLY PRE-SUBMISSION SIGN-OFFS & SCOREBOARD
# ==============================================================================
section "11. REMAINING HUMAN-ONLY SIGN-OFFS"
manual "Play Console: confirm Health Connect declaration APPROVED for exact 8 permissions above"
manual "Play Console: confirm Data Safety form matches default-on Crashlytics/Analytics (no Advertising ID)"
manual "Google Cloud Console (android-zoewave): verify Android API key SHA-1 restrictions (Debug + Upload + Play Signing)"
if [[ "$WITH_BLE_HUB" -eq 1 ]]; then
  manual "BLE Hardware Desk/Field Test: verify HRS + CPS + CSC + FTMS + OpenBikeControl live pairing, multi-sensor fusion, coasting zero-out, haptic buzz, and Health Connect HR sync"
fi
manual "Signed shrunk Release AAB physical test: Start -> Pause -> Resume -> Lock Screen -> Stop -> Ride Detail -> Health Connect Sync + QR Scanner + Settings Telemetry Toggle Persistence + Forced Crashlytics Test"

section "FINAL END-TO-END SCOREBOARD"
printf "${GREEN}PASS${NC}   : %d\n" "$PASS"
printf "${RED}FAIL${NC}   : %d\n" "$FAIL"
printf "${YELLOW}WARN${NC}   : %d\n" "$WARN"
printf "${BOLD}MANUAL${NC} : %d\n" "$MANUAL"

if [[ "$FAIL" -gt 0 ]]; then
  exit 1
fi
exit 0
