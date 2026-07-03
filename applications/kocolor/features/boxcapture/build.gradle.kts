plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.kocolor.features.boxcapture"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:data"))
    implementation(project(":applications:kocolor:model"))
    implementation(project(":applications:kocolor:data"))
    implementation(project(":applications:kocolor:features:analyzer"))
    implementation(project(":applications:kocolor:features:fda"))
    implementation(project(":applications:kocolor:features:chemicals"))
    implementation(project(":applications:kocolor:features:colors"))
    implementation(project(":applications:kocolor:features:cosmetics"))
    implementation(project(":applications:kocolor:features:makeupapi"))
    implementation(project(":features:ai:configuration"))
    implementation(project(":features:ai:local"))
    implementation(project(":features:camera:productcapture"))
    implementation(project(":features:graphics"))
    implementation(project(":applications:kocolor:features:chemicals"))

    implementation(libs.google.generative.ai)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation(project(":features:readers:ocr"))
    implementation(libs.kotlinx.serialization.json)

    // ML Kit
    implementation(libs.gms.play.services.code.scanner)
    implementation(libs.kotlinx.coroutines.play.services)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.compose)
    implementation(libs.google.accompanist.permissions)
}
