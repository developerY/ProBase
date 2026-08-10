# 🏛️ KoColor Architecture: The Offline-First Distributed Backend

## 🛑 The Core Philosophy
KoColor operates strictly on an **offline-first, zero-footprint philosophy**. User data (cosmetic scans, clothing profiles) never leaves the physical device. 

To solve the "Zero Item Problem" on initial launch without relying on a centralized, forced-sync cloud database, we utilize a distributed, zero-cost, two-tier architecture to deliver the Starter Pack payload.

## 🏗️ The Split-Responsibility Infrastructure

Our backend separates **compute** from **storage** using two free-tier services.

### 1. Compute & Orchestration: Hugging Face Spaces (Rust)
*   **The Framework:** A lightweight Rust server built with `axum` and serialized with `serde`.
*   **The Host:** Deployed via Hugging Face Docker Spaces.
*   **How it Works:** Hugging Face automatically reads the `Dockerfile` and builds the Rust binary into a container. By default, the application runs on port `7860`. 
*   **The Role:** This endpoint (`GET /api/v1/starter-pack`) dynamically generates and serves the JSON taxonomy mapping (Level 1-3 categories, formulations, color hex codes) to the Android client. 

### 2. Storage & CDN: GitHub Pages
*   **The Repository:** A dedicated Git repository (`kocolor-assets`) separate from the main `ProBase` monorepo.
*   **The Assets:** Highly compressed `.webp` and `.jpg` cosmetic/clothing images cropped to `512x512` pixels (~30-50KB each).
*   **The Limits:** GitHub provides a generous allocation, including a repository size limit of `1 GB` and a soft monthly bandwidth limit of `100 GB`. 
*   **The Role:** GitHub Pages acts as our global static CDN. The Hugging Face JSON payload simply references these public GitHub URLs (e.g., `image_07157d.jpg`).

---

## 🛤️ Data Ingestion & "Origin-Blind" Persistence

The Android application is completely agnostic to where the data comes from. Whether an image is downloaded from our GitHub Pages CDN or snapped manually by the user's CameraX capture, it enters the exact same strict local pipeline.

### The 3-Step Storage Pipeline
1. **Network Fetch:** The Jetpack Compose app calls the Hugging Face API to receive the JSON blueprint. 
2. **Physical Write (Internal Storage):** Coil resolves the static GitHub URLs in the background and writes the raw image bytes directly into the app's secure internal directory (`Context.filesDir`).
3. **Database Insertion (Room):** A standardized local URI string (e.g., `file:///data/user/0/com.zoewave.probase.kocolor/files/image_07157d.jpg`) is inserted into the Room database under the `image_data` column. 

*Raw bitmaps are **never** stored in SQLite to preserve blazing-fast database queries.*

### UI Rendering (Jetpack Compose & Coil)
Because the database only holds local `file://` URIs, the Jetpack Compose ViewModels require zero conditional logic. Coil seamlessly renders the offline images directly from device storage.

```kotlin
// The Jetpack Compose UI consumes a unified Flow from the Room DB.
// Coil parses the local URI and handles rendering without any network dependencies.

AsyncImage(
    model = item.imageData, 
    contentDescription = item.name,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize()
)

```

---

## 🧹 Standardized Garbage Collection

Because all items (both Server Starter Pack items and Local Camera Scans) exist identically in the local environment, deleting an item requires a single, universal execution.

```kotlin
suspend fun deleteItem(item: CosmeticEntity) {
    // Step 1: Purge physical image bytes from private internal storage
    val filePath = item.imageData.removePrefix("file://")
    val file = File(filePath)
    if (file.exists()) {
        file.delete()
    }

    // Step 2: Purge the relational metadata from the local Room database
    cosmeticDao.delete(item)
}

```

This ensures maximum privacy, complete offline autonomy, and pristine storage hygiene across the platform.

```

```