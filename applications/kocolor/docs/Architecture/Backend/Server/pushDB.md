# 🏛️ KoColor Architecture: Unified Offline Image Storage & Single Source of Truth (SSOT)

This document outlines the architecture for how KoColor handles, stores, and renders images across the platform. To maintain our strict **offline-first, zero-footprint philosophy**, we employ an "Origin-Blind" storage pattern using internal device storage (`Context.filesDir`) and Room Database.

---

## 🛑 The Core Problem
The application ingests images from two radically different sources:
1. **The Starter Pack Payload:** Downloaded over the network from the Rust Axum orchestration node as Base64/URLs to solve the "Zero Item Problem" on initial launch.
2. **CameraX Capture:** Snapped locally by the user scanning their physical wardrobe or cosmetics.

If handled separately, this would pollute the Jetpack Compose UI with conditional logic (e.g., `if (isFromServer) loadNetwork() else loadLocal()`), complicate cache management, and risk leaking private user scans into the public Android `MediaStore`.

---

## 🎯 The Solution: Origin-Blind Persistence
We decouple the **origin** of the data from the **consumption** of the data. By standardizing all images at the storage boundary, the Room database and the UI modules never need to know where an item came from.

### The Storage Pipeline
Regardless of the source, every image passes through this strict 3-step pipeline:

1. **Physical Write:** The raw PNG bytes are saved securely to the app's private, internal storage (`Context.filesDir`).
2. **URI Generation:** A standard local string path is generated (e.g., `file:///data/user/0/com.zoewave.probase.kocolor/files/kocolor_123.png`).
3. **Database Insertion:** *Only* that URI string is saved into the `image_data` column of the Room `@Entity`. Raw bitmaps are **never** stored in SQLite.

---

## 🛤️ Data Ingestion Flows

### Flow A: The Starter Pack (Rust Server)
When the user purchases the KoColor Starter Pack, the app requests the payload from the Axum server:
* Retrofit receives the JSON payload.
* The repository intercepts the Base64 image strings or network streams.
* The bytes are flushed into a new file in `Context.filesDir`.
* The resulting `file://` URI is inserted into Room via `dao.insertAll()`.

### Flow B: Camera Capture (User Generated)
When the user captures a new clothing item or cosmetic palette:
* `CameraX` is configured via `ImageCapture.OutputFileOptions` to write the camera sensor stream *directly* to a new file in `Context.filesDir`.
* The resulting `file://` URI is inserted into Room via `dao.insert()`.

---

## 🎨 UI Rendering & Coil Integration
Because both ingestion flows result in identical `@Entity` structures containing `file://` strings, our ViewModels and Compose screens are completely agnostic to the data's origin. 

We utilize **Coil** for image rendering because it natively handles `File` objects, `Uri` objects, and `file://` string URIs out of the box, requiring no extra setup. 

```kotlin
// The UI consumes a single Flow<List<CosmeticEntity>> from Room.
// Coil automatically parses the local URI and handles the memory caching.

AsyncImage(
    model = item.imageData, // Resolves to "file:///data/user/0/..."
    contentDescription = item.name,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize()
)

```

---

## 🧹 Standardized Garbage Collection (Deletion)

Because all items exist identically in the local environment, deleting an item requires a single, universal pipeline. There are no edge cases to check.

```kotlin
suspend fun deleteItem(item: CosmeticEntity) {
    // Step 1: Purge the physical PNG bytes from private internal storage
    val filePath = item.imageData.removePrefix("file://")
    val file = File(filePath)
    if (file.exists()) {
        file.delete()
    }

    // Step 2: Purge the relational metadata from the local Room database
    cosmeticDao.delete(item)
}

```

### ⚖️ Architectural Benefits

* **Zero UI Pollution:** No origin flags or conditional UI rendering.
* **True Offline Autonomy:** Downloaded server items behave exactly like local camera items when the device is entirely offline.
* **Storage Hygiene:** Server items and local scans consume space in the exact same directory, meaning they are both completely cleared if the user uninstalls the app or clears app data.