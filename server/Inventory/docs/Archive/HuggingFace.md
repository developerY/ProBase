Here is the complete Markdown guide you can save in your repository. It covers the specific Hugging Face Docker constraints, port mapping, and the exact terminal commands to run straight from the Zed IDE without breaking your monorepo structure.

```markdown
# 🚀 Deploying `kocolor` from Zed to Hugging Face Spaces

Hugging Face Spaces act as independent Git repositories. Because your Axum server is nested inside the `ProBase` monorepo (`server/kocolor/`), we need to configure the server to meet Hugging Face's Docker constraints and use a targeted deployment command.

## 1. Network Configuration (`main.rs`)
Hugging Face Docker Spaces strictly route external web traffic internally to port `7860`. 

Open `server/kocolor/src/main.rs` in Zed and ensure your Axum server binds to this exact port on the `0.0.0.0` host:

```rust
// The server must bind to 0.0.0.0 to accept traffic from outside the Docker container
let listener = tokio::net::TcpListener::bind("0.0.0.0:7860").await.unwrap();
println!("🚀 Server running on port 7860");
axum::serve(listener, app).await.unwrap();

```

## 2. The Deployment Configuration Files

Hugging Face requires two configuration files at the root of the uploaded code: a `Dockerfile` for the build process, and a `README.md` containing YAML metadata to configure the Space.

Create both of these files directly inside your `server/kocolor/` directory.

### A. The Metadata File (`README.md`)

Create this file to tell the Hugging Face engine that this is a Docker container. (Keep the `---` dashes, they are required for the YAML frontmatter).

```markdown
---
title: KoColor Starter Pack API
emoji: 💄
colorFrom: pink
colorTo: purple
sdk: docker
app_port: 7860
---

# KoColor Backend
This is the offline-first data seeding API for the KoColor Android application.

```

### B. The Build Instructions (`Dockerfile`)

Hugging Face strictly requires the container to run as a non-root user (User ID `1000`) for sandboxed security.

```dockerfile
# Stage 1: Build the Rust Binary
FROM rust:1.80-slim-bullseye as builder
WORKDIR /usr/src/app
COPY . .
# Build the Axum server for extreme speed
RUN cargo build --release

# Stage 2: Minimal Runtime Environment
FROM debian:bullseye-slim

# Hugging Face requires a non-root user (uid 1000)
RUN useradd -m -u 1000 user
USER user
ENV HOME=/home/user \
    PATH=/home/user/.local/bin:$PATH
WORKDIR $HOME/app

# Copy the compiled binary from the builder stage
COPY --from=builder --chown=user /usr/src/app/target/release/kocolor .

# Expose the mandatory Hugging Face port
EXPOSE 7860

# Execute the binary
CMD ["./kocolor"]

```

## 3. Creating the Space Online

1. Log into [Hugging Face](https://huggingface.co/spaces).
2. Click **Create new Space**.
3. Name it `kocolor-api`.
4. Select **Docker** as the Space SDK, and choose the **Blank** template.
5. Click **Create Space**.

## 4. Deploying from the Zed Terminal

To deploy just the `server/kocolor/` subdirectory without messing up your main `ProBase` Git repository, we will use a "throwaway git" command sequence.

Open Zed's integrated terminal (`cmd-j` or `ctrl-j`), and run these exact commands:

```bash
# 1. Navigate into the Rust server directory
cd server/kocolor

# 2. Create a temporary local Git instance just for Hugging Face
git init
git add .
git commit -m "Deploy Axum server to Hugging Face"

# 3. Add your Hugging Face Space as the remote (Replace YOUR_USERNAME)
git remote add huggingface [https://huggingface.co/spaces/YOUR_USERNAME/kocolor-api](https://huggingface.co/spaces/YOUR_USERNAME/kocolor-api)

# 4. Force push the code to Hugging Face (You will need your HF Access Token as the password)
git push --force huggingface master:main

# 5. Delete the temporary git instance so ProBase remains clean
rm -rf .git

```

*🔑 **Authentication Note:** When the terminal prompts you for a password during the push, you must use a **Hugging Face Access Token** (generated in your HF account settings), not your account password.*

## 5. Verification

Once the push completes, switch back to your browser. Your Hugging Face Space will immediately switch its status to **Building**.

Once the status turns to **Running**, your Axum API is globally live and instantly ready to serve your JSON payloads down to the Android client!

```

```