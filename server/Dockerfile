# Stage 1: Build the Rust binary
FROM rust:1.75-bookworm as builder
WORKDIR /usr/src/app
# Copy the source code
COPY . .
# Compile the binary in release mode
RUN cargo build --release

# Stage 2: Create the minimal production image
FROM debian:bookworm-slim
# Install CA certificates so reqwest can make secure HTTPS calls to the Gemini API
RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*
WORKDIR /usr/local/bin
# Copy ONLY the compiled binary from the builder stage
COPY --from=builder /usr/src/app/target/release/server .

# Expose the Cloud Run port
EXPOSE 8080

# Run the binary
CMD ["./server"]
