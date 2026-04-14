plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.photodo.features.camera"
}

dependencies {
    // --- Shared Core Projects ---
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    api(project(":core:model"))

    // --- PhotoTodo Modules ---
    implementation(project(":applications:photodo:db"))
    implementation(project(":applications:photodo:model"))
    implementation(project(":applications:photodo:apps:mobile:core"))
    implementation(project(":applications:photodo:apps:mobile:features:tasks"))

    // --- Core Features ---
    implementation(project(":features:camera"))

    // --- Third Party ---
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.collections.immutable)
}
