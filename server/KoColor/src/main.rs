use axum::{
    routing::{get, post},
    Router,
    Json,
};
use serde::{Deserialize, Serialize};

#[tokio::main]
async fn main() {
    let app = Router::new()
        .route("/health", get(health_check))
        .route("/api/extract", post(extract_vanity_data));

    // 0.0.0.0 is strictly required by Google Cloud Run
    let listener = tokio::net::TcpListener::bind("0.0.0.0:8080").await.unwrap();
    println!("Rust proxy running on port 8080...");
    axum::serve(listener, app).await.unwrap();
}

async fn health_check() -> &'static str {
    "Proxy is up and running securely."
}

#[derive(Deserialize)]
struct ExtractionRequest {
    base64_image: String,
    prompt: String,
}

#[derive(Serialize)]
struct ExtractionResponse {
    status: String,
    json_payload: String,
}

async fn extract_vanity_data(Json(_payload): Json<ExtractionRequest>) -> Json<ExtractionResponse> {
    // TODO: Verify App Check, Inject API Key, Call Gemini
    Json(ExtractionResponse {
        status: "success".to_string(),
        json_payload: "{\"brand\": \"Sample\", \"ingredients\": []}".to_string(),
    })
}
