plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.features.ar.naillab"
}

val downloadHandLandmarkerTask = tasks.register("downloadHandLandmarker") {
    val modelUrl = "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task"
    val outputDir = file("src/main/assets")
    val outputFile = file("${outputDir}/hand_landmarker.task")

    outputs.file(outputFile)

    doLast {
        if (!outputFile.exists()) {
            println("Downloading hand_landmarker.task...")
            outputDir.mkdirs()
            ant.invokeMethod("get", mapOf("src" to modelUrl, "dest" to outputFile))
        } else {
            println("hand_landmarker.task already exists, skipping download.")
        }
    }
}

tasks.named("preBuild") {
    dependsOn(downloadHandLandmarkerTask)
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))
    implementation(project(":applications:kocolor:model"))

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
