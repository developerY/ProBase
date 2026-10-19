#!/usr/bin/env bash
set -euo pipefail

# Delegate to deploy_cdn.sh
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/deploy_cdn.sh" "$@"
