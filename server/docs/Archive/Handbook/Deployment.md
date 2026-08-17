# Deployment Architecture: Hugging Face Spaces

This documentation outlines the infrastructure that makes Hugging Face the perfect host for the KoColor Rust backend (`kocolor`).

When using a "Docker Space" on Hugging Face, the code is deployed as a containerized service.

## The Deployment Pipeline

### 1. The Trigger (Git Push)
Hugging Face Spaces are backed by Git repositories. When code is pushed to the repository (including `main.rs`, `Cargo.toml`, `assets/`, and `Dockerfile`), it triggers an automated build pipeline.

### 2. The Build Phase
Hugging Face allocates a build machine and executes the `Dockerfile`:
- Downloads the Rust toolchain.
- Runs `cargo build --release` to compile the Axum server.
- Packages the binary and `assets/` into a lightweight Debian Linux image.

### 3. The Run Phase
Once built, the image is deployed to the production cluster:
- **Port Routing**: Hugging Face acts as a reverse proxy, routing HTTPS traffic to the container on **Port 7860**.
- **Permissions**: The container runs as user `1000` for a secure, sandboxed environment.
- **Execution**: The Axum server binds to `0.0.0.0:7860` and serves the `GET /api/v1/starter-pack` endpoint.

## The Free Tier "Sleep" Cycle
On the free tier, the Space will "pause" after approximately 48 hours of inactivity. The first request after a pause will trigger a wake-up cycle. Because the server is written in Rust, boot time is minimal, though the initial request may experience a few seconds of latency.

---
**Status**: Documentation Finalized
**Usage**: Reference for backend hosting and CI/CD.
