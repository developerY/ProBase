#!/usr/bin/env bash
set -euo pipefail

# Delegate to prog_rs deploy script
cd "$(dirname "$0")/../utils/prog_rs"
./deploy_cdn.sh "../../package/dist"
