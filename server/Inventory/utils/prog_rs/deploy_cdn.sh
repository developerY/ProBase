#!/usr/bin/env bash
set -euo pipefail

# =====================================================================
# KoColor CDN Atomic Deployment Script (Cloudflare Pages - 100% Free)
#   - Unlimited bandwidth & requests (No R2 billing)
#   - Built-in atomic deployment & automatic edge cache invalidation
# =====================================================================

DIST_DIR="${1:-"../../package/dist"}"
PAGES_PROJECT="${CLOUDFLARE_PAGES_PROJECT:-"kocolor-cdn"}"
PATH_PREFIX="${CLOUDFLARE_PATH_PREFIX:-"v1"}"

# Set default Cloudflare Account ID (Biologica@gmail.com's Account) if not exported
export CLOUDFLARE_ACCOUNT_ID="${CLOUDFLARE_ACCOUNT_ID:-"8e54bb926df3927e951a97c58ae19a04"}"

echo "============================================================"
echo "KoColor Free-Forever CDN Deployment (Cloudflare Pages)"
echo "Dist Directory : ${DIST_DIR}"
echo "Pages Project  : ${PAGES_PROJECT}"
echo "URL Prefix     : /${PATH_PREFIX}/"
echo "Account ID     : ${CLOUDFLARE_ACCOUNT_ID}"
echo "============================================================"

if [ ! -f "${DIST_DIR}/manifest.json" ]; then
  echo "ERROR: manifest.json not found in '${DIST_DIR}'! Run the Rust pipeline first."
  exit 1
fi

if ! compgen -G "${DIST_DIR}/*.kpkg" > /dev/null; then
  echo "ERROR: No .kpkg files found in '${DIST_DIR}'!"
  exit 1
fi

# 1. Prepare clean staging directory matching /v1/* URL structure
STAGE_DIR="$(mktemp -d)"
trap 'rm -rf "${STAGE_DIR}"' EXIT
mkdir -p "${STAGE_DIR}/${PATH_PREFIX}"

cp "${DIST_DIR}"/*.kpkg "${STAGE_DIR}/${PATH_PREFIX}/"
cp "${DIST_DIR}/manifest.json" "${STAGE_DIR}/${PATH_PREFIX}/manifest.json"

# 2. Configure Cloudflare Pages headers (ETag revalidation prevents SHA-256 cache mismatches)
cat <<EOF > "${STAGE_DIR}/_headers"
/${PATH_PREFIX}/manifest.json
  Content-Type: application/json
  Cache-Control: public, max-age=0, must-revalidate

/${PATH_PREFIX}/*.kpkg
  Content-Type: application/octet-stream
  Cache-Control: public, max-age=0, must-revalidate
EOF

# 3. Deploy atomically to Cloudflare Pages production
echo ""
echo "[Deploying] Uploading bundle snapshot to Cloudflare Pages..."
npx -y wrangler pages deploy "${STAGE_DIR}" \
  --project-name "${PAGES_PROJECT}" \
  --branch main \
  --commit-dirty=true

echo ""
echo "============================================================"
echo "Deployment Complete! Live at:"
echo "https://${PAGES_PROJECT}.pages.dev/${PATH_PREFIX}/manifest.json"
echo "============================================================"
