plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.features.ar.facelab"
}

val downloadFaceLandmarkerTask = tasks.register("downloadFaceLandmarker") {
    val modelUrl = "https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task"
    val outputDir = file("src/main/assets")
    val outputFile = file("${outputDir}/face_landmarker.task")

    outputs.file(outputFile)

    doLast {
        if (!outputFile.exists()) {
            println("Downloading face_landmarker.task...")
            outputDir.mkdirs()
            ant.invokeMethod("get", mapOf("src" to modelUrl, "dest" to outputFile))
        } else {
            println("face_landmarker.task already exists, skipping download.")
        }
    }
}

tasks.named("preBuild") {
    dependsOn(downloadFaceLandmarkerTask)
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))

    // MediaPipe
    implementation(libs.mediapipe.vision)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Compose
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.google.accompanist.permissions)
}
