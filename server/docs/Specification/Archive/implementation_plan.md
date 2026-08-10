# Rust Server Proxy Implementation

Implement a Rust-based serverless proxy using Axum to handle Gemini API requests and App Check verification. The server will be located in the `server/` directory at the project root, isolated from the Android Gradle build.

## User Review Required

> [!IMPORTANT]
> This plan sets up the boilerplate for a Rust server. You will need to provide your Gemini API key and configure Google Cloud Run for final deployment.

## Proposed Changes

### [Server Component]

#### [NEW] [Cargo.toml](file:///Users/developer/AndroidStudioProjects/ProBase/server/Cargo.toml)
Defines Rust dependencies: `axum`, `tokio`, `serde`, `reqwest`, `jsonwebtoken`.

#### [NEW] [main.rs](file:///Users/developer/AndroidStudioProjects/ProBase/server/src/main.rs)
Boilerplate Axum server with a health check and a placeholder for Gemini extraction.

#### [NEW] [Dockerfile](file:///Users/developer/AndroidStudioProjects/ProBase/server/Dockerfile)
Multi-stage Dockerfile for building and running the Rust application in a container.

### [Project Configuration]

#### [MODIFY] [.gitignore](file:///Users/developer/AndroidStudioProjects/ProBase/.gitignore)
Add `/server/target/` to ignore Rust build artifacts.

## Verification Plan

### Automated Tests
- None at this stage (boilerplate setup).

### Manual Verification
- Verify the files are created in the correct locations.
- The user can later run `cargo build` in the `server/` directory to verify compilation (requires Rust toolchain).
