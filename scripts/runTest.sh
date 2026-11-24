mv ~/Downloads/test_ashbike_e2e.sh scripts/test_ashbike_e2e.sh
chmod +x scripts/test_ashbike_e2e.sh

# Full build + unit tests + strict BLE Hub code/UUID/DI/R8 verification
./scripts/test_ashbike_e2e.sh

# Include on-device instrumented tests + live cold-launch smoke test
./scripts/test_ashbike_e2e.sh --device

# Quick static/artifact re-check without re-running Gradle
./scripts/test_ashbike_e2e.sh --fast

# If you ever need to run it against Phone v1 before BLE code lands
./scripts/test_ashbike_e2e.sh --v1-no-ble