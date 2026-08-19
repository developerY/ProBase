#!/usr/bin/env bash

# Exit immediately if a command fails or an unset variable is used
set -euo pipefail

# ANSI color codes for readable output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

WORKER_NAME="super-star-7240"
CUSTOM_DOMAIN="cdn.kocolor.com"

echo -e "${CYAN}==============================================${NC}"
echo -e "${CYAN}     🚀 KoColor CDN Deployment Engine         ${NC}"
echo -e "${CYAN}==============================================${NC}"

# 1. Ensure we locate the directory relative to this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 2. Automatically locate the staging folder
if [ -d "$SCRIPT_DIR/package/dist/staging" ]; then
    ASSETS_DIR="$SCRIPT_DIR/package/dist/staging"
elif [ -d "$SCRIPT_DIR/staging" ]; then
    ASSETS_DIR="$SCRIPT_DIR/staging"
elif [ -d "$SCRIPT_DIR/dist/staging" ]; then
    ASSETS_DIR="$SCRIPT_DIR/dist/staging"
else
    echo -e "${RED}❌ Error: Could not find the 'staging' folder!${NC}"
    echo "Checked locations:"
    echo "  - $SCRIPT_DIR/package/dist/staging"
    echo "  - $SCRIPT_DIR/staging"
    echo "  - $SCRIPT_DIR/dist/staging"
    exit 1
fi

# 3. Check for Node / npx installation
if ! command -v npx &> /dev/null; then
    echo -e "${RED}❌ Error: 'npx' is not installed.${NC}"
    echo -e "${YELLOW}Please install Node.js (LTS) on your Mac:${NC}"
    echo "  - Download installer: https://nodejs.org"
    echo "  - Or with Homebrew:   brew install node"
    exit 1
fi

# 4. Count files and check for unintended files
FILE_COUNT=$(find "$ASSETS_DIR" -type f | wc -l | tr -d ' ')

echo -e "Worker Name : ${GREEN}${WORKER_NAME}${NC}"
echo -e "Domain      : ${GREEN}${CUSTOM_DOMAIN}${NC}"
echo -e "Assets Path : ${GREEN}${ASSETS_DIR}${NC}"
echo -e "Total Files : ${GREEN}${FILE_COUNT} files detected${NC}"
echo ""

# Check for large .zip files in the staging folder
ZIP_FILES=$(find "$ASSETS_DIR" -maxdepth 2 -name "*.zip" || true)
if [ -n "$ZIP_FILES" ]; then
    echo -e "${YELLOW}⚠️  Note: .zip archive detected in staging folder:${NC}"
    echo "$ZIP_FILES"
    echo -e "${YELLOW}   (This will be uploaded to the CDN as a downloadable asset).${NC}"
    echo ""
fi

# 5. Execute Cloudflare Deployment
echo -e "${CYAN}Uploading and deploying assets to Cloudflare...${NC}"
npx wrangler deploy --name "$WORKER_NAME" --assets "$ASSETS_DIR"

echo ""
echo -e "${GREEN}==============================================${NC}"
echo -e "${GREEN}  ✅ Deployment Completed Successfully!       ${NC}"
echo -e "${GREEN}==============================================${NC}"
echo -e "Verify live URLs:"
echo -e "  - https://${CUSTOM_DOMAIN}/info.txt"
echo -e "  - https://${CUSTOM_DOMAIN}/inventory/dist/manifest.json"
echo ""